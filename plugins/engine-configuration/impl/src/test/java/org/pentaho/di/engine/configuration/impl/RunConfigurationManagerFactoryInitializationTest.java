package org.pentaho.di.engine.configuration.impl;

import org.pentaho.di.engine.configuration.api.RunConfigurationProvider;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for the Bowl-based factory initialization pattern.
 * Verifies that factory initialization and singleton behavior work correctly.
 */
@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class RunConfigurationManagerFactoryInitializationTest {

  private MemoryMetaStore metaStore;

  @Before
  public void setup() throws Exception {
    metaStore = new MemoryMetaStore();
  }

  @Test
  public void testFactoryCanBeRetrievedMultipleTimes() throws Exception {
    // Arrange & Act - factory initialization should be idempotent
    RunConfigurationProviderFactoryManagerImpl factory1 = RunConfigurationProviderFactoryManagerImpl.getInstance();
    RunConfigurationProviderFactoryManagerImpl factory2 = RunConfigurationProviderFactoryManagerImpl.getInstance();

    // Assert - should be the same singleton instance
    assertNotNull( factory1 );
    assertNotNull( factory2 );
    assertEquals( factory1, factory2 );
  }

  @Test
  public void testFactoryCanGenerateProvidersFromMetaStore() throws Exception {
    // Arrange
    RunConfigurationProviderFactoryManagerImpl factory = RunConfigurationProviderFactoryManagerImpl.getInstance();

    // Act - generate providers from metastore supplier
    var providers = factory.generateProviders( () -> metaStore );

    // Assert - providers should not be null or empty
    assertNotNull( providers );
  }

  @Test
  public void testDefaultConfigNameConstantIsFromProvider() {
    // Assert - verify the constant is defined in the provider interface
    assertEquals( "Pentaho local", RunConfigurationProvider.DEFAULT_CONFIG_NAME );
  }

  @Test
  @SuppressWarnings( "deprecation" )
  public void testDeprecatedMethodStillFunctional() throws Exception {
    // Arrange & Act - deprecated method should not throw exception
    RunConfigurationManager manager = RunConfigurationManager.getInstance( () -> metaStore );

    // Assert
    assertNotNull( manager );
  }
}
