/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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
  public void refresh( AbstractMeta meta, TreeNode treeNode, String filter ) {
    TransMeta transMeta = (TransMeta) meta;
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
