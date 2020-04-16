package com.hedera.tokendemo.service;

import com.hedera.tokendemo.config.AppData;
import com.hedera.tokendemo.domain.User;
import com.hedera.tokendemo.model.UserModel;
import com.hedera.tokendemo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserModel {

    private final UserRepository userRepository;
    private final AppData appData;

    public UserService(UserRepository userRepository, AppData appData) {
        this.userRepository = userRepository;
        this.appData = appData;
    }

    @Override
    public List<User> getUsers() {
        return this.userRepository.getUsers(appData.getUserName());
    }

    @Override
    public List<User> getAllUsers() {
        return (List<User>) this.userRepository.findAll();
    }

    @Override
    public boolean exists(String userName) {
        return userRepository.findByName(userName).isPresent();
    }

    @Override
    public void create(String userName, String publicKeys, String userOf) {
        if (! exists(userName)) {
            User user = new User();
            user.setName(userName);
            user.setPublicKeys(publicKeys);
            user.setUserOf(userOf);
            userRepository.save(user);
        }
    }

    @Override
    public long getIdFromName(String userName) throws RuntimeException {
        Optional<User> user = userRepository.findByName(userName);
        if (user.isPresent()) {
            return user.get().getId();
        } else {
            throw new RuntimeException("User " + userName + " not found");
        }
    }

    @Override
    public User getFromName(String userName) throws RuntimeException {
        Optional<User> user = userRepository.findByName(userName);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new RuntimeException ("User " + userName + " not found");
        }
    }

    @Override
    public String getNameFromId(long userId) throws RuntimeException {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return user.get().getName();
        } else {
            throw new RuntimeException ("User ID " + userId + " not found");
        }
    }
    
    @Override
    public List<User> usersForAccount(long accountId) {
        return userRepository.usersForAccount(accountId);
    }
}
