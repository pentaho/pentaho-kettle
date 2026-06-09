/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang3.ClassUtils;
import org.junit.Test;

/**
 * CVE-2025-48924: Test suite for commons-lang3 3.18.0 upgrade
 * 
 * These tests verify that ClassUtils methods work correctly with the upgraded commons-lang3
 * library and that the vulnerability to stack overflow with long inputs is fixed.
 */
public class ClassUtilsUpgradeTest {

  @Test
  public void testClassUtilsAvailable() {
    assertNotNull( "ClassUtils should be available from commons-lang3", ClassUtils.class );
  }

  @Test
  public void testGetAllInterfacesNormalClass() {
    Collection<Class<?>> interfaces = ClassUtils.getAllInterfaces( String.class );
    assertNotNull( "getAllInterfaces should return non-null collection", interfaces );
    assertTrue( "String class should have interfaces", interfaces.size() > 0 );
  }

  @Test
  public void testGetAllInterfacesInterface() {
    // Test with a concrete class that implements Cloneable
    Collection<Class<?>> interfaces = ClassUtils.getAllInterfaces( Object[].class );
    assertNotNull( "getAllInterfaces should return non-null collection for interface", interfaces );
    // Object[] implements Cloneable
    assertTrue( "Should contain Cloneable interface", interfaces.contains( Cloneable.class ) );
  }

  @Test
  public void testGetAllInterfacesMultipleInterfaces() {
    Collection<Class<?>> interfaces = ClassUtils.getAllInterfaces( HashSet.class );
    assertNotNull( "getAllInterfaces should return non-null collection", interfaces );
    assertTrue( "Should contain Set interface", interfaces.contains( java.util.Set.class ) );
    assertTrue( "Should contain Cloneable interface", interfaces.contains( Cloneable.class ) );
  }

  /**
   * CVE-2025-48924: Test that very long class names don't cause StackOverflowError
   */
  @Test
  public void testLongClassNameNoStackOverflow() {
    StringBuilder longClassName = new StringBuilder();
    for ( int i = 0; i < 5000; i++ ) {
      longClassName.append( "com.example." );
    }
    longClassName.append( "TestClass" );

    String maliciousInput = longClassName.toString();

    try {
      try {
        ClassUtils.getClass( maliciousInput );
      } catch ( ClassNotFoundException e ) {
        // Expected
      }
    } catch ( StackOverflowError e ) {
      throw new AssertionError( "Very long class name should not cause StackOverflowError", e );
    }
  }

  /**
   * CVE-2025-48924: Test that deeply nested class names don't cause StackOverflowError
   */
  @Test
  public void testDeeplyNestedClassNameNoStackOverflow() {
    StringBuilder nestedClassName = new StringBuilder( "java.lang" );
    for ( int i = 0; i < 1000; i++ ) {
      nestedClassName.append( "$Inner" );
    }

    String maliciousInput = nestedClassName.toString();

    try {
      try {
        ClassUtils.getClass( maliciousInput );
      } catch ( ClassNotFoundException e ) {
        // Expected
      }
    } catch ( StackOverflowError e ) {
      throw new AssertionError( "Deeply nested class name should not cause StackOverflowError", e );
    }
  }

  @Test
  public void testGetAllInterfacesPrimitive() {
    Collection<Class<?>> interfaces = ClassUtils.getAllInterfaces( int.class );
    assertNotNull( "getAllInterfaces should return non-null collection for primitive", interfaces );
  }

  @Test
  public void testGetAllInterfacesNull() {
    Collection<Class<?>> interfaces = ClassUtils.getAllInterfaces( (Class<?>) null );
    if ( interfaces != null ) {
      assertFalse( "Should not contain null in interfaces", interfaces.contains( null ) );
    }
  }

  @Test
  public void testPrimitiveToWrapper() {
    Class<?> wrappedInt = ClassUtils.primitiveToWrapper( int.class );
    assertNotNull( "primitiveToWrapper should return non-null for int", wrappedInt );
    assertTrue( "int should wrap to Integer", wrappedInt.equals( Integer.class ) );
  }

  @Test
  public void testWrapperToPrimitive() {
    Class<?> primitive = ClassUtils.wrapperToPrimitive( Integer.class );
    assertNotNull( "wrapperToPrimitive should return non-null for Integer", primitive );
    assertTrue( "Integer should unwrap to int", primitive.equals( int.class ) );
  }

  @Test
  public void testGetAllSuperclasses() {
    Collection<Class<?>> superclasses = ClassUtils.getAllSuperclasses( String.class );
    assertNotNull( "getAllSuperclasses should return non-null collection", superclasses );
    assertTrue( "Should contain Object class", superclasses.contains( Object.class ) );
  }

  @Test
  public void testGetClassStandard() throws ClassNotFoundException {
    Class<?> stringClass = ClassUtils.getClass( "java.lang.String" );
    assertNotNull( "getClass should return String class", stringClass );
    assertTrue( "Should return correct class", stringClass.equals( String.class ) );
  }

  @Test
  public void testGetClassWithInitialize() throws ClassNotFoundException {
    Class<?> stringClass = ClassUtils.getClass( "java.lang.String", true );
    assertNotNull( "getClass with initialize=true should return class", stringClass );
    assertTrue( "Should return correct class", stringClass.equals( String.class ) );
  }

  @Test
  public void testCommonLang3Version() {
    String classLocation = ClassUtils.class.getName();
    assertTrue( "ClassUtils should be from commons.lang3 package", classLocation.contains( "commons.lang3" ) );
  }

  @Test
  public void testBackwardCompatibility() throws ClassNotFoundException {
    ClassUtils.getAllInterfaces( String.class );
    ClassUtils.getAllSuperclasses( String.class );
    ClassUtils.primitiveToWrapper( int.class );
    ClassUtils.wrapperToPrimitive( Integer.class );
    ClassUtils.getClass( "java.lang.String" );
    ClassUtils.getClass( "java.lang.String", true );
  }

  @Test
  public void testPerformanceGetAllInterfaces() {
    long startTime = System.nanoTime();
    for ( int i = 0; i < 1000; i++ ) {
      ClassUtils.getAllInterfaces( String.class );
    }
    long endTime = System.nanoTime();
    long durationMs = ( endTime - startTime ) / 1_000_000;

    assertTrue( "getAllInterfaces should complete 1000 iterations in < 5 seconds, took: " + durationMs + "ms", 
               durationMs < 5000 );
  }

  @Test
  public void testMultipleSuccessiveCalls() {
    Class<?>[] classes = { String.class, Integer.class, Boolean.class, Double.class, Long.class };
    
    for ( Class<?> cls : classes ) {
      Collection<Class<?>> interfaces = ClassUtils.getAllInterfaces( cls );
      assertNotNull( "getAllInterfaces should work for: " + cls.getName(), interfaces );
    }
  }
}
