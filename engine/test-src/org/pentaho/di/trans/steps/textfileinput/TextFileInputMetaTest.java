/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.textfileinput;

import org.apache.commons.vfs.FileObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.resource.ResourceNamingInterface;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class TextFileInputMetaTest {
  private static final String FILE_NAME_NULL = null;
  private static final String FILE_NAME_VALID_PATH = "path/to/file";

  private TextFileInputMeta inputMeta;
  private VariableSpace variableSpace;

  @Before
  public void setUp() throws Exception {
    inputMeta = new TextFileInputMeta();
    inputMeta = spy( inputMeta );
    variableSpace = mock( VariableSpace.class );

    doReturn( "<def>" ).when( variableSpace ).environmentSubstitute( anyString() );
    doReturn( FILE_NAME_VALID_PATH ).when( variableSpace ).environmentSubstitute( FILE_NAME_VALID_PATH );
    FileObject mockedFileObject = mock( FileObject.class );
    doReturn( mockedFileObject ).when( inputMeta ).getFileObject( anyString(), eq( variableSpace ) );
  }

  @Test
  public void whenExportingResourcesWeGetFileObjectsOnlyFromFilesWithNotNullFileNames() throws Exception {
    inputMeta.setFileName( new String[] { FILE_NAME_NULL, FILE_NAME_VALID_PATH } );
    inputMeta.setFileMask( new String[] { StringUtil.EMPTY_STRING, StringUtil.EMPTY_STRING } );

    inputMeta.exportResources( variableSpace, null, mock( ResourceNamingInterface.class ), null, null );

    verify( inputMeta ).getFileObject( FILE_NAME_VALID_PATH, variableSpace );
    verify( inputMeta, never() ).getFileObject( FILE_NAME_NULL, variableSpace );
  }

}
