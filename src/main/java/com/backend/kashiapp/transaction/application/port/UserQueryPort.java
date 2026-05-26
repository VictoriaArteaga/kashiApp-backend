package com.backend.kashiapp.transaction.application.port;

import java.util.Optional;
import java.util.UUID;

public interface UserQueryPort {
    Optional<UUID> findUserIdByEmail(String email);
}
