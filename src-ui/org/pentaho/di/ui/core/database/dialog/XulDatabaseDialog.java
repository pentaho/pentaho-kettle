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
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.test.ui.database.DatabaseDialogHarness;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.containers.XulWindow;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.swt.SwtXulLoader;

public class XulDatabaseDialog extends Dialog {

  private DatabaseMeta databaseMeta;

  private Shell shell;

  private String databaseName;

  private ModifyListener lsMod;

  private boolean changed;

  private PropsUI props;

  private String previousDatabaseType;

  private java.util.List<DatabaseMeta> databases;

  private Map<String, String> extraOptions;

  private long database_id;

  private boolean modalDialog;

  XulEventHandler dataHandler = null;

  public XulDatabaseDialog(Shell parent, DatabaseMeta databaseMeta) {
    super(parent, SWT.NONE);
    this.databaseMeta = databaseMeta;
    this.databaseName = databaseMeta.getName();
    this.props = PropsUI.getInstance();
    this.databases = null;
    this.extraOptions = databaseMeta.getExtraOptions();
    this.database_id = databaseMeta.getID();
  }

  public String open() {
    Shell parent = getParent();

    // Load the XUL definition to a dom4j document...
    InputStream in = XulDatabaseDialog.class.getClassLoader().getResourceAsStream(
        "org/pentaho/ui/database/databasedialog.xul");

    Document doc = null;
    SAXReader rdr = new SAXReader();
    try {
      doc = rdr.read(in);
    } catch (DocumentException e1) {
      e1.printStackTrace();
    }

    XulDomContainer container = null;
    try {
      container = new SwtXulLoader().loadXul(doc);
      dataHandler = container.getEventHandler("dataHandler");
      if (databaseMeta != null) {
        dataHandler.setData(databaseMeta);
      }
      
    } catch (XulException e1) {
      e1.printStackTrace();
    }

    XulComponent boxElement = container.getDocumentRoot().getElementById("test-button-box");
    XulComponent parentElement = boxElement.getParent();

    XulDomContainer fragmentContainer = null;
    try {
      
      // Get new box fragment ...
      // This will effectively set up the SWT parent child relationship...
      
      String pkg = getClass().getPackage().getName().replace('.', '/');
      fragmentContainer = container.loadFragment(pkg.concat("/feature_override.xul"));
      XulComponent newBox = fragmentContainer.getDocumentRoot().getFirstChild();
      parentElement.replaceChild(boxElement, newBox);
      
      container.addEventHandler("featureHandler", DataOverrideHandler.class.getName());
      ((DataOverrideHandler)container.getEventHandler("featureHandler")).setDatabases(databases);
      
    } catch (XulException e) {
      e.printStackTrace(System.out);
    }
    
    XulWindow dialog = (XulWindow) container.getDocumentRoot().getRootElement();
    dialog.open();

    try {
      databaseMeta = (DatabaseMeta)dataHandler.getData();
    } catch (Exception e) {
      e.printStackTrace();
    }
    databaseName = databaseMeta.getDatabaseName();

    /*    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN
            | (modalDialog ? SWT.APPLICATION_MODAL : SWT.NONE));
        props.setLook(shell);
        shell.setImage(GUIResource.getInstance().getImageConnection());

        lsMod = new ModifyListener() {
          public void modifyText(ModifyEvent e) {
            databaseMeta.setChanged();
          }
        };
        changed = databaseMeta.hasChanged();

        shell.setText(Messages.getString("DatabaseDialog.Shell.title")); //$NON-NLS-1$
        BaseStepDialog.setSize(shell);

        databaseMeta.setChanged(changed);
        shell.open();
        Display display = parent.getDisplay();
        while (!shell.isDisposed()) {
          if (!display.readAndDispatch())
            display.sleep();
        }
    */
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