package com.aloy.coreapp.repos;

import com.aloy.coreapp.enums.BadgeType;
import com.aloy.coreapp.model.Badge;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BadgeRepository extends MongoRepository<Badge, String> {

    Optional<Badge> findByUserIdAndBadgeType(String userId, BadgeType badgeType);

    List<Badge> findByUserId(String userId);
}
