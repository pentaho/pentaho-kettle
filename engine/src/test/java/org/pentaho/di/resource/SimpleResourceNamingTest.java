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


package org.pentaho.di.resource;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;



public class SimpleResourceNamingTest {

  @Test
  public void testCreateNewParameterName() {

    SimpleResourceNaming simpleResourceNaming1 = new SimpleResourceNaming();
    SimpleResourceNaming simpleResourceNaming2 = new SimpleResourceNaming();

    String prefix = UUID.randomUUID().toString();
    String originalFilePath = UUID.randomUUID().toString();
    String extension = UUID.randomUUID().toString();
    ResourceNamingInterface.FileNamingType namingType = ResourceNamingInterface.FileNamingType.DATA_FILE;

    simpleResourceNaming1.nameResource( prefix, originalFilePath, extension, namingType );
    assertEquals( 1, SimpleResourceNaming.getParameterNr() );
    simpleResourceNaming2.nameResource( prefix, originalFilePath, extension, namingType );
    assertEquals( 2, SimpleResourceNaming.getParameterNr() );
  }

}
