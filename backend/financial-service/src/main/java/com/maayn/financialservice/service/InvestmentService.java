package com.maayn.financialservice.service;

import com.maayn.financialservice.entity.AssetHoldingDocument;
import com.maayn.financialservice.entity.InvestmentAccountDocument;
import com.maayn.financialservice.repo.AssetHoldingRepo;
import com.maayn.financialservice.repo.InvestmentAccountRepo;
import com.maayn.financialservice.support.ReferenceNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.investment.*;
import maayn.veld.generated.services.IInvestmentService;
import maayn.veld.generated.sdk.transaction.TransactionClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvestmentService implements IInvestmentService {

    private final InvestmentAccountRepo investmentAccountRepo;
    private final AssetHoldingRepo assetHoldingRepo;
    private final TransactionClient transactionClient;
    private final ReferenceNumberGenerator referenceNumberGenerator;

    // Mock asset prices (in real implementation, fetch from external API)
    private static final Map<String, BigDecimal> MOCK_PRICES = new HashMap<>() {{
        put("AAPL", new BigDecimal("175.50"));
        put("GOOGL", new BigDecimal("140.25"));
        put("MSFT", new BigDecimal("380.75"));
        put("TSLA", new BigDecimal("245.00"));
        put("AMZN", new BigDecimal("145.50"));
    }};

    @Override
    public InvestmentAccountResponse openInvestmentAccount(OpenInvestmentAccountRequest request) throws Exception {
        log.info("Opening investment account for customer: {}", request.getCustomerId());

        InvestmentAccountDocument account = InvestmentAccountDocument.builder()
                .id(UUID.randomUUID())
                .accountNumber(referenceNumberGenerator.generate("INV"))
                .customerId(request.getCustomerId())
                .linkedAccountId(request.getAccountId())
                .status("ACTIVE")
                .totalValue(BigDecimal.ZERO)
                .currency(request.getCurrency())
                .openedDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        InvestmentAccountDocument saved = investmentAccountRepo.save(account);
        log.info("Investment account created: {}", saved.getAccountNumber());

        return mapToAccountResponse(saved);
    }

    @Override
    public InvestmentAccountResponse getInvestmentAccount(String accountId) throws Exception {
        UUID id = UUID.fromString(accountId);
        InvestmentAccountDocument account = investmentAccountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Investment account not found: " + accountId));

        return mapToAccountResponse(account);
    }

    @Override
    public TransactionResponse buyAsset(String accountId, BuyAssetRequest request) throws Exception {
        UUID id = UUID.fromString(accountId);
        InvestmentAccountDocument account = investmentAccountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Investment account not found: " + accountId));

        // Get current asset price
        BigDecimal currentPrice = getCurrentPrice(request.getSymbol());
        BigDecimal totalCost = currentPrice.multiply(request.getQuantity()).setScale(2, RoundingMode.HALF_UP);

        // TODO: Deduct funds from linked bank account via Transaction Service

        // Update or create holding
        AssetHoldingDocument holding = assetHoldingRepo
                .findByInvestmentAccountIdAndSymbol(id, request.getSymbol())
                .orElse(AssetHoldingDocument.builder()
                        .id(UUID.randomUUID())
                        .investmentAccountId(id)
                        .symbol(request.getSymbol())
                        .assetType("STOCK") // Default
                        .quantity(BigDecimal.ZERO)
                        .averagePurchasePrice(BigDecimal.ZERO)
                        .build());

        // Update holding
        BigDecimal newQuantity = holding.getQuantity().add(request.getQuantity());
        BigDecimal newAvgPrice = holding.getQuantity().multiply(holding.getAveragePurchasePrice())
                .add(request.getQuantity().multiply(currentPrice))
                .divide(newQuantity, 2, RoundingMode.HALF_UP);

        holding.setQuantity(newQuantity);
        holding.setAveragePurchasePrice(newAvgPrice);
        holding.setCurrentPrice(currentPrice);
        holding.setTotalValue(newQuantity.multiply(currentPrice));
        holding.setUnrealizedGainLoss(holding.getTotalValue().subtract(newQuantity.multiply(newAvgPrice)));
        holding.setUpdatedAt(LocalDateTime.now());

        if (holding.getPurchaseDate() == null) {
            holding.setPurchaseDate(LocalDateTime.now());
        }

        assetHoldingRepo.save(holding);

        // Update account total value
        updateAccountTotalValue(account);

        log.info("Asset purchased: {} x {} @ {}", request.getQuantity(), request.getSymbol(), currentPrice);

        return createTransactionResponse();
    }

    @Override
    public TransactionResponse sellAsset(String accountId, SellAssetRequest request) throws Exception {
        UUID id = UUID.fromString(accountId);
        InvestmentAccountDocument account = investmentAccountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Investment account not found: " + accountId));

        AssetHoldingDocument holding = assetHoldingRepo
                .findByInvestmentAccountIdAndSymbol(id, request.getSymbol())
                .orElseThrow(() -> new RuntimeException("Asset not found in portfolio: " + request.getSymbol()));

        if (holding.getQuantity().compareTo(request.getQuantity()) < 0) {
            throw new RuntimeException("Insufficient holdings. Available: " + holding.getQuantity());
        }

        BigDecimal currentPrice = getCurrentPrice(request.getSymbol());
        BigDecimal saleValue = currentPrice.multiply(request.getQuantity()).setScale(2, RoundingMode.HALF_UP);

        // Update holding
        BigDecimal newQuantity = holding.getQuantity().subtract(request.getQuantity());
        if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
            assetHoldingRepo.delete(holding);
        } else {
            holding.setQuantity(newQuantity);
            holding.setCurrentPrice(currentPrice);
            holding.setTotalValue(newQuantity.multiply(currentPrice));
            holding.setUnrealizedGainLoss(holding.getTotalValue().subtract(newQuantity.multiply(holding.getAveragePurchasePrice())));
            holding.setUpdatedAt(LocalDateTime.now());
            assetHoldingRepo.save(holding);
        }

        // TODO: Credit funds to linked bank account via Transaction Service

        updateAccountTotalValue(account);

        log.info("Asset sold: {} x {} @ {}", request.getQuantity(), request.getSymbol(), currentPrice);

        return createTransactionResponse();
    }

    @Override
    public Portfolio getPortfolio(String accountId, GetPortfolioRequest request) throws Exception {
        UUID id = UUID.fromString(accountId);
        InvestmentAccountDocument account = investmentAccountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Investment account not found: " + accountId));

        List<AssetHoldingDocument> holdings = assetHoldingRepo.findByInvestmentAccountId(id);

        // Update current prices
        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (AssetHoldingDocument holding : holdings) {
            BigDecimal currentPrice = getCurrentPrice(holding.getSymbol());
            holding.setCurrentPrice(currentPrice);
            holding.setTotalValue(holding.getQuantity().multiply(currentPrice));
            holding.setUnrealizedGainLoss(holding.getTotalValue()
                    .subtract(holding.getQuantity().multiply(holding.getAveragePurchasePrice())));

            totalValue = totalValue.add(holding.getTotalValue());
            totalCost = totalCost.add(holding.getQuantity().multiply(holding.getAveragePurchasePrice()));
        }

        Portfolio portfolio = new Portfolio();
        portfolio.setInvestmentAccountId(id);
        portfolio.setTotalValue(totalValue);
        portfolio.setTotalCost(totalCost);
        portfolio.setUnrealizedGainLoss(totalValue.subtract(totalCost));
        portfolio.setHoldings(holdings.stream().map(this::mapToAssetHolding).toList());
        portfolio.setCurrency(account.getCurrency());
        portfolio.setLastUpdated(LocalDateTime.now());

        return portfolio;
    }

    @Override
    public PerformanceMetrics getPerformance(String accountId, GetPerformanceRequest request) throws Exception {
        Portfolio portfolio = getPortfolio(accountId, new GetPortfolioRequest());

        BigDecimal totalReturn = portfolio.getUnrealizedGainLoss();
        BigDecimal totalReturnPercentage = portfolio.getTotalCost().compareTo(BigDecimal.ZERO) > 0
                ? totalReturn.divide(portfolio.getTotalCost(), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setInvestmentAccountId(portfolio.getInvestmentAccountId());
        metrics.setTotalReturn(totalReturn);
        metrics.setTotalReturnPercentage(totalReturnPercentage);
        metrics.setDayChange(BigDecimal.ZERO); // TODO: Calculate
        metrics.setDayChangePercentage(BigDecimal.ZERO);
        metrics.setWeekChange(BigDecimal.ZERO);
        metrics.setMonthChange(BigDecimal.ZERO);
        metrics.setYearChange(BigDecimal.ZERO);
        metrics.setStartDate(request.getStartDate());
        metrics.setEndDate(request.getEndDate());

        return metrics;
    }

    @Override
    public List<Asset> listAssets() throws Exception {
        return MOCK_PRICES.entrySet().stream()
                .map(entry -> {
                    Asset asset = new Asset();
                    asset.setSymbol(entry.getKey());
                    asset.setName(entry.getKey() + " Inc.");
                    asset.setAssetType(AssetType.STOCK);
                    asset.setCurrentPrice(entry.getValue());
                    asset.setCurrency("USD");
                    return asset;
                })
                .toList();
    }

    @Override
    public Asset getAsset(String symbol) throws Exception {
        BigDecimal price = getCurrentPrice(symbol);
        Asset asset = new Asset();
        asset.setSymbol(symbol);
        asset.setName(symbol + " Inc.");
        asset.setAssetType(AssetType.STOCK);
        asset.setCurrentPrice(price);
        asset.setCurrency("USD");
        return asset;
    }

    private BigDecimal getCurrentPrice(String symbol) {
        return MOCK_PRICES.getOrDefault(symbol, new BigDecimal("100.00"));
    }

    private void updateAccountTotalValue(InvestmentAccountDocument account) {
        List<AssetHoldingDocument> holdings = assetHoldingRepo.findByInvestmentAccountId(account.getId());
        BigDecimal totalValue = holdings.stream()
                .map(AssetHoldingDocument::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        account.setTotalValue(totalValue);
        account.setUpdatedAt(LocalDateTime.now());
        investmentAccountRepo.save(account);
    }

    private InvestmentAccountResponse mapToAccountResponse(InvestmentAccountDocument account) {
        InvestmentAccount investmentAccount = new InvestmentAccount();
        investmentAccount.setId(account.getId());
        investmentAccount.setAccountNumber(account.getAccountNumber());
        investmentAccount.setCustomerId(account.getCustomerId());
        investmentAccount.setAccountId(account.getLinkedAccountId());
        investmentAccount.setStatus(InvestmentStatus.valueOf(account.getStatus()));
        investmentAccount.setTotalValue(account.getTotalValue());
        investmentAccount.setCurrency(account.getCurrency());
        investmentAccount.setOpenedDate(account.getOpenedDate());
        investmentAccount.setClosedDate(account.getClosedDate());
        investmentAccount.setCreatedAt(account.getCreatedAt());
        investmentAccount.setUpdatedAt(account.getUpdatedAt());

        InvestmentAccountResponse response = new InvestmentAccountResponse();
        response.setAccount(investmentAccount);
        return response;
    }

    private AssetHolding mapToAssetHolding(AssetHoldingDocument document) {
        AssetHolding holding = new AssetHolding();
        holding.setInvestmentAccountId(document.getInvestmentAccountId());
        holding.setSymbol(document.getSymbol());
        holding.setAssetType(AssetType.valueOf(document.getAssetType()));
        holding.setQuantity(document.getQuantity());
        holding.setAveragePurchasePrice(document.getAveragePurchasePrice());
        holding.setCurrentPrice(document.getCurrentPrice());
        holding.setTotalValue(document.getTotalValue());
        holding.setUnrealizedGainLoss(document.getUnrealizedGainLoss());
        holding.setPurchaseDate(document.getPurchaseDate());
        return holding;
    }

    private TransactionResponse createTransactionResponse() {
        TransactionResponse response = new TransactionResponse();
        // TODO: Create proper transaction record
        return response;
    }
}

