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

package org.pentaho.di.trans;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObject;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.util.Assert;
import org.pentaho.di.core.variables.Variables;

public class DefaultTransFactoryTest {

  @Before
  public void init() throws Exception {
    KettleClientEnvironment.init();
  }

  @Test
  public void testCreate() throws KettleException {
    TransMeta meta = new TransMeta(
      DefaultBowl.getInstance(), this.getClass().getResource( "one-step-trans.ktr" ).getPath(), new Variables() );
    LoggingObjectInterface loggingObject = new LoggingObject( "anything" );
    Trans trans = new DefaultTransFactory().create( meta, loggingObject );
    Assert.assertTrue( trans instanceof Trans );
  }
}
