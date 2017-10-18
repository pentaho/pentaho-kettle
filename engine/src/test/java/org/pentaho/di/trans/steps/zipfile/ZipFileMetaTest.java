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

package org.pentaho.di.trans.steps.zipfile;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.named.cluster.NamedClusterEmbedManager;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bgroves on 11/10/15.
 */
public class ZipFileMetaTest {

  private static final String SOURCE_FILENAME = "Files";
  private static final String TARGET_FILENAME = "ZipFile";
  private static final String BASE_FOLDER = "BaseFolder";
  private static final String OPERATION_TYPE = "move";
  private static final boolean ADD_RESULT_FILENAME = true;
  private static final boolean OVERWRITE_ZIP_ENTRY = true;
  private static final boolean CREATE_PARENT_FOLDER = true;
  private static final boolean KEEP_SOURCE_FOLDER = true;
  private static final String MOVE_TO_FOLDER_FIELD = "movetothisfolder";

  @BeforeClass
  public static void setUp() throws Exception {
    if ( !KettleClientEnvironment.isInitialized() ) {
      KettleClientEnvironment.init();
    }
  }

  @Test
  public void testGettersSetters() {
    ZipFileMeta zipFileMeta = new ZipFileMeta();
    zipFileMeta.setDynamicSourceFileNameField( SOURCE_FILENAME );
    zipFileMeta.setDynamicTargetFileNameField( TARGET_FILENAME );
    zipFileMeta.setBaseFolderField( BASE_FOLDER );
    zipFileMeta.setOperationType( ZipFileMeta.getOperationTypeByDesc( OPERATION_TYPE ) );
    zipFileMeta.setaddTargetFileNametoResult( ADD_RESULT_FILENAME );
    zipFileMeta.setOverwriteZipEntry( OVERWRITE_ZIP_ENTRY );
    zipFileMeta.setCreateParentFolder( CREATE_PARENT_FOLDER );
    zipFileMeta.setKeepSouceFolder( KEEP_SOURCE_FOLDER );
    zipFileMeta.setMoveToFolderField( MOVE_TO_FOLDER_FIELD );

    assertEquals( SOURCE_FILENAME, zipFileMeta.getDynamicSourceFileNameField() );
    assertEquals( TARGET_FILENAME, zipFileMeta.getDynamicTargetFileNameField() );
    assertEquals( BASE_FOLDER, zipFileMeta.getBaseFolderField() );
    assertEquals( ZipFileMeta.getOperationTypeByDesc( OPERATION_TYPE ), zipFileMeta.getOperationType() );
    assertEquals( MOVE_TO_FOLDER_FIELD, zipFileMeta.getMoveToFolderField() );
    assertTrue( zipFileMeta.isaddTargetFileNametoResult() );
    assertTrue( zipFileMeta.isOverwriteZipEntry() );
    assertTrue( zipFileMeta.isKeepSouceFolder() );
    assertTrue( zipFileMeta.isCreateParentFolder() );
    assertEquals( MOVE_TO_FOLDER_FIELD, zipFileMeta.getMoveToFolderField() );

    assertNotNull( zipFileMeta.getStepData() );
    assertTrue( zipFileMeta.supportsErrorHandling() );
  }

