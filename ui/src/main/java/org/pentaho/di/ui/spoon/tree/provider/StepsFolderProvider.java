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


package org.pentaho.di.ui.spoon.tree.provider;

import org.eclipse.swt.graphics.Image;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.Optional;


/**
 * Created by bmorrise on 6/26/18.
 */
public class StepsFolderProvider extends AutomaticTreeFolderProvider {

  private static Class<?> PKG = Spoon.class;
  public static final String STRING_STEPS = BaseMessages.getString( PKG, "Spoon.STRING_STEPS" );

  @Override
  public String getTitle() {
    return STRING_STEPS;
  }

  @Override
  public void refresh( Optional<AbstractMeta> meta, TreeNode treeNode, String filter ) {
    if ( !meta.isPresent() ) {
      return;
    }
    TransMeta transMeta = (TransMeta) meta.get();
    for ( int i = 0; i < transMeta.nrSteps(); i++ ) {
      StepMeta stepMeta = transMeta.getStep( i );
      if ( stepMeta.isMissing() ) {
        continue;
      }
      PluginInterface stepPlugin = PluginRegistry.getInstance().findPluginWithId( StepPluginType.class, stepMeta.getStepID() );

      if ( !filterMatch( stepMeta.getName(), filter ) ) {
        continue;
      }

      Image stepIcon = GUIResource.getInstance().getImagesStepsSmall().get( stepPlugin.getIds()[ 0 ] );
      if ( stepIcon == null ) {
        stepIcon = GUIResource.getInstance().getImageFolder();
      }

      TreeNode childTreeNode = createTreeNode( treeNode, stepMeta.getName(), stepIcon );
      childTreeNode.setData( "StepId", stepMeta.getStepID() );

      if ( stepMeta.isShared() ) {
        childTreeNode.setFont( GUIResource.getInstance().getFontBold() );
      }
      if ( !stepMeta.isDrawn() ) {
        childTreeNode.setForeground( GUIResource.getInstance().getColorDarkGray() );
      }
    }
  }

  @Override
  public Class getType() {
    return StepMeta.class;
  }
}
