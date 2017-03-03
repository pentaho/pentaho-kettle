/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.repository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.repository.dialog.SelectDirectoryDialog;


public class RepositoryDirectoryUI {

  private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

  /**
   * Set the name of this directory on a TreeItem. Also, create children on this TreeItem to reflect the subdirectories.
   * In these sub-directories, fill in the available transformations from the repository.
   *
   * @param ti
   *          The TreeItem to set the name on and to create the subdirectories
   * @param rep
   *          The repository
   * @param objectMap
   *          The tree path to repository object mapping to populate.
   * @param dircolor
   *          The color in which the directories will be drawn.
   * @param sortPosition
   *          The sort position
   * @param ascending
   *          The ascending flag
   * @param getTransformations
   *          Include transformations in the tree or not
   * @param getJobs
   *          Include jobs in the tree or not
   * @throws KettleDatabaseException
   */
  public static void getTreeWithNames( TreeItem ti, Repository rep, Color dircolor, int sortPosition,
    boolean includeDeleted, boolean ascending, boolean getTransformations, boolean getJobs,
    RepositoryDirectoryInterface dir, String filterString, Pattern pattern ) throws KettleDatabaseException {
    ti.setText( dir.getName() );
    ti.setData( dir );
    ti.setData( "isFolder", true );

    // First, we draw the directories
    List<RepositoryDirectoryInterface> children = dir.getChildren();
    Collections.sort( children, new Comparator<RepositoryDirectoryInterface>() {
      @Override
      public int compare( RepositoryDirectoryInterface o1, RepositoryDirectoryInterface o2 ) {
        return o1.getName().compareToIgnoreCase( o2.getName() );
      }
    } );
    for ( int i = 0; i < children.size(); i++ ) {
      RepositoryDirectory subdir = (RepositoryDirectory) children.get( i );
      TreeItem subti = new TreeItem( ti, SWT.NONE );
      subti.setImage( GUIResource.getInstance().getImageFolder() );
      subti.setData( "isFolder", true );
      getTreeWithNames(
        subti, rep, dircolor, sortPosition, includeDeleted, ascending, getTransformations, getJobs, subdir,
        filterString, pattern );
    }

    List<RepositoryElementMetaInterface> repositoryObjects =
        loadRepositoryObjects( dir, getTransformations, getJobs, rep );

    // Sort the directory list appropriately...
    RepositoryObject.sortRepositoryObjects( repositoryObjects, sortPosition, ascending );

    addToTree( ti, filterString, pattern, repositoryObjects );

    ti.setExpanded( dir.isRoot() );
  }

  protected static List<RepositoryElementMetaInterface> loadRepositoryObjects( RepositoryDirectoryInterface dir,
      boolean getTransformations, boolean getJobs, Repository rep ) throws KettleDatabaseException {
    // Then show the transformations & jobs in that directory...
    List<RepositoryElementMetaInterface> repositoryObjects = new ArrayList<RepositoryElementMetaInterface>();
    if ( dir.getRepositoryObjects() == null ) {
      try {
        dir.setRepositoryObjects( rep.getJobAndTransformationObjects( dir.getObjectId(), false ) );
      } catch ( KettleException e ) {
        throw new KettleDatabaseException( e );
      }
    }

    List<RepositoryObjectType> allowedTypes = new ArrayList<>( 2 );
    if ( getTransformations ) {
      allowedTypes.add( RepositoryObjectType.TRANSFORMATION );
    }
    if ( getJobs ) {
      allowedTypes.add( RepositoryObjectType.JOB );
    }
    for ( RepositoryElementMetaInterface repoObject : dir.getRepositoryObjects() ) {
      if ( allowedTypes.contains( repoObject.getObjectType() ) ) {
        repositoryObjects.add( repoObject );
      }
    }
    return repositoryObjects;
  }

