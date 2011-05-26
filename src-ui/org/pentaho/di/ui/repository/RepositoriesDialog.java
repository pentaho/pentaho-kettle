/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.ui.repository;

import java.util.Enumeration;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.repository.controllers.RepositoriesController;
import org.pentaho.di.ui.spoon.XulSpoonSettingsManager;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;

public class RepositoriesDialog {
  private static final Class<?> CLZ = RepositoriesDialog.class;
  private static Log log = LogFactory.getLog(RepositoriesDialog.class);
  private RepositoriesController repositoriesController = new RepositoriesController();
  private XulDomContainer container;
  private ILoginCallback callback;
  private ResourceBundle resourceBundle = new ResourceBundle() {

    @Override
    public Enumeration<String> getKeys() {
      return null;
    }

    @Override
    protected Object handleGetObject(String key) {
      return BaseMessages.getString(CLZ, key);
    }
    
  };  

  public RepositoriesDialog(Shell shell, String preferredRepositoryName, ILoginCallback callback) {
    try {
      this.callback = callback;
      SwtXulLoader swtLoader = new SwtXulLoader();
      swtLoader.setOuterContext(shell);
      swtLoader.setSettingsManager(XulSpoonSettingsManager.getInstance());
      container = swtLoader.loadXul("org/pentaho/di/ui/repository/xul/repositories.xul", resourceBundle); //$NON-NLS-1$
      final XulRunner runner = new SwtXulRunner();
      runner.addContainer(container);

      BindingFactory bf = new DefaultBindingFactory();
      bf.setDocument(container.getDocumentRoot());
      repositoriesController.setBindingFactory(bf);
      repositoriesController.setPreferredRepositoryName(preferredRepositoryName);
      repositoriesController.setMessages(resourceBundle);
      repositoriesController.setCallback(callback);
      repositoriesController.setShell(getShell());
      container.addEventHandler(repositoriesController);
      
      try {
        runner.initialize();
      } catch (XulException e) {
        SpoonFactory.getInstance().messageBox(e.getLocalizedMessage(), "Service Initialization Failed", false, Const.ERROR);          
        log.error(resourceBundle.getString("RepositoryLoginDialog.ErrorStartingXulApplication"), e);//$NON-NLS-1$
      }
    } catch (XulException e) {
     log.error(resourceBundle.getString("RepositoryLoginDialog.ErrorLoadingXulApplication"), e);//$NON-NLS-1$
    }
  }

  public Composite getDialogArea() {
    XulDialog dialog = (XulDialog) container.getDocumentRoot().getElementById("repository-login-dialog"); //$NON-NLS-1$
    return (Composite) dialog.getManagedObject();
  }
  
  public void show() {
    repositoriesController.show();    
  }

  public ILoginCallback getCallback() {
    return callback;
  }
  
  public Shell getShell() {
    XulDialog dialog = (XulDialog) container.getDocumentRoot().getElementById("repository-login-dialog"); //$NON-NLS-1$
    return (Shell) dialog.getRootObject();
  }
}
