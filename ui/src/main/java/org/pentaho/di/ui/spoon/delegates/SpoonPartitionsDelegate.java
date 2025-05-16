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


package org.pentaho.di.ui.spoon.delegates;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
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

public class SpoonPartitionsDelegate extends SpoonSharedObjectDelegate<PartitionSchema> {
  public SpoonPartitionsDelegate( Spoon spoon ) {
    super( spoon );
  }

  public void newPartitioningSchema( TransMeta transMeta ) {
    PartitionSchema partitionSchema = new PartitionSchema();
    try {
      PartitionSchemaManagementInterface partitionSchemaManagementInterface =
          spoon.getManagementBowl().getManager( PartitionSchemaManagementInterface.class );

      DatabaseManagementInterface databaseManagementInterface =
        spoon.getExecutionBowl().getManager( DatabaseManagementInterface.class );

      List<DatabaseMeta> databaseMetas = transMeta != null ? transMeta.getDatabases() :
        databaseManagementInterface.getAll();

      PartitionSchemaDialog dialog =
          new PartitionSchemaDialog( spoon.getShell(), partitionSchema, partitionSchemaManagementInterface.getAll(),
                    databaseMetas, spoon.getManagementBowl().getADefaultVariableSpace() );
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
                      databaseMetas, spoon.getManagementBowl().getADefaultVariableSpace() );
      if ( dialog.open() ) {
        String newName = partitionSchema.getName().trim();
        // This should be case insensitive. We only need to remove if the name changed beyond case. The Managers handle
        // case-only changes.
        if ( !newName.equalsIgnoreCase( originalName ) ) {
          partitionSchemaManager.remove( originalName );
          // ideally we wouldn't leak this repository-specific concept, but I don't see how at the moment.
          partitionSchema.setObjectId( null );
        }
        partitionSchemaManager.add( partitionSchema );
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

      refreshTree();
    } catch ( KettleException e ) {
      new ErrorDialog(
        spoon.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.ErrorDeletingClusterSchema.Title" ), BaseMessages
          .getString( PKG, "Spoon.Dialog.ErrorDeletingClusterSchema.Message" ), e );
    }
  }

  public void moveToProject( PartitionSchemaManagementInterface partitionSchemaManager, PartitionSchema partitionSchema )
    throws KettleException {
    moveCopy( partitionSchemaManager, spoon.getManagementBowl().getManager( PartitionSchemaManagementInterface.class ),
      partitionSchema, true, "Spoon.Message.OverwritePartitionSchemaYN" );
  }

  public void moveToGlobal( PartitionSchemaManagementInterface partitionSchemaManager, PartitionSchema partitionSchema )
    throws KettleException {
    moveCopy( partitionSchemaManager, spoon.getGlobalManagementBowl().getManager( PartitionSchemaManagementInterface.class ),
      partitionSchema, true, "Spoon.Message.OverwritePartitionSchemaYN" );
  }

  public void copyToProject( PartitionSchemaManagementInterface partitionSchemaManager, PartitionSchema partitionSchema )
    throws KettleException {
    moveCopy( partitionSchemaManager, spoon.getManagementBowl().getManager( PartitionSchemaManagementInterface.class ),
      partitionSchema, false, "Spoon.Message.OverwritePartitionSchemaYN" );
  }

  public void copyToGlobal( PartitionSchemaManagementInterface partitionSchemaManager, PartitionSchema partitionSchema )
    throws KettleException {
    moveCopy( partitionSchemaManager, spoon.getGlobalManagementBowl().getManager( PartitionSchemaManagementInterface.class ),
      partitionSchema, false, "Spoon.Message.OverwritePartitionSchemaYN" );
  }

  public void dupePartitionSchema( PartitionSchemaManagementInterface partitionSchemaManager, PartitionSchema partitionSchema  ) {
    ShowEditDialog<PartitionSchema> sed = ( ps, servers ) -> {
      DatabaseManagementInterface databaseManagementInterface =  spoon.getExecutionBowl().getManager( DatabaseManagementInterface.class );

      PartitionSchemaDialog dialog = new PartitionSchemaDialog( spoon.getShell(), ps, servers,
         databaseManagementInterface.getAll(), spoon.getManagementBowl().getADefaultVariableSpace() );
      if ( dialog.open() ) {
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
