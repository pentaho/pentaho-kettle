/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.fileinput.text;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.apache.commons.vfs2.FileObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.trans.steps.file.BaseFileInputFiles;

public class TextFileInputMetaTest {
  private static final String FILE_NAME_NULL = null;
  private static final String FILE_NAME_EMPTY = StringUtil.EMPTY_STRING;
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
  public void whenExportingResourcesWeGetFileObjectsOnlyFromFilesWithNotNullAndNotEmptyFileNames() throws Exception {
    inputMeta.inputFiles = new BaseFileInputFiles();
    inputMeta.inputFiles.fileName = new String[] { FILE_NAME_NULL, FILE_NAME_EMPTY, FILE_NAME_VALID_PATH };
    inputMeta.inputFiles.fileMask =
      new String[] { StringUtil.EMPTY_STRING, StringUtil.EMPTY_STRING, StringUtil.EMPTY_STRING };

    inputMeta.exportResources( variableSpace, null, mock( ResourceNamingInterface.class ), null, null );

    verify( inputMeta ).getFileObject( FILE_NAME_VALID_PATH, variableSpace );
    verify( inputMeta, never() ).getFileObject( FILE_NAME_NULL, variableSpace );
    verify( inputMeta, never() ).getFileObject( FILE_NAME_EMPTY, variableSpace );
  }

}
