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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.repository.Directory;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.BrowseController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.MainController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.SecurityController;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryDirectory;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;

/**
 *
 */
public class RepositoryExplorer {

  private static Log log = LogFactory.getLog(RepositoryExplorer.class);
  
  private MainController mainController = new MainController();
  private BrowseController browseController = new BrowseController();
  private SecurityController securityController = new SecurityController();

  private XulDomContainer container;
  
  private Directory repositoryDirectory; 

  public RepositoryExplorer(Directory rd, Repository rep, RepositoryExplorerCallback callback) {
    repositoryDirectory = rd;
    try {
      
      container = new SwtXulLoader().loadXul("org/pentaho/di/ui/repository/repositoryexplorer/xul/explorer-layout.xul");

      final XulRunner runner = new SwtXulRunner();
      runner.addContainer(container);

      BindingFactory bf = new DefaultBindingFactory();
      bf.setDocument(container.getDocumentRoot());

      mainController.setBindingFactory(bf);
      container.addEventHandler(mainController);

      browseController.setBindingFactory(bf);
      container.addEventHandler(browseController);
      browseController.setRepositoryDirectory(new UIRepositoryDirectory(repositoryDirectory, rep));
      browseController.setCallback(callback);

      boolean securityEnabled = rep.getRepositoryMeta().getRepositoryCapabilities().managesUsers();
      loadSecurityOverlay(securityEnabled);
      container.addEventHandler(securityController);
      if (securityEnabled){
        securityController.setBindingFactory(bf);
        securityController.setRepositoryUserInterface(rep.getSecurityProvider());
      }
      
      try {
        runner.initialize();
      } catch (XulException e) {
        log.error("error starting Xul application", e);
      }

    } catch (XulException e) {
      log.error("error loading Xul application", e);
    }
  }

  public Composite getDialogArea(){
    XulDialog dialog = (XulDialog) container.getDocumentRoot().getElementById("repository-explorer-dialog");
    return (Composite) dialog.getManagedObject();
  }
  
  public void show(){
    XulDialog dialog = (XulDialog) container.getDocumentRoot().getElementById("repository-explorer-dialog");
    dialog.show();
    
  }

  private void loadSecurityOverlay(boolean securityEnabled){
    try {
      String overlay = securityEnabled ? 
          "org/pentaho/di/ui/repository/repositoryexplorer/xul/security-enabled-layout-overlay.xul" :
            "org/pentaho/di/ui/repository/repositoryexplorer/xul/security-disabled-layout-overlay.xul";
      container.loadOverlay(overlay); //$NON-NLS-1$
    } catch (XulException e) {
      log.error("Error loading Xul overlay: security-layout-overlay.xul");
      e.printStackTrace();
    }
  }

  public Directory getRepositoryDirectory() {
    return repositoryDirectory;
  }

  public void setRepositoryDirectory(Directory repositoryDirectory) {
    this.repositoryDirectory = repositoryDirectory;
  }
  
}
