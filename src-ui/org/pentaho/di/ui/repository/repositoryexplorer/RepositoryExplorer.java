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

import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Directory;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.BrowseController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.ClustersController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.ConnectionsController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.ISecurityController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.MainController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.PartitionsController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.PermissionsController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.SecurityController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.SlavesController;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIObjectRegistery;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryDirectory;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryRole;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPluginManager;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulPromptBox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;
import org.pentaho.ui.xul.swt.custom.DialogConstant;

/**
 *
 */
public class RepositoryExplorer {

  private static Log log = LogFactory.getLog(RepositoryExplorer.class);

  @SuppressWarnings("unchecked")
  private static final Class CLZ = RepositoryExplorer.class;
  private MainController mainController = new MainController();

  private BrowseController browseController = new BrowseController();

  private static Class<?> securityControllerClass;
  
  private static ISecurityController securityController ;

  private ConnectionsController connectionsController = new ConnectionsController();

  private SlavesController slavesController = new SlavesController();

  private PartitionsController partitionsController = new PartitionsController();

  private ClustersController clustersController = new ClustersController();

  private PermissionsController permissionsController = new PermissionsController();

  private XulDomContainer container;

  private Directory repositoryDirectory;
  
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
  
  public static XulPromptBox promptCommitComment(org.pentaho.ui.xul.dom.Document document, ResourceBundle messages, String defaultMessage) throws XulException {
    XulPromptBox prompt = (XulPromptBox) document.createElement("promptbox"); //$NON-NLS-1$
    
    prompt.setTitle(messages.getString("RepositoryExplorer.CommitTitle"));//$NON-NLS-1$
    prompt.setButtons(new DialogConstant[] { DialogConstant.OK, DialogConstant.CANCEL });

    prompt.setMessage(messages.getString("RepositoryExplorer.CommitLabel"));//$NON-NLS-1$
    prompt.setValue(defaultMessage == null ? messages.getString("RepositoryExplorer.DefaultCommitMessage") : defaultMessage); //$NON-NLS-1$
    return prompt;
  }

  // private Repository repository;
  public RepositoryExplorer(final Repository rep, RepositorySecurityManager securityManager, RepositoryExplorerCallback callback,
      VariableSpace variableSpace) throws XulException {


    // If there is not security controller class instantiated and use the default one which is SecurityController
    if(getSecurityControllerClass() == null) {
      setSecurityControllerClass(SecurityController.class);
    }
    // If repository role class is not set, Set it with the default one
    if(UIObjectRegistery.getInstance().getRegisteredUIRepositoryRoleClass() == null) {
      UIObjectRegistery.getInstance().registerUIRepositoryRoleClass(UIRepositoryRole.class);
    }

    container = new SwtXulLoader().loadXul("org/pentaho/di/ui/repository/repositoryexplorer/xul/explorer-layout.xul", resourceBundle); //$NON-NLS-1$
    
    // Load functionality from plugins
    for(XulOverlay over : SpoonPluginManager.getInstance().getOverlaysforContainer("repository-explorer")) { //$NON-NLS-1$
      container.loadOverlay(over.getOverlayUri());
    }
    
    final XulRunner runner = new SwtXulRunner();
    runner.addContainer(container);

    BindingFactory bf = new DefaultBindingFactory();
    bf.setDocument(container.getDocumentRoot());

    mainController.setRepository(rep);
    mainController.setBindingFactory(bf);
    container.addEventHandler(mainController);

    boolean versionsEnabled = rep.getRepositoryMeta().getRepositoryCapabilities().supportsRevisions();
    loadVersionOverlay(versionsEnabled);
    browseController.setBindingFactory(bf);
    container.addEventHandler(browseController);
    browseController.setMessages(resourceBundle);
    browseController.setCallback(callback);

    permissionsController.setBindingFactory(bf);
    permissionsController.setBrowseController(browseController);
    permissionsController.setMessages(resourceBundle);
    container.addEventHandler(permissionsController);
    permissionsController.setRepositoryDirectory(new UIRepositoryDirectory(repositoryDirectory, rep));

    connectionsController.setRepository(rep);
    connectionsController.setMessages(resourceBundle);
    connectionsController.setBindingFactory(bf);
    container.addEventHandler(connectionsController);

    slavesController.setRepository(rep);
    slavesController.setBindingFactory(bf);
    slavesController.setMessages(resourceBundle);
    container.addEventHandler(slavesController);

    partitionsController.setRepository(rep);
    partitionsController.setVariableSpace(variableSpace);
    partitionsController.setBindingFactory(bf);
    partitionsController.setMessages(resourceBundle);
    container.addEventHandler(partitionsController);

    clustersController.setRepository(rep);
    clustersController.setBindingFactory(bf);
    clustersController.setMessages(resourceBundle);
    container.addEventHandler(clustersController);

    boolean securityEnabled = rep.getRepositoryMeta().getRepositoryCapabilities().managesUsers();
    loadSecurityOverlay(securityEnabled);

    boolean aclEnabled = rep.getRepositoryMeta().getRepositoryCapabilities().supportsAcls();
    loadAclOverlay(aclEnabled);

    container.addEventHandler((AbstractXulEventHandler) securityController);
    if (securityEnabled) {
      securityController.setBindingFactory(bf);
      securityController.setRepositorySecurityManager(securityManager);
      securityController.setRepository(rep);
      securityController.setMessages(resourceBundle);
    }

    boolean aclsEnabled = rep.getRepositoryMeta().getRepositoryCapabilities().supportsAcls();
    container.addEventHandler(permissionsController);
    if (aclsEnabled && securityEnabled) {
      permissionsController.setBindingFactory(bf);
      permissionsController.setRepositorySecurityManager(securityManager);
      permissionsController.setMessages(resourceBundle);
    }
    
    // Load handlers from plugin
    for(XulEventHandler handler : SpoonPluginManager.getInstance().getEventHandlersforContainer("repository-explorer")){ //$NON-NLS-1$
      container.addEventHandler(handler);
    }
    
    container.invokeLater(new Runnable() {
      public void run() {
        try {
          repositoryDirectory = rep.loadRepositoryDirectoryTree();
          browseController.setRepositoryDirectory(new UIRepositoryDirectory(repositoryDirectory, rep));
          browseController.init();
        } catch (Throwable e) {
          new ErrorDialog(((Spoon)SpoonFactory.getInstance()).getShell(), BaseMessages.getString(Spoon.class, "Spoon.Error"), e.getMessage(), e); //$NON-NLS-1$
        }
      }
    });

    List<XulOverlay> theXulOverlays = SpoonPluginManager.getInstance().getOverlaysforContainer("action-based-security"); //$NON-NLS-1$

    for (XulOverlay overlay : theXulOverlays) {
      this.container.loadOverlay(overlay.getOverlayUri());
    }
    
    try {
      runner.initialize();
    } catch (XulException e) {
      log.error(resourceBundle.getString("RepositoryExplorer.ErrorStartingXulApplication"), e);//$NON-NLS-1$
      new ErrorDialog(((Spoon)SpoonFactory.getInstance()).getShell(), BaseMessages.getString(Spoon.class, "Spoon.Error"), e.getMessage(), e); //$NON-NLS-1$
    }
  }