  @Test
  public void testLoadAndGetXml() throws Exception {
    ZipFileMeta zipFileMeta = new ZipFileMeta();
    Node stepnode = getTestNode();
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    IMetaStore metaStore = mock( IMetaStore.class );
    StepMeta mockParentStepMeta = mock( StepMeta.class );
    zipFileMeta.setParentStepMeta( mockParentStepMeta );
    TransMeta mockTransMeta = mock( TransMeta.class );
    NamedClusterEmbedManager embedManager = mock( NamedClusterEmbedManager.class );
    when( mockParentStepMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( mockTransMeta.getNamedClusterEmbedManager() ).thenReturn( embedManager );
    zipFileMeta.loadXML( stepnode, Collections.singletonList( dbMeta ), metaStore );
    assertXmlOutputMeta( zipFileMeta );
  }

  @Test
  public void testReadRep() throws Exception {
    ZipFileMeta zipFileMeta = new ZipFileMeta();
    Repository rep = mock( Repository.class );
    IMetaStore metastore = mock( IMetaStore.class );
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );

    StringObjectId oid = new StringObjectId( "oid" );

    when( rep.getStepAttributeString( oid, "sourcefilenamefield" ) ).thenReturn( SOURCE_FILENAME );
    when( rep.getStepAttributeString( oid, "targetfilenamefield" ) ).thenReturn( TARGET_FILENAME );
    when( rep.getStepAttributeString( oid, "baseFolderField" ) ).thenReturn( BASE_FOLDER );
    when( rep.getStepAttributeString( oid, "operation_type" ) ).thenReturn( OPERATION_TYPE );
    when( rep.getStepAttributeBoolean( oid, "addresultfilenames" ) ).thenReturn( ADD_RESULT_FILENAME );
    when( rep.getStepAttributeBoolean( oid, "overwritezipentry" ) ).thenReturn( OVERWRITE_ZIP_ENTRY );
    when( rep.getStepAttributeBoolean( oid, "createparentfolder" ) ).thenReturn( CREATE_PARENT_FOLDER );
    when( rep.getStepAttributeBoolean( oid, "keepsourcefolder" ) ).thenReturn( KEEP_SOURCE_FOLDER );
    when( rep.getStepAttributeString( oid, "movetofolderfield" ) ).thenReturn( MOVE_TO_FOLDER_FIELD );

    zipFileMeta.readRep( rep, metastore, oid, Collections.singletonList( dbMeta ) );

    assertEquals( SOURCE_FILENAME, zipFileMeta.getDynamicSourceFileNameField() );
    assertEquals( TARGET_FILENAME, zipFileMeta.getDynamicTargetFileNameField() );
    assertEquals( BASE_FOLDER, zipFileMeta.getBaseFolderField() );
    assertEquals( ZipFileMeta.getOperationTypeByDesc( OPERATION_TYPE ), zipFileMeta.getOperationType() );
    assertEquals( MOVE_TO_FOLDER_FIELD, zipFileMeta.getMoveToFolderField() );
    assertTrue( zipFileMeta.isaddTargetFileNametoResult() );
    assertTrue( zipFileMeta.isOverwriteZipEntry() );
    assertTrue( zipFileMeta.isKeepSouceFolder() );
    assertTrue( zipFileMeta.isCreateParentFolder() );

    Mockito.reset( rep, metastore );
    StringObjectId transid = new StringObjectId( "transid" );
    zipFileMeta.saveRep( rep, metastore, transid, oid );
    verify( rep ).saveStepAttribute( transid, oid, "sourcefilenamefield", SOURCE_FILENAME );
    verify( rep ).saveStepAttribute( transid, oid, "targetfilenamefield", TARGET_FILENAME );
    verify( rep ).saveStepAttribute( transid, oid, "baseFolderField", BASE_FOLDER );
    verify( rep ).saveStepAttribute( transid, oid, "operation_type", OPERATION_TYPE );
    verify( rep ).saveStepAttribute( transid, oid, "addresultfilenames", ADD_RESULT_FILENAME );
    verify( rep ).saveStepAttribute( transid, oid, "overwritezipentry", OVERWRITE_ZIP_ENTRY );
    verify( rep ).saveStepAttribute( transid, oid, "createparentfolder", CREATE_PARENT_FOLDER );
    verify( rep ).saveStepAttribute( transid, oid, "keepsourcefolder", KEEP_SOURCE_FOLDER );
    verify( rep ).saveStepAttribute( transid, oid, "movetofolderfield", MOVE_TO_FOLDER_FIELD );

    Mockito.verifyNoMoreInteractions( rep, metastore );
  }

  @Test
  public void testCheck() {
    ZipFileMeta zipFileMeta = new ZipFileMeta();
    zipFileMeta.setDefault();
    TransMeta transMeta = mock( TransMeta.class );
    StepMeta stepInfo = mock( StepMeta.class );
    RowMetaInterface prev = mock( RowMetaInterface.class );
    Repository repos = mock( Repository.class );
    IMetaStore metastore = mock( IMetaStore.class );
    RowMetaInterface info = mock( RowMetaInterface.class );
    ArrayList<CheckResultInterface> remarks = new ArrayList<>();

    zipFileMeta.check( remarks, transMeta, stepInfo, prev, new String[]{"input"}, new String[]{"output"}, info,
      new Variables(), repos, metastore );
    assertEquals( 2, remarks.size() );
    assertEquals( "Source Filename field is missing!", remarks.get( 0 ).getText() );
    assertEquals( "Step is receiving info from other steps.", remarks.get( 1 ).getText() );

    remarks = new ArrayList<>();
    zipFileMeta = new ZipFileMeta();
    zipFileMeta.setDynamicSourceFileNameField( "sourceFileField" );
    zipFileMeta.check( remarks, transMeta, stepInfo, prev, new String[0], new String[]{"output"}, info,
      new Variables(), repos, metastore );
    assertEquals( 2, remarks.size() );
    assertEquals( "Target Filename field was specified", remarks.get( 0 ).getText() );
    assertEquals( "No input received from other steps!", remarks.get( 1 ).getText() );
  }

  @Test
  public void testGetStep() throws Exception {
    StepMeta stepInfo = mock( StepMeta.class );
    when( stepInfo.getName() ).thenReturn( "Zip Step Name" );
    StepDataInterface stepData = mock( StepDataInterface.class );
    TransMeta transMeta = mock( TransMeta.class );
    when( transMeta.findStep( "Zip Step Name" ) ).thenReturn( stepInfo );
    Trans trans = mock( Trans.class );

    ZipFileMeta zipFileMeta = new ZipFileMeta();
    ZipFile zipFile = (ZipFile) zipFileMeta.getStep( stepInfo, stepData, 0, transMeta, trans );
    assertEquals( stepInfo, zipFile.getStepMeta() );
    assertEquals( stepData, zipFile.getStepDataInterface() );
    assertEquals( transMeta, zipFile.getTransMeta() );
    assertEquals( trans, zipFile.getTrans() );
    assertEquals( 0, zipFile.getCopy() );
  }

