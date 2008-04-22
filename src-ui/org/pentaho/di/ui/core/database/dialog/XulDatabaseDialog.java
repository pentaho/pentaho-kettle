/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.ui.core.database.dialog;

import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.containers.XulWindow;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.swt.SwtXulLoader;

public class XulDatabaseDialog {

  private DatabaseMeta databaseMeta;

  private Shell shell;
  
  private Shell parentShell;

  private String databaseName;

  // private PropsUI props;

  private java.util.List<DatabaseMeta> databases;

  private boolean modalDialog;

  XulEventHandler dataHandler = null;

  public XulDatabaseDialog(Shell parent, DatabaseMeta dbMeta) {
    parentShell = parent;
    databaseMeta = dbMeta;
    if(dbMeta != null){
      databaseName = databaseMeta.getName();
    }
    // props = PropsUI.getInstance();
    databases = null;
  }

  /**
   * Opens the XUL database dialog
 * @return databaseName (or NULL on error or cancel)
 */
public String open() {

    // Load the XUL definition to a dom4j document...
	Document doc = null;
	try {	  
		InputStream in = XulDatabaseDialog.class.getClassLoader().getResourceAsStream(
        	"org/pentaho/ui/database/databasedialog.xul");

		SAXReader rdr = new SAXReader();
		doc = rdr.read(in);
    } catch (Exception e) {
		new ErrorDialog(parentShell, Messages.getString("XulDatabaseDialog.Error.Titel"), Messages.getString("XulDatabaseDialog.Error.SAXReader"), e);
		return null;
    }

    XulDomContainer container = null;
    try {
      SwtXulLoader loader = new SwtXulLoader();
      loader.register("VARIABLETEXTBOX", "org.pentaho.di.ui.core.database.dialog.tags.ExtTextbox");
      container = loader.loadXul(doc);

      container.addEventHandler("dataHandler", DataOverrideHandler.class.getName());

      dataHandler = container.getEventHandler("dataHandler");
      if (databaseMeta != null) {
        dataHandler.setData(databaseMeta);
      }
      
      ((DataOverrideHandler) container.getEventHandler("dataHandler")).setDatabases(databases);
      ((DataOverrideHandler) container.getEventHandler("dataHandler")).getControls();

    } catch (XulException e1) {
      e1.printStackTrace();
    }

    try {
    // Inject the button panel that contains the "Feature List" and "Explore" buttons

        XulComponent boxElement = container.getDocumentRoot().getElementById("test-button-box");
        XulComponent parentElement = boxElement.getParent();

        XulDomContainer fragmentContainer = null;
      
		// Get new box fragment ...
		// This will effectively set up the SWT parent child relationship...
		  
		String pkg = getClass().getPackage().getName().replace('.', '/');
		fragmentContainer = container.loadFragment(pkg.concat("/feature_override.xul"));
		XulComponent newBox = fragmentContainer.getDocumentRoot().getFirstChild();
		parentElement.replaceChild(boxElement, newBox);
		  
      
    } catch (XulException e) {
		new ErrorDialog(parentShell, Messages.getString("XulDatabaseDialog.Error.Titel"), Messages.getString("XulDatabaseDialog.Error.HandleXul"), e);
		return null;
    }

    try {
        XulWindow dialog = (XulWindow) container.getDocumentRoot().getRootElement();
        shell = (Shell)dialog.getManagedObject();
        shell.setParent(parentShell);
    // props.setLook(shell);
        shell.setImage(GUIResource.getInstance().getImageConnection());
        
        dialog.open();
    	
        databaseMeta = (DatabaseMeta)dataHandler.getData();
        databaseName = databaseMeta.getDatabaseName();
    } catch (Exception e) {
		new ErrorDialog(parentShell, Messages.getString("XulDatabaseDialog.Error.Titel"), Messages.getString("XulDatabaseDialog.Error.Dialog"), e);
		return null;
    }
    return databaseName;
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