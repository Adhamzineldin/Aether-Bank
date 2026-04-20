package com.maayn.financialservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "asset_holdings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetHoldingDocument {
    
    @Id
    private UUID id;
    
    private UUID investmentAccountId;
    
    private String symbol; // AAPL, GOOGL, etc.
    
    private String assetType; // STOCK, MUTUAL_FUND, ETF, BOND
    
    private BigDecimal quantity;
    
    private BigDecimal averagePurchasePrice;
    
    private BigDecimal currentPrice;
    
    private BigDecimal totalValue;
    
    private BigDecimal unrealizedGainLoss;
    
    private LocalDateTime purchaseDate;
    
    private LocalDateTime updatedAt;
}

