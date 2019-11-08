package com.hedera.hcslib.persistence;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.MethodInfoList;
import io.github.classgraph.MethodParameterInfo;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Method;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.Timestamp;
import com.hedera.hcslib.interfaces.*;

import com.hedera.mirror.api.proto.java.MirrorGetTopicMessages.MirrorGetTopicMessagesResponse;

public final class TestPlugins {
    
//    public void LoadPlugins() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
  public static void main(String[] args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException { 
        try (ScanResult result = new ClassGraph().enableAllInfo()
                .whitelistPackages("com.hedera.plugin.persistence.inmemory")
                .scan()) {
                
            ClassInfoList list = result.getAllClasses();
//            for (ClassInfo classInfo : list) {
//                System.out.println(classInfo.getName());
//                for (ClassInfo classInfo2 : classInfo.getInterfaces()) {
//                    System.out.println(classInfo2.getName());
//                }
//            }            
            ClassInfoList classInfos = result.getClassesImplementing("com.hedera.hcslib.interfaces.LibMessagePersistence");
            for (ClassInfo classInfo : classInfos) {
                classInfo.loadClass();
                
                    for (MethodInfo methodInfo : classInfo.getMethodInfo("storeMessage")) {
                    System.out.println (methodInfo.getName());
                    MethodParameterInfo[] paramInfo = methodInfo.getParameterInfo();
                    for (int i=0; i < paramInfo.length; i++) {
                        System.out.println(" " + paramInfo[i].getTypeSignatureOrTypeDescriptor());
                    }
                }
                
                Class<?> clazz = Class.forName(classInfo.getName());
                Object object = clazz.getConstructor().newInstance(); 
                
                Class<?>[] methodParams = new Class[2];
                methodParams[0] = MessagePersistenceLevel.class;
                methodParams[1] = MirrorGetTopicMessagesResponse.Builder.class;

                Method method = clazz.getMethod("storeMessage", methodParams);
                
                MirrorGetTopicMessagesResponse.Builder mirrorGetTopicMessagesResponse = MirrorGetTopicMessagesResponse.newBuilder();
                mirrorGetTopicMessagesResponse.setConsensusTimestamp(Timestamp.newBuilder().setSeconds(10).setNanos(20));
                mirrorGetTopicMessagesResponse.setMessage(ByteString.copyFromUtf8("Hello There"));
                mirrorGetTopicMessagesResponse.setRunningHash(ByteString.copyFromUtf8("Running hash"));
                mirrorGetTopicMessagesResponse.setSequenceNumber(1);
                mirrorGetTopicMessagesResponse.build();
                
                method.setAccessible(true);
                method.invoke(object, MessagePersistenceLevel.FULL, mirrorGetTopicMessagesResponse);
            }
                  
        }
    }
}
