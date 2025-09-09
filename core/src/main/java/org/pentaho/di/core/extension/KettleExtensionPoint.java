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


package org.pentaho.di.core.extension;

public enum KettleExtensionPoint {

    SpoonStart( "SpoonStart", "Spoon has started" ),
    OpenRecent( "OpenRecent", "A recent file is opened" ),
  // Some transformation points
  //
    TransformationPrepareExecution( "TransformationPrepareExecution", "A transformation begins to prepare execution" ),
    TransformationStartThreads( "TransformationStartThreads", "A transformation begins to start" ),
    TransformationStart( "TransformationStart", "A transformation has started" ),
    TransformationHeartbeat( "TransformationHeartbeat",
      "A signal sent at regular intervals to indicate that the transformation is still active" ),
    TransformationFinish( "TransformationFinish", "A transformation finishes" ),
    TransformationMetaLoaded( "TransformationMetaLoaded", "Transformation metadata was loaded" ),
    TransPainterArrow( "TransPainterArrow", "Draw additional information on top of a transformation hop (arrow)" ),
    TransPainterStep( "TransPainterStep", "Draw additional information on top of a transformation step icon" ),
    TransPainterFlyout( "TransPainterFlyout", "Draw step flyout when step is clicked" ),
    TransPainterFlyoutTooltip( "TransPainterFlyoutTooltip", "Draw the flyout tooltips" ),
    TransPainterStart( "TransPainterStart", "Draw transformation or plugin metadata at the start (below the rest)" ),
    TransPainterEnd( "TransPainterEnd", "Draw transformation or plugin metadata at the end (on top of all the rest)" ),
    TransGraphMouseDown( "TransGraphMouseDown", "A mouse down event occurred on the canvas" ),
    TransGraphMouseUp( "TransGraphMouseUp", "A mouse up event occurred on the canvas" ),
    TransBeforeOpen( "TransBeforeOpen", "A transformation file is about to be opened" ),
    TransAfterOpen( "TransAfterOpen", "A transformation file was opened" ),
    TransBeforeSave( "TransBeforeSave", "A transformation file is about to be saved" ),
    TransAfterSave( "TransAfterSave", "A transformation file was saved" ),
    TransBeforeClose( "TransBeforeClose", "A transformation file is about to be closed" ),
    TransAfterClose( "TransAfterClose", "A transformation file was closed" ),
    TransChanged( "TransChanged", "A transformation has been changed" ),
    TransStepRightClick( "TransStepRightClick", "A right button was clicked on a step" ),
    TransGraphMouseMoved( "TransGraphMouseMoved", "The mouse was moved on the canvas" ),
    TransGraphMouseDoubleClick( "TransGraphMouseDoubleClick",
      "A left or right button was double-clicked in a transformation" ),
    TransBeforeDeleteSteps( "TransBeforeDeleteSteps", "Transformation steps about to be deleted" ),
    TransImportAfterSaveToRepo( "TransImportAfterSaveToRepo",
      "Transformation's shared objects created and it's content saved to repository" ),
    SpoonTransMetaExecutionStart( "SpoonTransMetaExecutionStart",
      "Spoon initiates the execution of a trans (TransMeta)" ),
    SpoonTransExecutionConfiguration( "SpoonTransExecutionConfiguration",
      "Right before Spoon configuration of transformation to be executed takes place" ),
    SpoonTransBeforeStart( "SpoonTransBeforeStart", "Right before the transformation is started" ),
    RunConfigurationSelection( "RunConfigurationSelection", "Check when run configuration is selected" ),
    RunConfigurationIsRemote( "RunConfigurationIsRemote", "Check when run configuration is pointing to a remote server" ),
    SpoonRunConfiguration( "SpoonRunConfiguration", "Send the run configuration" ),
    JobStart( "JobStart", "A job starts" ),
    JobHeartbeat( "JobHeartbeat", "A signal sent at regular intervals to indicate that the job is still active" ),
    JobFinish( "JobFinish", "A job finishes" ),
    JobBeforeJobEntryExecution( "JobBeforeJobEntryExecution", "Before a job entry executes" ),
    JobAfterJobEntryExecution( "JobAfterJobEntryExecution", "After a job entry executes" ),
    JobBeginProcessing( "JobBeginProcessing", "Start of a job at the end of the log table handling" ),
    JobPainterArrow( "JobPainterArrow", "Draw additional information on top of a job hop (arrow)" ),
    JobPainterJobEntry( "TransPainterJobEntry", "Draw additional information on top of a job entry copy icon" ),
    JobPainterStart( "JobPainterStart", "Draw job or plugin metadata at the start (below the rest)" ),
    JobPainterEnd( "JobPainterEnd", "Draw job or plugin metadata at the end (on top of all the rest)" ),
    JobGraphMouseDown( "JobGraphMouseDown", "A left or right button was clicked in a job" ),
    JobBeforeOpen( "JobBeforeOpen", "A job file is about to be opened" ),
    JobAfterOpen( "JobAfterOpen", "A job file was opened" ),
    JobBeforeSave( "JobBeforeSave", "A job file is about to be saved" ),
    JobAfterSave( "JobAfterSave", "A job file was saved" ),
    JobBeforeClose( "JobBeforeClose", "A job file is about to be closed" ),
    JobAfterClose( "JobAfterClose", "A job file was closed" ),
    JobChanged( "JobChanged", "A job has been changed" ),
    JobGraphMouseDoubleClick( "JobGraphMouseDoubleClick",
      "A left or right button was double-clicked in a job" ),
    JobGraphJobEntrySetMenu( "JobGraphJobEntrySetMenu", "Manipulate the menu on right click on a job entry" ),
    JobDialogShowRetrieveLogTableFields( "JobDialogShowRetrieveLogTableFields",
      "Show or retrieve the contents of the fields of a log channel on the log channel composite" ),
    JobEntryTransSave( "JobEntryTransSave", "Job entry trans is saved" ),

