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

package org.pentaho.di.ui.core.database.dialog;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.i18n.GlobalMessages;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.ui.database.DatabaseConnectionDialog;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulTree;

public class XulDatabaseDialog {

  private static Class<?> PKG = XulDatabaseDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private DatabaseMeta databaseMeta;
  
  /**
   * The original objectId of the databaseMeta before it was edited, possibly null.
   */
  private ObjectId databaseMetaObjectId;

  protected Shell shell;

  private Shell parentShell;

  private String databaseName;

  private java.util.List<DatabaseMeta> databases;

  private boolean modalDialog;

  DataOverrideHandler dataHandler = null;
  
  private LogChannelInterface log;
  
  private static final String EVENT_ID = "dataHandler"; //$NON-NLS-1$
  
  private static final String MESSAGES = "org.pentaho.di.ui.core.database.dialog.messages.messages"; //$NON-NLS-1$
  
  private static final String DIALOG_FRAGMENT_FILE = "/feature_override.xul"; //$NON-NLS-1$
  
  private static final String FRAGMENT_ID = "test-button-box"; //$NON-NLS-1$
  
  private static final String EXTENDED_WIDGET_CLASSNAME = "org.pentaho.di.ui.core.database.dialog.tags.ExtTextbox"; //$NON-NLS-1$
  
  private static final String EXTENDED_WIDGET_ID = "VARIABLETEXTBOX"; //$NON-NLS-1$
  
  private DatabaseConnectionDialog databaseDialogInstance;
  
  private XulDialog xulDialogComponent;
  
  public XulDatabaseDialog(Shell parent) {

    parentShell = parent;
    databases = null;
    
    log = new LogChannel("XulDatabaseDialog");
  }

  /**
   * Opens the XUL database dialog
  * @return databaseName (or NULL on error or cancel)
  * TODO: Fix deprecation warning in v3.2 by using the new dialog 
  */
  public String open() {
    if(databaseDialogInstance == null){
      createDialog();
    }

    try {
      
      // PDI-5088 clear previous options selections since we are re-using the dialog
      XulTree tree = (XulTree)xulDialogComponent.getDocument().getRootElement().getElementById("options-parameter-tree");
      tree.getRootChildren().removeAll();
      
      dataHandler.setData(databaseMeta);
      xulDialogComponent.show();    //Attention: onload: loadConnectionData() is called here the second time, see above for first time
                // caught with a HACK in DataHandler.loadConnectionData()

      databaseMeta = (DatabaseMeta) dataHandler.getData();
      
      // keep the original id
      if(databaseMeta != null) {
        databaseMeta.setObjectId(databaseMetaObjectId);
        databaseName = Const.isEmpty(databaseMeta.getName()) ? null : databaseMeta.getName();
      } else {
        databaseName = null;
      }
      
    } catch (Exception e) {
      new ErrorDialog(parentShell, BaseMessages.getString(PKG, "XulDatabaseDialog.Error.Title"), //$NON-NLS-1$ 
    		 BaseMessages.getString(PKG, "XulDatabaseDialog.Error.Dialog"), e); //$NON-NLS-1$
      return null;
    }
    return databaseName;
  }
  
