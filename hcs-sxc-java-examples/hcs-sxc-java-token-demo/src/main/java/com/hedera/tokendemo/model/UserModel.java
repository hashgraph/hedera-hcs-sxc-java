package com.hedera.tokendemo.model;

import com.hedera.tokendemo.domain.User;

import java.util.List;

public interface UserModel {

    List<User> getUsers();

    List<User> getAllUsers();

    long getIdFromName(String userName) throws Exception;

    boolean exists(String userName);

    void create(String userName, String publicKeys, String role);

    String getNameFromId(long userId) throws Exception;

    User getFromName(String userName) throws Exception;

    List<User> usersForAccount(long accountId);
}