    JobMetaLoaded( "JobMetaLoaded", "Job metadata was loaded" ),
    SpoonJobMetaExecutionStart( "SpoonJobMetaExecutionStart", "Spoon initiates the execution of a job (JobMeta)" ),
    SpoonJobExecutionConfiguration( "SpoonJobExecutionConfiguration",
      "Right before Spoon configuration of job to be executed takes place" ),

    DatabaseConnected( "DatabaseConnected", "A connection to a database was made" ),
    DatabaseDisconnected( "DatabaseDisconnected", "A connection to a database was terminated" ),

    StepBeforeInitialize( "StepBeforeInitialize", "Right before a step is about to be initialized" ),
    StepAfterInitialize( "StepAfterInitialize", "After a step is initialized" ),

    StepBeforeStart( "StepBeforeStart", "Right before a step is about to be started" ),
    StepFinished( "StepFinished", "After a step has finished" ),

    BeforeCheckSteps( "BeforeCheckSteps", "Right before a set of steps is about to be verified." ),
    AfterCheckSteps( "AfterCheckSteps", "After a set of steps has been checked for warnings/errors." ),
    BeforeCheckStep( "BeforeCheckStep", "Right before a step is about to be verified." ),
    AfterCheckStep( "AfterCheckStep", "After a step has been checked for warnings/errors." ),

    BeforeCarteStartup( "BeforeCarteStartup", "Right before the Carte webserver is started" ),
    CarteStartup( "CarteStartup", "Right after the Carte webserver has started and is fully functional" ),
    CarteShutdown( "CarteShutdown", "Right before the Carte webserver will shut down" ),

    SpoonViewTreeExtension( "SpoonViewTreeExtension", "View tree spoon extension" ),
    SpoonPopupMenuExtension( "SpoonPopupMenuExtension", "Pop up menu extension for the view tree" ),
    SpoonTreeDelegateExtension( "SpoonTreeDelegateExtension", "During the SpoonTreeDelegate execution" ),

  // Removed SpoonOpenSaveRepository Extension Point as per BACKLOG-36769
    SpoonOpenSaveRepository( "SpoonOpenSaveRepository", "Open the repository browser" ),
    SpoonOpenSaveNew( "SpoonOpenSaveNew", "Open the new file browser" ),
    SpoonBrowserFunction( "SpoonBrowserFunction", "Generic browser function handler" ),
    RepositoryImporterPatchTransStep( "RepositoryImporterPatchTransStep", "Patch the step in a transformation during repository import" ),
    RepositoryExporterPatchTransStep( "RepositoryExporterPatchTransStep", "Patch the step in a transformation during repository export" ),
    RequestLoginToRepository( "RequestLoginToRepository", "Request to login into repository" ),

    OpenMapping( "OpenMapping", "Trigger when opening a mapping from TransGraph" ),

    BeforeSaveToRepository( "BeforeSaveToRepository", "Before meta is saved to the repository" ),
    AfterDeleteRepositoryObject( "AfterDeleteRepositoryObject",
      "After an object has been deleted from the repository" ),
    /** Argument is the repository being changed to, or null if disconnecting */
    RepositoryChanging( "RepositoryChanging", "The process of connecting/disconnecting to/from a repository has been initiated but not yet completed" ),

   TransformationCreateNew( "TransformationCreateNew", "Create a New Empty Transformation in Spoon" ),
   JobCreateNew( "JobCreateNew", "Create a New Empty Job in Spoon" ),
   TransSharedObjectsLoaded( "TransSharedObjectsLoaded", "After the shared objects are loaded in Transformations." ),
   JobSharedObjectsLoaded( "JobSharedObjectsLoaded", "After the shared objects are loaded in Job." );


  public String id;

  public String description;

  private KettleExtensionPoint( String id, String description ) {
    this.id = id;
    this.description = description;
  }
}
