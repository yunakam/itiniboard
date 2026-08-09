package com.initiboard.api.repository;

import com.initiboard.api.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByBlockIdIn(Collection<Long> blockIds);
}