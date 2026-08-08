package com.initiboard.api.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BlockDeletionConfirmationService {

    private static final Duration TOKEN_VALIDITY = Duration.ofMinutes(5);

    private final Map<String, DeletionConfirmation> confirmations =
            new ConcurrentHashMap<>();

    public String issueToken(
            Long blockId,
            Set<Long> usagePlanIds
    ) {
        removeExpiredTokens();

        String token = UUID.randomUUID().toString();

        confirmations.put(
                token,
                new DeletionConfirmation(
                        blockId,
                        Set.copyOf(usagePlanIds),
                        Instant.now().plus(TOKEN_VALIDITY)
                )
        );

        return token;
    }

    public void verifyAndConsume(
            String token,
            Long blockId,
            Set<Long> currentUsagePlanIds
    ) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "Deletion confirmation token is required"
            );
        }

        removeExpiredTokens();

        DeletionConfirmation confirmation = confirmations.remove(token);

        if (confirmation == null) {
            throw new IllegalArgumentException(
                    "Deletion confirmation token is invalid or already used"
            );
        }

        if (confirmation.expiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException(
                    "Deletion confirmation token has expired"
            );
        }

        if (!confirmation.blockId.equals(blockId)) {
            throw new IllegalArgumentException(
                    "Deletion confirmation token does not match the block"
            );
        }

        if (!confirmation.usagePlanIds.equals(currentUsagePlanIds)) {
            throw new IllegalArgumentException(
                    "Block usage has changed since confirmation"
            );
        }
    }

    private void removeExpiredTokens() {
        Instant now = Instant.now();

        confirmations.entrySet().removeIf(
                entry -> entry.getValue().expiresAt().isBefore(now)
        );
    }

    private record DeletionConfirmation(
            Long blockId,
            Set<Long> usagePlanIds,
            Instant expiresAt
    ) {}
}
