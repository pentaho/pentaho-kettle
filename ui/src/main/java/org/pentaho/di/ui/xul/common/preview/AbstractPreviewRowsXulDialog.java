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

package org.pentaho.di.ui.xul.common.preview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.debug.BreakPointListener;
import org.pentaho.di.trans.debug.StepDebugMeta;
import org.pentaho.di.trans.debug.TransDebugMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulLoader;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.XulSettingsManager;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulProgressmeter;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.impl.XulEventHandler;

/**
 * User: gmoran Date: Jan 28, 2013
 */
public abstract class AbstractPreviewRowsXulDialog extends AbstractXulEventHandler {

  protected Object parent;

  private String xulFile;

  private XulDomContainer container;

  private XulRunner runner;

  protected XulDialog xulDialog;

  protected BindingFactory bf;

  protected LogChannel log;

  private BaseStepMeta meta = null;

  private int maxRows;

  private XulProgressmeter progressMeter;

  public AbstractPreviewRowsXulDialog( Object parent, BaseStepMeta stepMeta, int maxRows ) {

    this.xulFile = "org/pentaho/di/ui/xul/common/preview/xul/preview_rows.xul";
    this.parent = parent;
    this.meta = stepMeta;
    this.maxRows = maxRows;

    log = new LogChannel( "Row Preview" );

    try {
      initializeXul();
      progressMeter = (XulProgressmeter) document.getElementById( "progress" );
    } catch ( Exception e ) {
      log.logError( "Error initializing dialog...", e );
      throw new IllegalStateException( "Cannot load dialog due to error in initialization.", e );
    }
  }

  public void init() {

    final List<String> columns = new ArrayList<String>();
    final List<Object[]> data = new ArrayList<Object[]>();

    previewStep( data, columns );
    createPreviewRows( data, columns );

  }

  /**
   * TODO: This method should not be necessary once a XulTable can take bindings for creating xulcolumn definitions at
   * runtime and mapping the data to the columns.
   *
   * @param data
   * @param columns
   */
  protected abstract void createPreviewRows( List<Object[]> data, List<String> columns );

  /**
   * TODO: This method needs to communicate and control a UI wait status indicator (aka, progress monitor)
   *
   * @param data
   * @param columns
   */
  protected void previewStep( List<Object[]> data, List<String> columns ) {

    TransMeta previewMeta =
      TransPreviewFactory.generatePreviewTransformation( null, (StepMetaInterface) meta, "data_sync" );

    final Trans trans = new Trans( previewMeta );

    try {

      trans.prepareExecution( null );

      TransDebugMeta transDebugMeta = new TransDebugMeta( previewMeta );
      StepMeta stepMeta = previewMeta.findStep( "data_sync" );

      StepDebugMeta stepDebugMeta = new StepDebugMeta( stepMeta );
      stepDebugMeta.setReadingFirstRows( true );
      stepDebugMeta.setRowCount( maxRows );

      transDebugMeta.getStepDebugMetaMap().put( stepMeta, stepDebugMeta );
      transDebugMeta.addRowListenersToTransformation( trans );

      transDebugMeta.addBreakPointListers( new BreakPointListener() {
        public void breakPointHit( TransDebugMeta transDebugMeta, StepDebugMeta stepDebugMeta,
          RowMetaInterface rowBufferMeta, List<Object[]> rowBuffer ) {
          System.out.println( "break point hit...".concat( String.valueOf( stepDebugMeta.getRowCount() ) ) );
          trans.stopAll();
        }
      } );

      trans.startThreads();

      /*
       * if (previewMeta.getTransformationType() == TransformationType.Normal) { trans.waitUntilFinished(); }
       */

      int previousPct = 0;

      while ( !trans.isFinished() ) {
        // How many rows are done?
        int nrDone = 0;
        int nrTotal = 0;
        for ( StepDebugMeta debug : transDebugMeta.getStepDebugMetaMap().values() ) {
          nrDone += debug.getRowBuffer().size();
          nrTotal += debug.getRowCount();
        }

        int pct = 100 * nrDone / nrTotal;

        int worked = pct - previousPct;

        if ( worked > 0 ) {
          this.progressMeter.setValue( worked );
        }
        previousPct = pct;

        // Change the percentage...
        try {
          Thread.sleep( 500 );
        } catch ( InterruptedException e ) {
          // Ignore sleep interruption exception
        }

      }

      trans.stopAll();

      data.addAll( stepDebugMeta.getRowBuffer() );
      RowMetaInterface rowMeta = stepDebugMeta.getRowBufferMeta();

      for ( int i = 0; i < rowMeta.size(); i++ ) {
        ValueMetaInterface v = rowMeta.getValueMeta( i );
        columns.add( v.getName() );
      }

    } catch ( KettleException e ) {

      this.logError( "Data preview failed.", e );

    }

  }

