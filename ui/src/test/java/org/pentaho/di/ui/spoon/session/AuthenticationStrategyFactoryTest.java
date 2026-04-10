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

package org.pentaho.di.ui.spoon.session;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith( Parameterized.class )
public class AuthenticationStrategyFactoryTest {

  @Parameterized.Parameter( 0 )
  public String getStrategyInput;

  @Parameterized.Parameter( 1 )
  public String invalidRegistrationType;

  @Parameterized.Parameters( name = "get={0}, registerInvalid={1}" )
  public static Collection<Object[]> data() {
    return Arrays.asList( new Object[][] {
      { "SESSION", null },
      { "  SESSION  ", "" },
      { null, "   " },
      { "", null },
      { "   ", "" },
      { "NONEXISTENT", "   " }
    } );
  }

  private AuthenticationStrategy mockStrategy;
  private AuthenticationStrategy anotherMockStrategy;

  @BeforeClass
  public static void setUpClass() throws KettleException {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() {
    mockStrategy = mock( AuthenticationStrategy.class );
    anotherMockStrategy = mock( AuthenticationStrategy.class );
  }

  @After
  public void tearDown() {
    AuthenticationStrategyFactory.registerStrategy( "SESSION", new SessionBasedAuthStrategy() );
  }

  @Test
  public void defaultStrategyReturnsSessionBasedInstance() {
    AuthenticationStrategy strategy = AuthenticationStrategyFactory.getDefaultStrategy();

    assertNotNull( strategy );
    assertTrue( strategy instanceof SessionBasedAuthStrategy );
  }

  @Test
  public void getStrategyReturnsSessionBasedForInputVariants() {
    AuthenticationStrategy strategy = AuthenticationStrategyFactory.getStrategy( getStrategyInput );

    assertNotNull( strategy );
    assertTrue( strategy instanceof SessionBasedAuthStrategy );
  }

  @Test
  public void getStrategyIsCaseInsensitive() {
    AuthenticationStrategy lower = AuthenticationStrategyFactory.getStrategy( "session" );
    AuthenticationStrategy upper = AuthenticationStrategyFactory.getStrategy( "SESSION" );
    AuthenticationStrategy mixed = AuthenticationStrategyFactory.getStrategy( "SeSsIoN" );

    assertNotNull( lower );
    assertNotNull( upper );
    assertNotNull( mixed );
    assertTrue( lower instanceof SessionBasedAuthStrategy );
    assertSame( upper, lower );
    assertSame( mixed, lower );
  }

  @Test
  public void registerAndRetrieveCustomStrategy() {
    AuthenticationStrategyFactory.registerStrategy( "CUSTOM", mockStrategy );

    assertSame( mockStrategy, AuthenticationStrategyFactory.getStrategy( "CUSTOM" ) );
  }

  @Test
  public void registerNormalizesTypeToCaseInsensitiveLookup() {
    AuthenticationStrategyFactory.registerStrategy( "oauth", mockStrategy );

    assertSame( mockStrategy, AuthenticationStrategyFactory.getStrategy( "OAUTH" ) );
    assertSame( mockStrategy, AuthenticationStrategyFactory.getStrategy( "oauth" ) );
    assertSame( mockStrategy, AuthenticationStrategyFactory.getStrategy( "OaUtH" ) );
  }

  @Test
  public void registerTrimsWhitespaceFromType() {
    AuthenticationStrategyFactory.registerStrategy( "  TRIMMED  ", mockStrategy );

    assertSame( mockStrategy, AuthenticationStrategyFactory.getStrategy( "TRIMMED" ) );
  }

  @Test
  public void registerOverwritesPreviousStrategyForSameType() {
    AuthenticationStrategyFactory.registerStrategy( "REPLACE", mockStrategy );
    AuthenticationStrategyFactory.registerStrategy( "REPLACE", anotherMockStrategy );

    assertSame( anotherMockStrategy, AuthenticationStrategyFactory.getStrategy( "REPLACE" ) );
    assertNotSame( mockStrategy, AuthenticationStrategyFactory.getStrategy( "REPLACE" ) );
  }

  @Test
  public void registerMultipleStrategiesIndependently() {
    AuthenticationStrategyFactory.registerStrategy( "A", mockStrategy );
    AuthenticationStrategyFactory.registerStrategy( "B", anotherMockStrategy );

    assertSame( mockStrategy, AuthenticationStrategyFactory.getStrategy( "A" ) );
    assertSame( anotherMockStrategy, AuthenticationStrategyFactory.getStrategy( "B" ) );
    assertNotSame( AuthenticationStrategyFactory.getStrategy( "A" ),
      AuthenticationStrategyFactory.getStrategy( "B" ) );
  }

  @Test
  public void registerCustomStrategyDoesNotAffectDefaultStrategy() {
    AuthenticationStrategyFactory.registerStrategy( "CUSTOM", mockStrategy );

    AuthenticationStrategy defaultStrategy = AuthenticationStrategyFactory.getDefaultStrategy();
    assertTrue( defaultStrategy instanceof SessionBasedAuthStrategy );
    assertNotSame( mockStrategy, defaultStrategy );
  }

  @Test
  public void registerIgnoresInvalidTypeVariants() {
    AuthenticationStrategyFactory.registerStrategy( invalidRegistrationType, mockStrategy );

    assertNotNull( AuthenticationStrategyFactory.getDefaultStrategy() );
  }

  @Test
  public void registerIgnoresNullStrategy() {
    AuthenticationStrategyFactory.registerStrategy( "NULLSTRAT", null );

    AuthenticationStrategy result = AuthenticationStrategyFactory.getStrategy( "NULLSTRAT" );
    assertTrue( result instanceof SessionBasedAuthStrategy );
  }

  @Test
  public void registerIgnoresNullTypeAndNullStrategy() {
    AuthenticationStrategyFactory.registerStrategy( null, null );

    assertNotNull( AuthenticationStrategyFactory.getDefaultStrategy() );
  }

  @Test
  public void registerCanOverrideBuiltInSessionStrategy() {
    AuthenticationStrategyFactory.registerStrategy( "SESSION", mockStrategy );

    assertSame( mockStrategy, AuthenticationStrategyFactory.getStrategy( "SESSION" ) );
    assertSame( mockStrategy, AuthenticationStrategyFactory.getDefaultStrategy() );
  }

  @Test
  public void getStrategyTrimsWhitespaceOnLookupForCustomStrategy() {
    AuthenticationStrategyFactory.registerStrategy( "PADDED", mockStrategy );

    assertSame( mockStrategy, AuthenticationStrategyFactory.getStrategy( "  PADDED  " ) );
  }
}
