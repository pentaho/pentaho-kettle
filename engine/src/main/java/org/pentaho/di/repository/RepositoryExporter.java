//CHECKSTYLE:EmptyBlock:OFF
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

package org.pentaho.di.repository;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.ProgressNullMonitorListener;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.imp.ImportRules;
import org.pentaho.di.imp.rule.ImportValidationFeedback;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mapping.MappingMeta;

/**
 * <p>This class is used to read repository, load jobs and transformations, and export them into xml file.
 *  Xml file will be overwrite. In case of not success export this file will be deleted.</p>
 *
 * <p>It is possible to set some export rules (similar as import rules). In case of export rule violation - whole export
 * process will be treated as unsuccessful, export xml file will be deleted.</p>
 *
 * <p>In case of during export some item form repository will be failed to load in case of missing step plugin
 * etc - same as before - whole export process will be treated as failed, export xml file will be deleted.</p>
 *
 * <p>Internally this implementation uses 2 types of output xml writers - actual and null implementation. When during
 * export some item violates export rule - output xml file will be deleted, actual writer implementation will be
 * replaced by null (nothing-to-do) implementation. Monitor current status will be replaced by error message with number
 * of rule violation errors.</p>
 *
 * <p>Monitor's progress bar is not the actual progress, as we don't actually know the amount of real export. In current
 * implementation we avoid to discover repository for total amount of export work to show more sophisticated progress
 * bar. Export may be canceled using this monitor cancel action. Using monitor cancel is only the way to interrupt
 * running export without exception. In case of export is canceled - output export file will not be created.</p>
 *
 *  @see ProgressMonitorListener
 *  @see IRepositoryExporter
 *  @see IRepositoryExporterFeedback
 */
public class RepositoryExporter implements IRepositoryExporterFeedback {

  private static Class<?> PKG = RepositoryExporter.class;

  private Repository repository;
  private LogChannelInterface log;

  private ImportRules importRules;
  private boolean hasRules = false;

  private List<ExportFeedback> feedbackList = new ArrayList<ExportFeedback>();
  private boolean rulesViolation = false;

  /**
   * @param repository
   */
  public RepositoryExporter( Repository repository ) {
    this.log = repository.getLog();
    this.repository = repository;
    this.importRules = new ImportRules();
  }

  @Override
  public boolean isRulesViolation() {
    return rulesViolation;
  }

  @Override
  public void setImportRulesToValidate( ImportRules importRules ) {
    this.importRules = importRules;
    hasRules = ( importRules != null && !importRules.getRules().isEmpty() );
  }

  /**
   * <p>This implementation attempts to scan whole repository for all items according to export type. If we
   * have one or more export rules defined - it will <u>NOT throw exception on first export rule violation</u>.
   * Instead of it this method attempts to scan whole repository to get full picture how many items violates
   * export rules. This information is available as collection of feedbacks. To determine possible export rule
   * violations and not perform full collection scan for error feedbacks use isRuleViolation() call</p>
   *
   * @see #isRulesViolation()
   */
  @Override
  public List<ExportFeedback> exportAllObjectsWithFeedback( ProgressMonitorListener monitorOuter, String xmlFilename,
      RepositoryDirectoryInterface root, String exportType ) throws KettleException {
    return exportAllObjectsInternal( monitorOuter, xmlFilename, root, exportType, true );
  }

  /**
   * This implementation is backward compatible. This means if we have some export rules defined, and during
   * export one rule will be violated - we will throw exception and we will stop export.
   */
  @Override
  public void exportAllObjects( ProgressMonitorListener monitorOuter, String xmlFilename,
      RepositoryDirectoryInterface root, String exportType ) throws KettleException {
    exportAllObjectsInternal( monitorOuter, xmlFilename, root, exportType, false );
  }

