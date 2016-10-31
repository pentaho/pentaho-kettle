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

package org.pentaho.di.ui.spoon;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static junit.framework.Assert.*;

import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegates;
import org.pentaho.di.ui.spoon.delegates.SpoonTabsDelegate;

/**
 * Spoon tests
 *
 * @author Pavel Sakun
 * @see Spoon
 */
public class SpoonTest {

  private final Spoon spoon = mock( Spoon.class );

  @Before
  public void setUp() throws KettleException {
    doCallRealMethod().when( spoon ).copySelected( any( TransMeta.class ), anyListOf( StepMeta.class ),
        anyListOf( NotePadMeta.class ) );
    doCallRealMethod().when( spoon ).pasteXML( any( TransMeta.class ), anyString(), any( Point.class ) );
    doCallRealMethod().when( spoon ).delHop( any( TransMeta.class ), any( TransHopMeta.class ) );
    LogChannelInterface log = mock( LogChannelInterface.class );
    when( spoon.getLog() ).thenReturn( log );

    KettleEnvironment.init();
  }

  /**
   * test two steps
   * @see http://jira.pentaho.com/browse/PDI-689
   * 
   * @throws KettleException
   */
  @Test
  public void testCopyPasteStepsErrorHandling() throws KettleException {

    final TransMeta transMeta = new TransMeta();

    //for check copy both step and hop
    StepMeta sourceStep = new StepMeta( "CsvInput", "Step1", new CsvInputMeta() );
    StepMeta targetStep = new StepMeta( "Dummy", "Dummy Step1", new DummyTransMeta() );

    sourceStep.setSelected( true );
    targetStep.setSelected( true );

    transMeta.addStep( sourceStep );
    transMeta.addStep( targetStep  );

    StepErrorMeta errorMeta = new StepErrorMeta( transMeta, sourceStep, targetStep );
    sourceStep.setStepErrorMeta( errorMeta );
    errorMeta.setSourceStep( sourceStep );
    errorMeta.setTargetStep( targetStep );

    final int stepsSizeBefore = transMeta.getSteps().size();
    doAnswer( new Answer() {
      @Override
      public Object answer( InvocationOnMock invocation ) throws Throwable {
        spoon.pasteXML( transMeta, (String) invocation.getArguments()[0], mock( Point.class ) );
        assertTrue( "Steps was not copied", stepsSizeBefore < transMeta.getSteps().size() );
        //selected copied step
        for ( StepMeta s:transMeta.getSelectedSteps() ) {
          if ( s.getStepMetaInterface() instanceof CsvInputMeta ) {
            //check that stepError was copied
            assertNotNull( "Error hop was not copied", s.getStepErrorMeta() );
          }
        }
        return null;
      }
    } ).when( spoon ).toClipboard( anyString() );
    spoon.copySelected( transMeta, transMeta.getSelectedSteps(), Collections.<NotePadMeta>emptyList() );
  }

  /**
   * test copy one step with error handling 
   * @see http://jira.pentaho.com/browse/PDI-13358
   * 
   * @throws KettleException
   */
  @Test
  public void testCopyPasteOneStepWithErrorHandling() throws KettleException {

    final TransMeta transMeta = new TransMeta();
    StepMeta sourceStep = new StepMeta( "CsvInput", "Step1", new CsvInputMeta() );
    StepMeta targetStep = new StepMeta( "Dummy", "Dummy Step1", new DummyTransMeta() );

    sourceStep.setSelected( true );
    transMeta.addStep( sourceStep );
    transMeta.addStep( targetStep );

    StepErrorMeta errorMeta = new StepErrorMeta( transMeta, sourceStep, targetStep );
    sourceStep.setStepErrorMeta( errorMeta );
    errorMeta.setSourceStep( sourceStep );
    errorMeta.setTargetStep( targetStep );

    final int stepsSizeBefore = transMeta.getSteps().size();
    doAnswer( new Answer() {
      @Override
      public Object answer( InvocationOnMock invocation ) throws Throwable {
        spoon.pasteXML( transMeta, (String) invocation.getArguments()[0], mock( Point.class ) );
        assertTrue( "Steps was not copied", stepsSizeBefore < transMeta.getSteps().size() );
        //selected copied step
        for ( StepMeta s:transMeta.getSelectedSteps() ) {
          if ( s.getStepMetaInterface() instanceof CsvInputMeta ) {
            //check that stepError was empty, because we copy only one step from pair
            assertNull( "Error hop was not copied", s.getStepErrorMeta() );
          }
        }
        return null;
      }
    } ).when( spoon ).toClipboard( anyString() );

    spoon.copySelected( transMeta, transMeta.getSelectedSteps(), Collections.<NotePadMeta>emptyList() );
  }

