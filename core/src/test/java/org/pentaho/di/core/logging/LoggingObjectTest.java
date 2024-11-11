/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/



package org.pentaho.di.core.logging;

import junit.framework.Assert;
import org.junit.Test;

public class LoggingObjectTest {

  @Test
  public void testEquals() throws Exception {
    LoggingObjectInterface parent = new LoggingObject( new SimpleLoggingObject( "parent", LoggingObjectType.JOB, null ) );

    LoggingObject loggingObject1 = new LoggingObject( "test" );
    loggingObject1.setFilename( "fileName" );
    loggingObject1.setParent( parent );
    loggingObject1.setObjectName( "job1" );


    LoggingObject loggingObject2 = new LoggingObject( "test" );
    loggingObject2.setFilename( "fileName" );
    loggingObject2.setParent( parent );
    loggingObject2.setObjectName( "job2" );

    Assert.assertFalse( loggingObject1.equals( loggingObject2 ) );
  }

}
