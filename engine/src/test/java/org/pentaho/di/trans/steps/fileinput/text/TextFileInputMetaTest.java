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


package org.pentaho.di.trans.steps.fileinput.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.apache.commons.vfs2.FileObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.file.BaseFileInputFiles;
import org.pentaho.di.trans.steps.named.cluster.NamedClusterEmbedManager;

public class TextFileInputMetaTest {
  private static final String FILE_NAME_NULL = null;
  private static final String FILE_NAME_EMPTY = StringUtil.EMPTY_STRING;
  private static final String FILE_NAME_VALID_PATH = "path/to/file";

  private TextFileInputMeta inputMeta;
  private VariableSpace variableSpace;

  @Before
  public void setUp() throws Exception {
    NamedClusterEmbedManager  manager = mock( NamedClusterEmbedManager.class );

    TransMeta parentTransMeta = mock( TransMeta.class );
    doReturn( manager ).when( parentTransMeta ).getNamedClusterEmbedManager();

    StepMeta parentStepMeta = mock( StepMeta.class );
    doReturn( parentTransMeta ).when( parentStepMeta ).getParentTransMeta();

    inputMeta = new TextFileInputMeta();
    inputMeta.setParentStepMeta( parentStepMeta );
    inputMeta = spy( inputMeta );
    variableSpace = mock( VariableSpace.class );

    doReturn( "<def>" ).when( variableSpace ).environmentSubstitute( anyString() );
    doReturn( FILE_NAME_VALID_PATH ).when( variableSpace ).environmentSubstitute( FILE_NAME_VALID_PATH );
    FileObject mockedFileObject = mock( FileObject.class );
    doReturn( mockedFileObject ).when( inputMeta ).getFileObject( any( Bowl.class ), anyString(), eq( variableSpace ) );
  }

  @Test
  public void whenExportingResourcesWeGetFileObjectsOnlyFromFilesWithNotNullAndNotEmptyFileNames() throws Exception {
    inputMeta.inputFiles = new BaseFileInputFiles();
    inputMeta.inputFiles.fileName = new String[] { FILE_NAME_NULL, FILE_NAME_EMPTY, FILE_NAME_VALID_PATH };
    inputMeta.inputFiles.fileMask =
      new String[] { StringUtil.EMPTY_STRING, StringUtil.EMPTY_STRING, StringUtil.EMPTY_STRING };

    inputMeta.exportResources( DefaultBowl.getInstance(), null, variableSpace, null,
      mock( ResourceNamingInterface.class ), null, null );

    verify( inputMeta ).getFileObject( DefaultBowl.getInstance(), FILE_NAME_VALID_PATH, variableSpace );
    verify( inputMeta, never() ).getFileObject( DefaultBowl.getInstance(), FILE_NAME_NULL, variableSpace );
    verify( inputMeta, never() ).getFileObject( DefaultBowl.getInstance(), FILE_NAME_EMPTY, variableSpace );
  }

  @Test
  public void testGetXmlWorksIfWeUpdateOnlyPartOfInputFilesInformation() {
    inputMeta.inputFiles = new BaseFileInputFiles();
    inputMeta.inputFiles.fileName = new String[] { FILE_NAME_VALID_PATH };

    inputMeta.getXML();

    assertEquals( inputMeta.inputFiles.fileName.length, inputMeta.inputFiles.fileMask.length );
    assertEquals( inputMeta.inputFiles.fileName.length, inputMeta.inputFiles.excludeFileMask.length );
    assertEquals( inputMeta.inputFiles.fileName.length, inputMeta.inputFiles.fileRequired.length );
    assertEquals( inputMeta.inputFiles.fileName.length, inputMeta.inputFiles.includeSubFolders.length );
  }

  @Test
  public void testClonelWorksIfWeUpdateOnlyPartOfInputFilesInformation() {
    inputMeta.inputFiles = new BaseFileInputFiles();
    inputMeta.inputFiles.fileName = new String[] { FILE_NAME_VALID_PATH };

    TextFileInputMeta cloned = (TextFileInputMeta) inputMeta.clone();

    //since the equals was not override it should be other object
    assertNotEquals( inputMeta, cloned );
    assertEquals( cloned.inputFiles.fileName.length, inputMeta.inputFiles.fileName.length );
    assertEquals( cloned.inputFiles.fileMask.length, inputMeta.inputFiles.fileMask.length );
    assertEquals( cloned.inputFiles.excludeFileMask.length, inputMeta.inputFiles.excludeFileMask.length );
    assertEquals( cloned.inputFiles.fileRequired.length, inputMeta.inputFiles.fileRequired.length );
    assertEquals( cloned.inputFiles.includeSubFolders.length, inputMeta.inputFiles.includeSubFolders.length );

    assertEquals( cloned.inputFields.length, inputMeta.inputFields.length );
    assertEquals( cloned.getFilter().length, inputMeta.getFilter().length );
  }

}
