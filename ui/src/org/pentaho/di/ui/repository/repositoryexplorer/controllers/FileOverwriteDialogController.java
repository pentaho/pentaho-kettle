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

package org.pentaho.di.ui.repository.repositoryexplorer.controllers;

import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorer;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObject;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObjectsList;
import org.pentaho.di.ui.spoon.XulSpoonSettingsManager;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;

/**
 * Displays all files to overwrite in a list and prompts the user to continue or cancel.
 * 
 * @author cboyden
 *
 */
public class FileOverwriteDialogController extends AbstractXulEventHandler {
  private static final String name = "fileOverwriteDialogController"; //$NON-NLS-1$
  private static final Class<?> PKG = RepositoryExplorer.class;
  
  private static final ResourceBundle resourceBundle = new ResourceBundle() {

    @Override
    public Enumeration<String> getKeys() {
      return null;
    }

    @Override
    protected Object handleGetObject(String key) {
      return BaseMessages.getString(PKG, key);
    }
    
  };  

  private final XulDomContainer container;
  private final XulDialog dialog;
  private final BindingFactory bf;
  
  private final UIRepositoryObjectsList objects;
  
  private boolean overwriteFiles = false;
  
  public static FileOverwriteDialogController getInstance(Shell shell, List<UIRepositoryObject> objects) {
    try {
      SwtXulLoader swtLoader = new SwtXulLoader();
      swtLoader.setOuterContext(shell);
      swtLoader.setSettingsManager(XulSpoonSettingsManager.getInstance());
      XulDomContainer container = swtLoader.loadXul("org/pentaho/di/ui/repository/repositoryexplorer/xul/file-overwrite-dialog.xul", resourceBundle); //$NON-NLS-1$
      final XulRunner runner = new SwtXulRunner();
      runner.addContainer(container);
      
      FileOverwriteDialogController dialogController = new FileOverwriteDialogController(container, objects);
      
      container.addEventHandler(dialogController);
      
      runner.initialize();
      
      return dialogController;
    } catch (Exception e) {
      return null;
    }
  }
  
  protected FileOverwriteDialogController(XulDomContainer container, List<UIRepositoryObject> objects) {
    this.container = container;
    this.objects = new UIRepositoryObjectsList(objects);
    this.dialog = (XulDialog) this.container.getDocumentRoot().getElementById("file-overwrite-dialog"); //$NON-NLS-1$
    this.bf = new DefaultBindingFactory();
  }
  
  public void init() {
    try {
      bf.setDocument(container.getDocumentRoot());
      
      bf.setBindingType(Binding.Type.ONE_WAY);
      bf.createBinding(objects, "children", "file-list", "elements").fireSourceChanged(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    } catch (Exception e) {
      new ErrorDialog((Shell)container.getOuterContext(), BaseMessages.getString(PKG, "FileOverwriteDialog.ErrorDialog.Title"), BaseMessages.getString(PKG, "FileOverwriteDialog.ErrorDialog.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }
  
  @Override
  public String getName() {
    return name;
  }
  
  public void show() {
    dialog.show();
  }
  
  public void closeOverwrite() {
    // Cleanup dialog
    closeCancel();
    
    // Update result
    overwriteFiles = true;
  }
  
  public void closeCancel() {
    dialog.hide();
    objects.clear();
  }
  
  public boolean isOverwriteFiles() {
    return overwriteFiles;
  }
  
}