package com.backend.kashiapp.user.infraestructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.backend.kashiapp.user.application.mapper.UserMapper;
import com.backend.kashiapp.user.domain.models.User;
import com.backend.kashiapp.user.domain.repository.UserRepository;

@Component
public class UserRepositoryAdapter implements UserRepository {
    
    private final JpaUserRepository jpaUserRepository;
    private final UserMapper userMapper;
    
    public UserRepositoryAdapter(JpaUserRepository jpaUserRepository, UserMapper userMapper) {
        this.jpaUserRepository = jpaUserRepository;
        this.userMapper = userMapper;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaUserRepository.findById(id).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email).map(userMapper::toDomain); 
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByNumberPhone(String numberPhone) {
        return jpaUserRepository.existsByNumberPhone(numberPhone);
    }

    @Override
    public User save(User user) {
        UserEntity userEntity = userMapper.toEntity(user);
        UserEntity savedEntity = jpaUserRepository.save(userEntity);
        return userMapper.toDomain(savedEntity);
    }

}