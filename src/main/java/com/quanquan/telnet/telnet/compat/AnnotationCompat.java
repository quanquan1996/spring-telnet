package com.quanquan.telnet.telnet.compat;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Compatibility layer for handling annotation differences between JDK versions.
 * This class provides utility methods to work with annotations that might be in
 * different packages (javax.* vs jakarta.*) depending on the JDK version.
 */
public class AnnotationCompat {

    private static final boolean IS_JAKARTA_AVAILABLE;
    
    static {
        boolean jakartaAvailable = false;
        try {
            Class.forName("jakarta.annotation.Resource");
            jakartaAvailable = true;
        } catch (ClassNotFoundException e) {
            // Jakarta annotations not available
        }
        IS_JAKARTA_AVAILABLE = jakartaAvailable;
    }
    
    /**
     * Checks if Jakarta annotations are available in the current environment.
     * 
     * @return true if Jakarta annotations are available, false otherwise
     */
    public static boolean isJakartaAvailable() {
        return IS_JAKARTA_AVAILABLE;
    }
    
    /**
     * Gets the appropriate Resource annotation class based on the available packages.
     * 
     * @return the Resource annotation class (either javax.annotation.Resource or jakarta.annotation.Resource)
     * @throws ClassNotFoundException if neither annotation class is available
     */
    public static Class<? extends Annotation> getResourceAnnotationClass() throws ClassNotFoundException {
        if (IS_JAKARTA_AVAILABLE) {
            return (Class<? extends Annotation>) Class.forName("jakarta.annotation.Resource");
        } else {
            return (Class<? extends Annotation>) Class.forName("javax.annotation.Resource");
        }
    }
    
    /**
     * Gets the appropriate PreDestroy annotation class based on the available packages.
     * 
     * @return the PreDestroy annotation class (either javax.annotation.PreDestroy or jakarta.annotation.PreDestroy)
     * @throws ClassNotFoundException if neither annotation class is available
     */
    public static Class<? extends Annotation> getPreDestroyAnnotationClass() throws ClassNotFoundException {
        if (IS_JAKARTA_AVAILABLE) {
            return (Class<? extends Annotation>) Class.forName("jakarta.annotation.PreDestroy");
        } else {
            return (Class<? extends Annotation>) Class.forName("javax.annotation.PreDestroy");
        }
    }
    
    /**
     * Gets the appropriate PostConstruct annotation class based on the available packages.
     * 
     * @return the PostConstruct annotation class (either javax.annotation.PostConstruct or jakarta.annotation.PostConstruct)
     * @throws ClassNotFoundException if neither annotation class is available
     */
    public static Class<? extends Annotation> getPostConstructAnnotationClass() throws ClassNotFoundException {
        if (IS_JAKARTA_AVAILABLE) {
            return (Class<? extends Annotation>) Class.forName("jakarta.annotation.PostConstruct");
        } else {
            return (Class<? extends Annotation>) Class.forName("javax.annotation.PostConstruct");
        }
    }
    
    /**
     * Checks if a method has a PreDestroy annotation (either javax or jakarta).
     * 
     * @param method the method to check
     * @return true if the method has a PreDestroy annotation, false otherwise
     */
    public static boolean hasPreDestroyAnnotation(Method method) {
        try {
            return method.isAnnotationPresent(getPreDestroyAnnotationClass());
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Checks if a method has a PostConstruct annotation (either javax or jakarta).
     * 
     * @param method the method to check
     * @return true if the method has a PostConstruct annotation, false otherwise
     */
    public static boolean hasPostConstructAnnotation(Method method) {
        try {
            return method.isAnnotationPresent(getPostConstructAnnotationClass());
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}