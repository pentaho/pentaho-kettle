/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.jsoninput;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.metastore.api.IMetaStore;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bmorrise on 3/22/16.
 */
@RunWith( MockitoJUnitRunner.class )
public class JsonInputMetaTest {

  public static final String DATA = "data";
  public static final String NAME = "name";

  private String TEST_XML = "    <include>Y</include>" + Const.CR
    + "    <include_field>filename</include_field>" + Const.CR
    + "    <rownum>N</rownum>" + Const.CR
    + "    <addresultfile>N</addresultfile>" + Const.CR
    + "    <readurl>Y</readurl>" + Const.CR
    + "    <removeSourceField>Y</removeSourceField>" + Const.CR
    + "    <IsIgnoreEmptyFile>N</IsIgnoreEmptyFile>" + Const.CR
    + "    <doNotFailIfNoFile>N</doNotFailIfNoFile>" + Const.CR
    + "    <ignoreMissingPath>N</ignoreMissingPath>" + Const.CR
    + "    <rownum_field/>" + Const.CR
    + "    <file>" + Const.CR
    + "      <name>file.json</name>" + Const.CR
    + "      <filemask/>" + Const.CR
    + "      <exclude_filemask/>" + Const.CR
    + "      <file_required>N</file_required>" + Const.CR
    + "      <include_subfolders>N</include_subfolders>" + Const.CR
    + "    </file>" + Const.CR
    + "    <fields>" + Const.CR
    + "null    </fields>" + Const.CR
    + "    <limit>0</limit>" + Const.CR
    + "    <IsInFields>N</IsInFields>" + Const.CR
    + "    <IsAFile>N</IsAFile>" + Const.CR
    + "    <valueField/>" + Const.CR
    + "    <shortFileFieldName/>" + Const.CR
    + "    <pathFieldName/>" + Const.CR
    + "    <hiddenFieldName/>" + Const.CR
    + "    <lastModificationTimeFieldName/>" + Const.CR
    + "    <uriNameFieldName/>" + Const.CR
    + "    <rootUriNameFieldName/>" + Const.CR
    + "    <extensionFieldName/>" + Const.CR
    + "    <sizeFieldName/>" + Const.CR;

  JsonInputMeta jsonInputMeta;

  @Mock
  RowMetaInterface rowMeta;

  @Mock
  RowMetaInterface rowMetaInterfaceItem;

  @Mock
  StepMeta nextStep;

  @Mock
  VariableSpace space;

  @Mock
  Repository repository;

  @Mock
  IMetaStore metaStore;

  @Mock
  JsonInputMeta.InputFiles inputFiles;

  @Mock
  JsonInputField inputField;

  @Before
  public void setup() {
    jsonInputMeta = new JsonInputMeta();
    jsonInputMeta.setInputFiles( inputFiles );
    jsonInputMeta.setInputFields( new JsonInputField[] { inputField } );


    inputFiles.fileRequired = new String[] { " " };
    inputFiles.includeSubFolders = new String[] { " " };

    jsonInputMeta.setFileName( new String[] { "file.json" } );
    jsonInputMeta.setFileMask( new String[] { "" } );
    jsonInputMeta.setExcludeFileMask( new String[] { "" } );
    jsonInputMeta.setFileRequired( new String[] { "" } );
    jsonInputMeta.setIncludeSubFolders( new String[] { "" } );

    jsonInputMeta.setIncludeFilename( true );
    jsonInputMeta.setFilenameField( "filename" );
    jsonInputMeta.setReadUrl( true );
    jsonInputMeta.setRemoveSourceField( true );
  }

  @Test
  public void getFieldsRemoveSourceField() throws Exception {
    RowMetaInterface[] info = new RowMetaInterface[1];
    info[ 0 ] = rowMetaInterfaceItem;

    jsonInputMeta.setRemoveSourceField( true );
    jsonInputMeta.setFieldValue( DATA );
    jsonInputMeta.setInFields( true );

    when( rowMeta.indexOfValue( DATA ) ).thenReturn( 0 );

    jsonInputMeta.getFields( rowMeta, NAME, info, nextStep, space, repository, metaStore );

    verify( rowMeta ).removeValueMeta( 0 );
  }

  @Test
  public void verifyReadingRepoSetsAcceptFilenames() throws Exception {
    ObjectId objectId = () -> "id";
    when( repository.getStepAttributeBoolean( objectId, "IsInFields" ) ).thenReturn( true );
    jsonInputMeta.readRep( repository, null, objectId, null );
    Assert.assertTrue( jsonInputMeta.isInFields() );
    Assert.assertTrue( jsonInputMeta.inputFiles.acceptingFilenames );
  }

  @Test
  public void testGetXml() {
    String xml = jsonInputMeta.getXML();
    Assert.assertEquals( xml, TEST_XML );
  }
}
