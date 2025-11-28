package com.nc.ums.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nc.ums.domain.SendFailure;

@Repository
public interface SendFailureRepository extends JpaRepository<SendFailure, Long> {
    List<SendFailure> findByHandledFalse();
}
