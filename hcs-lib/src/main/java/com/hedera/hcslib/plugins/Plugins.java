package com.hedera.hcslib.plugins;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class Plugins {
    
  public static Class<?> find(String searchPackage, String searchInterface, Boolean required) throws Exception { 
        try (ScanResult result = new ClassGraph().enableAllInfo()
                .whitelistPackages(searchPackage)
                .scan()) {
                
            ClassInfoList classInfoList = result.getClassesImplementing(searchInterface);
            if (classInfoList.size() > 1) {
                String errorMessage = "Too many plugins matching package '" + searchPackage + "' and interface '" + searchInterface + "' found on classpath.";
                log.error(errorMessage);
                throw new Exception(errorMessage);
            } else if (required && (classInfoList.size() == 0)) {
                String errorMessage = "Required plugin matching package '" + searchPackage + "' and interface '" + searchInterface + "' not found on classpath.";
                log.error(errorMessage);
                throw new Exception(errorMessage);
            }
            return Class.forName(classInfoList.get(0).getName());
        }
    }
}
