package com.aimr.notify.dao.impl;

import com.aimr.notify.dao.interfaces.UserDao;
import com.aimr.notify.dao.repostiories.jpa.UserRepository;
import com.aimr.notify.models.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDaoImpl implements UserDao {

    private final UserRepository userRepository;

   @Override
   public Optional<User> findById(final String id){
        return userRepository.findById(id);
   }

   @Override
   public void save(final User user) {
       userRepository.save(user);
   }

   @Override
   public Optional<User> findByEmail(final String email){
        return userRepository.findByEmail(email);
   }

   @Override
   public void deleteUser(final User user){
        userRepository.delete(user);
   }
}
