package com.era.onlinesignature.repository;

import com.era.onlinesignature.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface LinkRepository extends JpaRepository<Link, Long> {
    List<Link> findAllByContractId(Long id);
    Optional<Link> findById(Long id);
}