  private synchronized List<ExportFeedback> exportAllObjectsInternal( ProgressMonitorListener monitorOuter,
      String xmlFilename, RepositoryDirectoryInterface root, String exportType, boolean feedback ) throws KettleException {
    this.feedbackList.clear();

    // deal with monitor
    ProgressMonitorDecorator monitor;
    if ( monitorOuter == null ) {
      monitor = new ProgressMonitorDecorator( new ProgressNullMonitorListener() );
    } else {
      monitor = new ProgressMonitorDecorator( monitorOuter );
    }

    monitor.beginTask( BaseMessages.getString( PKG, "Repository.Exporter.Monitor.BeginTask" ), 104 );

    FileObject output = KettleVFS.getFileObject( xmlFilename );

    ExportFeedback feed = new ExportFeedback();
    feed.setItemName( BaseMessages.getString( PKG, "Repository.Exporter.Feedback.CreateExportFile", xmlFilename ) );
    feed.setSimpleString( true );
    this.feedbackList.add( feed );

    ExportWriter writer = null;
    try {
      // prepare export
      writer = new ExportWriter( output );
      monitor.worked( 4 );

      monitor.subTask( BaseMessages.getString( PKG, "Repository.Exporter.Monitor.ConnectToRepository" ) );

      root = ( ( null == root ) ? repository.loadRepositoryDirectoryTree() : root );
      ExportType type = ExportType.valueOf( exportType.toUpperCase() );

      switch ( type ) {
        case ALL: {
          exportTransformations( monitor, root, writer, feedback );
          monitor.worked( 50 );
          exportJobs( monitor, root, writer, feedback );
          monitor.worked( 50 );
          break;
        }
        case TRANS: {
          exportTransformations( monitor, root, writer, feedback );
          monitor.worked( 100 );
          break;
        }
        case JOBS: {
          exportJobs( monitor, root, writer, feedback );
          monitor.worked( 100 );
          break;
        }
        default: {
          // this will never happens
          throw new KettleException( "Unsupported export type: " + type );
        }
      }
      monitor.subTask( BaseMessages.getString( PKG, "Repository.Exporter.Monitor.SavingResultFile" ) );
    } finally {
      try {
        if ( writer != null ) {
          writer.close();
        }
      } catch ( Exception e ) {
        log.logDebug( BaseMessages.getString( PKG, "Repository.Exporter.Exception.CloseExportFile", xmlFilename ) );
      }
    }
    if ( monitor != null ) {
      monitor.done();
    }
    return this.feedbackList;
  }

  private void exportJobs( ProgressMonitorDecorator monitor, RepositoryDirectoryInterface dirTree, ExportWriter writer,
      boolean feedback ) throws KettleException {
    try {
      monitor.subTask( BaseMessages.getString( PKG, "Repository.Exporter.Monitor.StartJobsExport" ) );
      writer.openJob();

      // Loop over all the directory id's
      ObjectId[] dirids = dirTree.getDirectoryIDs();
      log.logDebug( BaseMessages.getString( PKG, "Repository.Exporter.Log.DirectoryGoing", dirids.length, dirTree
          .getPath() ) );

      for ( int d = 0; d < dirids.length; d++ ) {
        if ( monitor.isCanceled() ) {
          cancelMonitorAction( writer );
          break;
        }
        RepositoryDirectoryInterface repdir = dirTree.findDirectory( dirids[d] );
        String[] jobs = repository.getJobNames( dirids[d], false );
        log.logDebug( BaseMessages.getString( PKG, "Repository.Exporter.Log.FindJobs",
            jobs.length, repdir.getName() ) );

        String dirPath = repdir.getPath();

        for ( int i = 0; i < jobs.length; i++ ) {
          if ( monitor.isCanceled() ) {
            break;
          }
          monitor.subTask( BaseMessages.getString( PKG, "Repository.Exporter.Monitor.ExportingJob", jobs[i] ) );

          log.logDebug( BaseMessages.getString( PKG, "Repository.Exporter.Log.LoadingJob", dirPath, jobs[i] ) );

          JobMeta jobMeta = repository.loadJob( jobs[i], repdir, null, null ); // reads last version

          // Pass the repository along in order for us to do correct exports to XML of object references
          jobMeta.setRepository( repository );
          // Check file repository export
          convertFromFileRepository( jobMeta );

          List<ImportValidationFeedback> errors = this.validateObject( jobMeta, feedback );
          if ( errors.isEmpty() ) {
            writer.writeJob( jobMeta.getXML() + Const.CR );
          } else {
            log.logError( BaseMessages.getString( PKG, "Repository.Exporter.Log.JobRuleViolation", jobs[i], repdir ) );
            this.rulesViolation = true;
            monitor.registerRuleViolation();
            writer.registerRuleViolation();
          }
          // do we need any feedback on this action?
          if ( feedback ) {
            ExportFeedback fb = new ExportFeedback();
            fb.setType( ExportFeedback.Type.JOB );
            fb.setItemName( jobMeta.getName() );
            fb.setItemPath( dirPath );
            ExportFeedback.Status status =
                errors.isEmpty() ? ExportFeedback.Status.EXPORTED : ExportFeedback.Status.REJECTED;
            fb.setStatus( status );
            fb.setResult( errors );
            this.feedbackList.add( fb );
          }
        }
      }
    } catch ( Exception e ) {
      throw new KettleException( "Error while exporting repository jobs", e );
    } finally {
      writer.closeJob();
    }
  }

