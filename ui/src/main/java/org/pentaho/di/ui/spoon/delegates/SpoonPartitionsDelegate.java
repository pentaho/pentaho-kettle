/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon.delegates;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.partition.PartitionSchemaManagementInterface;
import org.pentaho.di.shared.DatabaseManagementInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.partition.dialog.PartitionSchemaDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.tree.provider.PartitionsFolderProvider;

public class SpoonPartitionsDelegate extends SpoonSharedObjectDelegate {
  public SpoonPartitionsDelegate( Spoon spoon ) {
    super( spoon );
  }

  public void newPartitioningSchema( TransMeta transMeta ) {
    PartitionSchema partitionSchema = new PartitionSchema();
    try {
      PartitionSchemaManagementInterface partitionSchemaManagementInterface =
          spoon.getBowl().getManager( PartitionSchemaManagementInterface.class );

      DatabaseManagementInterface databaseManagementInterface = spoon.getExecutionBowl().getManager( DatabaseManagementInterface.class );

      List<DatabaseMeta> databaseMetas = transMeta != null ? transMeta.getDatabases() :
        databaseManagementInterface.getAll();

      PartitionSchemaDialog dialog =
          new PartitionSchemaDialog( spoon.getShell(), partitionSchema, partitionSchemaManagementInterface.getAll(),
                    databaseMetas, spoon.getBowl().getADefaultVariableSpace() );
      if ( dialog.open() ) {
        List<PartitionSchema> partitions = partitionSchemaManagementInterface.getAll();
        if ( isDuplicate( partitions, partitionSchema ) ) {
          new ErrorDialog(
            spoon.getShell(), getMessage( "Spoon.Dialog.ErrorSavingPartition.Title" ), getMessage(
            "Spoon.Dialog.ErrorSavingPartition.Message", partitionSchema.getName() ),
            new KettleException( getMessage( "Spoon.Dialog.ErrorSavingPartition.NotUnique" ) ) );
          return;
        }

        partitionSchemaManagementInterface.add( partitionSchema );

        if ( spoon.rep != null ) {
          if ( !spoon.rep.getSecurityProvider().isReadOnly() ) {
            spoon.rep.save( partitionSchema, Const.VERSION_COMMENT_INITIAL_VERSION, null );
            if ( sharedObjectSyncUtil != null ) {
              sharedObjectSyncUtil.reloadTransformationRepositoryObjects( false );
            }
          } else {
            throw new KettleException( BaseMessages.getString(
              PKG, "Spoon.Dialog.Exception.ReadOnlyRepositoryUser" ) );
          }
        }
      }
    } catch ( KettleException e ) {
      showSaveErrorDialog( partitionSchema, e );
    }
    refreshTree();

  }

  //public void editPartitionSchema( TransMeta transMeta, PartitionSchema partitionSchema ) {
  public void editPartitionSchema( TransMeta transMeta, PartitionSchemaManagementInterface partitionSchemaManager, PartitionSchema partitionSchema ) {
    String originalName = partitionSchema.getName();
    try {
      DatabaseManagementInterface databaseManagementInterface = spoon.getExecutionBowl().getManager( DatabaseManagementInterface.class );

      List<DatabaseMeta> databaseMetas = transMeta != null ? transMeta.getDatabases() :
        databaseManagementInterface.getAll();

      PartitionSchemaDialog dialog =
        new PartitionSchemaDialog( spoon.getShell(), partitionSchema, partitionSchemaManager.getAll(),
                      databaseMetas, spoon.getBowl().getADefaultVariableSpace() );
      if ( dialog.open() ) {
        String newName = partitionSchema.getName().trim();
        if ( !newName.equals( originalName ) ) {
          partitionSchemaManager.remove( originalName );
          refreshTree();
        }
        partitionSchemaManager.add( partitionSchema );

        if ( spoon.rep != null && partitionSchema.getObjectId() != null ) {
          saveSharedObjectToRepository( partitionSchema, null );
          if ( sharedObjectSyncUtil != null ) {
            sharedObjectSyncUtil.synchronizePartitionSchemas( partitionSchema, originalName );
          }
        }
        refreshTree();
      }
    } catch ( KettleException exception ) {
      showSaveErrorDialog( partitionSchema, exception );
    }
  }

