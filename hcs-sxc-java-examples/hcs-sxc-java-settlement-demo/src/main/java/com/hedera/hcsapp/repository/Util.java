/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hedera.hcsapp.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class Util {
    @PersistenceContext
    private EntityManager entityManager;

 
    
    public void stashData(){
        entityManager.createNativeQuery("SCRIPT TO 'h2data/stash.sql'").getResultList();
    }
    
    public void stashPopData(){
        entityManager.createNativeQuery("DROP ALL OBJECTS;RUNSCRIPT FROM 'h2data/stash.sql'").executeUpdate();
        
    }
    
}
