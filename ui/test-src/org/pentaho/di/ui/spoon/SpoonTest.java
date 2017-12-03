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

package org.pentaho.di.ui.spoon;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static junit.framework.Assert.*;

import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
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
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegates;
import org.pentaho.di.ui.spoon.delegates.SpoonTabsDelegate;
import org.pentaho.metastore.stores.delegate.DelegatingMetaStore;
import org.pentaho.xul.swt.tab.TabItem;
import org.pentaho.xul.swt.tab.TabSet;

/**
 * Spoon tests
 *
 * @author Pavel Sakun
 * @see Spoon
 */
public class SpoonTest {

  private final Spoon spoon = mock( Spoon.class );
  private final LogChannelInterface log = mock( LogChannelInterface.class );
  private static SpoonPerspective mockSpoonPerspective = mock( SpoonPerspective.class );
  private static SpoonPerspectiveManager perspective = SpoonPerspectiveManager.getInstance();

  static {
    perspective.addPerspective( mockSpoonPerspective );
  }

  @Before
  public void setUp() throws KettleException {
    doCallRealMethod().when( spoon ).copySelected( any( TransMeta.class ), anyListOf( StepMeta.class ),
        anyListOf( NotePadMeta.class ) );
    doCallRealMethod().when( spoon ).pasteXML( any( TransMeta.class ), anyString(), any( Point.class ) );
    doCallRealMethod().when( spoon ).delHop( any( TransMeta.class ), any( TransHopMeta.class ) );
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

    doReturn( null ).when( abstractMeta ).getVersioningEnabled();
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

  @Test
  public void testNullParamSaveToFile() throws Exception {
    doCallRealMethod().when( spoon ).saveToFile( any() );
    assertFalse( spoon.saveToFile( null ) );
  }

  @Test
  public void testJobToRepSaveToFile() throws Exception {
    JobMeta mockJobMeta = mock( JobMeta.class );

    prepareSetSaveTests( spoon, log, mockSpoonPerspective, mockJobMeta, false, false, MainSpoonPerspective.ID, true,
        true, null, null, true, true );

    doCallRealMethod().when( spoon ).saveToFile( mockJobMeta );
    assertTrue( spoon.saveToFile( mockJobMeta ) );
    verify( mockJobMeta ).setRepository( spoon.rep );
    verify( mockJobMeta ).setMetaStore( spoon.metaStore );

    verify( mockJobMeta ).setFilename( null );

    verify( spoon.delegates.tabs ).renameTabs();
    verify( spoon ).enableMenus();
  }

  @Test
  public void testJobToFileSaveToFile() throws Exception {
    JobMeta mockJobMeta = mock( JobMeta.class );

    prepareSetSaveTests( spoon, log, mockSpoonPerspective, mockJobMeta, true, false, "NotMainSpoonPerspective", true,
        true, null, "filename", true, true );

    doCallRealMethod().when( spoon ).saveToFile( mockJobMeta );
    assertTrue( spoon.saveToFile( mockJobMeta ) );
    verify( mockJobMeta ).setRepository( spoon.rep );
    verify( mockJobMeta ).setMetaStore( spoon.metaStore );

    verify( spoon.delegates.tabs ).renameTabs();
    verify( spoon ).enableMenus();
  }

  @Test
  public void testJobToFileWithoutNameSaveToFile() throws Exception {
    JobMeta mockJobMeta = mock( JobMeta.class );

    prepareSetSaveTests( spoon, log, mockSpoonPerspective, mockJobMeta, true, false, "NotMainSpoonPerspective", true,
        true, null, null, true, true );

    doCallRealMethod().when( spoon ).saveToFile( mockJobMeta );
    doReturn( true ).when( spoon ).saveFileAs( mockJobMeta );
    assertTrue( spoon.saveToFile( mockJobMeta ) );
    verify( mockJobMeta ).setRepository( spoon.rep );
    verify( mockJobMeta ).setMetaStore( spoon.metaStore );

    verify( spoon.delegates.tabs ).renameTabs();
    verify( spoon ).enableMenus();
  }

  @Test
  public void testJobToFileCantSaveToFile() throws Exception {
    JobMeta mockJobMeta = mock( JobMeta.class );

    prepareSetSaveTests( spoon, log, mockSpoonPerspective, mockJobMeta, true, false, "NotMainSpoonPerspective", true,
        true, null, null, true, false );

    doCallRealMethod().when( spoon ).saveToFile( mockJobMeta );
    doReturn( true ).when( spoon ).saveFileAs( mockJobMeta );
    assertFalse( spoon.saveToFile( mockJobMeta ) );
    verify( mockJobMeta ).setRepository( spoon.rep );
    verify( mockJobMeta ).setMetaStore( spoon.metaStore );

    verify( spoon.delegates.tabs ).renameTabs();
    verify( spoon ).enableMenus();
  }

  @Test
  public void testTransToRepSaveToFile() throws Exception {
    TransMeta mockTransMeta = mock( TransMeta.class );

    prepareSetSaveTests( spoon, log, mockSpoonPerspective, mockTransMeta, false, false, MainSpoonPerspective.ID, true,
        true, null, null, true, true );

    doCallRealMethod().when( spoon ).saveToFile( mockTransMeta );
    assertTrue( spoon.saveToFile( mockTransMeta ) );
    verify( mockTransMeta ).setRepository( spoon.rep );
    verify( mockTransMeta ).setMetaStore( spoon.metaStore );

    verify( mockTransMeta ).setFilename( null );

    verify( spoon.delegates.tabs ).renameTabs();
    verify( spoon ).enableMenus();
  }

  @Test
  public void testJobToRepSaveFileAs() throws Exception {
    JobMeta mockJobMeta = mock( JobMeta.class );

    //passing a invalid type so not running GUIResource class
    prepareSetSaveTests( spoon, log, mockSpoonPerspective, mockJobMeta, false, false, MainSpoonPerspective.ID, true,
        true, "Invalid TYPE", null, true, true );

    doCallRealMethod().when( spoon ).saveFileAs( mockJobMeta );
    assertTrue( spoon.saveFileAs( mockJobMeta ) );
    verify( mockJobMeta ).setRepository( spoon.rep );
    verify( mockJobMeta ).setMetaStore( spoon.metaStore );

    verify( mockJobMeta ).setObjectId( null );
    verify( mockJobMeta ).setFilename( null );

    verify( spoon.delegates.tabs ).findTabMapEntry( mockJobMeta );
    verify( spoon ).enableMenus();
  }

  @Test
  public void testJobToRepSaveFileAsFailed() throws Exception {
    JobMeta mockJobMeta = mock( JobMeta.class );

    //passing a invalid type so not running GUIResource class
    prepareSetSaveTests( spoon, log, mockSpoonPerspective, mockJobMeta, false, false, MainSpoonPerspective.ID, false,
        true, "Invalid TYPE", null, true, true );

    doCallRealMethod().when( spoon ).saveFileAs( mockJobMeta );
    assertFalse( spoon.saveFileAs( mockJobMeta ) );
    verify( mockJobMeta ).setRepository( spoon.rep );
    verify( mockJobMeta ).setMetaStore( spoon.metaStore );

    verify( mockJobMeta ).setObjectId( null );
    verify( mockJobMeta ).setFilename( null );

    verify( spoon ).enableMenus();
  }

  @Test
  public void testJobToXMLFileSaveFileAs() throws Exception {
    JobMeta mockJobMeta = mock( JobMeta.class );

    //passing a invalid type so not running GUIResource class
    prepareSetSaveTests( spoon, log, mockSpoonPerspective, mockJobMeta, true, true, "NotMainSpoonPerspective", true,
        true, "Invalid TYPE", null, true, true );

    doCallRealMethod().when( spoon ).saveFileAs( mockJobMeta );
    assertTrue( spoon.saveFileAs( mockJobMeta ) );
    verify( mockJobMeta ).setRepository( spoon.rep );
    verify( mockJobMeta ).setMetaStore( spoon.metaStore );

    verify( spoon.delegates.tabs ).findTabMapEntry( mockJobMeta );
    verify( spoon ).enableMenus();
  }

  @Test
  public void testJobToXMLFileSaveFileAsFailed() throws Exception {
    JobMeta mockJobMeta = mock( JobMeta.class );

    //passing a invalid type so not running GUIResource class
    prepareSetSaveTests( spoon, log, mockSpoonPerspective, mockJobMeta, true, true, "NotMainSpoonPerspective", true,
        false, "Invalid TYPE", null, true, true );

    doCallRealMethod().when( spoon ).saveFileAs( mockJobMeta );
    assertFalse( spoon.saveFileAs( mockJobMeta ) );
    verify( mockJobMeta ).setRepository( spoon.rep );
    verify( mockJobMeta ).setMetaStore( spoon.metaStore );

    verify( spoon ).enableMenus();
  }

  @Test
  public void testTransToRepSaveFileAs() throws Exception {
    TransMeta mockTransMeta = mock( TransMeta.class );

    //passing a invalid type so not running GUIResource class
    prepareSetSaveTests( spoon, log, mockSpoonPerspective, mockTransMeta, false, false, MainSpoonPerspective.ID, true,
        true, "Invalid TYPE", null, true, true );

    doCallRealMethod().when( spoon ).saveFileAs( mockTransMeta );
    assertTrue( spoon.saveFileAs( mockTransMeta ) );
    verify( mockTransMeta ).setRepository( spoon.rep );
    verify( mockTransMeta ).setMetaStore( spoon.metaStore );

    verify( mockTransMeta ).setObjectId( null );
    verify( mockTransMeta ).setFilename( null );

    verify( spoon.delegates.tabs ).findTabMapEntry( mockTransMeta );
    verify( spoon ).enableMenus();
  }

  @Test
  public void testTransToRepSaveFileAsFailed() throws Exception {
    TransMeta mockTransMeta = mock( TransMeta.class );

    //passing a invalid type so not running GUIResource class
    prepareSetSaveTests( spoon, log, mockSpoonPerspective, mockTransMeta, false, false, MainSpoonPerspective.ID, false,
        true, "Invalid TYPE", null, true, true );

    doCallRealMethod().when( spoon ).saveFileAs( mockTransMeta );
    assertFalse( spoon.saveFileAs( mockTransMeta ) );
    verify( mockTransMeta ).setRepository( spoon.rep );
    verify( mockTransMeta ).setMetaStore( spoon.metaStore );

    verify( mockTransMeta ).setObjectId( null );
    verify( mockTransMeta ).setFilename( null );

    verify( spoon ).enableMenus();
  }

  @Test
  public void testTransToXMLFileSaveFileAs() throws Exception {
    TransMeta mockTransMeta = mock( TransMeta.class );

    //passing a invalid type so not running GUIResource class
    prepareSetSaveTests( spoon, log, mockSpoonPerspective, mockTransMeta, true, true, "NotMainSpoonPerspective", true,
        true, "Invalid TYPE", null, true, true );

    doCallRealMethod().when( spoon ).saveFileAs( mockTransMeta );
    assertTrue( spoon.saveFileAs( mockTransMeta ) );
    verify( mockTransMeta ).setRepository( spoon.rep );
    verify( mockTransMeta ).setMetaStore( spoon.metaStore );

    verify( spoon.delegates.tabs ).findTabMapEntry( mockTransMeta );
    verify( spoon ).enableMenus();
  }

  @Test
  public void testTransToXMLFileSaveFileAsFailed() throws Exception {
    TransMeta mockTransMeta = mock( TransMeta.class );

    //passing a invalid type so not running GUIResource class
    prepareSetSaveTests( spoon, log, mockSpoonPerspective, mockTransMeta, true, true, "NotMainSpoonPerspective", true,
        false, "Invalid TYPE", null, true, true );

    doCallRealMethod().when( spoon ).saveFileAs( mockTransMeta );
    assertFalse( spoon.saveFileAs( mockTransMeta ) );
    verify( mockTransMeta ).setRepository( spoon.rep );
    verify( mockTransMeta ).setMetaStore( spoon.metaStore );

    verify( spoon ).enableMenus();
  }

  @Test
  public void testTransToRepSaveObjectIdNotNullToFile() throws Exception {
    TransMeta mockTransMeta = mock( TransMeta.class );

    prepareSetSaveTests( spoon, log, mockSpoonPerspective, mockTransMeta, false, false, MainSpoonPerspective.ID, true,
        true, null, null, false, true );

    doCallRealMethod().when( spoon ).saveToFile( mockTransMeta );
    assertTrue( spoon.saveToFile( mockTransMeta ) );
    verify( mockTransMeta ).setRepository( spoon.rep );
    verify( mockTransMeta ).setMetaStore( spoon.metaStore );

    verify( mockTransMeta, never() ).setFilename( null );

    verify( spoon.delegates.tabs ).renameTabs();
    verify( spoon ).enableMenus();
  }

  private static void prepareSetSaveTests( Spoon spoon, LogChannelInterface log, SpoonPerspective spoonPerspective,
      AbstractMeta metaData, boolean repIsNull, boolean basicLevel, String perspectiveID, boolean saveToRepository,
      boolean saveXMLFile, String fileType, String filename, boolean objectIdIsNull, boolean canSave )
      throws Exception {

    TabMapEntry mockTabMapEntry = mock( TabMapEntry.class );
    TabItem mockTabItem = mock( TabItem.class );

    Repository mockRepository = mock( Repository.class );
    DelegatingMetaStore mockMetaStore = mock( DelegatingMetaStore.class );

    spoon.rep = repIsNull ? null : mockRepository;
    spoon.metaStore = mockMetaStore;
    spoon.delegates = mock( SpoonDelegates.class );
    spoon.delegates.tabs = mock( SpoonTabsDelegate.class );
    spoon.props = mock( PropsUI.class );

    doReturn( perspectiveID ).when( spoonPerspective ).getId();

    doReturn( basicLevel ).when( log ).isBasic();
    doReturn( basicLevel ).when( log ).isDetailed();
    doReturn( mockTabMapEntry ).when( spoon.delegates.tabs ).findTabMapEntry( any() );
    doReturn( mockTabItem ).when( mockTabMapEntry ).getTabItem();
    doReturn( saveToRepository ).when( spoon ).saveToRepository( metaData, true );
    doReturn( saveXMLFile ).when( spoon ).saveXMLFile( metaData, false );
    if ( objectIdIsNull ) {
      doReturn( null ).when( metaData ).getObjectId();
    } else {
      doReturn( new ObjectId() {
        @Override public String getId() {
          return "objectId";
        }
      } ).when( metaData ).getObjectId();
    }

    //saveFile
    doReturn( filename ).when( metaData ).getFilename();
    doReturn( canSave ).when( metaData ).canSave();
    doReturn( false ).when( spoon.props ).useDBCache();
    doReturn( saveToRepository ).when( spoon ).saveToRepository( metaData );
    doReturn( saveXMLFile ).when( spoon ).save( metaData, filename, false );

    doReturn( fileType ).when( metaData ).getFileType();
  }

  @Test
  public void testLoadLastUsedTransLocalWithRepository() throws Exception {
    String repositoryName = "repositoryName";
    String fileName = "fileName";

    setLoadLastUsedJobLocalWithRepository( false, repositoryName, null, fileName, true );
    verify( spoon ).openFile( fileName, true );
  }

  @Test
  public void testLoadLastUsedTransLocalNoRepository() throws Exception {
    String repositoryName = null;
    String fileName = "fileName";

    setLoadLastUsedJobLocalWithRepository( false, repositoryName, null, fileName, true );
    verify( spoon ).openFile( fileName, false );
  }

  @Test
  public void testLoadLastUsedTransLocalNoFilename() throws Exception {
    String repositoryName = null;
    String fileName = null;

    setLoadLastUsedJobLocalWithRepository( false, repositoryName, null, fileName, true );
    verify( spoon, never() ).openFile( anyString(), anyBoolean() );
  }

  @Test
  public void testLoadLastUsedJobLocalWithRepository() throws Exception {
    String repositoryName = null;
    String fileName = "fileName";

    setLoadLastUsedJobLocalWithRepository( false, repositoryName, null, fileName, false );
    verify( spoon ).openFile( fileName, false );
  }

  @Test
  public void testLoadLastUsedRepTransNoRepository() throws Exception {
    String repositoryName = null;
    String fileName = "fileName";

    setLoadLastUsedJobLocalWithRepository( true, repositoryName, null, fileName, false );
    verify( spoon, never() ).openFile( anyString(), anyBoolean() );
  }

  private void setLoadLastUsedJobLocalWithRepository( boolean isSourceRepository, String repositoryName,
      String directoryName, String fileName, boolean isTransformation ) throws Exception {
    LastUsedFile mockLastUsedFile = mock( LastUsedFile.class );

    if ( repositoryName != null ) {
      Repository mockRepository = mock( Repository.class );
      spoon.rep = mockRepository;
      doReturn( repositoryName ).when( mockRepository ).getName();
    } else {
      spoon.rep = null;
    }

    doReturn( isSourceRepository ).when( mockLastUsedFile ).isSourceRepository();
    doReturn( repositoryName ).when( mockLastUsedFile ).getRepositoryName();
    doReturn( directoryName ).when( mockLastUsedFile ).getDirectory();
    doReturn( fileName ).when( mockLastUsedFile ).getFilename();
    doReturn( isTransformation ).when( mockLastUsedFile ).isTransformation();
    doReturn( !isTransformation ).when( mockLastUsedFile ).isJob();

    doCallRealMethod().when( spoon ).loadLastUsedFile( mockLastUsedFile, repositoryName );
    spoon.loadLastUsedFile( mockLastUsedFile, repositoryName );
  }

  @Test
  public void testCancelPromptToSave() throws Exception {
    setPromptToSave( SWT.CANCEL, false );
    assertFalse( spoon.promptForSave() );
  }

  @Test
  public void testNoPromptToSave() throws Exception {
    SpoonBrowser mockBrowser = setPromptToSave( SWT.NO, false );
    assertTrue( spoon.promptForSave() );
    verify( mockBrowser, never() ).applyChanges();
  }

  @Test
  public void testYesPromptToSave() throws Exception {
    SpoonBrowser mockBrowser = setPromptToSave( SWT.YES, false );
    assertTrue( spoon.promptForSave() );
    verify( mockBrowser ).applyChanges();
  }

  @Test
  public void testCanClosePromptToSave() throws Exception {
    setPromptToSave( SWT.YES, true );
    assertTrue( spoon.promptForSave() );
  }

  @Test
  public void testVersioningEnabled() throws Exception {
    Repository repository = mock( Repository.class );
    RepositorySecurityProvider securityProvider = mock( RepositorySecurityProvider.class );
    doReturn( securityProvider ).when( repository ).getSecurityProvider();
    EngineMetaInterface jobTransMeta = spy( new TransMeta() );
    RepositoryDirectoryInterface repositoryDirectoryInterface = mock( RepositoryDirectoryInterface.class );
    doReturn( "/home" ).when( repositoryDirectoryInterface ).toString();
    doReturn( "trans" ).when( jobTransMeta ).getName();
    doReturn( true ).when( jobTransMeta ).getVersioningEnabled();

    boolean result = Spoon.isVersionEnabled( repository, jobTransMeta );

    assertTrue( result );
    verify( securityProvider, never() ).isVersioningEnabled( anyString() );
  }

  @Test
  public void testVersioningDisabled() throws Exception {
    Repository repository = mock( Repository.class );
    RepositorySecurityProvider securityProvider = mock( RepositorySecurityProvider.class );
    doReturn( securityProvider ).when( repository ).getSecurityProvider();
    EngineMetaInterface jobTransMeta = spy( new TransMeta() );
    RepositoryDirectoryInterface repositoryDirectoryInterface = mock( RepositoryDirectoryInterface.class );
    doReturn( "/home" ).when( repositoryDirectoryInterface ).toString();
    doReturn( "trans" ).when( jobTransMeta ).getName();
    doReturn( false ).when( jobTransMeta ).getVersioningEnabled();

    boolean result = Spoon.isVersionEnabled( repository, jobTransMeta );

    assertFalse( result );
    verify( securityProvider, never() ).isVersioningEnabled( anyString() );
  }

  @Test
  public void testVersioningCheckingOnServer() throws Exception {
    Repository repository = mock( Repository.class );
    RepositorySecurityProvider securityProvider = mock( RepositorySecurityProvider.class );
    doReturn( securityProvider ).when( repository ).getSecurityProvider();
    EngineMetaInterface jobTransMeta = spy( new TransMeta() );
    RepositoryDirectoryInterface repositoryDirectoryInterface = mock( RepositoryDirectoryInterface.class );
    doReturn( "/home" ).when( repositoryDirectoryInterface ).toString();
    doReturn( "trans" ).when( jobTransMeta ).getName();
    doReturn( true ).when( securityProvider ).isVersioningEnabled( anyString() );

    boolean result = Spoon.isVersionEnabled( repository, jobTransMeta );
    assertTrue( result );
  }

  private SpoonBrowser setPromptToSave( int buttonPressed, boolean canbeClosed ) throws Exception {
    TabMapEntry mockTabMapEntry = mock( TabMapEntry.class );
    TabSet mockTabSet = mock( TabSet.class );
    ArrayList<TabMapEntry> lTabs = new ArrayList<>();
    lTabs.add( mockTabMapEntry );

    SpoonBrowser mockSpoonBrowser = mock( SpoonBrowser.class );

    spoon.delegates = mock( SpoonDelegates.class );
    spoon.delegates.tabs = mock( SpoonTabsDelegate.class );
    spoon.tabfolder = mockTabSet;

    doReturn( lTabs ).when( spoon.delegates.tabs ).getTabs();
    doReturn( mockSpoonBrowser ).when( mockTabMapEntry ).getObject();
    doReturn( canbeClosed ).when( mockSpoonBrowser ).canBeClosed();
    doReturn( buttonPressed ).when( mockSpoonBrowser ).showChangedWarning();

    doCallRealMethod().when( spoon ).promptForSave();

    return mockSpoonBrowser;
  }
}
