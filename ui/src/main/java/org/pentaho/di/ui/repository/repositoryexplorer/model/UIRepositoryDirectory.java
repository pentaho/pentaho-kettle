/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.repository.repositoryexplorer.model;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryObjectType;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class UIRepositoryDirectory extends UIRepositoryObject {

  private static final long serialVersionUID = -2003651575793768451L;

  private RepositoryDirectoryInterface rd;
  private UIRepositoryDirectory uiParent = null;
  private UIRepositoryDirectories kidDirectoryCache = null;
  private UIRepositoryObjects kidElementCache = null;
  private boolean expanded = false;
  private boolean populated = false;

  public UIRepositoryDirectory() {
    super();
    kidDirectoryCache = null;
    kidElementCache = null;
  }

  public UIRepositoryDirectory( RepositoryDirectoryInterface rd, UIRepositoryDirectory uiParent, Repository rep ) {
    super( rd, rep );
    this.uiParent = uiParent;
    this.rd = rd;
    kidDirectoryCache = null;
    kidElementCache = null;
  }

  public UIRepositoryDirectories getChildren() {
    if ( kidDirectoryCache == null ) {
      kidDirectoryCache = new UIRepositoryDirectories();

      if ( getParent() != null ) {
        RepositoryDirectory repositoryDirectory = new RepositoryDirectory();
        repositoryDirectory.setObjectId( null );
        UIRepositoryDirectory uiRepositoryDirectory = new UIRepositoryDirectory( repositoryDirectory, null, null );
        kidDirectoryCache.add( uiRepositoryDirectory );
      }
    }
    return kidDirectoryCache;
  }

  public void populateChildren() {
    if ( kidDirectoryCache == null || !populated ) {
      kidDirectoryCache = new UIRepositoryDirectories();
    }

    for ( RepositoryDirectoryInterface child : rd.getChildren() ) {
      try {
        kidDirectoryCache.add( UIObjectRegistry.getInstance().constructUIRepositoryDirectory( child, this, rep ) );
      } catch ( UIObjectCreationException e ) {
        kidDirectoryCache.add( new UIRepositoryDirectory( child, this, rep ) );
      }
    }
    populated = true;
  }

  public boolean isPopulated() {
    return populated;
  }

  public void setPopulated( boolean populated ) {
    this.populated = populated;
  }

  public void cleanup() {
    if ( !populated ) {
      kidDirectoryCache = new UIRepositoryDirectories();
    }
  }

  public void setChildren( UIRepositoryDirectories children ) {
    kidDirectoryCache = children;
  }

  // TODO: Abstract working model; should throw RepositoryException
  // TODO: We will need a way to reset this cache when a directory or element changes
  public UIRepositoryObjects getRepositoryObjects() throws KettleException {
    // We've been here before.. use the cache
    if ( getObjectId() == null ) {
      return new UIRepositoryObjects();
    }

    if ( kidElementCache != null ) {
      return kidElementCache;
    } else if ( uiParent != null && getName() != null && uiParent.checkDirNameExistsInRepo( getName() ) == null ) {
      kidElementCache = new UIRepositoryObjects();
      return kidElementCache;
    }

    if ( kidElementCache == null ) {
      kidElementCache = new UIRepositoryObjects() {
        private static final long serialVersionUID = 6901479331535375165L;

        public void onRemove( UIRepositoryObject child ) {
          List<? extends RepositoryElementMetaInterface> dirRepoObjects = getDirectory().getRepositoryObjects();
          if ( dirRepoObjects != null ) {
            Iterator<? extends RepositoryElementMetaInterface> iter = dirRepoObjects.iterator();
            while ( iter.hasNext() ) {
              RepositoryElementMetaInterface e = iter.next();
              if ( child.getObjectId().equals( e.getObjectId() ) ) {
                iter.remove();
                return;
              }
            }
          }
        }
      };
    }

    if ( !populated ) {
      populateChildren();
    }
    for ( UIRepositoryObject child : getChildren() ) {
      kidElementCache.add( child );
    }

    List<RepositoryElementMetaInterface> jobsAndTransformations = getDirectory().getRepositoryObjects();
    if ( jobsAndTransformations == null ) {
      RepositoryDirectoryInterface dir = getDirectory();
      jobsAndTransformations = rep.getJobAndTransformationObjects( dir.getObjectId(), false );
      dir.setRepositoryObjects( jobsAndTransformations );
    }

    for ( RepositoryElementMetaInterface child : jobsAndTransformations ) {
      if ( child.getObjectType().equals( RepositoryObjectType.TRANSFORMATION ) ) {
        try {
          kidElementCache.add( UIObjectRegistry.getInstance().constructUITransformation( child, this, rep ) );
        } catch ( UIObjectCreationException e ) {
          kidElementCache.add( new UITransformation( child, this, rep ) );
        }
      } else if ( child.getObjectType().equals( RepositoryObjectType.JOB ) ) {
        try {
          kidElementCache.add( UIObjectRegistry.getInstance().constructUIJob( child, this, rep ) );
        } catch ( UIObjectCreationException e ) {
          kidElementCache.add( new UIJob( child, this, rep ) );
        }
      }
    }
    return kidElementCache;
  }

  public String toString() {
    return getName();
  }

  public void setName( String name ) throws Exception {
    if ( getDirectory().getName().equalsIgnoreCase( name ) ) {
      return;
    }

    rep.renameRepositoryDirectory( getDirectory().getObjectId(), null, name );
    // Update the object reference so the new name is displayed
    obj = rep.findDirectory( getObjectId() );
    getParent().refresh();
  }

  public String getDescription() {
    return null;
  }

  public String getLockMessage() {
    return null;
  }

  public Date getModifiedDate() {
    return null;
  }

  public String getModifiedUser() {
    return null;
  }

  public RepositoryObjectType getRepositoryElementType() {
    return null;
  }

  @Override
  public boolean isDeleted() {
    return super.isDeleted();
  }

  @Override
  public String getType() {
    return null;
  }

  @Override
  public String getFormatModifiedDate() {
    return null;
  }

  public RepositoryDirectory getDirectory() {
    return (RepositoryDirectory) rd;
  }

  @Override
  public String getImage() {
    return "ui/images/folder.svg";
  }

  public void delete() throws Exception {
    if ( uiParent.checkDirNameExistsInRepo( getName() ) != null ) {
      rep.deleteRepositoryDirectory( getDirectory() );
    }
    uiParent.getChildren().remove( this );
    if ( uiParent.getRepositoryObjects().contains( this ) ) {
      uiParent.getRepositoryObjects().remove( this );
    }
    uiParent.refresh();
  }

  /**
   * Check if a subdirectory already exists in the repository.
   * This is to help fix PDI-5202
   * Since the ui directories are case insensitive, we look for a repo directory with the same name ignoring case.
   * If we find an existing directory, we return the name so we can use that to get hold of the directory
   * as it is known in the repository.
   * If we don't find such a directory, we return null
   * @param name - the name of a subdirectory
   * @return null if the subdirectory does not exist, or the name of the subdirectory as it is known inside the repo.
   * @throws KettleException
   */
  public String checkDirNameExistsInRepo( String name ) throws KettleException {
    String[] dirNames = rep.getDirectoryNames( getObjectId() );
    for ( String dirName : dirNames ) {
      if ( dirName.equalsIgnoreCase( name ) ) {
        return dirName;
      }
    }
    return null;
  }

  public UIRepositoryDirectory createFolder( String name ) throws Exception {
    RepositoryDirectoryInterface thisDir = getDirectory();
    RepositoryDirectoryInterface dir;
    //PDI-5202: the directory might exist already. If so, don't create a new one.
    String dirName = checkDirNameExistsInRepo( name );
    if ( dirName == null ) {
      dir = rep.createRepositoryDirectory( thisDir, name );
    } else {
      dir = rep.findDirectory( thisDir.getPath() + "/" + dirName );
    }
    UIRepositoryDirectory newDir = null;
    try {
      newDir = UIObjectRegistry.getInstance().constructUIRepositoryDirectory( dir, this, rep );
    } catch ( UIObjectCreationException uoe ) {
      newDir = new UIRepositoryDirectory( dir, this, rep );
    }
    UIRepositoryDirectories directories = getChildren();
    if ( !contains( directories, newDir ) ) {
      directories.add( newDir );
    } else {
      throw new KettleException( "Unable to create folder with the same name [" + name + "]" );
    }
    kidElementCache = null; // rebuild the element cache for correct positioning.
    return newDir;
  }

  public void fireCollectionChanged() {

    firePropertyChange( "children", null, getChildren() );

    getChildren(); // prime cache before firing event (already primed from above getChildren call but to be consistent)
    kidDirectoryCache.fireCollectionChanged();
    try {
      getRepositoryObjects(); // prime cache before firing event
      if ( kidElementCache != null ) {
        kidElementCache.fireCollectionChanged();
      }
    } catch ( KettleException ignored ) {
      // Ignore errors
    }
  }

  @Override
  public void move( UIRepositoryDirectory newParentDir ) throws Exception {
    if ( newParentDir != null ) {
      rep.renameRepositoryDirectory( obj.getObjectId(), newParentDir.getDirectory(), null );
      // Try to make sure the directories are updated properly
      if ( !newParentDir.equals( getParent() ) ) {
        getParent().getChildren().remove( this );
        newParentDir.getChildren().add( this );
        getParent().refresh();
        newParentDir.refresh();
      }
    }
  }

  protected UIRepositoryDirectory getParentDirectory() {
    return uiParent;
  }

  protected UIRepositoryDirectory getRootDirectory() {
    UIRepositoryDirectory parent = uiParent, result = this;

    while ( parent != null ) {
      result = parent;
      parent = parent.getParentDirectory();
    }

    return result;
  }

  /**
   * Synchronize this folder with the back-end
   *
   *
   */
  public void refresh() {
    kidElementCache = null;
    kidDirectoryCache = null;
    rd.clear();
    populateChildren();
    try {
      if ( obj != null ) {
        getRepositoryObjects();
      }
    } catch ( KettleException ignored ) {
      // Ignored
    }
    fireCollectionChanged();
  }

  @Override
  public int getCategory() {
    return 10;
  }

  public boolean isExpanded() {
    return expanded;
  }

  public void setExpanded( boolean expand ) {
    this.expanded = expand;
  }

  public void toggleExpanded() {
    setExpanded( !isExpanded() );
    firePropertyChange( "expanded", null, this.expanded );
  }

  public UIRepositoryDirectory getParent() {
    return uiParent;
  }

  public String getPath() {
    return ( (RepositoryDirectory) rd ).getPath();
  }

  public boolean isVisible() {
    return rd.isVisible();
  }

  // begin PDI-3326 hack

  @Override
  public int size() {
    return getChildren().size();
  }

  @Override
  public UIRepositoryObject get( int index ) {
    return getChildren().get( index );
  }

  @Override
  public Iterator<UIRepositoryObject> iterator() {
    return getChildren().iterator();
  }

  public boolean contains( String dirName ) {
    UIRepositoryDirectories directories = getChildren();
    UIRepositoryObject dir;
    for ( int i = 0; i < directories.size(); i++ ) {
      dir = directories.get( i );
      if ( !( dir instanceof UIRepositoryDirectory ) ) {
        continue;
      } else if ( dir.getName() == null && dirName == null ) {
        return true;
      } else if ( dir.getName().equalsIgnoreCase( dirName ) ) {
        return true;
      }
    }
    return false;
  }

  private boolean contains( UIRepositoryDirectories directories, UIRepositoryDirectory searchDir ) {
    for ( int i = 0; i < directories.size(); i++ ) {
      UIRepositoryObject dir = directories.get( i );
      if ( dir instanceof UIRepositoryDirectory ) {
        if ( dir.getName() != null && dir.getName().equals( searchDir.getName() ) ) {
          return true;
        }
      }
    }
    return false;
  }

  // end PDI-3326 hack

  // Must implement equals/hashcode to compare object ids since the cache of directories may be refreshed
  // and therefore would not be the same instances
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    ObjectId id = getObjectId();
    result = prime * result + ( ( id == null ) ? 0 : id.hashCode() );
    return result;
  }

  @Override
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( obj == null ) {
      return false;
    }
    if ( getClass() != obj.getClass() ) {
      return false;
    }
    UIRepositoryDirectory other = (UIRepositoryDirectory) obj;
    ObjectId id = getObjectId();
    ObjectId otherId = other.getObjectId();
    if ( id == null ) {
      if ( otherId != null ) {
        return false;
      }
    } else if ( !id.equals( otherId ) ) {
      return false;
    }
    return true;
  }
}
