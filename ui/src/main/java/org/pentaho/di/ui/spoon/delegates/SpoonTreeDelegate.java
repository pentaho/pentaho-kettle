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

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.dnd.DragAndDropContainer;
import org.pentaho.di.core.dnd.XMLTransfer;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.widget.tree.LeveledTreeNode;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TreeSelection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class SpoonTreeDelegate extends SpoonDelegate {
  private static final Set<String> CONFIG_OBJECT_NAMES = Set.of( Spoon.STRING_CONNECTIONS, Spoon.STRING_PARTITIONS,
                                                                 Spoon.STRING_SLAVES, Spoon.STRING_CLUSTERS );

  public SpoonTreeDelegate( Spoon spoon ) {
    super( spoon );
  }

  /**
   * @return The object that is selected in the tree or null if we couldn't figure it out. (titles etc. == null)
   */
  public TreeSelection[] getTreeObjects( final Tree tree, Tree selectionTree, Tree coreObjectsTree ) {
    List<TreeSelection> objects = new ArrayList<TreeSelection>();

    if ( selectionTree != null && !selectionTree.isDisposed() && tree.equals( selectionTree ) ) {
      TreeItem[] selection = selectionTree.getSelection();
      for ( int s = 0; s < selection.length; s++ ) {
        TreeItem treeItem = selection[s];
        String[] path = ConstUI.getTreeStrings( treeItem );

        TreeSelection object = null;

        switch ( path.length ) {
          case 0:
            break;
          case 1: // ------complete-----
            if ( path[0].equals( Spoon.STRING_CONFIGURATIONS ) ) { // the top level config entry

              // nothing to do by selecting the Configurations node.
            }
            break;

          case 2: // ------complete-----
            if ( path[0].equals( Spoon.STRING_CONFIGURATIONS ) ) {
              if ( path[1].equals( Spoon.STRING_CONNECTIONS ) ) {
                object = new TreeSelection( treeItem, path[1], DatabaseMeta.class );
              }
              if ( path[1].equals( Spoon.STRING_PARTITIONS ) ) {
                object = new TreeSelection( treeItem, path[1], PartitionSchema.class );
              }
              if ( path[1].equals( Spoon.STRING_SLAVES ) ) {
                object = new TreeSelection( treeItem, path[1], SlaveServer.class );
              }
              if ( path[1].equals( Spoon.STRING_CLUSTERS ) ) {
                object = new TreeSelection( treeItem, path[1], ClusterSchema.class );
              }
              executeExtensionPoint( new SpoonTreeDelegateExtension( treeItem, spoon.getActiveTransformation(), path,
                2, objects ) );
            }
            break;

          case 3: // ------complete-----
            if ( path[0].equals( Spoon.STRING_CONFIGURATIONS ) ) {


              if ( CONFIG_OBJECT_NAMES.contains( path[1] ) ) {
                String name = LeveledTreeNode.getName( treeItem );
                LeveledTreeNode.LEVEL level = LeveledTreeNode.getLevel( treeItem );

                object = new TreeSelection( treeItem, name, new SpoonTreeLeveledSelection( path[1], name, level ) );
              }
              executeExtensionPoint( new SpoonTreeDelegateExtension( treeItem, spoon.getActiveTransformation(), path,
                3, objects ) );
            }
            break;

          case 4:
            // TODO BACKLOG-40523
            //if ( path[0].equals( Spoon.STRING_CONFIGURATIONS ) ) {
            //
            //  if ( path[1].equals( Spoon.STRING_CLUSTERS ) ) {
            //    ClusterSchema clusterSchema = transMeta.findClusterSchema( path[2] );
            //    object =
            //      new TreeSelection( treeItem, path[3], clusterSchema.findSlaveServer( path[3]), clusterSchema, transMeta );
            //  }
            //}
            break;
          default:
            break;
        }

        if ( object != null ) {
          objects.add( object );
        }
      }
    }
    if ( tree != null && coreObjectsTree != null && tree.equals( coreObjectsTree ) ) {
      TreeItem[] selection = coreObjectsTree.getSelection();
      for ( int s = 0; s < selection.length; s++ ) {
        TreeItem treeItem = selection[s];
        String[] path = ConstUI.getTreeStrings( treeItem );

        TreeSelection object = null;

        switch ( path.length ) {
          case 0:
            break;
          case 2: // Job entries
            if ( spoon.showJob ) {
              PluginRegistry registry = PluginRegistry.getInstance();
              Class<? extends PluginTypeInterface> pluginType = JobEntryPluginType.class;
              PluginInterface plugin = registry.findPluginWithName( pluginType, path[1] );

              // Retry for Start
              //
              if ( plugin == null ) {
                if ( path[1].equalsIgnoreCase( JobMeta.STRING_SPECIAL_START ) ) {
                  plugin = registry.findPluginWithId( pluginType, JobMeta.STRING_SPECIAL );
                }
              }
              // Retry for Dummy
              //
              if ( plugin == null ) {
                if ( path[1].equalsIgnoreCase( JobMeta.STRING_SPECIAL_DUMMY ) ) {
                  plugin = registry.findPluginWithId( pluginType, JobMeta.STRING_SPECIAL );
                }
              }

              if ( plugin != null ) {
                object = new TreeSelection( treeItem, path[1], plugin );
              }
            }

            if ( spoon.showTrans ) {
              String stepId = (String) treeItem.getData( "StepId" );

              if ( stepId != null ) {
                object = new TreeSelection( treeItem, path[1], PluginRegistry.getInstance().findPluginWithId( StepPluginType.class, stepId ) );
              } else {
                object = new TreeSelection( treeItem, path[1], PluginRegistry.getInstance().findPluginWithName( StepPluginType.class, path[1] ) );
              }
            }
            break;
          default:
            break;
        }

        if ( object != null ) {
          objects.add( object );
        }
      }
    }

    return objects.toArray( new TreeSelection[objects.size()] );
  }

  public void addDragSourceToTree( final Tree tree, final Tree selectionTree, final Tree coreObjectsTree ) {
    // Drag & Drop for steps
    Transfer[] ttypes = new Transfer[] { XMLTransfer.getInstance() };

    DragSource ddSource = new DragSource( tree, DND.DROP_MOVE );
    ddSource.setTransfer( ttypes );
    ddSource.addDragListener( new DragSourceListener() {
      @Override
      public void dragStart( DragSourceEvent event ) {
        TreeSelection[] treeObjects = getTreeObjects( tree, selectionTree, coreObjectsTree );
        if ( treeObjects.length == 0 ) {
          event.doit = false;
          return;
        }

        spoon.hideToolTips();

        TreeSelection treeObject = treeObjects[0];
        Object object = treeObject.getSelection();
        TransMeta transMeta = spoon.getActiveTransformation();
        // JobMeta jobMeta = spoon.getActiveJob();

        if ( object instanceof StepMeta
          || object instanceof PluginInterface || ( object instanceof DatabaseMeta && transMeta != null )
          || object instanceof TransHopMeta || object instanceof JobEntryCopy ) {
          event.doit = true;
        } else {
          event.doit = false;
        }
      }

      @Override
      public void dragSetData( DragSourceEvent event ) {
        TreeSelection[] treeObjects = getTreeObjects( tree, selectionTree, coreObjectsTree );
        if ( treeObjects.length == 0 ) {
          event.doit = false;
          return;
        }

        int type = 0;
        String id = null;
        String data = null;

        TreeSelection treeObject = treeObjects[0];
        Object object = treeObject.getSelection();

        if ( object instanceof StepMeta ) {
          StepMeta stepMeta = (StepMeta) object;
          type = DragAndDropContainer.TYPE_STEP;
          data = stepMeta.getName(); // name of the step.
        } else if ( object instanceof PluginInterface ) {
          PluginInterface plugin = (PluginInterface) object;
          Class<? extends PluginTypeInterface> pluginType = plugin.getPluginType();
          if ( Const.classIsOrExtends( pluginType, StepPluginType.class ) ) {
            type = DragAndDropContainer.TYPE_BASE_STEP_TYPE;
            id = plugin.getIds()[ 0 ];
            data = plugin.getName(); // Step type name
          } else {
            type = DragAndDropContainer.TYPE_BASE_JOB_ENTRY;
            data = plugin.getName(); // job entry type name
            if ( treeObject.getItemText().equals( JobMeta.createStartEntry().getName() ) ) {
              data = treeObject.getItemText();
            } else if ( treeObject.getItemText().equals( JobMeta.createDummyEntry().getName() ) ) {
              data = treeObject.getItemText();
            }
          }
        } else if ( object instanceof DatabaseMeta ) {
          DatabaseMeta databaseMeta = (DatabaseMeta) object;
          type = DragAndDropContainer.TYPE_DATABASE_CONNECTION;
          data = databaseMeta.getName();
        } else if ( object instanceof TransHopMeta ) {
          TransHopMeta hop = (TransHopMeta) object;
          type = DragAndDropContainer.TYPE_TRANS_HOP;
          data = hop.toString(); // nothing for really ;-)
        } else if ( object instanceof JobEntryCopy ) {
          JobEntryCopy jobEntryCopy = (JobEntryCopy) object;
          type = DragAndDropContainer.TYPE_JOB_ENTRY;
          data = jobEntryCopy.getName(); // name of the job entry.
        } else {
          event.doit = false;
          return; // ignore anything else you drag.
        }

        event.data = new DragAndDropContainer( type, data, id );
      }

      @Override
      public void dragFinished( DragSourceEvent event ) {
      }
    } );

  }

  private void executeExtensionPoint( SpoonTreeDelegateExtension extension ) {
    try {
      ExtensionPointHandler
          .callExtensionPoint( log, KettleExtensionPoint.SpoonTreeDelegateExtension.id, extension );
    } catch ( Exception e ) {
      log.logError( "Error handling SpoonTreeDelegate through extension point", e );
    }
  }

}
