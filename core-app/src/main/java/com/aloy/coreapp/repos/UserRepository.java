package com.aloy.coreapp.repos;

import com.aloy.coreapp.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByPhoneNumberAndActiveIsTrue(String phoneNumber);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByEmailAndActiveIsTrue(String email);
}
