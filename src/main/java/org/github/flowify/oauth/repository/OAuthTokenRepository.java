package org.github.flowify.oauth.repository;

import org.github.flowify.oauth.entity.OAuthToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface OAuthTokenRepository extends MongoRepository<OAuthToken, String> {

    Optional<OAuthToken> findByUserIdAndService(String userId, String service);

    List<OAuthToken> findByUserId(String userId);

    void deleteByUserIdAndService(String userId, String service);

    void deleteByUserId(String userId);
}