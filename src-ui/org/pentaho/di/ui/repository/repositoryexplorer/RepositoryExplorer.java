/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2009 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.di.ui.repository.repositoryexplorer;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.IRepositoryService;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.MainController;
import org.pentaho.di.ui.repository.repositoryexplorer.uisupport.IRepositoryExplorerUISupport;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPluginManager;
import org.pentaho.di.ui.spoon.XulSpoonSettingsManager;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;
import org.pentaho.ui.xul.swt.tags.SwtDialog;

/**
 *
 */
public class RepositoryExplorer {

  private static Log log = LogFactory.getLog(RepositoryExplorer.class);

  @SuppressWarnings("unchecked")
  private static final Class CLZ = RepositoryExplorer.class;

  private MainController mainController = new MainController();

  private XulDomContainer container;

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
  // private Repository repository;
  public RepositoryExplorer(Shell shell, final Repository rep, RepositoryExplorerCallback callback, VariableSpace variableSpace)
      throws XulException {
    SwtXulLoader swtXulLoader = new SwtXulLoader();
    swtXulLoader.setOuterContext(shell);
    swtXulLoader.setSettingsManager(XulSpoonSettingsManager.getInstance());
    swtXulLoader.registerClassLoader(getClass().getClassLoader());
    container = swtXulLoader.loadXul("org/pentaho/di/ui/repository/repositoryexplorer/xul/explorer-layout.xul", resourceBundle); //$NON-NLS-1$

    SpoonPluginManager.getInstance().applyPluginsForContainer("repository-explorer", container); //$NON-NLS-1$

    final XulRunner runner = new SwtXulRunner();
    runner.addContainer(container);

    mainController.setRepository(rep);
    mainController.setCallback(callback);
    
    container.addEventHandler(mainController);

    List<IRepositoryExplorerUISupport> uiSupportList = new ArrayList<IRepositoryExplorerUISupport>();
    try {
      for (Class<? extends IRepositoryService> sevice : rep.getServiceInterfaces()) {
        IRepositoryExplorerUISupport uiSupport = UISupportRegistery.getInstance().createUISupport(sevice);
        if (uiSupport != null) {
          uiSupportList.add(uiSupport);
          uiSupport.apply(container);
        }
      }
    } catch (Exception e) {
      log.error(resourceBundle.getString("RepositoryExplorer.ErrorStartingXulApplication"), e);//$NON-NLS-1$
      new ErrorDialog(((Spoon) SpoonFactory.getInstance()).getShell(), BaseMessages.getString(Spoon.class,
          "Spoon.Error"), e.getMessage(), e); //$NON-NLS-1$
    }
    // Call the init method for all the Active UISupportController
    for (IRepositoryExplorerUISupport uiSupport : uiSupportList) {
      try {
        uiSupport.initControllers(rep);
      } catch (ControllerInitializationException e) {
        log.error(resourceBundle.getString("RepositoryExplorer.ErrorStartingXulApplication"), e);//$NON-NLS-1$
        new ErrorDialog(((Spoon) SpoonFactory.getInstance()).getShell(), BaseMessages.getString(Spoon.class,
            "Spoon.Error"), e.getMessage(), e); //$NON-NLS-1$
      }
    }

    try {
      runner.initialize();
    } catch (XulException e) {
      log.error(resourceBundle.getString("RepositoryExplorer.ErrorStartingXulApplication"), e);//$NON-NLS-1$
      new ErrorDialog(((Spoon) SpoonFactory.getInstance()).getShell(), BaseMessages.getString(Spoon.class,
          "Spoon.Error"), e.getMessage(), e); //$NON-NLS-1$
    }
  }

  public void show() {
    XulDialog dialog = (XulDialog) container.getDocumentRoot().getElementById("repository-explorer-dialog"); //$NON-NLS-1$
    dialog.show();

  }
  
  public void dispose(){
    SwtDialog dialog = (SwtDialog) container.getDocumentRoot().getElementById("repository-explorer-dialog"); //$NON-NLS-1$
    dialog.dispose();
  }
}
