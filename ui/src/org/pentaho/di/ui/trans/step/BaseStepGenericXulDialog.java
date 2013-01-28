/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.step;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.spoon.XulSpoonResourceBundle;
import org.pentaho.di.ui.spoon.XulSpoonSettingsManager;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulLoader;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.impl.XulEventHandler;

/**
 * User: gmoran
 * Date: Jan 28, 2013
 */
public abstract class BaseStepGenericXulDialog extends AbstractXulEventHandler {
  private static Class<?> PKG = StepInterface.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  public static final LoggingObjectInterface loggingObject = new SimpleLoggingObject("Step dialog",
      LoggingObjectType.STEPDIALOG, null);

  protected static VariableSpace variables = new Variables();

  protected String stepname;

  protected XulLabel wlStepname;

  protected XulTextbox wStepname;

  protected XulButton wOK, wGet, wPreview, wSQL, wCreate, wCancel;

  protected TransMeta transMeta;

  protected Object parent;

  protected BaseStepMeta baseStepMeta;

  protected Repository repository;

  protected StepMeta stepMeta;

  protected LogChannel log;

  private String xulFile;

  private XulDomContainer container;

  private XulRunner runner;

  protected XulDialog xulDialog;

  protected BindingFactory bf;

  public BaseStepGenericXulDialog( String xulFile, Object parent, BaseStepMeta baseStepMeta, TransMeta transMeta, String stepname ) {

    this.log = new LogChannel(baseStepMeta);
    this.transMeta = transMeta;
    this.stepname = stepname;
    this.stepMeta = transMeta.findStep(stepname);
    this.baseStepMeta = baseStepMeta;
    this.xulFile = xulFile;
    this.parent = parent;

    try {
      initializeXul();
    } catch (Exception e) {
      e.printStackTrace();
      log.logError("Error initializing ("+stepname+") step dialog", e);
      throw new IllegalStateException("Cannot load dialog due to error in initialization", e);
    }
  }

  /**
   * The implementors of this method should call the sibling method initializeXul(XulLoder, BindingFactory, XulRunner, parent) with the 
   * desired Xul implementation classes - SWT or Swing. 
   * 
   * @throws XulException
   */
  protected abstract void initializeXul() throws XulException;
  
  protected void initializeXul(XulLoader loader, BindingFactory bindingFactory, XulRunner runner, Object parent) throws XulException {
    bf = bindingFactory;
    this.runner = runner;
    loader.registerClassLoader(getClass().getClassLoader());
    loader.setSettingsManager(XulSpoonSettingsManager.getInstance());
    loader.setOuterContext(parent);
    container = loader.loadXul( xulFile, new XulSpoonResourceBundle(getClassForMessages()));
    bf.setDocument(container.getDocumentRoot());
    
    for(XulEventHandler h : getEventHandlers()){
      container.addEventHandler(h); 
    }

    this.runner.addContainer(container);

    // try and get the dialog
    xulDialog = (XulDialog) container.getDocumentRoot().getRootElement();
    runner.initialize();
  }
  
  protected BindingFactory getBindingFactory(){
    return bf;
  }

  protected List<XulEventHandler> getEventHandlers(){
    return Collections.singletonList((XulEventHandler) this);
  }

  public String getName(){
    return "handler";
  }

  public String open(){
    xulDialog.show();
    return stepname;
  }

  public void close(){
    xulDialog.hide();
  }

  public abstract void onAccept();

  public abstract void onCancel();

  protected abstract Class<?> getClassForMessages();

  abstract public void dispose();
  
  public void addDatabases( XulMenuList<?> wConnection ) {
    addDatabases(wConnection, null);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void addDatabases( XulMenuList wConnection, Class<? extends DatabaseInterface> databaseType ) {
    List<String> databases = new ArrayList<String>();
    for (int i = 0; i < transMeta.nrDatabases(); i++) {
      DatabaseMeta ci = transMeta.getDatabase(i);
      if (databaseType == null || ci.getDatabaseInterface().getClass().equals(databaseType)) {
        databases.add(ci.getName());
      }
    }
    wConnection.setElements(databases);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void selectDatabase( XulMenuList wConnection, String name ) {
    wConnection.setSelectedItem(wConnection);
  }

  public Repository getRepository() {
    return repository;
  }

  public void setRepository( Repository repository ) {
    this.repository = repository;
  }

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
    log.logMinimal(message);
  }

  public void logMinimal( String message, Object... arguments ) {
    log.logMinimal(message, arguments);
  }

  public void logBasic( String message ) {
    log.logBasic(message);
  }

  public void logBasic( String message, Object... arguments ) {
    log.logBasic(message, arguments);
  }

  public void logDetailed( String message ) {
    log.logDetailed(message);
  }

  public void logDetailed( String message, Object... arguments ) {
    log.logDetailed(message, arguments);
  }

  public void logDebug( String message ) {
    log.logDebug(message);
  }

  public void logDebug( String message, Object... arguments ) {
    log.logDebug(message, arguments);
  }

  public void logRowlevel( String message ) {
    log.logRowlevel(message);
  }

  public void logRowlevel( String message, Object... arguments ) {
    log.logRowlevel(message, arguments);
  }

  public void logError( String message ) {
    log.logError(message);
  }
  public void logError( String message, Throwable e ) {

    log.logError(message, e);
  }

  public void logError( String message, Object... arguments ) {
    log.logError(message, arguments);
  }

}