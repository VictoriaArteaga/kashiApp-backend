package com.backend.kashiapp.user.domain.models;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.backend.kashiapp.user.domain.models.enums.AccountStatus;

import lombok.Data;

@Data
public class User {
    private UUID id;
    private String email;
    private String passwordHash;
    private String username;
    private OffsetDateTime creationDate;
    private String numberPhone;
    private AccountStatus accountStatus;
    private Integer failedAttempts;
    private OffsetDateTime lockedUntil;
    private String identificationNumber;

    public void delete(){
        this.accountStatus = AccountStatus.DELETED;
    }

    public void block(){
        this.accountStatus = AccountStatus.BLOCKED;
    }
    
    public void resetFailedAttempts(){
        this.failedAttempts = 0;
        this.lockedUntil = null;
    }

    public void recordFailedAttempt(){
        this.failedAttempts ++;
        if(this.failedAttempts >= 5){
            this.lockedUntil = OffsetDateTime.now().plusMinutes(15);
        }
    }

    public boolean isTemporallyLocker(){
        return this.lockedUntil != null && this.lockedUntil.isAfter(OffsetDateTime.now());
    }

    public boolean isBlocked(){
        return this.accountStatus == AccountStatus.BLOCKED;
    }
    public boolean isDeleted(){
        return this.accountStatus == AccountStatus.DELETED;
    }

    public void unlock(){
        this.accountStatus = AccountStatus.ACTIVE;
    }

}