  @Test
  public void testOperationType() {
    assertEquals( 0, ZipFileMeta.getOperationTypeByDesc( null ) );
    assertEquals( 1, ZipFileMeta.getOperationTypeByDesc( "Move source file" ) );
    assertEquals( 1, ZipFileMeta.getOperationTypeByDesc( "move" ) );
    assertEquals( 0, ZipFileMeta.getOperationTypeByDesc( "doesn't exist" ) );

    assertEquals( "Move source file", ZipFileMeta.getOperationTypeDesc( 1 ) );
    assertEquals( "Do nothing", ZipFileMeta.getOperationTypeDesc( 100 ) );
    assertEquals( "Do nothing", ZipFileMeta.getOperationTypeDesc( -1 ) );
  }

  private Node getTestNode() throws KettleXMLException {
    String xml = "<step>" + Const.CR
      + "<name>Zip file</name>" + Const.CR
      + "<type>ZipFile</type>" + Const.CR
      + "<description/>" + Const.CR
      + "<distribute>Y</distribute>" + Const.CR
      + "<custom_distribution/>" + Const.CR
      + "<copies>1</copies>" + Const.CR
      + "<partitioning>" + Const.CR
      + "  <method>none</method>" + Const.CR
      + "  <schema_name/>" + Const.CR
      + "</partitioning>" + Const.CR
      + "<sourcefilenamefield>Files</sourcefilenamefield>" + Const.CR
      + "<targetfilenamefield>ZipFile</targetfilenamefield>" + Const.CR
      + "<baseFolderField>BaseFolder</baseFolderField>" + Const.CR
      + "<operation_type>move</operation_type>" + Const.CR
      + "<addresultfilenames>Y</addresultfilenames>" + Const.CR
      + "<overwritezipentry>Y</overwritezipentry>" + Const.CR
      + "<createparentfolder>Y</createparentfolder>" + Const.CR
      + "<keepsourcefolder>Y</keepsourcefolder>" + Const.CR
      + "<movetofolderfield/>" + Const.CR
      + "<cluster_schema/>" + Const.CR
      + "<remotesteps>" + Const.CR
      + "  <input></input>" + Const.CR
      + "  <output></output>" + Const.CR
      + "</remotesteps>" + Const.CR
      + "<GUI>" + Const.CR
      + "  <xloc>608</xloc>" + Const.CR
      + "  <yloc>48</yloc>" + Const.CR
      + "  <draw>Y</draw>" + Const.CR
      + "</GUI>" + Const.CR
      + "</step>" + Const.CR;
    return XMLHandler.loadXMLString( xml, "step" );
  }

  private void assertXmlOutputMeta( ZipFileMeta zipOutputFile ) {
    assertEquals( "BaseFolder", zipOutputFile.getBaseFolderField() );
    assertEquals( "Files", zipOutputFile.getDynamicSourceFileNameField() );
    assertEquals( "ZipFile", zipOutputFile.getDynamicTargetFileNameField() );
    assertEquals( null, zipOutputFile.getMoveToFolderField() );
    assertEquals( "    <sourcefilenamefield>Files</sourcefilenamefield>" + Const.CR
      + "    <targetfilenamefield>ZipFile</targetfilenamefield>" + Const.CR
      + "    <baseFolderField>BaseFolder</baseFolderField>" + Const.CR
      + "    <operation_type>move</operation_type>" + Const.CR
      + "    <addresultfilenames>Y</addresultfilenames>" + Const.CR
      + "    <overwritezipentry>Y</overwritezipentry>" + Const.CR
      + "    <createparentfolder>Y</createparentfolder>" + Const.CR
      + "    <keepsourcefolder>Y</keepsourcefolder>" + Const.CR
      + "    <movetofolderfield/>" + Const.CR, zipOutputFile.getXML() );

    zipOutputFile.setDefault();

    assertEquals( "    <sourcefilenamefield>Files</sourcefilenamefield>" + Const.CR
      + "    <targetfilenamefield>ZipFile</targetfilenamefield>" + Const.CR
      + "    <baseFolderField>BaseFolder</baseFolderField>" + Const.CR
      + "    <operation_type/>" + Const.CR
      + "    <addresultfilenames>N</addresultfilenames>" + Const.CR
      + "    <overwritezipentry>N</overwritezipentry>" + Const.CR
      + "    <createparentfolder>N</createparentfolder>" + Const.CR
      + "    <keepsourcefolder>N</keepsourcefolder>" + Const.CR
      + "    <movetofolderfield/>" + Const.CR, zipOutputFile.getXML() );
  }
}
