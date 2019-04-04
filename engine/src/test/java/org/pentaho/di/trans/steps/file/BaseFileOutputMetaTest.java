/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
