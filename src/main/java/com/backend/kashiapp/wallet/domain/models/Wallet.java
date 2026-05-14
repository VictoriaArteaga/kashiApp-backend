package com.backend.kashiapp.wallet.domain.models;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class Wallet {
    private UUID id;
    private UUID userId;
    private BigDecimal balance;
    private boolean visible;
    private OffsetDateTime updatedAt;

    public void applyBalanceChange(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.updatedAt = OffsetDateTime.now();
    }

    public void toggleVisibility() {
        this.visible = !this.visible;
        this.updatedAt = OffsetDateTime.now();
    }
}
