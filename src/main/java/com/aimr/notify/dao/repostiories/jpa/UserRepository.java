package com.aimr.notify.dao.repostiories.jpa;

import com.aimr.notify.models.entity.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<@NonNull User, @NonNull String> {
    Optional<User> findByEmail(String userName);
}
