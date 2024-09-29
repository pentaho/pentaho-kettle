/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon.delegates;

import com.google.common.annotations.VisibleForTesting;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.undo.TransAction;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.special.JobEntrySpecial;
import org.pentaho.di.job.entries.sql.JobEntrySQL;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.job.dialog.JobExecutionConfigurationDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.di.ui.spoon.TabMapEntry.ObjectType;
import org.pentaho.di.ui.spoon.job.JobGraph;
import org.pentaho.di.ui.spoon.wizards.RipDatabaseWizardPage1;
import org.pentaho.di.ui.spoon.wizards.RipDatabaseWizardPage2;
import org.pentaho.di.ui.spoon.wizards.RipDatabaseWizardPage3;
import org.pentaho.xul.swt.tab.TabItem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpoonJobDelegate extends SpoonDelegate {
  private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!!

  /**
   * This contains a map between the name of a transformation and the TransMeta object. If the transformation has no
   * name it will be mapped under a number [1], [2] etc.
   */
  private List<JobMeta> jobMap;

  public SpoonJobDelegate( Spoon spoon ) {
    super( spoon );
    jobMap = new ArrayList<>();
  }

  public JobEntryCopy newJobEntry( JobMeta jobMeta, String type_desc, boolean openit ) {
    PluginRegistry registry = PluginRegistry.getInstance();
    PluginInterface jobPlugin;

    try {
      jobPlugin = PluginRegistry.getInstance().findPluginWithName( JobEntryPluginType.class, type_desc );
      if ( jobPlugin == null ) {
        // Check if it's not START or DUMMY
        if ( JobMeta.STRING_SPECIAL_START.equalsIgnoreCase( type_desc ) || JobMeta.STRING_SPECIAL_DUMMY.equalsIgnoreCase( type_desc ) ) {
          jobPlugin = registry.findPluginWithId( JobEntryPluginType.class, JobMeta.STRING_SPECIAL );
        }
      }

      if ( jobPlugin != null ) {
        // Determine name & number for this entry.

        // See if the name is already used...
        //
        String entry_name = type_desc;
        int nr = 2;
        JobEntryCopy check = jobMeta.findJobEntry( entry_name, 0, true );
        while ( check != null ) {
          entry_name = type_desc + " " + nr++;
          check = jobMeta.findJobEntry( entry_name, 0, true );
        }

        // Generate the appropriate class...
        JobEntryInterface jei = (JobEntryInterface) registry.loadClass( jobPlugin );
        jei.setPluginId( jobPlugin.getIds()[0] );
        jei.setName( entry_name );

        if ( jei.isSpecial() ) {
          if ( JobMeta.STRING_SPECIAL_START.equalsIgnoreCase( type_desc ) ) {
            // Check if start is already on the canvas...
            if ( jobMeta.findStart() != null ) {
              JobGraph.showOnlyStartOnceMessage( spoon.getShell() );
              return null;
            }
            ( (JobEntrySpecial) jei ).setStart( true );
          }
          if ( JobMeta.STRING_SPECIAL_DUMMY.equalsIgnoreCase( type_desc ) ) {
            ( (JobEntrySpecial) jei ).setDummy( true );
          }
        }

        if ( openit ) {
          JobEntryDialogInterface d = getJobEntryDialog( jei, jobMeta );
          if ( d != null && d.open() != null ) {
            JobEntryCopy jge = new JobEntryCopy();
            jge.setEntry( jei );
            jge.setLocation( 50, 50 );
            jge.setNr( 0 );
            jobMeta.addJobEntry( jge );

            // Verify that the name is not already used in the job.
            //
            jobMeta.renameJobEntryIfNameCollides( jge );

            spoon.addUndoNew( jobMeta, new JobEntryCopy[] { jge }, new int[] { jobMeta.indexOfJobEntry( jge ) } );
            spoon.refreshGraph();
            spoon.refreshTree();
            return jge;
          } else {
            return null;
          }
        } else {
          JobEntryCopy jge = new JobEntryCopy();
          jge.setEntry( jei );
          jge.setLocation( 50, 50 );
          jge.setNr( 0 );
          jobMeta.addJobEntry( jge );
          spoon.addUndoNew( jobMeta, new JobEntryCopy[] { jge }, new int[] { jobMeta.indexOfJobEntry( jge ) } );
          spoon.refreshGraph();
          spoon.refreshTree();
          return jge;
        }
      } else {
        return null;
      }
    } catch ( Throwable e ) {
      new ErrorDialog( spoon.getShell(),
        BaseMessages.getString( PKG, "Spoon.ErrorDialog.UnexpectedErrorCreatingNewJobGraphEntry.Title" ),
        BaseMessages.getString( PKG, "Spoon.ErrorDialog.UnexpectedErrorCreatingNewJobGraphEntry.Message" ),
        new Exception( e ) );
      return null;
    }
  }

  public JobEntryDialogInterface getJobEntryDialog( JobEntryInterface jobEntryInterface, JobMeta jobMeta ) {
    Class<?>[] paramClasses = new Class<?>[] { Shell.class, JobEntryInterface.class, Repository.class, JobMeta.class };
    Object[] paramArgs = new Object[] { spoon.getShell(), jobEntryInterface, spoon.getRepository(), jobMeta };

    PluginRegistry registry = PluginRegistry.getInstance();
    PluginInterface plugin = registry.getPlugin( JobEntryPluginType.class, jobEntryInterface );
    String dialogClassName = plugin.getClassMap().get( JobEntryDialogInterface.class );
    if ( dialogClassName == null ) {
      // try the deprecated way
      log.logDebug( "Use of JobEntryInterface#getDialogClassName is deprecated, use PluginDialog annotation instead." );
      dialogClassName = jobEntryInterface.getDialogClassName();
    }

    try {
      Class<JobEntryDialogInterface> dialogClass = registry.getClass( plugin, dialogClassName );
      Constructor<JobEntryDialogInterface> dialogConstructor = dialogClass.getConstructor( paramClasses );
      JobEntryDialogInterface entryDialogInterface = dialogConstructor.newInstance( paramArgs );
      entryDialogInterface.setMetaStore( spoon.getMetaStore() );
      return entryDialogInterface;
    } catch ( Throwable t ) {
      t.printStackTrace();
      String errorTitle = BaseMessages.getString( PKG, "Spoon.Dialog.ErrorCreatingJobDialog.Title" );
      String errorMsg = BaseMessages.getString( PKG, "Spoon.Dialog.ErrorCreatingJobEntryDialog.Message", dialogClassName );
      spoon.getLog().logError( spoon.toString(), errorMsg );
      new ErrorDialog( spoon.getShell(), errorTitle, errorMsg, t );
      return null;
    }
  }

  public void editJobEntry( JobMeta jobMeta, JobEntryCopy je ) {
    try {
      spoon.getLog().logBasic(
        spoon.toString(), BaseMessages.getString( PKG, "Spoon.Log.EditJobEntry", je.getName() ) );

      JobEntryCopy before = (JobEntryCopy) je.clone_deep();

      JobEntryInterface jei = je.getEntry();

      JobEntryDialogInterface d = getJobEntryDialog( jei, jobMeta );
      if ( d != null ) {
        if ( d.open() != null ) {
          // First see if the name changed.
          // If so, we need to verify that the name is not already used in the job.
          //
          jobMeta.renameJobEntryIfNameCollides( je );

          JobEntryCopy after = (JobEntryCopy) je.clone();
          spoon.addUndoChange(
            jobMeta, new JobEntryCopy[] { before }, new JobEntryCopy[] { after }, new int[] { jobMeta
              .indexOfJobEntry( je ) } );
          spoon.refreshGraph();
          spoon.refreshTree();
        }
      } else {
        MessageBox mb = new MessageBox( spoon.getShell(), SWT.OK | SWT.ICON_INFORMATION );
        mb.setMessage( BaseMessages.getString( PKG, "Spoon.Dialog.JobEntryCanNotBeChanged.Message" ) );
        mb.setText( BaseMessages.getString( PKG, "Spoon.Dialog.JobEntryCanNotBeChanged.Title" ) );
        mb.open();
      }

    } catch ( Exception e ) {
      if ( !spoon.getShell().isDisposed() ) {
        new ErrorDialog( spoon.getShell(),
          BaseMessages.getString( PKG, "Spoon.ErrorDialog.ErrorEditingJobEntry.Title" ),
          BaseMessages.getString( PKG, "Spoon.ErrorDialog.ErrorEditingJobEntry.Message" ), e );
      }
    }
  }

  public void deleteJobEntryCopies( JobMeta job, JobEntryCopy[] jobEntries ) {

    // Hops belonging to the deleting jobEntries are placed in a single transaction and removed.
    List<JobHopMeta> jobHops = new ArrayList<>();
    int[] hopIndexes = new int[job.nrJobHops()];
    int hopIndex = 0;
    for ( int i = job.nrJobHops() - 1; i >= 0; i-- ) {
      JobHopMeta hi = job.getJobHop( i );
      for ( int j = 0; j < jobEntries.length && hopIndex < hopIndexes.length; j++ ) {
        if ( hi.getFromEntry().equals( jobEntries[j] ) || hi.getToEntry().equals( jobEntries[j] ) ) {
          int idx = job.indexOfJobHop( hi );
          jobHops.add( (JobHopMeta) hi.clone() );
          hopIndexes[hopIndex] = idx;
          job.removeJobHop( idx );
          spoon.refreshTree();
          hopIndex++;
          break;
        }
      }
    }
    if ( !jobHops.isEmpty() ) {
      JobHopMeta[] hops = jobHops.toArray( new JobHopMeta[jobHops.size()] );
      spoon.addUndoDelete( job, hops, hopIndexes );
    }

    // Deleting jobEntries are placed all in a single transaction and removed.
    int[] positions = new int[jobEntries.length];
    for ( int i = 0; i < jobEntries.length; i++ ) {
      int pos = job.indexOfJobEntry( jobEntries[i] );
      job.removeJobEntry( pos );
      positions[i] = pos;
    }
    spoon.addUndoDelete( job, jobEntries, positions );

    spoon.refreshTree();
    spoon.refreshGraph();
  }

  public void deleteJobEntryCopies( JobMeta jobMeta, JobEntryCopy jobEntry ) {

    for ( int i = jobMeta.nrJobHops() - 1; i >= 0; i-- ) {
      JobHopMeta hi = jobMeta.getJobHop( i );
      if ( hi.getFromEntry().equals( jobEntry ) || hi.getToEntry().equals( jobEntry ) ) {
        int idx = jobMeta.indexOfJobHop( hi );
        spoon.addUndoDelete( jobMeta, new JobHopMeta[] { (JobHopMeta) hi.clone() }, new int[] { idx } );
        jobMeta.removeJobHop( idx );
        spoon.refreshTree();
      }
    }

    int pos = jobMeta.indexOfJobEntry( jobEntry );
    jobMeta.removeJobEntry( pos );
    spoon.addUndoDelete( jobMeta, new JobEntryCopy[] { jobEntry }, new int[] { pos } );

    spoon.refreshTree();
    spoon.refreshGraph();
  }

  public void dupeJobEntry( JobMeta jobMeta, JobEntryCopy jobEntry ) {
    if ( jobEntry == null ) {
      return;
    }

    if ( jobEntry.isStart() ) {
      MessageBox mb = new MessageBox( spoon.getShell(), SWT.OK | SWT.ICON_INFORMATION );
      mb.setMessage( BaseMessages.getString( PKG, "Spoon.Dialog.OnlyUseStartOnce.Message" ) );
      mb.setText( BaseMessages.getString( PKG, "Spoon.Dialog.OnlyUseStartOnce.Title" ) );
      mb.open();
      return;
    }

    JobEntryCopy dupejge = (JobEntryCopy) jobEntry.clone();
    dupejge.setNr( jobMeta.findUnusedNr( dupejge.getName() ) );
    if ( dupejge.isDrawn() ) {
      Point p = jobEntry.getLocation();
      dupejge.setLocation( p.x + 10, p.y + 10 );
    }
    jobMeta.addJobEntry( dupejge );
    spoon.refreshGraph();
    spoon.refreshTree();
    spoon.setShellText();

  }

  public void copyJobEntries( List<JobEntryCopy> jec ) {
    if ( jec == null || jec.size() == 0 ) {
      return;
    }

    StringBuilder xml = new StringBuilder( XMLHandler.getXMLHeader() );
    xml.append( XMLHandler.openTag( Spoon.XML_TAG_JOB_JOB_ENTRIES ) ).append( Const.CR );

    for ( JobEntryCopy aJec : jec ) {
      xml.append( aJec.getXML() );
    }

    xml.append( "    " ).append( XMLHandler.closeTag( Spoon.XML_TAG_JOB_JOB_ENTRIES ) ).append( Const.CR );

    spoon.toClipboard( xml.toString() );
  }

  public void pasteXML( JobMeta jobMeta, String clipcontent, Point loc ) {
    try {
      Document doc = XMLHandler.loadXMLString( clipcontent );

      // De-select all, re-select pasted steps...
      jobMeta.unselectAll();

      Node entriesnode = XMLHandler.getSubNode( doc, Spoon.XML_TAG_JOB_JOB_ENTRIES );
      int nr = XMLHandler.countNodes( entriesnode, "entry" );
      spoon.getLog().logDebug( spoon.toString(), "I found " + nr + " job entries to paste on location: " + loc );
      List<JobEntryCopy> entryList = new ArrayList<>( nr );

      // Point min = new Point(loc.x, loc.y);
      Point min = new Point( 99999999, 99999999 );

      for ( int i = 0; i < nr; i++ ) {
        Node entrynode = XMLHandler.getSubNodeByNr( entriesnode, "entry", i );
        JobEntryCopy copy =
          new JobEntryCopy(
            entrynode, jobMeta.getDatabases(), jobMeta.getSlaveServers(), spoon.getRepository(), spoon
              .getMetaStore() );
        if ( copy.isStart() && ( jobMeta.findStart() != null ) ) {
          JobGraph.showOnlyStartOnceMessage( spoon.getShell() );
          continue;
        }
        String name = jobMeta.getAlternativeJobentryName( copy.getName() );
        copy.setName( name );

        if ( loc != null ) {
          Point p = copy.getLocation();

          if ( min.x > p.x ) {
            min.x = p.x;
          }
          if ( min.y > p.y ) {
            min.y = p.y;
          }
        }

        entryList.add( copy );
      }

      JobEntryCopy[] entries = entryList.toArray( new JobEntryCopy[] {} );

      // What's the difference between loc and min?
      // This is the offset:
      Point offset = new Point( loc.x - min.x, loc.y - min.y );

      // Undo/redo object positions...
      int[] position = new int[entries.length];

      for ( int i = 0; i < entries.length; i++ ) {
        Point p = entries[i].getLocation();
        String name = entries[i].getName();

        entries[i].setLocation( p.x + offset.x, p.y + offset.y );

        // Check the name, find alternative...
        entries[i].setName( jobMeta.getAlternativeJobentryName( name ) );
        jobMeta.addJobEntry( entries[i] );
        position[i] = jobMeta.indexOfJobEntry( entries[i] );
      }

      // Save undo information too...
      spoon.addUndoNew( jobMeta, entries, position );

      if ( jobMeta.hasChanged() ) {
        spoon.refreshTree();
        spoon.refreshGraph();
      }
    } catch ( KettleException e ) {
      new ErrorDialog( spoon.getShell(),
        BaseMessages.getString( PKG, "Spoon.ErrorDialog.ErrorPasingJobEntries.Title" ),
        BaseMessages.getString( PKG, "Spoon.ErrorDialog.ErrorPasingJobEntries.Message" ), e );
    }
  }

  public void newJobHop( JobMeta jobMeta, JobEntryCopy fr, JobEntryCopy to ) {
    JobHopMeta hi = new JobHopMeta( fr, to );
    jobMeta.addJobHop( hi );
    spoon.addUndoNew( jobMeta, new JobHopMeta[] { hi }, new int[] { jobMeta.indexOfJobHop( hi ) } );
    spoon.refreshGraph();
    spoon.refreshTree();
  }

  /**
   * Create a job that extracts tables & data from a database.
   * <p>
   * <p>
   *
   * 0) Select the database to rip
   * <p>
   * 1) Select the tables in the database to rip
   * <p>
   * 2) Select the database to dump to
   * <p>
   * 3) Select the repository directory in which it will end up
   * <p>
   * 4) Select a name for the new job
   * <p>
   * 5) Create an empty job with the selected name.
   * <p>
   * 6) Create 1 transformation for every selected table
   * <p>
   * 7) add every created transformation to the job & evaluate
   * <p>
   *
   */
  public void ripDBWizard() {
    final List<DatabaseMeta> databases = spoon.getActiveDatabases();
    if ( databases.size() == 0 ) {
      return; // Nothing to do here
    }

    final RipDatabaseWizardPage1 page1 = new RipDatabaseWizardPage1( "1", databases );
    final RipDatabaseWizardPage2 page2 = new RipDatabaseWizardPage2( "2" );
    final RipDatabaseWizardPage3 page3 = new RipDatabaseWizardPage3( "3", spoon.getRepository() );

    Wizard wizard = new Wizard() {
      public boolean performFinish() {
        try {
          JobMeta jobMeta =
            ripDB( databases, page3.getJobname(), page3.getRepositoryDirectory(), page3.getDirectory(), page1
              .getSourceDatabase(), page1.getTargetDatabase(), page2.getSelection() );
          if ( jobMeta == null ) {
            return false;
          }

          if ( page3.getRepositoryDirectory() != null ) {
            spoon.saveToRepository( jobMeta, false );
          } else {
            spoon.saveToFile( jobMeta );
          }

          addJobGraph( jobMeta );
          return true;
        } catch ( Exception e ) {
          new ErrorDialog( spoon.getShell(), "Error", "An unexpected error occurred!", e );
          return false;
        }
      }

      /**
       * @see org.eclipse.jface.wizard.Wizard#canFinish()
       */
      public boolean canFinish() {
        return page3.canFinish();
      }
    };

    wizard.addPage( page1 );
    wizard.addPage( page2 );
    wizard.addPage( page3 );

    WizardDialog wd = new WizardDialog( spoon.getShell(), wizard );
    WizardDialog.setDefaultImage( GUIResource.getInstance().getImageWizard() );
    wd.setMinimumPageSize( 700, 400 );
    wd.updateSize();
    wd.open();
  }

  public JobMeta ripDB( final List<DatabaseMeta> databases, final String jobname,
    final RepositoryDirectoryInterface repdir, final String directory, final DatabaseMeta sourceDbInfo,
    final DatabaseMeta targetDbInfo, final String[] tables ) {
    //
    // Create a new job...
    //

    final JobMeta jobMeta = new JobMeta();
    jobMeta.setDatabases( databases );
    jobMeta.setFilename( null );
    jobMeta.setName( jobname );

    if ( spoon.getRepository() != null ) {
      jobMeta.setRepositoryDirectory( repdir );
    } else {
      jobMeta.setFilename( Const.createFilename( directory, jobname, "." + Const.STRING_JOB_DEFAULT_EXT ) );
    }

    spoon.refreshTree();
    spoon.refreshGraph();

    final Point location = new Point( 50, 50 );

    // The start entry...
    final JobEntryCopy start = JobMeta.createStartEntry();
    start.setLocation( new Point( location.x, location.y ) );
    start.setDrawn();
    jobMeta.addJobEntry( start );

    // final Thread parentThread = Thread.currentThread();

    // Create a dialog with a progress indicator!
    IRunnableWithProgress op = monitor -> {
      try {
        // This is running in a new process: copy some KettleVariables
        // info
        // LocalVariables.getInstance().createKettleVariables(Thread.currentThread().getName(),
        // parentThread.getName(), true);

        monitor.beginTask( BaseMessages.getString( PKG, "Spoon.RipDB.Monitor.BuildingNewJob" ), tables.length );
        monitor.worked( 0 );
        JobEntryCopy previous = start;

        // Loop over the table-names...
        for ( int i = 0; i < tables.length && !monitor.isCanceled(); i++ ) {
          monitor.setTaskName( BaseMessages.getString( PKG, "Spoon.RipDB.Monitor.ProcessingTable" )
            + tables[i] + "]..." );


          TableInputMeta tii = new TableInputMeta();
          tii.setDefault();
          tii.setDatabaseMeta( sourceDbInfo );
          tii.setSQL( "SELECT * FROM " + tables[i] ); // It's already quoted!

          // Create the new transformation
          TransMeta transMeta = new TransMeta();
          setTransMetaFileNaming( repdir, directory, sourceDbInfo, targetDbInfo, tables, i, transMeta );

          // Add the source & target db
          setTransMetaDatabase( sourceDbInfo, targetDbInfo, transMeta );

          // Add a note
          setTransMetaNote( sourceDbInfo, targetDbInfo, tables, i, transMeta );

          // Add the TableInputMeta step
          StepMeta fromstep = getFromStep( sourceDbInfo, tables, i, tii );
          transMeta.addStep( fromstep );

          // Add the TableOutputMeta step
          StepMeta tostep = getToStep( targetDbInfo, tables[ i ] );
          transMeta.addStep( tostep );

          // Add a hop between the two steps
          TransHopMeta hi = new TransHopMeta( fromstep, tostep );
          transMeta.addTransHop( hi );

          // Now we generate the SQL needed to run for this transformation.
          String sql = getSQLString( sourceDbInfo, tii, transMeta );

          // Now, save the transformation
          saveTransformation( transMeta );

          // We can now continue with the population of the job
          location.x = 250;
          if ( i > 0 ) {
            location.y += 100;
          }

          // We can continue defining the job.

          // First the SQL, but only if needed!
          // If the table exists & has the correct format, nothing is
          // done
          //
          if ( !Utils.isEmpty( sql ) ) {
            String jesqlname = BaseMessages.getString( PKG, "Spoon.RipDB.JobEntrySQL.Name" ) + tables[i] + "]";
            JobEntrySQL jesql = new JobEntrySQL( jesqlname );
            jesql.setDatabase( targetDbInfo );
            jesql.setSQL( sql );
            jesql
              .setDescription( BaseMessages.getString( PKG, "Spoon.RipDB.JobEntrySQL.Description" )
                + targetDbInfo + "].[" + tables[i] + "]" );

            JobEntryCopy jecsql = new JobEntryCopy();
            jecsql.setEntry( jesql );
            jecsql.setLocation( new Point( location.x, location.y ) );
            jecsql.setDrawn();
            jobMeta.addJobEntry( jecsql );

            // Add the hop too...
            JobHopMeta jhi = new JobHopMeta( previous, jecsql );
            jobMeta.addJobHop( jhi );
            previous = jecsql;
          }

          //
          // Add the jobentry for the transformation too...
          //
          String jetransname = BaseMessages.getString( PKG, "Spoon.RipDB.JobEntryTrans.Name" ) + tables[i] + "]";
          JobEntryTrans jetrans = new JobEntryTrans( jetransname );
          jetrans.setTransname( transMeta.getName() );
          if ( spoon.getRepository() != null ) {
            jetrans.setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
            jetrans.setDirectory( transMeta.getRepositoryDirectory().getPath() );
          } else {
            jetrans.setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
            jetrans.setFileName( Const.createFilename( "${"
              + Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY + "}", transMeta.getName(), "."
              + Const.STRING_TRANS_DEFAULT_EXT ) );
          }

          JobEntryCopy jectrans = new JobEntryCopy( jetrans );
          jectrans
            .setDescription( BaseMessages.getString( PKG, "Spoon.RipDB.JobEntryTrans.Description1" )
              + Const.CR + BaseMessages.getString( PKG, "Spoon.RipDB.JobEntryTrans.Description2" )
              + sourceDbInfo + "].[" + tables[i] + "]" + Const.CR
              + BaseMessages.getString( PKG, "Spoon.RipDB.JobEntryTrans.Description3" ) + targetDbInfo + "].["
              + tables[i] + "]" );
          jectrans.setDrawn();
          location.x += 400;
          jectrans.setLocation( new Point( location.x, location.y ) );
          jobMeta.addJobEntry( jectrans );

          // Add a hop between the last 2 job entries.
          JobHopMeta jhi2 = new JobHopMeta( previous, jectrans );
          jobMeta.addJobHop( jhi2 );
          previous = jectrans;

          monitor.worked( 1 );
        }

        monitor.worked( 100 );
        monitor.done();
      } catch ( Exception e ) {
        new ErrorDialog( spoon.getShell(), "Error", "An unexpected error occurred!", e );
      }
    };

    try {
      ProgressMonitorDialog pmd = new ProgressMonitorDialog( spoon.getShell() );
      pmd.run( false, true, op );
    } catch ( InvocationTargetException | InterruptedException e ) {
      new ErrorDialog( spoon.getShell(),
        BaseMessages.getString( PKG, "Spoon.ErrorDialog.RipDB.ErrorRippingTheDatabase.Title" ),
        BaseMessages.getString( PKG, "Spoon.ErrorDialog.RipDB.ErrorRippingTheDatabase.Message" ), e );
      return null;
    } finally {
      spoon.refreshGraph();
      spoon.refreshTree();
    }

    return jobMeta;
  }

  private void saveTransformation( TransMeta transMeta ) throws KettleException, InvocationTargetException {
    boolean ok;
    if ( spoon.getRepository() != null ) {
      ok = spoon.saveToRepository( transMeta, false );
    } else {
      ok = spoon.saveToFile( transMeta );
    }
    if ( !ok ) {
      throw new InvocationTargetException( new Exception(
        BaseMessages.getString( PKG, "Spoon.RipDB.Exception.UnableToSaveTransformationToRepository" ) ),
        BaseMessages.getString( PKG, "Spoon.RipDB.Exception.UnableToSaveTransformationToRepository" ) );
    }
  }

  private String getSQLString( DatabaseMeta sourceDbInfo, TableInputMeta tii, TransMeta transMeta )
    throws InvocationTargetException {
    // First set the limit to 1 to speed things up!
    String tmpSql = tii.getSQL();
    tii.setSQL( tii.getSQL() + sourceDbInfo.getLimitClause( 1 ) );
    String sql;
    try {
      sql = transMeta.getSQLStatementsString();
    } catch ( KettleStepException kse ) {
      throw new InvocationTargetException( kse, BaseMessages.getString(
        PKG, "Spoon.RipDB.Exception.ErrorGettingSQLFromTransformation" )
        + transMeta + "] : " + kse.getMessage() );
    }
    // remove the limit
    tii.setSQL( tmpSql );
    return sql;
  }

  private StepMeta getToStep( DatabaseMeta targetDbInfo, String table ) {
    String tostepname = BaseMessages.getString( PKG, "Spoon.RipDB.Monitor.ToStep.Name" ) + table + "]";
    TableOutputMeta toi = new TableOutputMeta();
    toi.setDatabaseMeta( targetDbInfo );
    toi.setTableName( table );
    toi.setCommitSize( 100 );
    toi.setTruncateTable( true );

    String tostepid = PluginRegistry.getInstance().getPluginId( StepPluginType.class, toi );
    StepMeta tostep = new StepMeta( tostepid, tostepname, toi );
    tostep.setLocation( 500, 100 );
    tostep.setDraw( true );
    tostep
      .setDescription( BaseMessages.getString( PKG, "Spoon.RipDB.Monitor.ToStep.Description1" )
        + table + BaseMessages.getString( PKG, "Spoon.RipDB.Monitor.ToStep.Description2" )
        + targetDbInfo + "]" );
    return tostep;
  }

  private StepMeta getFromStep( DatabaseMeta sourceDbInfo, String[] tables, int i, TableInputMeta tii ) {
    String fromstepname =
      BaseMessages.getString( PKG, "Spoon.RipDB.Monitor.FromStep.Name" ) + tables[i] + "]";


    String fromstepid = PluginRegistry.getInstance().getPluginId( StepPluginType.class, tii );
    StepMeta fromstep = new StepMeta( fromstepid, fromstepname, tii );
    fromstep.setLocation( 150, 100 );
    fromstep.setDraw( true );
    fromstep
      .setDescription( BaseMessages.getString( PKG, "Spoon.RipDB.Monitor.FromStep.Description" )
        + tables[i] + BaseMessages.getString( PKG, "Spoon.RipDB.Monitor.FromStep.Description2" )
        + sourceDbInfo + "]" );
    return fromstep;
  }

  private void setTransMetaNote( DatabaseMeta sourceDbInfo, DatabaseMeta targetDbInfo, String[] tables, int i,
                                 TransMeta transMeta ) {
    String note =
      BaseMessages.getString( PKG, "Spoon.RipDB.Monitor.Note1" )
        + tables[i] + BaseMessages.getString( PKG, "Spoon.RipDB.Monitor.Note2" ) + sourceDbInfo + "]"
        + Const.CR;
    note +=
      BaseMessages.getString( PKG, "Spoon.RipDB.Monitor.Note3" )
        + tables[i] + BaseMessages.getString( PKG, "Spoon.RipDB.Monitor.Note4" ) + targetDbInfo + "]";
    NotePadMeta ni = new NotePadMeta( note, 150, 10, -1, -1 );
    transMeta.addNote( ni );
  }

  private void setTransMetaDatabase( DatabaseMeta sourceDbInfo, DatabaseMeta targetDbInfo, TransMeta transMeta ) {
    transMeta.addDatabase( sourceDbInfo );
    transMeta.addDatabase( targetDbInfo );
  }

  @VisibleForTesting
  void setTransMetaFileNaming( RepositoryDirectoryInterface repdir, String directory,
                                       DatabaseMeta sourceDbInfo, DatabaseMeta targetDbInfo, String[] tables, int i,
                                       TransMeta transMeta ) {
    String transname =
      BaseMessages.getString( PKG, "Spoon.RipDB.Monitor.Transname1" )
        + sourceDbInfo + "].[" + tables[i]
        + BaseMessages.getString( PKG, "Spoon.RipDB.Monitor.Transname2" ) + targetDbInfo + "]";

    if ( repdir != null ) {
      transMeta.setRepositoryDirectory( repdir );
      transMeta.setName( transname );
    } else {
      transMeta.setFilename( Const.createFilename( directory, transname, "."
        + Const.STRING_TRANS_DEFAULT_EXT ) );
    }
  }

  public boolean isDefaultJobName( String name ) {
    if ( !name.startsWith( Spoon.STRING_JOB ) ) {
      return false;
    }

    // see if there are only digits behind the job...
    // This will detect:
    // "Job"
    // "Job "
    // "Job 1"
    // "Job 2"
    // ...
    for ( int i = Spoon.STRING_JOB.length() + 1; i < name.length(); i++ ) {
      if ( !Character.isDigit( name.charAt( i ) ) ) {
        return false;
      }
    }
    return true;
  }

  public JobGraph findJobGraphOfJob( JobMeta jobMeta ) {
    // Now loop over the entries in the tab-map
    for ( TabMapEntry mapEntry : spoon.delegates.tabs.getTabs() ) {
      if ( mapEntry.getObject() instanceof JobGraph ) {
        JobGraph jobGraph = (JobGraph) mapEntry.getObject();
        if ( jobGraph.getMeta().equals( jobMeta ) ) {
          return jobGraph;
        }
      }
    }
    return null;
  }

  /**
   * Add a job to the job map
   *
   * @param jobMeta
   *          the job to add to the map
   * @return true if the job was added
   */
  public boolean addJob( JobMeta jobMeta ) {
    int index = getJobList().indexOf( jobMeta );
    if ( index < 0 ) {
      getJobList().add( jobMeta );
      return true;
    } else {
      /*
       * ShowMessageDialog dialog = new ShowMessageDialog(spoon.getShell(), SWT.OK | SWT.ICON_INFORMATION,
       * BaseMessages.getString(PKG, "Spoon.Dialog.JobAlreadyLoaded.Title"), "'" + jobMeta.toString() + "'" + Const.CR +
       * Const.CR + BaseMessages.getString(PKG, "Spoon.Dialog.JobAlreadyLoaded.Message")); dialog.setTimeOut(6);
       * dialog.open();
       */
      return false;
    }

  }

  /**
   * @param jobMeta
   *          the transformation to close, make sure it's ok to dispose of it BEFORE you call this.
   */
  public void closeJob( JobMeta jobMeta ) {
    // Close the associated tabs...
    //
    TabMapEntry entry = getSpoon().delegates.tabs.findTabMapEntry( jobMeta );
    if ( entry != null ) {
      getSpoon().delegates.tabs.removeTab( entry );
    }

    // Also remove it from the item from the jobMap
    // Otherwise it keeps showing up in the objects tree
    //
    int index = getJobList().indexOf( jobMeta );
    while ( index >= 0 ) {
      getJobList().remove( index );
      index = getJobList().indexOf( jobMeta );
    }

    getSpoon().refreshTree();
    getSpoon().enableMenus();
  }

  protected Spoon getSpoon() {
    return this.spoon;
  }

  public void addJobGraph( JobMeta jobMeta ) {
    boolean added = addJob( jobMeta );
    if ( added ) {
      // See if there already is a tab for this graph with the short default name.
      // If there is, set that one to show the location as well.
      // If not, simply add it without
      // If no, add it
      // If yes, select that tab
      //
      boolean showLocation = false;
      boolean addTab = true;
      String tabName = spoon.delegates.tabs.makeTabName( jobMeta, false );

      TabMapEntry tabEntry = spoon.delegates.tabs.findTabMapEntry( tabName, ObjectType.JOB_GRAPH );
      if ( tabEntry != null ) {
        // We change the already loaded job to also show the location.
        //
        showLocation = true;
        tabEntry.setShowingLocation( true );
        String newTabName = spoon.delegates.tabs.makeTabName( tabEntry.getObject().getMeta(), true );
        tabEntry.getTabItem().setText( newTabName );

        // Try again, including the location of the object...
        //
        tabName = spoon.delegates.tabs.makeTabName( jobMeta, true );
        tabEntry = spoon.delegates.tabs.findTabMapEntry( tabName, ObjectType.JOB_GRAPH );
        if ( tabEntry != null ) {
          // Already loaded, simply select the tab item in question...
          //
          addTab = false;
        }
      }

      if ( addTab ) {
        JobGraph jobGraph = new JobGraph( spoon.tabfolder.getSwtTabset(), spoon, jobMeta );

        PropsUI props = PropsUI.getInstance();

        if ( tabName.length() >= getMaxTabLength() ) {
          tabName = new StringBuilder().append( tabName.substring( 0, getMaxTabLength() ) ).append( "\u2026" ).toString();
        }
        TabItem tabItem = new TabItem( spoon.tabfolder, tabName, tabName, props.getSashWeights() );
        String toolTipText =
          BaseMessages.getString( PKG, "Spoon.TabJob.Tooltip", spoon.delegates.tabs.makeTabName(
            jobMeta, showLocation ) );
        if ( !Utils.isEmpty( jobMeta.getFilename() ) ) {
          toolTipText += Const.CR + Const.CR + jobMeta.getFilename();
        }
        tabItem.setToolTipText( toolTipText );
        tabItem.setImage( GUIResource.getInstance().getImageJobGraph() );
        tabItem.setControl( jobGraph );

        // OK, also see if we need to open a new history window.
        if ( jobMeta.getJobLogTable().getDatabaseMeta() != null
          && !Utils.isEmpty( jobMeta.getJobLogTable().getTableName() ) ) {
          jobGraph.addAllTabs();
          jobGraph.extraViewTabFolder.setSelection( jobGraph.jobHistoryDelegate.getJobHistoryTab() );
        }

        String versionLabel = jobMeta.getObjectRevision() == null ? null : jobMeta.getObjectRevision().getName();

        tabEntry =
          new TabMapEntry(
            tabItem, jobMeta.getFilename(), jobMeta.getName(), jobMeta.getRepositoryDirectory(), versionLabel,
            jobGraph, ObjectType.JOB_GRAPH, jobMeta.getVariable( Spoon.CONNECTION ) );
        tabEntry.setShowingLocation( showLocation );

        spoon.delegates.tabs.addTab( tabEntry );
      }

      int idx = spoon.tabfolder.indexOf( tabEntry.getTabItem() );

      // keep the focus on the graph
      spoon.tabfolder.setSelected( idx );

      spoon.setUndoMenu( jobMeta );
      spoon.enableMenus();
    } else {
      TabMapEntry tabEntry = spoon.delegates.tabs.findTabMapEntry( jobMeta );

      if ( tabEntry != null ) {
        int idx = spoon.tabfolder.indexOf( tabEntry.getTabItem() );

        // keep the focus on the graph
        spoon.tabfolder.setSelected( idx );

        // keep the focus on the graph
        spoon.tabfolder.setSelected( idx );
        spoon.setUndoMenu( jobMeta );
        spoon.enableMenus();
      }
    }
  }

  /*
   * private void addJobLog(JobMeta jobMeta) { // See if there already is a tab for this log // If no, add it // If yes,
   * select that tab // String tabName = spoon.delegates.tabs.makeJobLogTabName(jobMeta); TabItem tabItem =
   * spoon.delegates.tabs.findTabItem(tabName, TabMapEntry.OBJECT_TYPE_JOB_LOG); if (tabItem == null) { JobLog jobLog =
   * new JobLog(spoon.tabfolder.getSwtTabset(), spoon, jobMeta); tabItem = new TabItem(spoon.tabfolder, tabName,
   * tabName); tabItem.setText(tabName); tabItem.setToolTipText(BaseMessages.getString(PKG,
   * "Spoon.Title.ExecLogJobView.Tooltip", spoon.delegates.tabs .makeJobGraphTabName(jobMeta)));
   * tabItem.setControl(jobLog);
   *
   * // If there is an associated history window, we want to keep that // one up-to-date as well. // JobHistory
   * jobHistory = findJobHistoryOfJob(jobMeta); TabItem historyItem =
   * spoon.delegates.tabs.findTabItem(spoon.delegates.tabs.makeJobHistoryTabName(jobMeta),
   * TabMapEntry.OBJECT_TYPE_JOB_HISTORY);
   *
   * if (jobHistory != null && historyItem != null) { JobHistoryRefresher jobHistoryRefresher = new
   * JobHistoryRefresher(historyItem, jobHistory); spoon.tabfolder.addListener(jobHistoryRefresher); //
   * jobLog.setJobHistoryRefresher(jobHistoryRefresher); }
   *
   * spoon.delegates.tabs.addTab(new TabMapEntry(tabItem, tabName, jobLog, TabMapEntry.OBJECT_TYPE_JOB_LOG)); } int idx
   * = spoon.tabfolder.indexOf(tabItem); spoon.tabfolder.setSelected(idx); }
   */

  public List<JobMeta> getJobList() {
    return jobMap;
  }

  public JobMeta getJob( String name ) {
    TabMapEntry entry = spoon.delegates.tabs.findTabMapEntry( name, ObjectType.JOB_GRAPH );
    if ( entry != null ) {
      return (JobMeta) entry.getObject().getManagedObject();
    }

    // TODO: remove part below
    //
    for ( JobMeta jobMeta : jobMap ) {
      if ( name != null && name.equals( jobMeta.getName() ) ) {
        return jobMeta;
      }
    }
    return null;
  }

  public JobMeta[] getLoadedJobs() {
    return jobMap.toArray( new JobMeta[jobMap.size()] );
  }

  public void redoJobAction( JobMeta jobMeta, TransAction transAction ) {
    switch ( transAction.getType() ) {
    //
    // NEW
    //
      case TransAction.TYPE_ACTION_NEW_JOB_ENTRY:
        // re-delete the entry at correct location:
        JobEntryCopy[] si = (JobEntryCopy[]) transAction.getCurrent();
        int[] idx = transAction.getCurrentIndex();
        for ( int i = 0; i < idx.length; i++ ) {
          jobMeta.addJobEntry( idx[i], si[i] );
        }
        spoon.refreshTree();
        spoon.refreshGraph();
        break;

      case TransAction.TYPE_ACTION_NEW_NOTE:
        // re-insert the note at correct location:
        NotePadMeta[] ni = (NotePadMeta[]) transAction.getCurrent();
        idx = transAction.getCurrentIndex();
        for ( int i = 0; i < idx.length; i++ ) {
          jobMeta.addNote( idx[i], ni[i] );
        }
        spoon.refreshTree();
        spoon.refreshGraph();
        break;

      case TransAction.TYPE_ACTION_NEW_JOB_HOP:
        // re-insert the hop at correct location:
        JobHopMeta[] hi = (JobHopMeta[]) transAction.getCurrent();
        idx = transAction.getCurrentIndex();
        for ( int i = 0; i < idx.length; i++ ) {
          jobMeta.addJobHop( idx[i], hi[i] );
        }
        spoon.refreshTree();
        spoon.refreshGraph();
        break;

      //
      // DELETE
      //
      case TransAction.TYPE_ACTION_DELETE_JOB_ENTRY:
        // re-remove the entry at correct location:
        idx = transAction.getCurrentIndex();
        for ( int i = idx.length - 1; i >= 0; i-- ) {
          jobMeta.removeJobEntry( idx[i] );
        }
        spoon.refreshTree();
        spoon.refreshGraph();
        break;

      case TransAction.TYPE_ACTION_DELETE_NOTE:
        // re-remove the note at correct location:
        idx = transAction.getCurrentIndex();
        for ( int i = idx.length - 1; i >= 0; i-- ) {
          jobMeta.removeNote( idx[i] );
        }
        spoon.refreshTree();
        spoon.refreshGraph();
        break;

      case TransAction.TYPE_ACTION_DELETE_JOB_HOP:
        // re-remove the hop at correct location:
        idx = transAction.getCurrentIndex();
        for ( int i = idx.length - 1; i >= 0; i-- ) {
          jobMeta.removeJobHop( idx[i] );
        }
        spoon.refreshTree();
        spoon.refreshGraph();
        break;

      //
      // CHANGE
      //

      // We changed a step : undo this...
      case TransAction.TYPE_ACTION_CHANGE_JOB_ENTRY:
        // replace with "current" version.
        for ( int i = 0; i < transAction.getCurrent().length; i++ ) {
          JobEntryCopy copy = (JobEntryCopy) ( (JobEntryCopy) ( transAction.getCurrent()[i] ) ).clone_deep();
          jobMeta.getJobEntry( transAction.getCurrentIndex()[i] ).replaceMeta( copy );
        }
        spoon.refreshTree();
        spoon.refreshGraph();
        break;

      // We changed a note : undo this...
      case TransAction.TYPE_ACTION_CHANGE_NOTE:
        // Delete & re-insert
        ni = (NotePadMeta[]) transAction.getCurrent();
        idx = transAction.getCurrentIndex();

        for ( int i = 0; i < idx.length; i++ ) {
          jobMeta.removeNote( idx[i] );
          jobMeta.addNote( idx[i], ni[i] );
        }
        spoon.refreshTree();
        spoon.refreshGraph();
        break;

      // We changed a hop : undo this...
      case TransAction.TYPE_ACTION_CHANGE_JOB_HOP:
        // Delete & re-insert
        hi = (JobHopMeta[]) transAction.getCurrent();
        idx = transAction.getCurrentIndex();

        for ( int i = 0; i < idx.length; i++ ) {
          jobMeta.removeJobHop( idx[i] );
          jobMeta.addJobHop( idx[i], hi[i] );
        }
        spoon.refreshTree();
        spoon.refreshGraph();
        break;

      //
      // CHANGE POSITION
      //
      case TransAction.TYPE_ACTION_POSITION_JOB_ENTRY:
        // Find the location of the step:
        idx = transAction.getCurrentIndex();
        Point[] p = transAction.getCurrentLocation();
        for ( int i = 0; i < p.length; i++ ) {
          JobEntryCopy entry = jobMeta.getJobEntry( idx[i] );
          entry.setLocation( p[i] );
        }
        spoon.refreshGraph();
        break;

      case TransAction.TYPE_ACTION_POSITION_NOTE:
        idx = transAction.getCurrentIndex();
        Point[] curr = transAction.getCurrentLocation();
        for ( int i = 0; i < idx.length; i++ ) {
          NotePadMeta npi = jobMeta.getNote( idx[i] );
          npi.setLocation( curr[i] );
        }
        spoon.refreshGraph();
        break;

      default:
        break;
    }
  }

  public void undoJobAction( JobMeta jobMeta, TransAction transAction ) {
    switch ( transAction.getType() ) {
    // We created a new entry : undo this...
      case TransAction.TYPE_ACTION_NEW_JOB_ENTRY:
        // Delete the entry at correct location:
        int[] idx = transAction.getCurrentIndex();
        for ( int i = idx.length - 1; i >= 0; i-- ) {
          jobMeta.removeJobEntry( idx[i] );
        }
        spoon.refreshTree();
        spoon.refreshGraph();
        break;

      // We created a new note : undo this...
      case TransAction.TYPE_ACTION_NEW_NOTE:
        // Delete the note at correct location:
        idx = transAction.getCurrentIndex();
        for ( int i = idx.length - 1; i >= 0; i-- ) {
          jobMeta.removeNote( idx[i] );
        }
        spoon.refreshTree();
        spoon.refreshGraph();
        break;

      // We created a new hop : undo this...
      case TransAction.TYPE_ACTION_NEW_JOB_HOP:
        // Delete the hop at correct location:
        idx = transAction.getCurrentIndex();
        for ( int i = idx.length - 1; i >= 0; i-- ) {
          jobMeta.removeJobHop( idx[i] );
        }
        spoon.refreshTree();
        spoon.refreshGraph();
        break;

      //
      // DELETE
      //

      // We delete an entry : undo this...
      case TransAction.TYPE_ACTION_DELETE_JOB_ENTRY:
        // un-Delete the entry at correct location: re-insert
        JobEntryCopy[] ce = (JobEntryCopy[]) transAction.getCurrent();
        idx = transAction.getCurrentIndex();
        for ( int i = 0; i < ce.length; i++ ) {
          jobMeta.addJobEntry( idx[i], ce[i] );
        }
        spoon.refreshTree();
        spoon.refreshGraph();
        break;

      // We delete new note : undo this...
      case TransAction.TYPE_ACTION_DELETE_NOTE:
        // re-insert the note at correct location:
        NotePadMeta[] ni = (NotePadMeta[]) transAction.getCurrent();
        idx = transAction.getCurrentIndex();
        for ( int i = 0; i < idx.length; i++ ) {
          jobMeta.addNote( idx[i], ni[i] );
        }
        spoon.refreshTree();
        spoon.refreshGraph();
        break;

      // We deleted a new hop : undo this...
      case TransAction.TYPE_ACTION_DELETE_JOB_HOP:
        // re-insert the hop at correct location:
        JobHopMeta[] hi = (JobHopMeta[]) transAction.getCurrent();
        idx = transAction.getCurrentIndex();
        for ( int i = 0; i < hi.length; i++ ) {
          jobMeta.addJobHop( idx[i], hi[i] );
        }
        spoon.refreshTree();
        spoon.refreshGraph();
        break;

      //
      // CHANGE
      //

      // We changed a job entry: undo this...
      case TransAction.TYPE_ACTION_CHANGE_JOB_ENTRY:
        // Delete the current job entry, insert previous version.
        for ( int i = 0; i < transAction.getPrevious().length; i++ ) {
          JobEntryCopy copy = (JobEntryCopy) ( (JobEntryCopy) transAction.getPrevious()[i] ).clone();
          jobMeta.getJobEntry( transAction.getCurrentIndex()[i] ).replaceMeta( copy );

        }
        spoon.refreshTree();
        spoon.refreshGraph();
        break;

      // We changed a note : undo this...
      case TransAction.TYPE_ACTION_CHANGE_NOTE:
        // Delete & re-insert
        NotePadMeta[] prev = (NotePadMeta[]) transAction.getPrevious();
        idx = transAction.getCurrentIndex();
        for ( int i = 0; i < idx.length; i++ ) {
          jobMeta.removeNote( idx[i] );
          jobMeta.addNote( idx[i], prev[i] );
        }
        spoon.refreshTree();
        spoon.refreshGraph();
        break;

      // We changed a hop : undo this...
      case TransAction.TYPE_ACTION_CHANGE_JOB_HOP:
        // Delete & re-insert
        JobHopMeta[] prevHops = (JobHopMeta[]) transAction.getPrevious();
        idx = transAction.getCurrentIndex();
        for ( int i = 0; i < idx.length; i++ ) {
          jobMeta.removeJobHop( idx[i] );
          jobMeta.addJobHop( idx[i], prevHops[i] );
        }
        spoon.refreshTree();
        spoon.refreshGraph();
        break;

      //
      // POSITION
      //

      // The position of a step has changed: undo this...
      case TransAction.TYPE_ACTION_POSITION_JOB_ENTRY:
        // Find the location of the step:
        idx = transAction.getCurrentIndex();
        Point[] p = transAction.getPreviousLocation();
        for ( int i = 0; i < p.length; i++ ) {
          JobEntryCopy entry = jobMeta.getJobEntry( idx[i] );
          entry.setLocation( p[i] );
        }
        spoon.refreshGraph();
        break;

      // The position of a note has changed: undo this...
      case TransAction.TYPE_ACTION_POSITION_NOTE:
        idx = transAction.getCurrentIndex();
        Point[] prevLoc = transAction.getPreviousLocation();
        for ( int i = 0; i < idx.length; i++ ) {
          NotePadMeta npi = jobMeta.getNote( idx[i] );
          npi.setLocation( prevLoc[i] );
        }
        spoon.refreshGraph();
        break;

      default:
        break;
    }
  }

  public void executeJob( JobMeta jobMeta, boolean local, boolean remote, Date replayDate, boolean safe,
    String startCopyName, int startCopyNr ) throws KettleException {

    if ( jobMeta == null ) {
      return;
    }

    JobExecutionConfiguration executionConfiguration = spoon.getJobExecutionConfiguration();

    // Remember the variables set previously
    //
    Object[] data = spoon.variables.getData();
    String[] fields = spoon.variables.getRowMeta().getFieldNames();
    Map<String, String> variableMap = new HashMap<>();
    for ( int idx = 0; idx < fields.length; idx++ ) {
      variableMap.put( fields[idx], data[idx].toString() );
    }

    executionConfiguration.setVariables( variableMap );
    executionConfiguration.getUsedVariables( jobMeta );
    executionConfiguration.setReplayDate( replayDate );
    executionConfiguration.setRepository( spoon.rep );
    executionConfiguration.setSafeModeEnabled( safe );
    executionConfiguration.setStartCopyName( startCopyName );
    executionConfiguration.setStartCopyNr( startCopyNr );

    executionConfiguration.getUsedArguments( jobMeta, spoon.getArguments(), spoon.getMetaStore() );
    executionConfiguration.setLogLevel( DefaultLogLevel.getLogLevel() );

    JobExecutionConfigurationDialog dialog = newJobExecutionConfigurationDialog( executionConfiguration, jobMeta );

    if ( !jobMeta.isShowDialog() || dialog.open() ) {

      JobGraph jobGraph = spoon.getActiveJobGraph();
      jobGraph.jobLogDelegate.addJobLog();

      // Set the variables that where specified...
      //
      for ( String varName : executionConfiguration.getVariables().keySet() ) {
        String varValue = executionConfiguration.getVariables().get( varName );
        jobMeta.setVariable( varName, varValue );
      }

      // Set and activate the parameters...
      //
      for ( String paramName : executionConfiguration.getParams().keySet() ) {
        String paramValue = executionConfiguration.getParams().get( paramName );
        jobMeta.setParameterValue( paramName, paramValue );
      }
      jobMeta.activateParameters();

      // Set the log level
      //
      if ( executionConfiguration.getLogLevel() != null ) {
        jobMeta.setLogLevel( executionConfiguration.getLogLevel() );
      }

      // Set the start step name
      //
      if ( executionConfiguration.getStartCopyName() != null ) {
        jobMeta.setStartCopyName( executionConfiguration.getStartCopyName() );
      }

      // Set the run options
      //
      jobMeta.setClearingLog( executionConfiguration.isClearingLog() );
      jobMeta.setSafeModeEnabled( executionConfiguration.isSafeModeEnabled() );
      jobMeta.setExpandingRemoteJob( executionConfiguration.isExpandingRemoteJob() );
      jobMeta.setGatheringMetrics( executionConfiguration.isGatheringMetrics() );

      ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.SpoonJobMetaExecutionStart.id, jobMeta );
      ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.SpoonJobExecutionConfiguration.id,
          executionConfiguration );

      try {
        ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.SpoonTransBeforeStart.id, new Object[] {
          executionConfiguration, jobMeta, jobMeta, spoon.getRepository()
        } );
      } catch ( KettleException e ) {
        log.logError( e.getMessage(), jobMeta.getFilename() );
        return;
      }

      if ( !executionConfiguration.isExecutingLocally() && !executionConfiguration.isExecutingRemotely() ) {
        if ( jobMeta.hasChanged() ) {
          jobGraph.showSaveFileMessage();
        }
      }

      // Is this a local execution?
      //
      if ( executionConfiguration.isExecutingLocally() ) {
        jobGraph.startJob( executionConfiguration );
      } else if ( executionConfiguration.isExecutingRemotely() ) {
        // Executing remotely
        // Check if jobMeta has changed
        jobGraph.handleJobMetaChanges( jobMeta );

        // Activate the parameters, turn them into variables...
        // jobMeta.hasChanged()
        jobMeta.activateParameters();

        if ( executionConfiguration.getRemoteServer() != null ) {
          Job.sendToSlaveServer( jobMeta, executionConfiguration, spoon.rep, spoon.getMetaStore() );
          spoon.delegates.slaves.addSpoonSlave( executionConfiguration.getRemoteServer() );
        } else {
          MessageBox mb = new MessageBox( spoon.getShell(), SWT.OK | SWT.ICON_ERROR );
          mb.setMessage( BaseMessages.getString( PKG, "Spoon.Dialog.NoRemoteServerSpecified.Message" ) );
          mb.setText( BaseMessages.getString( PKG, "Spoon.Dialog.NoRemoteServerSpecified.Title" ) );
          mb.open();
        }
      }
    }
  }

  @VisibleForTesting
  JobExecutionConfigurationDialog newJobExecutionConfigurationDialog( JobExecutionConfiguration executionConfiguration, JobMeta jobMeta ) {
    return new JobExecutionConfigurationDialog( spoon.getShell(), executionConfiguration, jobMeta );
  }
}
