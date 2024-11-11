/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.engine.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.SelectionTreeExtension;
import org.pentaho.di.ui.spoon.Spoon;

@ExtensionPoint( id = "RunConfigurationViewTreeExtension", description = "Refreshes execution environment subtree",
  extensionPointId = "SpoonViewTreeExtension" )
public class RunConfigurationViewTreeExtension implements ExtensionPointInterface {

  private static final Class<?> PKG = RunConfigurationViewTreeExtension.class;
  public static String TREE_LABEL = BaseMessages.getString( PKG, "RunConfigurationTree.Title" );

  private RunConfigurationDelegate runConfigurationDelegate = RunConfigurationDelegate.getInstance();

  @Override public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {
    SelectionTreeExtension selectionTreeExtension = (SelectionTreeExtension) object;
    if ( selectionTreeExtension.getAction().equals( Spoon.REFRESH_SELECTION_EXTENSION ) ) {
      refreshTree( selectionTreeExtension );
    }
    if ( selectionTreeExtension.getAction().equals( Spoon.EDIT_SELECTION_EXTENSION ) ) {
      if ( selectionTreeExtension.getSelection() instanceof RunConfiguration ) {
        RunConfiguration runConfiguration = (RunConfiguration) selectionTreeExtension.getSelection();
        runConfigurationDelegate.edit( runConfiguration );
      }
      if ( selectionTreeExtension.getSelection() instanceof String ) {
        runConfigurationDelegate.edit( runConfigurationDelegate.load( (String) selectionTreeExtension.getSelection()
        ) );
      }
    }
  }

  private void refreshTree( SelectionTreeExtension selectionTreeExtension ) {
    TreeItem tiRootName = selectionTreeExtension.getTiRootName();
    GUIResource guiResource = selectionTreeExtension.getGuiResource();

    TreeItem tiEETitle = createTreeItem( tiRootName, TREE_LABEL, guiResource.getImageFolder(), 0 );

    for ( RunConfiguration runConfiguration : runConfigurationDelegate.load() ) {
      String imageFile = runConfiguration.isReadOnly() ? "images/run_tree_disabled.svg" : "images/run_tree.svg";
      createTreeItem( tiEETitle, runConfiguration.getName(), getRunConfigurationImage( guiResource, imageFile ), -1,
        runConfiguration.isReadOnly() );
    }
  }

  private Image getRunConfigurationImage( GUIResource guiResource, String file ) {
    return guiResource
      .getImage( file, getClass().getClassLoader(), ConstUI.MEDIUM_ICON_SIZE, ConstUI.MEDIUM_ICON_SIZE );
  }

  private TreeItem createTreeItem( TreeItem parent, String text, Image image, int index ) {
    return createTreeItem( parent, text, image, index, false );
  }

  private TreeItem createTreeItem( TreeItem parent, String text, Image image, int index, boolean disabled ) {
    TreeItem item = index == -1 ? new TreeItem( parent, SWT.NONE ) : new TreeItem( parent, SWT.NONE, index );

    if ( disabled ) {
      item.setForeground( getDisabledColor() );
    }

    item.setText( text );
    item.setImage( image );
    return item;
  }

  private Color getDisabledColor() {
    Device device = Display.getCurrent();
    return new Color( device, 188, 188, 188 );
  }

}
