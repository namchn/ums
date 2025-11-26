package com.nc.ums.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nc.ums.domain.Outbox;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, Long> {
    List<Outbox> findByPublishedFalse();
}
