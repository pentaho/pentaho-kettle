/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
import org.pentaho.di.ui.xul.KettleXulLoader;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.swt.SwtXulRunner;

/**
 * Displays all files to overwrite in a list and prompts the user to continue or cancel.
 *
 * @author cboyden
 *
 */
public class FileOverwriteDialogController extends AbstractXulEventHandler {
  private static final String name = "fileOverwriteDialogController";
  private static final Class<?> PKG = RepositoryExplorer.class;

  private static final ResourceBundle resourceBundle = new ResourceBundle() {

    @Override
    public Enumeration<String> getKeys() {
      return null;
    }

    @Override
    protected Object handleGetObject( String key ) {
      return BaseMessages.getString( PKG, key );
    }

  };

  private final XulDomContainer container;
  private final XulDialog dialog;
  private final BindingFactory bf;

  private final UIRepositoryObjectsList objects;

  private boolean overwriteFiles = false;

  private MainController mainController;

  public static FileOverwriteDialogController getInstance( Shell shell, List<UIRepositoryObject> objects ) {
    try {
      KettleXulLoader swtLoader = new KettleXulLoader();
      swtLoader.setOuterContext( shell );
      swtLoader.setSettingsManager( XulSpoonSettingsManager.getInstance() );
      XulDomContainer container =
        swtLoader.loadXul(
          "org/pentaho/di/ui/repository/repositoryexplorer/xul/file-overwrite-dialog.xul", resourceBundle );
      final XulRunner runner = new SwtXulRunner();
      runner.addContainer( container );

      FileOverwriteDialogController dialogController = new FileOverwriteDialogController( container, objects );

      container.addEventHandler( dialogController );

      runner.initialize();

      return dialogController;
    } catch ( Exception e ) {
      return null;
    }
  }

  protected FileOverwriteDialogController( XulDomContainer container, List<UIRepositoryObject> objects ) {
    this.container = container;
    this.objects = new UIRepositoryObjectsList( objects );
    this.dialog = (XulDialog) this.container.getDocumentRoot().getElementById( "file-overwrite-dialog" );
    this.bf = new DefaultBindingFactory();
  }

  public void init() {
    try {
      bf.setDocument( container.getDocumentRoot() );

      mainController = (MainController) this.getXulDomContainer().getEventHandler( "mainController" );

      bf.setBindingType( Binding.Type.ONE_WAY );
      bf.createBinding( objects, "children", "file-list", "elements" ).fireSourceChanged();
    } catch ( Exception e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        new ErrorDialog( (Shell) container.getOuterContext(),
          BaseMessages.getString( PKG, "FileOverwriteDialog.ErrorDialog.Title" ),
          BaseMessages.getString( PKG, "FileOverwriteDialog.ErrorDialog.Message" ), e );
      }
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