  public Composite getDialogArea() {
    XulDialog dialog = (XulDialog) container.getDocumentRoot().getElementById("repository-explorer-dialog"); //$NON-NLS-1$
    return (Composite) dialog.getManagedObject();
  }

  public void show() {
    XulDialog dialog = (XulDialog) container.getDocumentRoot().getElementById("repository-explorer-dialog"); //$NON-NLS-1$
    dialog.show();

  }

  private void loadSecurityOverlay(boolean securityEnabled) {
    String overlay = null;
    try {
      overlay = securityEnabled ? "org/pentaho/di/ui/repository/repositoryexplorer/xul/security-enabled-layout-overlay.xul" : //$NON-NLS-1$
          "org/pentaho/di/ui/repository/repositoryexplorer/xul/security-disabled-layout-overlay.xul"; //$NON-NLS-1$
      container.loadOverlay(overlay);
    } catch (XulException e) {
      log.error(BaseMessages.getString(CLZ, "RepositoryExplorer.ErrorLoadingXulOverlay", overlay));//$NON-NLS-1$
      e.printStackTrace();
    }
  }

  private void loadAclOverlay(boolean aclEnabled) {
    String overlay = null;
    try {
      overlay = aclEnabled ? "org/pentaho/di/ui/repository/repositoryexplorer/xul/acl-enabled-layout-overlay.xul" : //$NON-NLS-1$
          "org/pentaho/di/ui/repository/repositoryexplorer/xul/acl-disabled-layout-overlay.xul"; //$NON-NLS-1$
      container.loadOverlay(overlay);
    } catch (XulException e) {
      log.error(BaseMessages.getString(CLZ, "RepositoryExplorer.ErrorLoadingXulOverlay", overlay));//$NON-NLS-1$
      e.printStackTrace();
    }
  }

  private void loadVersionOverlay(boolean versionEnabled) {
    String overlay = null;
    try {
      overlay = versionEnabled ? "org/pentaho/di/ui/repository/repositoryexplorer/xul/version-layout-overlay.xul" : //$NON-NLS-1$
          "org/pentaho/di/ui/repository/repositoryexplorer/xul/version-disabled-layout-overlay.xul"; //$NON-NLS-1$
      container.loadOverlay(overlay);
    } catch (XulException e) {
      log.error(BaseMessages.getString(CLZ, "RepositoryExplorer.ErrorLoadingXulOverlay", overlay));//$NON-NLS-1$
      e.printStackTrace();
    }
  }

  public Directory getRepositoryDirectory() {
    return repositoryDirectory;
  }

  public void setRepositoryDirectory(Directory repositoryDirectory) {
    this.repositoryDirectory = repositoryDirectory;
  }

  public static void setSecurityControllerClass(Class<?> clz) {
    securityControllerClass = clz;
    try {
      securityController = (ISecurityController) securityControllerClass.newInstance();
    } catch (InstantiationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static Class<?> getSecurityControllerClass() {
    return securityControllerClass;
  }

}
