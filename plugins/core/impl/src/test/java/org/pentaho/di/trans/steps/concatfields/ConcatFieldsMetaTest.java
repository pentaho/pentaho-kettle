/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.concatfields;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMetaTest;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ConcatFieldsMetaTest {
  private static final String SOME_FILE_NAME = "XPTO";

  @ClassRule
  public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  @Test
  public void testLoadSave() throws KettleException {
    List<String> attributes = new ArrayList<>( TextFileOutputMetaTest.getMetaAttributes() );
    attributes.addAll( Arrays.asList( "targetFieldName", "targetFieldLength", "removeSelectedFields" ) );

    LoadSaveTester<ConcatFieldsMeta> loadSaveTester =
      new LoadSaveTester<>( ConcatFieldsMeta.class, attributes, TextFileOutputMetaTest.getGetterMap(),
        TextFileOutputMetaTest.getSetterMap(), TextFileOutputMetaTest.getAttributeValidators(),
        TextFileOutputMetaTest.getTypeValidators() );

    loadSaveTester.testSerialization();
  }

  /**
   * Tests based on PDI-18028
   */
  @Test
  public void testFileNameHandling() throws KettleException, IOException {
    ConcatFieldsMeta concatFieldsMeta = new ConcatFieldsMeta();

    // Guarantee that the defaults are correct
    concatFieldsMeta.setDefault();
    assertEquals( StringUtil.EMPTY_STRING, concatFieldsMeta.getFileName() );
    assertFalse( concatFieldsMeta.isFileNameInField() );


    // 'isFileNameInField' will always return 'false'
    concatFieldsMeta = new ConcatFieldsMeta();
    concatFieldsMeta.setFileNameInField( true );
    assertFalse( concatFieldsMeta.isFileNameInField() );
    concatFieldsMeta.setFileNameInField( false );
    assertFalse( concatFieldsMeta.isFileNameInField() );


    // 'exportResources' will always return 'null'
    concatFieldsMeta = new ConcatFieldsMeta();
    concatFieldsMeta.setOutputFields( new TextFileField[] {} );
    assertNull( concatFieldsMeta.exportResources( mock( VariableSpace.class ), new HashMap<>(),
      mock( ResourceNamingInterface.class ), mock( Repository.class ), mock(
        IMetaStore.class ) ) );


    // Saving the StepMeta, will always ignore the filename
    concatFieldsMeta = new ConcatFieldsMeta();
    concatFieldsMeta.setOutputFields( new TextFileField[] {} );
    concatFieldsMeta.setFileName( SOME_FILE_NAME );
    StringBuilder sb = new StringBuilder();
    concatFieldsMeta.saveSource( sb, StringUtil.EMPTY_STRING );
    assertFalse( sb.toString().contains( SOME_FILE_NAME ) );

    Repository rep = mock( Repository.class );
    IMetaStore metaStore = mock( IMetaStore.class );
    StringObjectId id_transformation = new StringObjectId( "id_transformation" );
    StringObjectId id_step = new StringObjectId( "id_step" );

    doNothing().when( rep ).saveStepAttribute( any(), any(), anyInt(), anyString(), anyString() );
    concatFieldsMeta.saveRep( rep, metaStore, id_transformation, id_step );
    verify( rep ).saveStepAttribute( id_transformation, id_step, "fileNameInField", false );
    verify( rep ).saveStepAttribute( id_transformation, id_step, "file_name", StringUtil.EMPTY_STRING );


    // Persisted wrong data will result in a valid StepMeta
    concatFieldsMeta = new ConcatFieldsMeta();
    String ktr = IOUtils.toString( this.getClass().getResourceAsStream( "pdi18028.ktr" ), "UTF-8" );

    Node node = XMLHandler.loadXMLString( ktr ).getFirstChild();
    concatFieldsMeta.loadXML( node, Collections.emptyList(), metaStore );
    assertEquals( StringUtil.EMPTY_STRING, concatFieldsMeta.getFileName() );
    assertFalse( concatFieldsMeta.isFileNameInField() );
  }
}
