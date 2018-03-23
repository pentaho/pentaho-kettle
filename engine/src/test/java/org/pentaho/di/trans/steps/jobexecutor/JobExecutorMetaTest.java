/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.jobexecutor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.metastore.api.IMetaStore;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;

/**
 * <p>
 * PDI-11979 - Fieldnames in the "Execution results" tab of the Job executor step saved incorrectly in repository.
 * </p>
 *
 */
public class JobExecutorMetaTest {

  LoadSaveTester loadSaveTester;

  /**
   * Check all simple string fields.
   *
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {

    List<String> attributes =
        Arrays.asList( "fileName", "jobName", "directoryPath", "groupSize", "groupField", "groupTime",
            "executionTimeField", "executionFilesRetrievedField", "executionLogTextField",
            "executionLogChannelIdField", "executionResultField", "executionNrErrorsField", "executionLinesReadField",
            "executionLinesWrittenField", "executionLinesInputField", "executionLinesOutputField",
            "executionLinesRejectedField", "executionLinesUpdatedField", "executionLinesDeletedField",
            "executionExitStatusField" );

    // executionResultTargetStepMeta -? (see for switch case meta)
    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();
    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    loadSaveTester =
        new LoadSaveTester( JobExecutorMeta.class, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  @Test
  public void testRemoveHopFrom() throws Exception {
    JobExecutorMeta jobExecutorMeta = new JobExecutorMeta();
    jobExecutorMeta.setExecutionResultTargetStepMeta( new StepMeta() );
    jobExecutorMeta.setResultRowsTargetStepMeta( new StepMeta() );
    jobExecutorMeta.setResultFilesTargetStepMeta( new StepMeta() );

    jobExecutorMeta.cleanAfterHopFromRemove();

    assertNull( jobExecutorMeta.getExecutionResultTargetStepMeta() );
    assertNull( jobExecutorMeta.getResultRowsTargetStepMeta() );
    assertNull( jobExecutorMeta.getResultFilesTargetStepMeta() );
  }

  @Test
  public void testExportResources() throws KettleException {
    JobExecutorMeta jobExecutorMeta = spy( new JobExecutorMeta() );
    JobMeta jobMeta = mock( JobMeta.class );

    String testName = "test";

    doReturn( jobMeta ).when( jobExecutorMeta ).loadJobMetaProxy( any( JobExecutorMeta.class ),
            any( Repository.class ), any( VariableSpace.class ) );
    when( jobMeta.exportResources( any( JobMeta.class ), any( Map.class ), any( ResourceNamingInterface.class ),
            any( Repository.class ), any( IMetaStore.class ) ) ).thenReturn( testName );

    jobExecutorMeta.exportResources( null, null, null, null, null );

    verify( jobMeta ).setFilename( "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}/" + testName );
    verify( jobExecutorMeta ).setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
  }

  @Test
  public void testLoadJobMeta() throws KettleException {
    String param1 = "param1";
    String param2 = "param2";
    String param3 = "param3";
    String parentValue1 = "parentValue1";
    String parentValue2 = "parentValue2";
    String childValue3 = "childValue3";

    JobExecutorMeta jobExecutorMeta = spy( new JobExecutorMeta() );
    Repository repository = Mockito.mock( Repository.class );

    JobMeta meta = new JobMeta();
    meta.setVariable( param2, "childValue2 should be override" );
    meta.setVariable( param3, childValue3 );

    Mockito.doReturn( meta ).when( repository )
      .loadJob( Mockito.eq( "test.kjb" ), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject() );

    VariableSpace parentSpace = new Variables();
    parentSpace.setVariable( param1, parentValue1 );
    parentSpace.setVariable( param2, parentValue2 );

    jobExecutorMeta.setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
    jobExecutorMeta.setFileName( "/home/admin/test.kjb" );

    JobMeta jobMeta;

    jobExecutorMeta.getParameters().setInheritingAllVariables( false );
    jobMeta = JobExecutorMeta.loadJobMeta( jobExecutorMeta, repository, parentSpace );
    Assert.assertEquals( null, jobMeta.getVariable( param1 ) );
    Assert.assertEquals( parentValue2, jobMeta.getVariable( param2 ) );
    Assert.assertEquals( childValue3, jobMeta.getVariable( param3 ) );

    jobExecutorMeta.getParameters().setInheritingAllVariables( true );
    jobMeta = JobExecutorMeta.loadJobMeta( jobExecutorMeta, repository, parentSpace );
    Assert.assertEquals( parentValue1, jobMeta.getVariable( param1 ) );
    Assert.assertEquals( parentValue2, jobMeta.getVariable( param2 ) );
    Assert.assertEquals( childValue3, jobMeta.getVariable( param3 ) );
  }
}
