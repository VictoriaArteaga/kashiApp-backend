package com.backend.kashiapp.user.application.mapper;

import org.springframework.stereotype.Component;

import com.backend.kashiapp.user.domain.models.User;
import com.backend.kashiapp.user.infraestructure.persistence.UserEntity;

@Component
public class UserMapper {

    public User toDomain(UserEntity userEntity) {
        User user = new User ();
        user.setId(userEntity.getId());
        user.setUsername(userEntity.getUsername());
        user.setEmail(userEntity.getEmail());
        user.setPasswordHash(userEntity.getPasswordHash());
        user.setCreationDate(userEntity.getCreationDate());
        user.setNumberPhone(userEntity.getNumberPhone());
        user.setAccountStatus(userEntity.getAccountStatus());
        user.setLockedUntil(userEntity.getLockedUntil());
        user.setFailedAttempts(userEntity.getFailedAttempts());
        user.setIdentificationNumber(userEntity.getIdentificationNumber());
        return user;
    }

    //convertir de domain a entity
    public UserEntity toEntity(User user) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(user.getId());
        userEntity.setUsername(user.getUsername());
        userEntity.setEmail(user.getEmail());
        userEntity.setPasswordHash(user.getPasswordHash());
        userEntity.setCreationDate(user.getCreationDate());
        userEntity.setNumberPhone(user.getNumberPhone());
        userEntity.setAccountStatus(user.getAccountStatus());
        userEntity.setLockedUntil(user.getLockedUntil());
        userEntity.setFailedAttempts(user.getFailedAttempts());
        userEntity.setIdentificationNumber(user.getIdentificationNumber());
        return userEntity;
    }
}
