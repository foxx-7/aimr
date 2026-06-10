package com.aimr.notify.dao.interfaces;

import com.aimr.notify.models.entity.User;

import java.util.Optional;

public interface UserDao {

    Optional<User> findById(String id);

    void save(User user);

    Optional<User> findByEmail(String email);

    void deleteUser(User user);
}
