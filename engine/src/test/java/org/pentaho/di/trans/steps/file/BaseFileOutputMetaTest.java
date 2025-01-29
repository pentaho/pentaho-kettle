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


package org.pentaho.di.trans.steps.file;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class BaseFileOutputMetaTest {

  @Mock BaseFileOutputMeta meta;

  @Before
  public void setup() {
    meta = Mockito.spy( BaseFileOutputMeta.class );
  }

  @Test
  public void testGetFiles() {
    String[] filePaths;

    filePaths = meta.getFiles( "foo", "txt", false );
    assertNotNull( filePaths );
    assertEquals( 1, filePaths.length );
    assertEquals( "foo.txt", filePaths[ 0 ] );
    filePaths = meta.getFiles( "foo", "txt", true );
    assertNotNull( filePaths );
    assertEquals( 1, filePaths.length );
    assertEquals( "foo.txt", filePaths[ 0 ] );

    when( meta.isStepNrInFilename() ).thenReturn( true );

    filePaths = meta.getFiles( "foo", "txt", false );
    assertNotNull( filePaths );
    assertEquals( 1, filePaths.length );
    assertEquals( "foo_<step>.txt", filePaths[ 0 ] );
    filePaths = meta.getFiles( "foo", "txt", true );
    assertNotNull( filePaths );
    assertEquals( 4, filePaths.length );
    assertEquals( "foo_0.txt", filePaths[ 0 ] );
    assertEquals( "foo_1.txt", filePaths[ 1 ] );
    assertEquals( "foo_2.txt", filePaths[ 2 ] );
    assertEquals( "...", filePaths[ 3 ] );

    when( meta.isPartNrInFilename() ).thenReturn( true );

    filePaths = meta.getFiles( "foo", "txt", false );
    assertNotNull( filePaths );
    assertEquals( 1, filePaths.length );
    assertEquals( "foo_<step>_<partition>.txt", filePaths[ 0 ] );

    when( meta.getSplitEvery() ).thenReturn( 1 );

    filePaths = meta.getFiles( "foo", "txt", false );
    assertNotNull( filePaths );
    assertEquals( 1, filePaths.length );
    assertEquals( "foo_<step>_<partition>_<split>.txt", filePaths[ 0 ] );

  }

}
