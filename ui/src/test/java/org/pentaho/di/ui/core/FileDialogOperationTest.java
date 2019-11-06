/*
 * Copyright 2019 Hitachi Vantara. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package org.pentaho.di.ui.core;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.ui.core.widget.TextVar;

import static org.junit.Assert.assertEquals;

@RunWith( MockitoJUnitRunner.class )
public class FileDialogOperationTest {

  private @Mock TextVar textVar;
  private FileDialogOperation fileDialogOperation;

  @Before
  public void setUp() throws Exception {
    fileDialogOperation = new FileDialogOperation( FileDialogOperation.OPEN );
  }

  @Test
  public void startsWithProviderDerivedFromPath() {
    assertProvider( "hc://user/one/two", "clusters" );
    assertProvider( "pvfs://user/one/two", "vfs" );
    assertProvider( "/user/one/two", "local" );
  }

  public void assertProvider( String path, String provider ) {
    Mockito.reset( textVar );
    Mockito.when( textVar.getText() ).thenReturn( path );
    FileDialogOperation.setStartLocation( textVar, fileDialogOperation );
    assertEquals( provider, fileDialogOperation.getProvider() );
  }

  @Test
  public void openSetsThePath() {
    fileDialogOperation.setPath( "pvfs://s3/ackbar/files" );
    fileDialogOperation.setFilename( "calamari" );
    FileDialogOperation.handleOpen( textVar, fileDialogOperation );
    Mockito.verify( textVar ).setText( "pvfs://s3/ackbar/files" );
  }

  @Test
  public void saveConcatenatesPathAndFile() {
    fileDialogOperation.setPath( "pvfs://s3/ackbar/files" );
    fileDialogOperation.setFilename( "calamari" );
    FileDialogOperation.handleSave( textVar, fileDialogOperation );
    Mockito.verify( textVar ).setText( "pvfs://s3/ackbar/files/calamari" );
  }
}