  /**
   * Testing displayed test in case versioning enabled
   * @see http://jira.pentaho.com/browse/BACKLOG-11607
   *
   * @throws KettleException
   */
  @Test
  public void testSetShellTextForTransformationWVersionEnabled() {
    TransMeta mockTransMeta = mock( TransMeta.class );
    Shell
        mockShell =
        prepareSetShellTextTests( spoon, mockTransMeta, true, true, false, false, false, false, false, false );

    verify( mockShell ).setText( "Spoon - [RepositoryName] transformationName v1.0" );
  }

  @Test
  public void testSetShellTextForTransformationWVersionEnabledRepIsNull() {
    TransMeta mockTransMeta = mock( TransMeta.class );
    Shell
        mockShell =
        prepareSetShellTextTests( spoon, mockTransMeta, true, true, true, false, false, false, false, false );

    verify( mockShell ).setText( "Spoon - transformationName v1.0" );
  }

  @Test
  public void testSetShellTextForTransformationWVersionEnabledRevIsNull() {
    TransMeta mockTransMeta = mock( TransMeta.class );
    Shell
        mockShell =
        prepareSetShellTextTests( spoon, mockTransMeta, true, true, true, true, false, false, false, false );

    verify( mockShell ).setText( "Spoon - transformationName" );
  }

  @Test
  public void testSetShellTextForTransformationWVersionEnabledChanged() {
    TransMeta mockTransMeta = mock( TransMeta.class );
    Shell
        mockShell =
        prepareSetShellTextTests( spoon, mockTransMeta, true, true, false, false, true, false, false, false );

    verify( mockShell ).setText( "Spoon - [RepositoryName] transformationName v1.0 " + BaseMessages
        .getString( Spoon.class, "Spoon.Various.Changed" ) );
  }

  @Test
  public void testSetShellTextForTransformationWVersionEnabledNameIsNull() {
    TransMeta mockTransMeta = mock( TransMeta.class );
    Shell
        mockShell =
        prepareSetShellTextTests( spoon, mockTransMeta, true, true, false, false, false, true, false, false );

    verify( mockShell ).setText( "Spoon - [RepositoryName] transformationFilename v1.0" );
  }

  @Test
  public void testSetShellTextForTransformationWVersionEnabledNameFileNameNull() {
    TransMeta mockTransMeta = mock( TransMeta.class );
    Shell
        mockShell =
        prepareSetShellTextTests( spoon, mockTransMeta, true, true, false, false, false, true, true, false );

    verify( mockShell ).setText( "Spoon - [RepositoryName] tabName v1.0" );
  }

  @Test
  public void testSetShellTextForTransformationWVersionEnabledNameFileNameTabNull() {
    TransMeta mockTransMeta = mock( TransMeta.class );
    Shell
        mockShell =
        prepareSetShellTextTests( spoon, mockTransMeta, true, true, false, false, false, true, true, true );

    verify( mockShell )
        .setText( "Spoon - [RepositoryName] " + BaseMessages.getString( Spoon.class, "Spoon.Various.NoName" ) + " v1.0" );
  }

  @Test
  public void testSetShellTextForTransformationWVersionDisabled() {
    TransMeta mockTransMeta = mock( TransMeta.class );
    Shell
        mockShell =
        prepareSetShellTextTests( spoon, mockTransMeta, false, true, false, false, false, false, false, false );

    verify( mockShell ).setText( "Spoon - [RepositoryName] transformationName" );
  }

  @Test
  public void testSetShellTextForJobWVersionEnabled() {
    JobMeta mockJobMeta = mock( JobMeta.class );
    Shell
        mockShell =
        prepareSetShellTextTests( spoon, mockJobMeta, true, false, false, false, false, false, false, false );

    verify( mockShell ).setText( "Spoon - [RepositoryName] jobName v1.0" );
  }

  @Test
  public void testSetShellTextForJobWVersionEnabledRepIsNull() {
    JobMeta mockJobMeta = mock( JobMeta.class );
    Shell
        mockShell =
        prepareSetShellTextTests( spoon, mockJobMeta, true, false, true, false, false, false, false, false );

    verify( mockShell ).setText( "Spoon - jobName v1.0" );
  }

  @Test
  public void testSetShellTextForJobWVersionEnabledRevIsNull() {
    JobMeta mockJobMeta = mock( JobMeta.class );
    Shell
        mockShell =
        prepareSetShellTextTests( spoon, mockJobMeta, true, false, true, true, false, false, false, false );

    verify( mockShell ).setText( "Spoon - jobName" );
  }

