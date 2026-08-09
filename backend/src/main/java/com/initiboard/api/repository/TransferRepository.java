package com.initiboard.api.repository;

import com.initiboard.api.entity.Activity;
import com.initiboard.api.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    List<Transfer> findByBlockIdIn(Collection<Long> blockIds);
}