  private void cancelMonitorAction( ExportWriter writer ) throws IOException {
    // delete file
    writer.registerRuleViolation();
    // clear feedback list
    if ( feedbackList != null ) {
      feedbackList.clear();
    }
  }

  private List<ImportValidationFeedback> validateObject( Object subject, boolean boolFeedback ) throws KettleException {
    if ( !hasRules ) {
      return Collections.emptyList();
    }
    if ( !boolFeedback ) {
      // this is call from Pan, job Executor or somthing else - we should throw
      // exception if one or more export rules is viloated.
      RepositoryImporter.validateImportedElement( importRules, subject );
    }

    List<ImportValidationFeedback> feedback = importRules.verifyRules( subject );
    List<ImportValidationFeedback> errors = new ArrayList<ImportValidationFeedback>( feedback.size() );

    for ( ImportValidationFeedback res : feedback ) {
      if ( res.isError() ) {
        errors.add( res );
      }
    }
    return errors;
  }

  private void convertFromFileRepository( JobMeta jobMeta ) {

    if ( repository instanceof KettleFileRepository ) {

      KettleFileRepository fileRep = (KettleFileRepository) repository;

      // The id of the job is the filename.
      // Setting the filename also sets internal variables needed to load the trans/job referenced.
      //
      String jobMetaFilename = fileRep.calcFilename( jobMeta.getObjectId() );
      jobMeta.setFilename( jobMetaFilename );

      for ( JobEntryCopy copy : jobMeta.getJobCopies() ) {
        JobEntryInterface entry = copy.getEntry();
        if ( entry instanceof JobEntryTrans ) {
          // convert to a named based reference.
          //
          JobEntryTrans trans = (JobEntryTrans) entry;
          if ( trans.getSpecificationMethod() == ObjectLocationSpecificationMethod.FILENAME ) {
            try {
              TransMeta meta = trans.getTransMeta( repository, repository.getMetaStore(), jobMeta );
              FileObject fileObject = KettleVFS.getFileObject( meta.getFilename() );
              trans.setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
              trans.setFileName( null );
              trans.setTransname( meta.getName() );
              trans.setDirectory( Const.NVL( calcRepositoryDirectory( fileRep, fileObject ), "/" ) );
            } catch ( Exception e ) {
              log.logError( BaseMessages.getString( PKG, "Repository.Exporter.Log.UnableToLoadJobTrans", trans
                  .getName() ), e );
            }
          }
        }

        if ( entry instanceof JobEntryJob ) {
          // convert to a named based reference.
          //
          JobEntryJob jobEntryJob = (JobEntryJob) entry;
          if ( jobEntryJob.getSpecificationMethod() == ObjectLocationSpecificationMethod.FILENAME ) {
            try {
              JobMeta meta = jobEntryJob.getJobMeta( repository, repository.getMetaStore(), jobMeta );
              FileObject fileObject = KettleVFS.getFileObject( meta.getFilename() );
              jobEntryJob.setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
              jobEntryJob.setFileName( null );
              jobEntryJob.setJobName( meta.getName() );
              jobEntryJob.setDirectory( Const.NVL( calcRepositoryDirectory( fileRep, fileObject ), "/" ) );
            } catch ( Exception e ) {
              log.logError( BaseMessages.getString( PKG, "Repository.Exporter.Log.UnableToLoadJobJob", jobEntryJob
                  .getName() ), e );
            }
          }
        }
      }
    }
  }

