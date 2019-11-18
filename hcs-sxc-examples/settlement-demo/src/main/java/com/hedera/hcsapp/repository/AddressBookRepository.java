package com.hedera.hcsapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.hedera.hcsapp.entities.AddressBook;

public interface AddressBookRepository extends CrudRepository<AddressBook, String> {
    @Query("SELECT ab FROM AddressBook ab WHERE ab.publicKey <> :userKey")
    List<AddressBook> findAllUsersButMe(@Param("userKey") String userKey);

    @Query("SELECT ab FROM AddressBook ab WHERE ab.name = :userName")
    AddressBook findUserByName(@Param("userName") String userName);
    
    @Query("SELECT ab FROM AddressBook ab WHERE ab.publicKey <> :userKey AND roles LIKE CONCAT('%',:role,'%')")
    List<AddressBook> findAllBuyersSellersButMe(@Param("userKey") String userKey, @Param("role") String role);
}

