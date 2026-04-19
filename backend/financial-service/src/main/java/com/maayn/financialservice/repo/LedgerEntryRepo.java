package com.maayn.financialservice.repo;

import com.maayn.financialservice.entity.LedgerEntryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LedgerEntryRepo extends MongoRepository<LedgerEntryDocument, UUID> {
}