  private void convertFromFileRepository( TransMeta transMeta ) {

    Object[] metaInjectObjectArray = new Object[4];
    metaInjectObjectArray[0] = transMeta;
    metaInjectObjectArray[1] = PKG;

    if ( repository instanceof KettleFileRepository ) {

      KettleFileRepository fileRep = (KettleFileRepository) repository;

      metaInjectObjectArray[2] = fileRep;

      // The id of the transformation is the relative filename.
      // Setting the filename also sets internal variables needed to load the trans/job referenced.
      //
      String transMetaFilename = fileRep.calcFilename( transMeta.getObjectId() );
      transMeta.setFilename( transMetaFilename );

      for ( StepMeta stepMeta : transMeta.getSteps() ) {
        if ( stepMeta.isMapping() ) {
          MappingMeta mappingMeta = (MappingMeta) stepMeta.getStepMetaInterface();

          // convert to a named based reference.
          //
          if ( mappingMeta.getSpecificationMethod() == ObjectLocationSpecificationMethod.FILENAME ) {
            try {
              TransMeta meta = MappingMeta.loadMappingMeta( mappingMeta, fileRep, fileRep.metaStore, transMeta );
              FileObject fileObject = KettleVFS.getFileObject( meta.getFilename() );
              mappingMeta.setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
              mappingMeta.setFileName( null );
              mappingMeta.setTransName( meta.getName() );
              mappingMeta.setDirectoryPath( Const.NVL( calcRepositoryDirectory( fileRep, fileObject ), "/" ) );
            } catch ( Exception e ) {
              log.logError( BaseMessages.getString( PKG, "Repository.Exporter.Log.UnableToLoadTransInMap", mappingMeta
                  .getName() ), e );
            }
          }
        }

        metaInjectObjectArray[3] = stepMeta;

        try {
          ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.RepositoryExporterPatchTransStep.id, metaInjectObjectArray );
        } catch ( KettleException ke ) {
          log.logError( ke.getMessage(), ke );
        }
      }
    }
  }

  private String calcRepositoryDirectory( KettleFileRepository fileRep, FileObject fileObject ) throws FileSystemException {
    String path = fileObject.getParent().getName().getPath();
    String baseDirectory = fileRep.getRepositoryMeta().getBaseDirectory();
    // Double check!
    //
    if ( path.startsWith( baseDirectory ) ) {
      return path.substring( baseDirectory.length() );
    } else {
      return path;
    }
  }

  private void exportTransformations( ProgressMonitorDecorator monitor, RepositoryDirectoryInterface dirTree,
      ExportWriter writer, boolean feedback ) throws KettleException {
    try {
      writer.openTrans();
      monitor.subTask( BaseMessages.getString( PKG, "Repository.Exporter.Monitor.StartTransExport" ) );

      // Loop over all the directory id's
      ObjectId[] dirids = dirTree.getDirectoryIDs();
      log.logDebug( BaseMessages.getString( PKG, "Repository.Exporter.Log.DirectoryGoing", dirids.length, dirTree
          .getPath() ) );
      for ( int d = 0; d < dirids.length; d++ ) {
        if ( monitor.isCanceled() ) {
          cancelMonitorAction( writer );
          break;
        }

        RepositoryDirectoryInterface repdir = dirTree.findDirectory( dirids[d] );
        String[] trans = repository.getTransformationNames( dirids[d], false );
        log.logDebug( BaseMessages.getString( PKG, "Repository.Exporter.Log.FindTrans",
            trans.length, repdir.getName() ) );

        String dirPath = repdir.getPath();

        for ( int i = 0; i < trans.length; i++ ) {
          if ( monitor.isCanceled() ) {
            break;
          }
          log.logDebug( BaseMessages
              .getString( PKG, "Repository.Exporter.Log.LoadingTransformation", dirPath, trans[i] ) );
          monitor.subTask( BaseMessages.getString( PKG,
              "Repository.Exporter.Monitor.ExportTransformation", trans[i] ) );
          TransMeta transMeta = repository.loadTransformation( trans[i], repdir, null, true, null ); // reads last //
                                                                                                     // version
          transMeta.setRepository( repository );
          convertFromFileRepository( transMeta );

          List<ImportValidationFeedback> errors = this.validateObject( transMeta, feedback );
          if ( errors.isEmpty() ) {
            writer.writeTrans( transMeta.getXML() + Const.CR );
          } else {
            log.logError( BaseMessages.getString( PKG,
                "Repository.Exporter.Log.TransRuleViolation", trans[i], repdir ) );
            this.rulesViolation = true;
            monitor.registerRuleViolation();
            writer.registerRuleViolation();
          }
          // do we need any feedback on this action?
          if ( feedback ) {
            ExportFeedback fb = new ExportFeedback();
            fb.setType( ExportFeedback.Type.TRANSFORMATION );
            fb.setItemName( transMeta.getName() );
            fb.setItemPath( dirPath );
            ExportFeedback.Status status =
                errors.isEmpty() ? ExportFeedback.Status.EXPORTED : ExportFeedback.Status.REJECTED;
            fb.setStatus( status );
            fb.setResult( errors );
            this.feedbackList.add( fb );
          }
        }
      }
    } catch ( Exception e ) {
      throw new KettleException( "Error while exporting repository transformations", e );
    } finally {
      writer.closeTrans();
    }
  }

  private class ExportWriter implements IExportWriter {

    private IExportWriter delegate;
    private FileObject writeTo;

    private boolean fileDeleted = false;

    ExportWriter( FileObject writeTo ) throws KettleException {
      this.writeTo = writeTo;
      this.delegate = new ExportStFileWriter( writeTo );
    }

    void registerRuleViolation() throws IOException {
      // close output streams
      this.delegate.close();
      // make attempt to delete file
      if ( !fileDeleted ) {
        fileDeleted = writeTo.delete();
      }
      // seems we are unable to delete file?
      if ( !fileDeleted ) {
        log.logDebug( BaseMessages.getString( PKG, "Repository.Exporter.Log.UnableToDeleteFile" ) );
      }
      // and we will not use real work writer anymore.
      this.delegate = new NullStFileWriter();
    }

    @Override
    public void writeJob( String str ) throws KettleException {
      delegate.writeJob( str );
    }

    @Override
    public void writeTrans( String str ) throws KettleException {
      delegate.writeTrans( str );
    }

    @Override
    public void closeJob() throws KettleException {
      delegate.closeJob();
    }

    @Override
    public void closeTrans() throws KettleException {
      delegate.closeTrans();
    }

    @Override
    public void close() throws IOException {
      delegate.close();
    }

    @Override
    public void openJob() throws KettleException {
      delegate.openJob();
    }

    @Override
    public void openTrans() throws KettleException {
      delegate.openTrans();
    }
  }

  interface IExportWriter {
    void writeJob( String str ) throws KettleException;

    void writeTrans( String str ) throws KettleException;

    void openJob() throws KettleException;

    void closeJob() throws KettleException;

    void openTrans() throws KettleException;

    void closeTrans() throws KettleException;

    void close() throws IOException;
  }

  // encapsulate common tag values
  enum XmlElements {
    REPO_START( "<repository>" + Const.CR + Const.CR ),
    REPO_END( "</repository>" + Const.CR + Const.CR ),
    JOBS_START( "<jobs>" + Const.CR ),
    JOBS_END( "</jobs>" + Const.CR ),
    TRANS_START( "<transformations>" + Const.CR ),
    TRANS_END( "</transformations>" + Const.CR );

    private String tag;

    XmlElements( String tag ) {
      this.tag = tag;
    }

    String getTag() {
      return tag;
    }
  }

  /**
   * Empty implementation if we do not want to write anymore.
   *
   */
  private class NullStFileWriter implements IExportWriter {

    NullStFileWriter() {
    }

    @Override
    public void writeJob( String str ) throws KettleException {
    }

    @Override
    public void writeTrans( String str ) throws KettleException {
    }

    @Override
    public void closeJob() throws KettleException {
    }

    @Override
    public void closeTrans() throws KettleException {
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void openJob() throws KettleException {
    }

    @Override
    public void openTrans() throws KettleException {
    }
  }

  /**
   * One day will be replaced by real StAX implementation. Actual write activity is delayed before real attempt to write
   * anything.
   *
   */
  private class ExportStFileWriter implements IExportWriter {

    private FileObject writeTo;
    private OutputStream os;
    private OutputStreamWriter out;

    private boolean start = false;
    private boolean trans = false;
    private boolean jobs = false;

    ExportStFileWriter( FileObject writeTo ) throws KettleException {
      this.writeTo = writeTo;
      // we will write even it is empty file
      write();
    }

    private void write() throws KettleException {
      if ( !start ) {
        this.prepareToWrite();
        this.writeXmlRoot();
        start = true;
      }
    }

    @Override
    public void openTrans() throws KettleException {
      try {
        if ( !trans ) {
          out.write( XmlElements.TRANS_START.getTag() );
          trans = true;
        }
      } catch ( IOException e ) {
        throw new KettleException( e );
      }
    }

    @Override
    public void writeTrans( String str ) throws KettleException {
      try {
        out.write( str );
      } catch ( IOException e ) {
        throw new KettleException( e );
      }
    }

    @Override
    public void closeTrans() throws KettleException {
      if ( trans ) {
        try {
          out.write( XmlElements.TRANS_END.getTag() );
        } catch ( IOException e ) {
          throw new KettleException( e );
        }
      }
    }

    @Override
    public void openJob() throws KettleException {
      try {
        if ( !jobs ) {
          out.write( XmlElements.JOBS_START.getTag() );
          jobs = true;
        }
      } catch ( IOException e ) {
        throw new KettleException( e );
      }
    }

    @Override
    public void writeJob( String str ) throws KettleException {
      try {
        out.write( str );
      } catch ( IOException e ) {
        throw new KettleException( e );
      }
    }

    public void closeJob() throws KettleException {
      if ( jobs ) {
        try {
          out.write( XmlElements.JOBS_END.getTag() );
        } catch ( IOException e ) {
          throw new KettleException( e );
        }
      }
    }

    private void prepareToWrite() throws KettleException {
      try {
        os = writeTo.getContent().getOutputStream();
        out = new OutputStreamWriter( os, Const.XML_ENCODING );
      } catch ( UnsupportedEncodingException e ) {
        throw new KettleException( e );
      } catch ( FileSystemException e ) {
        throw new KettleException( e );
      }
    }

    private void writeXmlRoot() throws KettleException {
      try {
        out.write( XMLHandler.getXMLHeader() );
        out.write( XmlElements.REPO_START.getTag() );
      } catch ( IOException e ) {
        throw new KettleException( e );
      }
    }

    public void close() throws IOException {
      if ( start && out != null ) {
        out.write( XmlElements.REPO_END.getTag() );
      }

      try {
        if ( out != null ) {
          out.close();
        }
      } catch ( IOException e ) {
      }
      try {
        if ( os != null ) {
          os.close();
        }
      } catch ( IOException e ) {
      }
    }
  }

  private class ProgressMonitorDecorator implements ProgressMonitorListener {
    private ProgressMonitorListener monitor;

    private boolean violation = false;
    private int vn = 0;

    /**
     * simple progress monitor
     *
     * @param monitor
     */
    ProgressMonitorDecorator( ProgressMonitorListener monitor ) {
      this.monitor = monitor;
    }

    @Override
    public void beginTask( String message, int nrWorks ) {
      monitor.beginTask( message, nrWorks );
    }

    @Override
    public void done() {
      monitor.done();
    }

    @Override
    public boolean isCanceled() {
      return monitor.isCanceled();
    }

    @Override
    public void subTask( String message ) {
      if ( !violation ) {
        monitor.subTask( message );
      } else {
        monitor.subTask( BaseMessages.getString( PKG, "Repository.Exporter.Monitor.ExportRulesViolated", vn ) );
      }
    }

    public void registerRuleViolation() {
      vn++;
      violation = true;
    }

    /**
     * This method should not be used directly in this calls, as we will use % implementation
     */
    @Override
    public void worked( int nrWorks ) {
      monitor.worked( nrWorks );
    }

    @Override
    public void setTaskName( String taskName ) {
      monitor.setTaskName( taskName );
    }
  }

  public enum ExportType {
    ALL, TRANS, JOBS;
  }
}