  private static void addToTree( TreeItem ti, String filterString, Pattern pattern,
      List<RepositoryElementMetaInterface> repositoryObjects ) {
    for ( int i = 0; i < repositoryObjects.size(); i++ ) {
      boolean add = false;
      RepositoryElementMetaInterface repositoryObject = repositoryObjects.get( i );

      if ( filterString == null && pattern == null ) {
        add = true;
      } else {
        add |= addItem( repositoryObject.getName(), filterString, pattern );
        add |= addItem( repositoryObject.getDescription(), filterString, pattern );
        add |= addItem( repositoryObject.getModifiedUser(), filterString, pattern );
        if ( !add && repositoryObject.getModifiedDate() != null ) {
          SimpleDateFormat simpleDateFormat = new SimpleDateFormat( DATE_FORMAT );
          add = addItem( simpleDateFormat.format( repositoryObject.getModifiedDate() ), filterString, pattern );
        }
        if ( !add && repositoryObject.getObjectType() != null ) {
          add = addItem( repositoryObject.getObjectType().getTypeDescription(), filterString, pattern );
        }
      }

      if ( add ) {
        createTreeItem( ti, repositoryObject );
      }
    }
  }

  private static void createTreeItem( TreeItem parent, RepositoryElementMetaInterface repositoryObject ) {
    TreeItem tiObject = new TreeItem( parent, SWT.NONE );
    tiObject.setData( repositoryObject );
    if ( repositoryObject.getObjectType() == RepositoryObjectType.TRANSFORMATION ) {
      tiObject.setImage( GUIResource.getInstance().getImageTransRepo() );
    } else if ( repositoryObject.getObjectType() == RepositoryObjectType.JOB ) {
      tiObject.setImage( GUIResource.getInstance().getImageJobRepo() );
    }

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat( DATE_FORMAT );
    tiObject.setText( 0, Const.NVL( repositoryObject.getName(), "" ) );
    tiObject.setText( 1, Const.NVL( repositoryObject.getObjectType().getTypeDescription(), "" ).toUpperCase() );
    tiObject.setText( 2, Const.NVL( repositoryObject.getModifiedUser(), "" ) );
    tiObject.setText( 3, repositoryObject.getModifiedDate() != null ? simpleDateFormat
      .format( repositoryObject.getModifiedDate() ) : "" );
    tiObject.setText( 4, Const.NVL( repositoryObject.getDescription(), "" ) );

    if ( repositoryObject.isDeleted() ) {
      tiObject.setForeground( GUIResource.getInstance().getColorRed() );
    }
  }

  private static boolean addItem( String name, String filter, Pattern pattern ) {
    boolean add = false;
    if ( name != null ) {
      if ( pattern != null ) {
        Matcher matcher = pattern.matcher( name );
        if ( matcher.matches() ) {
          add = true;
        }
      } else {
        if ( name.toUpperCase().indexOf( filter ) >= 0 ) {
          add = true;
        }
      }
    }
    return add;
  }

  /**
   * Gets a directory tree on a TreeItem to work with.
   *
   * @param ti
   *          The TreeItem to set the directory tree on
   * @param dircolor
   *          The color of the directory tree item.
   */
  public static void getDirectoryTree( TreeItem ti, Color dircolor, RepositoryDirectoryInterface dir ) {
    ti.setText( dir.getName() );
    ti.setForeground( dircolor );

    // First, we draw the directories
    for ( int i = 0; i < dir.getNrSubdirectories(); i++ ) {
      RepositoryDirectory subdir = dir.getSubdirectory( i );
      TreeItem subti = new TreeItem( ti, SWT.NONE );
      subti.setImage( GUIResource.getInstance().getImageFolder() );
      getDirectoryTree( subti, dircolor, subdir );
    }
  }

  public static RepositoryDirectoryInterface chooseDirectory( Shell shell, Repository rep, RepositoryDirectoryInterface directoryFrom ) {
    if ( rep == null ) {
      return null;
    }

    if ( directoryFrom == null ) {
      try {
        directoryFrom = rep.getUserHomeDirectory();
      } catch ( KettleException ex ) {
        directoryFrom = new RepositoryDirectory();
      }
    }
    ObjectId idDirectoryFrom = directoryFrom.getObjectId();

    SelectDirectoryDialog sdd = new SelectDirectoryDialog( shell, SWT.NONE, rep );

    //PDI-13867: root dir and its direct subdirectories are restricted.
    HashSet<String> restrictedPaths = new HashSet<String>();
    restrictedPaths.add( directoryFrom.findRoot().getPath() );
    restrictedPaths.add( "/home" );
    sdd.setRestrictedPaths( restrictedPaths );

    //TODO: expand and select directoryFrom in the dialog.

    RepositoryDirectoryInterface rd = sdd.open();
    if ( rd == null || idDirectoryFrom == rd.getObjectId() ) {
      return null;
    }
    return rd;
  }

}
