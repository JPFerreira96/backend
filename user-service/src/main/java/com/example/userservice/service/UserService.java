package com.example.userservice.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        User userCreate = new User();
        userCreate.setName(user.getName());
        userCreate.setEmail(user.getEmail());
        userCreate.setPassword(user.getPassword());
        return userRepository.save(userCreate);
    }

    public User updateUser(Long id, User user) {
        User userUpdate = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        userUpdate.setName(user.getName());
        userUpdate.setEmail(user.getEmail());
        userUpdate.setPassword(user.getPassword());
        return userRepository.save(userUpdate);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}