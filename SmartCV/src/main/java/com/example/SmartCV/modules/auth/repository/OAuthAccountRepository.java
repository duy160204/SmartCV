package com.example.SmartCV.modules.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.SmartCV.modules.auth.domain.OAuthAccount;

@Repository

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {

    Optional<OAuthAccount> findByProviderAndProviderUserId(String provider, String providerUserId);
}
