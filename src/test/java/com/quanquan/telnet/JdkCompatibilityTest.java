package com.quanquan.telnet;

import com.quanquan.telnet.telnet.compat.AnnotationCompat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify JDK compatibility features
 */
public class JdkCompatibilityTest {

    @Test
    public void testJdkVersion() {
        String javaVersion = System.getProperty("java.version");
        System.out.println("Running tests on Java version: " + javaVersion);
        
        // Just verify that we can get the Java version
        assertNotNull(javaVersion);
    }
    
    @Test
    public void testAnnotationCompatibility() {
        // Test that our annotation compatibility layer works
        try {
            Class<?> resourceAnnotationClass = AnnotationCompat.getResourceAnnotationClass();
            assertNotNull(resourceAnnotationClass);
            System.out.println("Resource annotation class: " + resourceAnnotationClass.getName());
            
            Class<?> preDestroyAnnotationClass = AnnotationCompat.getPreDestroyAnnotationClass();
            assertNotNull(preDestroyAnnotationClass);
            System.out.println("PreDestroy annotation class: " + preDestroyAnnotationClass.getName());
            
            Class<?> postConstructAnnotationClass = AnnotationCompat.getPostConstructAnnotationClass();
            assertNotNull(postConstructAnnotationClass);
            System.out.println("PostConstruct annotation class: " + postConstructAnnotationClass.getName());
        } catch (ClassNotFoundException e) {
            fail("Failed to load annotation classes: " + e.getMessage());
        }
    }
}