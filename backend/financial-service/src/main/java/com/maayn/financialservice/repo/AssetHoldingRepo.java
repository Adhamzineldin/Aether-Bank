package com.maayn.financialservice.repo;

import com.maayn.financialservice.entity.AssetHoldingDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetHoldingRepo extends MongoRepository<AssetHoldingDocument, UUID> {
    
    List<AssetHoldingDocument> findByInvestmentAccountId(UUID investmentAccountId);
    
    Optional<AssetHoldingDocument> findByInvestmentAccountIdAndSymbol(UUID investmentAccountId, String symbol);
}

