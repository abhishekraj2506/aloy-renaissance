package com.aloy.coreapp.repos;

import com.aloy.coreapp.model.RetroReward;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RetroRewardRepository extends MongoRepository<RetroReward, String> {

    Optional<RetroReward> findBySourceAndSourceUniqueId(String source, String sourceUniqueId);
}
