/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.core.logging;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class LoggingPluginTypeTest {

  private LoggingPlugin annotation;

  @Before
  public void setUp() throws Exception {
    annotation = mock( LoggingPlugin.class );
    when( annotation.id() ).thenReturn( "id" );
  }

  @Test
  public void pickUpsId() throws Exception {
    assertEquals( "id", LoggingPluginType.getInstance().extractID( annotation ) );
  }

  @Test
  public void pickUpName_NameIsSpecified() throws Exception {
    when( annotation.name() ).thenReturn( "name" );
    assertEquals( "name", LoggingPluginType.getInstance().extractName( annotation ) );
  }

  @Test
  public void pickUpName_NameIsNotSpecified() throws Exception {
    assertEquals( "id", LoggingPluginType.getInstance().extractName( annotation ) );
  }
}
