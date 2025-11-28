package com.nc.ums.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nc.ums.domain.Outbox;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, Long> {
	
	boolean existsByAggregateIdAndPublishedFalse(String aggregateId);
    Optional<Outbox> findByAggregateId(String aggregateId);
    List<Outbox> findByPublishedFalse();
}
