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


package org.pentaho.di.trans.steps.jobexecutor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryBowl;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.metastore.api.IMetaStore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * <p>
 * PDI-11979 - Fieldnames in the "Execution results" tab of the Job executor step saved incorrectly in repository.
 * </p>
 *
 */
public class JobExecutorMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

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
    Map<String, String> getterMap = new HashMap<>();
    Map<String, String> setterMap = new HashMap<>();
    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<>();

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<>();
    loadSaveTester =
        new LoadSaveTester( JobExecutorMeta.class, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  @Test
  public void testRemoveHopFrom() {
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

    doReturn( jobMeta ).when( jobExecutorMeta ).loadJobMetaProxy( nullable( Bowl.class ),
            nullable( JobExecutorMeta.class ), nullable( Repository.class ), nullable( VariableSpace.class ) );
    when( jobMeta.exportResources( any( Bowl.class ), any( JobMeta.class ), nullable( Map.class ),
      nullable( ResourceNamingInterface.class ), nullable( Repository.class ), nullable( IMetaStore.class ) ) )
      .thenReturn( testName );

    jobExecutorMeta.exportResources( DefaultBowl.getInstance(), null, null, null, null, null );

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
    Mockito.when( repository.getBowl() ).thenReturn( new RepositoryBowl( repository ) );

    JobMeta meta = new JobMeta();
    meta.setVariable( param2, "childValue2 should be override" );
    meta.setVariable( param3, childValue3 );

    Mockito.doReturn( meta ).when( repository )
      .loadJob( Mockito.eq( "test.kjb" ), Mockito.any(), Mockito.any(), Mockito.any() );

    VariableSpace parentSpace = new Variables();
    parentSpace.setVariable( param1, parentValue1 );
    parentSpace.setVariable( param2, parentValue2 );

    jobExecutorMeta.setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
    jobExecutorMeta.setFileName( "/home/admin/test.kjb" );
    StepMeta parentStepMeta = mock( StepMeta.class );
    when ( parentStepMeta.getParentTransMeta( ) ).thenReturn( mock( TransMeta.class ) );
    jobExecutorMeta.setParentStepMeta( parentStepMeta );
    JobMeta jobMeta;

    jobExecutorMeta.getParameters().setInheritingAllVariables( false );
    jobMeta = JobExecutorMeta.loadJobMeta( DefaultBowl.getInstance(), jobExecutorMeta, repository, parentSpace );
    assertNull( jobMeta.getVariable( param1 ) );
    Assert.assertEquals( parentValue2, jobMeta.getVariable( param2 ) );
    Assert.assertEquals( childValue3, jobMeta.getVariable( param3 ) );

    jobExecutorMeta.getParameters().setInheritingAllVariables( true );
    jobMeta = JobExecutorMeta.loadJobMeta( DefaultBowl.getInstance(), jobExecutorMeta, repository, parentSpace );
    Assert.assertEquals( parentValue1, jobMeta.getVariable( param1 ) );
    Assert.assertEquals( parentValue2, jobMeta.getVariable( param2 ) );
    Assert.assertEquals( childValue3, jobMeta.getVariable( param3 ) );
  }
}
