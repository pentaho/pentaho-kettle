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


package org.pentaho.di.engine.configuration.impl.pentaho;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.CheckedMetaStoreSupplier;

import static org.junit.Assert.assertEquals;

/**
 * Created by bmorrise on 4/4/17.
 */
@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class DefaultRunConfigurationProviderTest {

  private DefaultRunConfigurationProvider defaultRunConfigurationProvider;

  @Mock
  private CheckedMetaStoreSupplier metastoreSupplier;

  @Mock
  private DefaultRunConfigurationExecutor defaultRunConfigurationExecutor;

  @Before
  public void setup() {
    defaultRunConfigurationProvider =
      new DefaultRunConfigurationProvider( metastoreSupplier );
  }

  @Test
  public void testLoadNullName() {
    RunConfiguration defaultRunConfiguration = defaultRunConfigurationProvider.load( null );

    assertEquals( defaultRunConfiguration.getType(), "Pentaho" );
  }

}
