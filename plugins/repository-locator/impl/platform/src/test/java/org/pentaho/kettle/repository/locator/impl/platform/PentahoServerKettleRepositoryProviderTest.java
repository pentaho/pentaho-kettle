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

package org.pentaho.kettle.repository.locator.impl.platform;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.www.CarteSingleton;
import org.pentaho.di.www.SlaveServerConfig;
import org.pentaho.di.www.TransformationMap;
import org.pentaho.kettle.repository.locator.api.KettleRepositoryProvider;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class PentahoServerKettleRepositoryProviderTest {

  @Mock
  private CarteSingleton carteSingleton;

  @Mock
  private TransformationMap transformationMap;

  @Mock
  private SlaveServerConfig slaveServerConfig;

  @Mock
  private Repository repository;

  private PentahoServerKettleRepositoryProvider provider;

  @Before
  public void setUp() {
    provider = new PentahoServerKettleRepositoryProvider();
  }

  @Test
  public void testImplementsKettleRepositoryProvider() {
    assertTrue(provider instanceof KettleRepositoryProvider);
  }

  @Test
  public void testGetPriority() {
    assertEquals(150, provider.getPriority());
  }

  @Test
  public void testGetRepositorySuccess() throws KettleException {
    // Given
    try (MockedStatic<CarteSingleton> mockedCarteSingleton = mockStatic(CarteSingleton.class)) {
      mockedCarteSingleton.when(CarteSingleton::getInstance).thenReturn(carteSingleton);
      when(carteSingleton.getTransformationMap()).thenReturn(transformationMap);
      when(transformationMap.getSlaveServerConfig()).thenReturn(slaveServerConfig);
      when(slaveServerConfig.getRepository()).thenReturn(repository);

      // When
      Repository result = provider.getRepository();

      // Then
      assertNotNull(result);
      assertEquals(repository, result);
      verify(carteSingleton).getTransformationMap();
      verify(transformationMap).getSlaveServerConfig();
      verify(slaveServerConfig).getRepository();
    }
  }

  @Test
  public void testGetRepositoryWithKettleException() throws KettleException {
    // Given
    try (MockedStatic<CarteSingleton> mockedCarteSingleton = mockStatic(CarteSingleton.class)) {
      mockedCarteSingleton.when(CarteSingleton::getInstance).thenReturn(carteSingleton);
      when(carteSingleton.getTransformationMap()).thenReturn(transformationMap);
      when(transformationMap.getSlaveServerConfig()).thenReturn(slaveServerConfig);
      when(slaveServerConfig.getRepository()).thenThrow(new KettleException("Test exception"));

      // When
      Repository result = provider.getRepository();

      // Then
      assertNull(result);
      verify(carteSingleton).getTransformationMap();
      verify(transformationMap).getSlaveServerConfig();
      verify(slaveServerConfig).getRepository();
    }
  }

  @Test
  public void testGetRepositoryWithNullTransformationMap() {
    // Given
    try (MockedStatic<CarteSingleton> mockedCarteSingleton = mockStatic(CarteSingleton.class)) {
      mockedCarteSingleton.when(CarteSingleton::getInstance).thenReturn(carteSingleton);
      when(carteSingleton.getTransformationMap()).thenReturn(null);

      // When & Then
      try {
        Repository result = provider.getRepository();
        fail("Expected NullPointerException");
      } catch (NullPointerException e) {
        // Expected behavior when transformationMap is null
      }
      
      verify(carteSingleton).getTransformationMap();
    }
  }

  @Test
  public void testGetRepositoryWithNullSlaveServerConfig() {
    // Given
    try (MockedStatic<CarteSingleton> mockedCarteSingleton = mockStatic(CarteSingleton.class)) {
      mockedCarteSingleton.when(CarteSingleton::getInstance).thenReturn(carteSingleton);
      when(carteSingleton.getTransformationMap()).thenReturn(transformationMap);
      when(transformationMap.getSlaveServerConfig()).thenReturn(null);

      // When & Then
      try {
        Repository result = provider.getRepository();
        fail("Expected NullPointerException");
      } catch (NullPointerException e) {
        // Expected behavior when slaveServerConfig is null
      }

      verify(carteSingleton).getTransformationMap();
      verify(transformationMap).getSlaveServerConfig();
    }
  }

  @Test
  public void testGetRepositoryWithNullCarteSingleton() {
    // Given
    try (MockedStatic<CarteSingleton> mockedCarteSingleton = mockStatic(CarteSingleton.class)) {
      mockedCarteSingleton.when(CarteSingleton::getInstance).thenReturn(null);

      // When & Then
      try {
        Repository result = provider.getRepository();
        fail("Expected NullPointerException");
      } catch (NullPointerException e) {
        // Expected behavior when CarteSingleton.getInstance() returns null
      }
    }
  }

  @Test
  public void testConstructor() {
    // Given & When
    PentahoServerKettleRepositoryProvider newProvider = new PentahoServerKettleRepositoryProvider();

    // Then
    assertNotNull(newProvider);
    assertTrue(newProvider instanceof KettleRepositoryProvider);
    assertEquals(150, newProvider.getPriority());
  }

  @Test
  public void testServiceProviderAnnotation() {
    // Verify that the class has the correct ServiceProvider annotation
    assertTrue(provider.getClass().isAnnotationPresent(org.pentaho.di.core.service.ServiceProvider.class));
    
    org.pentaho.di.core.service.ServiceProvider annotation = 
        provider.getClass().getAnnotation(org.pentaho.di.core.service.ServiceProvider.class);
    
    assertEquals("PentahoServerKettleRepositoryProvider", annotation.id());
    assertEquals("Get the repository from the provided Carte Server", annotation.description());
    assertEquals(KettleRepositoryProvider.class, annotation.provides());
  }

  @Test
  public void testMultipleCallsToGetRepository() throws KettleException {
    // Given
    try (MockedStatic<CarteSingleton> mockedCarteSingleton = mockStatic(CarteSingleton.class)) {
      mockedCarteSingleton.when(CarteSingleton::getInstance).thenReturn(carteSingleton);
      when(carteSingleton.getTransformationMap()).thenReturn(transformationMap);
      when(transformationMap.getSlaveServerConfig()).thenReturn(slaveServerConfig);
      when(slaveServerConfig.getRepository()).thenReturn(repository);

      // When
      Repository result1 = provider.getRepository();
      Repository result2 = provider.getRepository();

      // Then
      assertNotNull(result1);
      assertNotNull(result2);
      assertEquals(repository, result1);
      assertEquals(repository, result2);
      assertEquals(result1, result2);
      
      // Verify that the method chain is called each time (no caching)
      verify(carteSingleton, times(2)).getTransformationMap();
      verify(transformationMap, times(2)).getSlaveServerConfig();
      verify(slaveServerConfig, times(2)).getRepository();
    }
  }

  @Test
  public void testGetRepositoryReturnsNullRepository() throws KettleException {
    // Given
    try (MockedStatic<CarteSingleton> mockedCarteSingleton = mockStatic(CarteSingleton.class)) {
      mockedCarteSingleton.when(CarteSingleton::getInstance).thenReturn(carteSingleton);
      when(carteSingleton.getTransformationMap()).thenReturn(transformationMap);
      when(transformationMap.getSlaveServerConfig()).thenReturn(slaveServerConfig);
      when(slaveServerConfig.getRepository()).thenReturn(null);

      // When
      Repository result = provider.getRepository();

      // Then
      assertNull(result);
      verify(carteSingleton).getTransformationMap();
      verify(transformationMap).getSlaveServerConfig();
      verify(slaveServerConfig).getRepository();
    }
  }

  @Test
  public void testGetRepositoryExceptionHandling() throws KettleException {
    // Given
    KettleException kettleException = new KettleException("Repository access failed");
    
    try (MockedStatic<CarteSingleton> mockedCarteSingleton = mockStatic(CarteSingleton.class)) {
      mockedCarteSingleton.when(CarteSingleton::getInstance).thenReturn(carteSingleton);
      when(carteSingleton.getTransformationMap()).thenReturn(transformationMap);
      when(transformationMap.getSlaveServerConfig()).thenReturn(slaveServerConfig);
      when(slaveServerConfig.getRepository()).thenThrow(kettleException);

      // When
      Repository result = provider.getRepository();

      // Then
      assertNull(result);
      
      // Verify that the exception was caught and handled gracefully
      verify(carteSingleton).getTransformationMap();
      verify(transformationMap).getSlaveServerConfig();
      verify(slaveServerConfig).getRepository();
    }
  }
}
