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

package org.pentaho.di.trans.steps.metainject;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.metastore.api.IMetaStore;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MetaInjectMetaTest {

  private static final String SOURCE_STEP_NAME = "SOURCE_STEP_NAME";

  private static final String SOURCE_FIELD_NAME = "SOURCE_STEP_NAME";

  private static final String TARGET_STEP_NAME = "TARGET_STEP_NAME";

  private static final String TARGET_FIELD_NAME = "TARGET_STEP_NAME";

  private static final String TEST_FILE_NAME = "TEST_FILE_NAME";

  private static final String EXPORTED_FILE_NAME =
      "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}/" + TEST_FILE_NAME;

  private MetaInjectMeta metaInjectMeta;

  @Before
  public void before() {
    metaInjectMeta = new MetaInjectMeta();
  }

  @Test
  public void getResourceDependencies() {
    TransMeta transMeta = mock( TransMeta.class );
    StepMeta stepMeta = mock( StepMeta.class );

    List<ResourceReference> actualResult = metaInjectMeta.getResourceDependencies( transMeta, stepMeta );
    assertEquals( 1, actualResult.size() );
    ResourceReference reference = actualResult.iterator().next();
    assertEquals( 0, reference.getEntries().size() );
  }

  @Test
  public void getResourceDependencies_with_defined_fileName() {
    TransMeta transMeta = mock( TransMeta.class );
    StepMeta stepMeta = mock( StepMeta.class );
    metaInjectMeta.setFileName( "FILE_NAME" );
    doReturn( "FILE_NAME_WITH_SUBSTITUTIONS" ).when( transMeta ).environmentSubstitute( "FILE_NAME" );

    List<ResourceReference> actualResult = metaInjectMeta.getResourceDependencies( transMeta, stepMeta );
    assertEquals( 1, actualResult.size() );
    ResourceReference reference = actualResult.iterator().next();
    assertEquals( 1, reference.getEntries().size() );
  }

  @Test
  public void getResourceDependencies_with_defined_transName() {
    TransMeta transMeta = mock( TransMeta.class );
    StepMeta stepMeta = mock( StepMeta.class );
    metaInjectMeta.setTransName( "TRANS_NAME" );
    doReturn( "TRANS_NAME_WITH_SUBSTITUTIONS" ).when( transMeta ).environmentSubstitute( "TRANS_NAME" );

    List<ResourceReference> actualResult = metaInjectMeta.getResourceDependencies( transMeta, stepMeta );
    assertEquals( 1, actualResult.size() );
    ResourceReference reference = actualResult.iterator().next();
    assertEquals( 1, reference.getEntries().size() );
  }

  @Test
  public void getResourceDependencies_repository_full_path() {
    // checks getResourceDependencies() returns action file resource w/ transname including full repository path name
    TransMeta transMeta = mock( TransMeta.class );
    StepMeta stepMeta = mock( StepMeta.class );
    metaInjectMeta.setTransName( "TRANS_NAME" );
    metaInjectMeta.setDirectoryPath( "/REPO/DIR" );
    doReturn( "TRANS_NAME_SUBS" ).when( transMeta ).environmentSubstitute( "TRANS_NAME" );
    doReturn( "/REPO/DIR_SUBS" ).when( transMeta ).environmentSubstitute( "/REPO/DIR" );

    List<ResourceReference> actualResult = metaInjectMeta.getResourceDependencies( transMeta, stepMeta );
    assertEquals( 1, actualResult.size() );
    ResourceReference reference = actualResult.iterator().next();
    assertEquals( 1, reference.getEntries().size() );
    ResourceEntry resourceEntry = reference.getEntries().get( 0 );
    assertEquals( "/REPO/DIR_SUBS/TRANS_NAME_SUBS", resourceEntry.getResource() );
    assertEquals(ResourceEntry.ResourceType.ACTIONFILE, resourceEntry.getResourcetype() );
  }



  @Test
  public void exportResources() throws KettleException {
    VariableSpace variableSpace = mock( VariableSpace.class );
    ResourceNamingInterface resourceNamingInterface = mock( ResourceNamingInterface.class );
    Repository repository = mock( Repository.class );
    IMetaStore metaStore = mock( IMetaStore.class );

    MetaInjectMeta injectMetaSpy = spy( metaInjectMeta );
    TransMeta transMeta = mock( TransMeta.class );
    Map<String, ResourceDefinition> definitions = Collections.<String, ResourceDefinition>emptyMap();
    doReturn( TEST_FILE_NAME ).when( transMeta ).exportResources( transMeta, definitions, resourceNamingInterface,
        repository, metaStore );
    doReturn( transMeta ).when( injectMetaSpy ).loadTransformationMeta( repository, variableSpace );

    String actualExportedFileName =
        injectMetaSpy.exportResources( variableSpace, definitions, resourceNamingInterface, repository, metaStore );
    assertEquals( TEST_FILE_NAME, actualExportedFileName );
    assertEquals( EXPORTED_FILE_NAME, injectMetaSpy.getFileName() );
    verify( transMeta ).exportResources( transMeta, definitions, resourceNamingInterface, repository, metaStore );
  }

  @Test
  public void convertToMap() {
    MetaInjectMapping metaInjectMapping = new MetaInjectMapping();
    metaInjectMapping.setSourceStep( SOURCE_STEP_NAME );
    metaInjectMapping.setSourceField( SOURCE_FIELD_NAME );
    metaInjectMapping.setTargetStep( TARGET_STEP_NAME );
    metaInjectMapping.setTargetField( TARGET_FIELD_NAME );

    Map<TargetStepAttribute, SourceStepField> actualResult =
        MetaInjectMeta.convertToMap( Collections.singletonList( metaInjectMapping ) );

    assertEquals( 1, actualResult.size() );

    TargetStepAttribute targetStepAttribute = actualResult.keySet().iterator().next();
    assertEquals( TARGET_STEP_NAME, targetStepAttribute.getStepname() );
    assertEquals( TARGET_FIELD_NAME, targetStepAttribute.getAttributeKey() );

    SourceStepField sourceStepField = actualResult.values().iterator().next();
    assertEquals( SOURCE_STEP_NAME, sourceStepField.getStepname() );
    assertEquals( SOURCE_FIELD_NAME, sourceStepField.getField() );
  }

  @Test
  public void testLoadMappingMetaWhenConnectedToRep() throws Exception {
    String variablePath = "Internal.Entry.Current.Directory";
    String virtualDir = "/testFolder/test";
    String fileName = "testTrans.ktr";

    VariableSpace variables = new Variables();
    variables.setVariable( variablePath, virtualDir );

    MetaInjectMeta metaInjectMetaMock = mock( MetaInjectMeta.class );
    when( metaInjectMetaMock.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.FILENAME );
    when( metaInjectMetaMock.getFileName() ).thenReturn( "${" + variablePath + "}/" + fileName );

    // mock repo and answers
    Repository rep = mock( Repository.class );

    doAnswer( invocation -> {
      String originalArgument = (String) ( invocation.getArguments() )[ 0 ];
      // be sure that the variable was replaced by real path
      assertEquals( originalArgument, virtualDir );
      return null;
    } ).when( rep ).findDirectory( anyString() );

    doAnswer( invocation -> {
      String originalArgument = (String) ( invocation.getArguments() )[ 0 ];
      // be sure that transformation name was resolved correctly
      assertEquals( originalArgument, fileName );
      return mock( TransMeta.class );
    } ).when( rep ).loadTransformation( anyString(), nullable( RepositoryDirectoryInterface.class ),
      nullable( ProgressMonitorListener.class ), anyBoolean(), nullable( String.class ) );

    assertNotNull( MetaInjectMeta.loadTransformationMeta( metaInjectMetaMock, rep, null, variables ) );
  }


}
