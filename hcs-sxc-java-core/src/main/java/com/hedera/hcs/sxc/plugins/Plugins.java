package com.hedera.hcs.sxc.plugins;

/*-
 * ‌
 * hcs-sxc-java
 * ​
 * Copyright (C) 2019 - 2020 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

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
