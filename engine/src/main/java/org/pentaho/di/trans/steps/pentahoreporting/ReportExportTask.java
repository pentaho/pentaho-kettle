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

package org.pentaho.di.trans.steps.pentahoreporting;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.layout.output.ReportProcessor;
import org.pentaho.reporting.engine.classic.core.modules.gui.common.StatusListener;
import org.pentaho.reporting.engine.classic.core.modules.gui.common.StatusType;
import org.pentaho.reporting.engine.classic.core.modules.gui.commonswing.SwingGuiContext;
import org.pentaho.reporting.libraries.base.util.IOUtils;
import org.pentaho.reporting.libraries.base.util.Messages;
import org.pentaho.reporting.libraries.base.util.ObjectUtilities;

public abstract class ReportExportTask implements Runnable {
  protected static final Log logger = LogFactory.getLog( ReportExportTask.class );

  public static final String BASE_RESOURCE_CLASS = "org.pentaho.di.trans.steps.pentahoreporting.messages.messages"; //$NON-NLS-1$

  /**
   * Provides access to externalized strings
   */
  private Messages messages;

  protected MasterReport report;
  protected StatusListener statusListener;
  protected Boolean createParentFolder;
  protected String targetPath;
  protected FileObject targetFile;

  /**
   * Creates a new PDF export task.
   */
  public ReportExportTask( final MasterReport report, final SwingGuiContext swingGuiContext, String targetPath,
      Boolean createParentFolder ) {
    if ( report == null ) {
      throw new NullPointerException( "ReportExportTask(..): Report parameter cannot be null" );
    }

    this.report = report;
    this.statusListener = swingGuiContext.getStatusListener();

    // Check if the current Locale is supported:
    // If not, use the default (US) locale.
    Locale locale = swingGuiContext.getLocale();
    try {
      ResourceBundle.getBundle( BASE_RESOURCE_CLASS, swingGuiContext.getLocale() );
    } catch ( MissingResourceException e ) {
      locale = Locale.US;
    }

    this.messages =
        new Messages( locale, BASE_RESOURCE_CLASS,
                ObjectUtilities.getClassLoader( ReportExportTask.class ) );

    this.targetPath = targetPath;
    this.createParentFolder = createParentFolder;
  }

  /**
   * When an object implementing interface <code>Runnable</code> is used to create a thread, starting the thread causes
   * the object's <code>run</code> method to be called in that separately executing thread.
   * <p/>
   * The general contract of the method <code>run</code> is that it may take any action whatsoever.
   *
   * @see Thread#run()
   */
  public void run() {
    try {
      targetFile = KettleVFS.getFileObject( targetPath );
      if ( targetFile.exists() ) {
        if ( !targetFile.delete() ) {
          throw new ReportProcessingException( messages.getErrorString( "ReportExportTask.ERROR_0001_TARGET_EXISTS" ) ); //$NON-NLS-1$
        }
      }

      if ( createParentFolder ) {
        targetFile.getParent().createFolder();
      }

      execute();
    } catch ( Exception ex ) {
      statusListener.setStatus( StatusType.ERROR, messages.getString( "ReportExportTask.USER_EXPORT_FAILED" ), ex ); //$NON-NLS-1$
      logger.error( "Failed" ); //$NON-NLS-1$
    }
  }

  protected void execute() throws Exception {
    BufferedOutputStream fout = null;
    ReportProcessor reportProcessor = null;
    try {
      fout = new BufferedOutputStream( targetFile.getContent().getOutputStream() );
      reportProcessor = createReportProcessor( fout );
      reportProcessor.processReport();
      statusListener.setStatus( StatusType.INFORMATION, messages.getString( "ReportExportTask.USER_EXPORT_COMPLETE" ), //$NON-NLS-1$
        null );
      reportProcessor.close();
      try {
        fout.close();
      } catch ( Exception ex ) {
      }
    } catch ( Exception ex ) {
      try {
        reportProcessor.close();
      } catch ( Exception ex2 ) {
      }
      try {
        fout.close();
      } catch ( Exception ex2 ) {
      }
      try {
        if ( targetFile.exists() && !targetFile.delete() ) {
          logger.warn( "Unable to delete incomplete export: " + targetFile ); //$NON-NLS-1$
        }
      } catch ( Exception ex2 ) {
        logger.warn( "Unable to delete incomplete export: " + targetFile ); //$NON-NLS-1$
      }
      throw ex;
    }
  }

  protected abstract ReportProcessor createReportProcessor( OutputStream fout ) throws Exception;

  protected String getSuffix( final String filename ) {
    final String suffix = IOUtils.getInstance().getFileExtension( filename );
    if ( suffix.length() == 0 ) {
      return ""; //$NON-NLS-1$
    }
    return suffix.substring( 1 );
  }
}