  public void delPartitionSchema( PartitionSchemaManagementInterface partitionSchemaManager, PartitionSchema partitionSchema ) {
    MessageBox mb = new MessageBox( spoon.getShell(), SWT.YES | SWT.NO | SWT.ICON_QUESTION );
    mb.setMessage( BaseMessages.getString( PKG, "Spoon.Message.DeletePartitionSchemaAsk.Message", partitionSchema.getName() ) );
    mb.setText( BaseMessages.getString( PKG, "Spoon.ExploreDB.DeleteConnectionAsk.Title" ) );
    int response = mb.open();

    if ( response != SWT.YES ) {
      return;
    }
    deletePartitionSchema( partitionSchemaManager, partitionSchema );
  }
  public void deletePartitionSchema( PartitionSchemaManagementInterface partitionSchemaManager, PartitionSchema partitionSchema ) {
    try {
      spoon.getLog().logBasic( "Deleting the partition schema " +  partitionSchema.getName() );
      partitionSchemaManager.remove( partitionSchema );

      if ( spoon.rep != null && partitionSchema.getObjectId() != null ) {
        // remove the partition schema from the repository too...
        spoon.rep.deletePartitionSchema( partitionSchema.getObjectId() );
        if ( sharedObjectSyncUtil != null ) {
          sharedObjectSyncUtil.deletePartitionSchema( partitionSchema );
        }
      }
      refreshTree();
    } catch ( KettleException e ) {
      new ErrorDialog(
        spoon.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.ErrorDeletingClusterSchema.Title" ), BaseMessages
          .getString( PKG, "Spoon.Dialog.ErrorDeletingClusterSchema.Message" ), e );
    }
  }

  public void moveToProject( PartitionSchemaManagementInterface partitionSchemaManager, PartitionSchema partitionSchema )
    throws KettleException {
    moveCopy( partitionSchemaManager, spoon.getBowl().getManager( PartitionSchemaManagementInterface.class ), partitionSchema, true,
      "Spoon.Message.OverwritePartitionSchemaYN" );
  }

  public void moveToGlobal( PartitionSchemaManagementInterface partitionSchemaManager, PartitionSchema partitionSchema )
    throws KettleException {
    moveCopy( partitionSchemaManager, DefaultBowl.getInstance().getManager( PartitionSchemaManagementInterface.class ), partitionSchema, true,
      "Spoon.Message.OverwritePartitionSchemaYN" );
  }

  public void copyToProject( PartitionSchemaManagementInterface partitionSchemaManager, PartitionSchema partitionSchema )
    throws KettleException {
    moveCopy( partitionSchemaManager, spoon.getBowl().getManager( PartitionSchemaManagementInterface.class ), partitionSchema, false,
      "Spoon.Message.OverwritePartitionSchemaYN" );
  }

  public void copyToGlobal( PartitionSchemaManagementInterface partitionSchemaManager, PartitionSchema partitionSchema )
    throws KettleException {
    moveCopy( partitionSchemaManager, DefaultBowl.getInstance().getManager( PartitionSchemaManagementInterface.class ), partitionSchema, false,
      "Spoon.Message.OverwritePartitionSchemaYN" );
  }

  public void dupePartitionSchema( PartitionSchemaManagementInterface partitionSchemaManager, PartitionSchema partitionSchema  ) {
    ShowEditDialog<PartitionSchema> sed = ( ps, servers ) -> {
      DatabaseManagementInterface databaseManagementInterface =  spoon.getExecutionBowl().getManager( DatabaseManagementInterface.class );

      PartitionSchemaDialog dialog = new PartitionSchemaDialog( spoon.getShell(), ps, servers,
         databaseManagementInterface.getAll(), spoon.getBowl().getADefaultVariableSpace() );
      if ( dialog.open() ) {
        String newServerName = ps.getName().trim();
        partitionSchemaManager.add( ps );
      }
    };
    dupeSharedObject( partitionSchemaManager, partitionSchema, sed );
  }

  private void showSaveErrorDialog( PartitionSchema partitionSchema, KettleException e ) {
    new ErrorDialog( spoon.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingPartition.Title" ),
        BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingPartition.Message", partitionSchema.getName() ), e );
  }


  @Override
  protected void refreshTree() {
    spoon.refreshTree( PartitionsFolderProvider.STRING_PARTITIONS );
  }
}