  @Test
  public void testSetShellTextForJobWVersionDisabled() {
    JobMeta mockJobMeta = mock( JobMeta.class );
    Shell
        mockShell =
        prepareSetShellTextTests( spoon, mockJobMeta, false, false, false, false, false, false, false, false );

    verify( mockShell ).setText( "Spoon - [RepositoryName] jobName" );
  }

  private static Shell prepareSetShellTextTests( Spoon spoon, AbstractMeta abstractMeta, boolean versionEnabled,
      boolean isTransformation, boolean repIsNull, boolean revIsNull, boolean hasChanged, boolean nameIsNull,
      boolean filenameIsNull, boolean tabNameIsNull ) {
    Shell mockShell = mock( Shell.class );
    ObjectRevision mockObjectRevision = revIsNull ? null : mock( ObjectRevision.class );
    RepositoryDirectory mockRepDirectory = mock( RepositoryDirectory.class );
    Repository mockRepository = repIsNull ? null : mock( Repository.class );
    RepositorySecurityProvider mockRepSecurityProvider = mock( RepositorySecurityProvider.class );
    SpoonDelegates mockDelegate = mock( SpoonDelegates.class );
    SpoonTabsDelegate mockDelegateTabs = mock( SpoonTabsDelegate.class );
    spoon.rep = mockRepository;
    spoon.delegates = mockDelegate;
    mockDelegate.tabs = mockDelegateTabs;

    doCallRealMethod().when( spoon ).openSpoon();
    doCallRealMethod().when( spoon ).setShellText();

    doReturn( mockShell ).when( spoon ).getShell();
    if ( !tabNameIsNull ) {
      doReturn( "tabName" ).when( spoon ).getActiveTabText();
    }

    doReturn( false ).when( mockShell ).isDisposed();
    setTransJobValues( abstractMeta, spoon, mockObjectRevision, mockRepDirectory, isTransformation, hasChanged,
        nameIsNull, filenameIsNull );

    if ( !revIsNull ) {
      doReturn( "1.0" ).when( mockObjectRevision ).getName();
    }
    doReturn( "/admin" ).when( mockRepDirectory ).getPath();

    if ( !repIsNull ) {
      doReturn( mockRepSecurityProvider ).when( mockRepository ).getSecurityProvider();
      doReturn( versionEnabled ).when( mockRepSecurityProvider ).isVersioningEnabled( anyString() );
    }

    doReturn( "RepositoryName" ).when( spoon ).getRepositoryName();

    doReturn( new ArrayList<TabMapEntry>() ).when( mockDelegateTabs ).getTabs();

    try {
      spoon.openSpoon();
    } catch ( NullPointerException e ) {
      //ignore work is done
    }

    spoon.setShellText();

    return mockShell;

  }

  private static void setTransJobValues( AbstractMeta mockObjMeta, Spoon spoon, ObjectRevision objectRevision,
      RepositoryDirectory repositoryDirectory, boolean isTransformation, boolean hasChanged, boolean nameIsNull,
      boolean filenameIsNull ) {

    if ( isTransformation ) {
      doReturn( mockObjMeta ).when( spoon ).getActiveTransformation();
      doReturn( null ).when( spoon ).getActiveJob();
    } else {
      doReturn( null ).when( spoon ).getActiveTransformation();
      doReturn( mockObjMeta ).when( spoon ).getActiveJob();
    }
    if ( objectRevision != null ) {
      doReturn( objectRevision ).when( mockObjMeta ).getObjectRevision();
    }

    if ( !filenameIsNull ) {
      doReturn( isTransformation ? "transformationFilename" : "jobFilename" ).when( mockObjMeta ).getFilename();
    }
    doReturn( hasChanged ).when( mockObjMeta ).hasChanged();
    if ( !nameIsNull ) {
      doReturn( isTransformation ? "transformationName" : "jobName" ).when( mockObjMeta ).getName();
    }
    doReturn( repositoryDirectory ).when( mockObjMeta ).getRepositoryDirectory();
    doReturn( isTransformation ? RepositoryObjectType.TRANSFORMATION : RepositoryObjectType.JOB ).when( mockObjMeta )
        .getRepositoryElementType();
  }

  @Test
  public void testDelHop() throws Exception {

    StepMetaInterface stepMetaInterface = Mockito.mock( StepMetaInterface.class );
    StepMeta step = new StepMeta();
    step.setStepMetaInterface( stepMetaInterface );

    TransHopMeta transHopMeta = new TransHopMeta();
    transHopMeta.setFromStep( step );

    TransMeta transMeta = Mockito.mock( TransMeta.class );

    spoon.delHop( transMeta, transHopMeta );
    Mockito.verify( stepMetaInterface, times( 1 ) ).cleanAfterHopFromRemove( );

  }

}
