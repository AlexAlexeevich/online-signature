package com.era.onlinesignature.repository;

import com.era.onlinesignature.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    Optional<Contract> findByNameContractAndSubscriberId(String contract, Long subscriberId);
    Optional<Contract> findByShortUrl(String shortUrl);
}
