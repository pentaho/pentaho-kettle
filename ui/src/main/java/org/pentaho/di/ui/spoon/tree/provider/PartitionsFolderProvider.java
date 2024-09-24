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

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.tree.TreeFolderProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bmorrise on 6/28/18.
 */
public class PartitionsFolderProvider extends TreeFolderProvider {

  private static Class<?> PKG = Spoon.class;
  public static final String STRING_PARTITIONS = BaseMessages.getString( PKG, "Spoon.STRING_PARTITIONS" );
  private GUIResource guiResource;
  private Spoon spoon;

  public PartitionsFolderProvider( GUIResource guiResource, Spoon spoon ) {
    this.guiResource = guiResource;
    this.spoon = spoon;
  }

  public PartitionsFolderProvider() {
    this( GUIResource.getInstance(), Spoon.getInstance() );
  }

  @Override
  public void refresh( AbstractMeta meta, TreeNode treeNode, String filter ) {

    TransMeta transMeta = (TransMeta) meta;

    List<PartitionSchema> partitionSchemas;
    try {
      partitionSchemas = pickupPartitionSchemas( transMeta );
    } catch ( KettleException e ) {
      new ErrorDialog( Spoon.getInstance().getShell(),
              BaseMessages.getString( PKG, "Spoon.ErrorDialog.Title" ),
              BaseMessages.getString( PKG, "Spoon.ErrorDialog.ErrorFetchingFromRepo.PartitioningSchemas" ),
              e
      );

      return;
    }

    // Put the steps below it.
    for ( PartitionSchema partitionSchema : partitionSchemas ) {
      if ( !filterMatch( partitionSchema.getName(), filter ) ) {
        continue;
      }
      TreeNode childTreeNode = createTreeNode( treeNode, partitionSchema.getName(), guiResource
              .getImagePartitionSchema() );
      if ( partitionSchema.isShared() ) {
        childTreeNode.setFont( guiResource.getFontBold() );
      }
    }
  }

  private List<PartitionSchema> pickupPartitionSchemas( TransMeta transMeta ) throws KettleException {
    Repository rep = spoon.getRepository();
    if ( rep != null ) {
      ObjectId[] ids = rep.getPartitionSchemaIDs( false );
      List<PartitionSchema> result = new ArrayList<>( ids.length );
      for ( ObjectId id : ids ) {
        PartitionSchema schema = rep.loadPartitionSchema( id, null );
        result.add( schema );
      }
      return result;
    }

    return transMeta.getPartitionSchemas();
  }

  @Override
  public String getTitle() {
    return STRING_PARTITIONS;
  }

  @Override
  public Class getType() {
    return PartitionSchema.class;
  }
}