  /**
   * The implementors of this method should call the sibling method initializeXul(XulLoder, BindingFactory, XulRunner,
   * parent) with the desired Xul implementation classes - SWT or Swing.
   *
   * @throws XulException
   */
  protected abstract void initializeXul() throws XulException;

  protected void initializeXul( XulLoader loader, BindingFactory bindingFactory, XulRunner runner, Object parent ) throws XulException {

    bf = bindingFactory;
    this.runner = runner;

    loader.registerClassLoader( getClass().getClassLoader() );
    loader.setSettingsManager( getSettingsManager() );
    loader.setOuterContext( parent );

    container = loader.loadXul( xulFile, getResourceBundle() );

    bf.setDocument( container.getDocumentRoot() );

    for ( XulEventHandler h : getEventHandlers() ) {
      container.addEventHandler( h );
    }

    this.runner.addContainer( container );

    // try and get the dialog
    xulDialog = (XulDialog) container.getDocumentRoot().getRootElement();
    runner.initialize();
  }

  public abstract XulSettingsManager getSettingsManager();

  public abstract ResourceBundle getResourceBundle();

  protected BindingFactory getBindingFactory() {
    return bf;
  }

  protected List<XulEventHandler> getEventHandlers() {
    return Collections.singletonList( (XulEventHandler) this );
  }

  public String getName() {
    return "handler";
  }

  // TODO: decide what to return here...
  public String open() {
    xulDialog.show();
    return null;
  }

  public void close() {
    xulDialog.hide();
  }

  public abstract void onAccept();

  public abstract void onCancel();

  protected abstract Class<?> getClassForMessages();

  public abstract void dispose();

  public boolean isBasic() {
    return log.isBasic();
  }

  public boolean isDetailed() {
    return log.isDetailed();
  }

  public boolean isDebug() {
    return log.isDebug();
  }

  public boolean isRowLevel() {
    return log.isRowLevel();
  }

  public void logMinimal( String message ) {
    log.logMinimal( message );
  }

  public void logMinimal( String message, Object... arguments ) {
    log.logMinimal( message, arguments );
  }

  public void logBasic( String message ) {
    log.logBasic( message );
  }

  public void logBasic( String message, Object... arguments ) {
    log.logBasic( message, arguments );
  }

  public void logDetailed( String message ) {
    log.logDetailed( message );
  }

  public void logDetailed( String message, Object... arguments ) {
    log.logDetailed( message, arguments );
  }

  public void logDebug( String message ) {
    log.logDebug( message );
  }

  public void logDebug( String message, Object... arguments ) {
    log.logDebug( message, arguments );
  }

  public void logRowlevel( String message ) {
    log.logRowlevel( message );
  }

  public void logRowlevel( String message, Object... arguments ) {
    log.logRowlevel( message, arguments );
  }

  public void logError( String message ) {
    log.logError( message );
  }

  public void logError( String message, Throwable e ) {

    log.logError( message, e );
  }

  public void logError( String message, Object... arguments ) {
    log.logError( message, arguments );
  }

}
