package com.aimr.notify.infra.postgres.dao;

import com.aimr.notify.infra.postgres.repo.UserRepository;
import com.aimr.notify.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDao {

    private final UserRepository userRepository;

   public Optional<User> fetchUserById(final String id){
        return userRepository.findById(id);
   }

   public void saveUser(final User user) {
       userRepository.save(user);
   }

   public Optional<User> fetchUserByEmail(final String email){
        return userRepository.findByEmail(email);
   }

   public void deleteUser(final User user){
        userRepository.delete(user);
   }
}
