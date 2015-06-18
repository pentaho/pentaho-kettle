/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.extension;

public enum KettleExtensionPoint {

  // Some transformation points
  //
    TransformationPrepareExecution( "TransformationPrepareExecution", "A transformation begins to prepare execution" ),
    TransformationStartThreads( "TransformationStartThreads", "A transformation begins to start" ),
    TransformationStart( "TransformationStart", "A transformation has started" ),
    TransformationFinish( "TransformationFinish", "A transformation finishes" ),
    TransformationMetaLoaded( "TransformationMetaLoaded", "Transformation metadata was loaded" ),
    TransPainterArrow( "TransPainterArrow", "Draw additional information on top of a transformation hop (arrow)" ),
    TransPainterStep( "TransPainterStep", "Draw additional information on top of a transformation step icon" ),
    TransPainterStart( "TransPainterStart", "Draw transformation or plugin metadata at the start (below the rest)" ),
    TransPainterEnd( "TransPainterEnd", "Draw transformation or plugin metadata at the end (on top of all the rest)" ),
    TransGraphMouseDown( "TransGraphMouseDown", "A left or right button was clicked in a transformation" ),
    TransGraphMouseDoubleClick( "TransGraphMouseDoubleClick",
      "A left or right button was double-clicked in a transformation" ),
    SpoonTransMetaExecutionStart( "SpoonTransMetaExecutionStart",
      "Spoon initiates the execution of a trans (TransMeta)" ),
    SpoonTransExecutionConfiguration( "SpoonTransExecutionConfiguration",
      "Right before Spoon configuration of transformation to be executed takes place" ),

    JobStart( "JobStart", "A job starts" ),
    JobFinish( "JobFinish", "A job finishes" ),
    JobBeforeJobEntryExecution( "JobBeforeJobEntryExecution", "Before a job entry executes" ),
    JobAfterJobEntryExecution( "JobAfterJobEntryExecution", "After a job entry executes" ),
    JobBeginProcessing( "JobBeginProcessing", "Start of a job at the end of the log table handling" ),
    JobPainterArrow( "JobPainterArrow", "Draw additional information on top of a job hop (arrow)" ),
    JobPainterJobEntry( "TransPainterJobEntry", "Draw additional information on top of a job entry copy icon" ),
    JobPainterStart( "JobPainterStart", "Draw job or plugin metadata at the start (below the rest)" ),
    JobPainterEnd( "JobPainterEnd", "Draw job or plugin metadata at the end (on top of all the rest)" ),
    JobGraphMouseDown( "JobGraphMouseDown", "A left or right button was clicked in a job" ),
    JobGraphMouseDoubleClick( "JobGraphMouseDoubleClick",
      "A left or right button was double-clicked in a job" ),
    JobGraphJobEntrySetMenu( "JobGraphJobEntrySetMenu", "Manipulate the menu on right click on a job entry" ),
    JobDialogShowRetrieveLogTableFields( "JobDialogShowRetrieveLogTableFields",
      "Show or retrieve the contents of the fields of a log channel on the log channel composite" ),

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

    CarteStartup( "CarteStartup", "Right after the Carte webserver has started and is fully functional" ),
    CarteShutdown( "CarteShutdown", "Right before the Carte webserver will shut down" ),

    SpoonViewTreeExtension ( "SpoonViewTreeExtension" , "View tree spoon extension" ),

    SpoonPopupMenuExtension ( "SpoonPopupMenuExtension" , "Pop up menu extension for the view tree" ),

    SpoonTreeDelegateExtension ( "SpoonTreeDelegateExtension" , "During the SpoonTreeDelegate execution" );

  public String id;

  public String description;

  private KettleExtensionPoint( String id, String description ) {
    this.id = id;
    this.description = description;
  }
}