  @SuppressWarnings("deprecation")
  private void createDialog(){
    XulDomContainer container = null;
    try {
      databaseDialogInstance = new DatabaseConnectionDialog();
      databaseDialogInstance.registerClass(EXTENDED_WIDGET_ID, EXTENDED_WIDGET_CLASSNAME);
      container = databaseDialogInstance.getSwtInstance(parentShell);  //Attention: onload: loadConnectionData() is called here the first time, see below for second time

      container.addEventHandler(EVENT_ID, DataOverrideHandler.class.getName());

      dataHandler = (DataOverrideHandler)container.getEventHandler(EVENT_ID);
      if (databaseMeta != null) {
        dataHandler.setData(databaseMeta);
      }
      dataHandler.setDatabases(databases);
      dataHandler.getControls();

    } catch (XulException e) {
      new ErrorDialog(parentShell, BaseMessages.getString(PKG, "XulDatabaseDialog.Error.Title"), //$NON-NLS-1$ 
         BaseMessages.getString(PKG, "XulDatabaseDialog.Error.HandleXul"), e); //$NON-NLS-1$
      return;
    }

    try {
      // Inject the button panel that contains the "Feature List" and "Explore" buttons

      XulComponent boxElement = container.getDocumentRoot().getElementById(FRAGMENT_ID);
      XulComponent parentElement = boxElement.getParent();

      ResourceBundle res = null;
      Locale primaryLocale = GlobalMessages.getLocale();
      Locale failOverLocale = LanguageChoice.getInstance().getFailoverLocale();
      try{
        res = GlobalMessages.getBundle(primaryLocale, MESSAGES);
      }catch(MissingResourceException e){
        try{
          res = GlobalMessages.getBundle(failOverLocale, MESSAGES);
        }catch(MissingResourceException e2){
          res = null;
          log.logError(BaseMessages.getString(PKG, "XulDatabaseDialog.Error.ResourcesNotFound.Title"),  //$NON-NLS-1$
              BaseMessages.getString(PKG, "XulDatabaseDialog.Error.ResourcesNotFound",   //$NON-NLS-1$
                  primaryLocale == null ? "" : primaryLocale.toString(),  //$NON-NLS-1$
                  failOverLocale == null ? "" : failOverLocale.toString()),   //$NON-NLS-1$
              e2);
        }
      }

      XulDomContainer fragmentContainer = null;
      String pkg = getClass().getPackage().getName().replace('.', '/');
      
      // Kludge: paths of execution do not account for a null resourcebundle gracefully, need 
      // to check for it here. 
      if (res != null){
        fragmentContainer = container.loadFragment(pkg.concat(DIALOG_FRAGMENT_FILE), res);
      } else{
        fragmentContainer = container.loadFragment(pkg.concat(DIALOG_FRAGMENT_FILE));
      }
      
      XulComponent newBox = fragmentContainer.getDocumentRoot().getFirstChild();
      parentElement.replaceChild(boxElement, newBox);

    } catch (Exception e) {
      new ErrorDialog(parentShell, BaseMessages.getString(PKG, "XulDatabaseDialog.Error.Title"), //$NON-NLS-1$ 
          BaseMessages.getString(PKG, "XulDatabaseDialog.Error.HandleXul"), e); //$NON-NLS-1$
      return;
    }

    try {
      xulDialogComponent = (XulDialog) container.getDocumentRoot().getRootElement();
      ((Shell) xulDialogComponent.getRootObject()).setImage(GUIResource.getInstance().getImageConnection());
      
      parentShell.addDisposeListener(new DisposeListener(){

        public void widgetDisposed(DisposeEvent arg0) {
          xulDialogComponent.hide();
        }
        
      });
   
    } catch (Exception e) {
      new ErrorDialog(parentShell, BaseMessages.getString(PKG, "XulDatabaseDialog.Error.Title"), //$NON-NLS-1$ 
         BaseMessages.getString(PKG, "XulDatabaseDialog.Error.Dialog"), e); //$NON-NLS-1$
      return;
    }
  }
  
  public void setDatabaseMeta(DatabaseMeta dbMeta){
    databaseMeta = dbMeta;
    databaseMetaObjectId = dbMeta.getObjectId();
    if (dbMeta != null) {
      databaseName = databaseMeta.getName();
    }
  }

  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  public void setDatabases(java.util.List<DatabaseMeta> databases) {
    this.databases = databases;
  }

  /**
   * @return the modalDialog
   */
  public boolean isModalDialog() {
    return modalDialog;
  }

  /**
   * @param modalDialog the modalDialog to set
   */
  public void setModalDialog(boolean modalDialog) {
    this.modalDialog = modalDialog;
  }

}