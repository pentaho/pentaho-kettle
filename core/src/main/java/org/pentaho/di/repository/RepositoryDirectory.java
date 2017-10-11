/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.repository;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Node;

/**
 * This class defines the location of a transformation, job or schema in the repository. That means that it's just an
 * extra parameter for recognizing a transformation, job or schema. It allows for sub-directories by linking back to
 * itself.
 *
 * TODO: This class is referenced in a large amount of Interfaces. We should convert it into an interface.
 *
 * @author Matt
 * @since 09-nov-2004
 *
 */
public class RepositoryDirectory implements RepositoryDirectoryInterface {
  public static final String DIRECTORY_SEPARATOR = "/";

  private RepositoryDirectoryInterface parent;
  private List<RepositoryDirectoryInterface> children;
  private List<RepositoryElementMetaInterface> repositoryObjects;

  private String directoryname;

  private ObjectId id;

  /**
   * True to show this directory in UIs. Not necessarily persisted. Each repo impl decides whether to mark each dir as
   * visible.
   */
  private boolean visible = true;

  /**
   * Create a new sub-directory in a certain other directory.
   *
   * @param parent
   *          The directory to create the sub-directory in
   * @param directoryname
   *          The name of the new directory.
   */
  public RepositoryDirectory( RepositoryDirectoryInterface parent, String directoryname ) {
    this.parent = parent;
    this.directoryname = directoryname;
    this.children = new ArrayList<RepositoryDirectoryInterface>(); // default: no subdirectories...
    this.id = null; // The root directory!
  }

  /**
   * Create an empty repository directory. With the name and parent set to empty, this is the root directory.
   *
   */
  public RepositoryDirectory() {
    this( null, (String) null );
  }

  @Override
  public List<RepositoryDirectoryInterface> getChildren() {
    return children;
  }

  @Override
  public void setChildren( List<RepositoryDirectoryInterface> children ) {
    this.children = children;
  }

  @Override
  public List<RepositoryElementMetaInterface> getRepositoryObjects() {
    return repositoryObjects;
  }

  @Override
  public void setRepositoryObjects( List<RepositoryElementMetaInterface> repositoryObjects ) {
    this.repositoryObjects = repositoryObjects;
  }

  @Override
  public void clear() {
    this.parent = null;
    this.directoryname = null;
    this.children = new ArrayList<RepositoryDirectoryInterface>(); // default: no subdirectories...
  }

  /**
   * Get the database ID in the repository for this object.
   *
   * @return the database ID in the repository for this object.
   */
  @Override
  public ObjectId getObjectId() {
    return id;
  }

  /**
   * Set the database ID for this object in the repository.
   *
   * @param id
   *          the database ID for this object in the repository.
   */
  @Override
  public void setObjectId( ObjectId id ) {
    this.id = id;
  }

  /**
   * Change the parent of this directory. (move directory)
   *
   * @param parent
   *          The new parent of this directory.
   */
  @Override
  public void setParent( RepositoryDirectoryInterface parent ) {
    this.parent = parent;
  }

  /**
   * get the parent directory for this directory.
   *
   * @return The parent directory of null if this is the root directory.
   */
  @Override
  public RepositoryDirectoryInterface getParent() {
    return this.parent;
  }

  /**
   * Set the directory name (rename)
   *
   * @param directoryname
   *          The new directory name
   */
  @Override
  public void setName( String directoryname ) {
    this.directoryname = directoryname;
  }

  /**
   * Get the name of this directory...
   *
   * @return the name of this directory
   */
  @Override
  public String getName() {
    if ( directoryname == null ) {
      return DIRECTORY_SEPARATOR;
    }
    return directoryname;
  }

  /**
   * Check whether or not this is the root of the directory trees. (default)
   *
   * @return true if this is the root directory node. False if it is not.
   */
  @Override
  public boolean isRoot() {
    return parent == null && directoryname == null;
  }

  /**
   * Describe the complete path to ( and including) this directory, separated by the
   * RepositoryDirectory.DIRECTORY_SEPARATOR property (slash).
   *
   * @return The complete path to this directory.
   */
  @Override
  public String getPath() {
    // Root!
    if ( getParent() == null ) {
      return DIRECTORY_SEPARATOR;
    } else {
      if ( getParent().getParent() == null ) {
        return DIRECTORY_SEPARATOR + getName();
      } else {
        return getParent().getPath() + DIRECTORY_SEPARATOR + getName();
      }
    }
  }

  /**
   * Describe the complete path to ( and including) this directory, as an array of strings.
   *
   * @return The complete path to this directory.
   */
  @Override
  public String[] getPathArray() {
    // First determine the depth of the tree...
    int depth = 1;
    RepositoryDirectoryInterface follow = getParent();
    if ( follow != null ) {
      depth++;
      follow = follow.getParent();
    }

    // Then put something in it...
    String[] retval = new String[depth];
    int level = depth - 1;
    retval[level] = getName();

    follow = getParent();
    if ( follow != null ) {
      level--;
      retval[level] = follow.getName();

      follow = follow.getParent();
    }

    return retval;
  }

  /**
   * Add a subdirectory to this directory.
   *
   * @param subdir
   *          The subdirectory to add.
   */
  @Override
  public void addSubdirectory( RepositoryDirectoryInterface subdir ) {
    subdir.setParent( this );
    children.add( subdir );
  }

  /**
   * Counts the number of subdirectories in this directory.
   *
   * @return The number of subdirectories
   */
  @Override
  public int getNrSubdirectories() {
    return children.size();
  }

  /**
   * Get a subdirectory on a certain position.
   *
   * @param i
   *          The subdirectory position
   * @return The subdirectory with on a certain position
   */
  @Override
  public RepositoryDirectory getSubdirectory( int i ) {
    if ( children == null ) {
      return null;
    }
    return (RepositoryDirectory) children.get( i );
  }

  /**
   * Find the directory by following the path of strings
   *
   * @param path
   *          The path to the directory we're looking for.
   * @return The directory if one can be found, null if no directory was found.
   */
  @Override
  public RepositoryDirectory findDirectory( String[] path ) {
    // Is it root itself?
    if ( isRoot() && path.length == 1 && path[0].equalsIgnoreCase( DIRECTORY_SEPARATOR ) ) {
      return this;
    }

    if ( path.length < 1 ) {
      return this;
    }

    String[] directoryPath;

    // Skip the root directory, it doesn't really exist as such.
    if ( path.length > 0 && path[0].equalsIgnoreCase( DIRECTORY_SEPARATOR ) ) {
      // Copy the path exception the highest level, we go down one...
      directoryPath = new String[path.length - 1];
      for ( int x = 0; x < directoryPath.length; x++ ) {
        directoryPath[x] = path[x + 1];
      }
    } else {
      directoryPath = path;
    }

    // The root directory?
    if ( isRoot() && directoryPath.length == 1 && directoryPath[0].equalsIgnoreCase( DIRECTORY_SEPARATOR ) ) {
      return this;
    } else if ( directoryPath.length == 1 && directoryPath[0].equalsIgnoreCase( getName() ) ) {
      // This directory?
      return this;
    } else if ( directoryPath.length >= 1 ) {
      // A direct subdirectory?
      RepositoryDirectory follow = this;
      for ( int i = 0; i < directoryPath.length; i++ ) {
        RepositoryDirectory directory = follow.findChild( directoryPath[i] );
        if ( directory == null ) {
          return null;
        }
        follow = directory;
      }
      return follow;

      /*
       * for (int i=0;i<getNrSubdirectories();i++) { RepositoryDirectory subdir = getSubdirectory(i); if
       * (subdir.getDirectoryName().equalsIgnoreCase(directoryPath[0])) { if (directoryPath.length==1) return subdir; //
       * we arrived at the destination...
       *
       * // Copy the path exception the highest level, we go down one... String subpath[] = new
       * String[directoryPath.length-1]; for (int x=0;x<subpath.length;x++) subpath[x]=directoryPath[x+1];
       *
       * // Perhaps the rest of the path is the same too? RepositoryDirectory look = subdir.findDirectory(subpath); if
       * (look!=null) return look; } }
       */
    }

    return null;
  }

  /**
   * Find a directory using the path to the directory with file.separator between the dir-names.
   *
   * @param path
   *          The path to the directory
   * @return The directory if one was found, null if nothing was found.
   */
  @Override
  public RepositoryDirectory findDirectory( String path ) {
    String[] newPath = Const.splitPath( path, DIRECTORY_SEPARATOR );

    String[] p = null;

    if ( parent == null ) {
      // This doesn't include the root:
      p = new String[newPath.length + 1];
      p[0] = DIRECTORY_SEPARATOR;

      for ( int i = 0; i < newPath.length; i++ ) {
        p[i + 1] = newPath[i];
      }
    } else {
      p = newPath;
    }

    return findDirectory( p );
  }

  @Override
  public RepositoryDirectory findChild( String name ) {
    for ( RepositoryDirectoryInterface child : children ) {
      if ( child.getName().equalsIgnoreCase( name ) ) {
        return (RepositoryDirectory) child;
      }
    }
    return null;
  }

  /**
   * Find the sub-directory with a certain ID
   *
   * @param id_directory
   *          the directory ID to look for.
   * @return The RepositoryDirectory if the ID was found, null if nothing could be found.
   */
  @Override
  public RepositoryDirectory findDirectory( ObjectId id_directory ) {
    // Check for the root directory...
    //
    if ( getObjectId() == null && id_directory == null ) {
      return this;
    }

    if ( getObjectId() != null && getObjectId().equals( id_directory ) ) {
      return this;
    }

    for ( int i = 0; i < getNrSubdirectories(); i++ ) {
      RepositoryDirectory rd = getSubdirectory( i ).findDirectory( id_directory );
      if ( rd != null ) {
        return rd;
      }
    }

    return null;
  }

  /**
   * Return the description of this directory & the subdirectories in XML.
   *
   * @return The XML describing this directory.
   */
  public String getXML() {
    return getXML( 0 );
  }

  public String getXML( int level ) {
    String spaces = Const.rightPad( " ", level );
    StringBuilder retval = new StringBuilder( 200 );

    retval.append( spaces ).append( "<repdir>" ).append( Const.CR );
    retval.append( spaces ).append( "  " ).append( XMLHandler.addTagValue( "name", getName() ) );

    if ( getNrSubdirectories() > 0 ) {
      retval.append( spaces ).append( "    <subdirs>" ).append( Const.CR );
      for ( int i = 0; i < getNrSubdirectories(); i++ ) {
        RepositoryDirectory subdir = getSubdirectory( i );
        retval.append( subdir.getXML( level + 1 ) );
      }
      retval.append( spaces ).append( "    </subdirs>" ).append( Const.CR );
    }

    retval.append( spaces ).append( "</repdir>" ).append( Const.CR );

    return retval.toString();
  }

  /**
   * Load the directory & subdirectories from XML
   *
   * @param repdirnode
   *          The node in which the Repository directory information resides.
   * @return True if all went well, false if an error occured.
   */
  public boolean loadXML( Node repdirnode ) {
    try {
      clear();

      directoryname = XMLHandler.getTagValue( repdirnode, "name" );
      Node subdirsnode = XMLHandler.getSubNode( repdirnode, "subdirs" );
      if ( subdirsnode != null ) {
        int n = XMLHandler.countNodes( subdirsnode, "repdir" );
        for ( int i = 0; i < n; i++ ) {
          Node subdirnode = XMLHandler.getSubNodeByNr( subdirsnode, "repdir", i );
          RepositoryDirectory subdir = new RepositoryDirectory();
          if ( subdir.loadXML( subdirnode ) ) {
            subdir.setParent( this );
            addSubdirectory( subdir );
          } else {
            return false;
          }
        }
      }
      return true;
    } catch ( Exception e ) {
      return false;
    }
  }

  /**
   * Get all the directory-id in this directory and the subdirectories.
   *
   * @return an array of all the directory id's (this directory & subdirectories)
   */
  @Override
  public ObjectId[] getDirectoryIDs() {
    List<ObjectId> ids = new ArrayList<ObjectId>();
    getDirectoryIDs( ids );

    return ids.toArray( new ObjectId[ids.size()] );
  }

  /**
   * Fill an arraylist with all the ID_DIRECTORY values in the tree below and including this directory.
   *
   * @param ids
   *          The arraylist that will contain the directory IDs.
   */
  private void getDirectoryIDs( List<ObjectId> ids ) {
    if ( getObjectId() != null ) {
      ids.add( getObjectId() );
    }

    for ( int i = 0; i < getNrSubdirectories(); i++ ) {
      getSubdirectory( i ).getDirectoryIDs( ids );
    }
  }

  /**
   * Find the root of the directory tree starting from this directory.
   *
   * @return the root of the directory tree
   */
  @Override
  public RepositoryDirectoryInterface findRoot() {
    if ( isRoot() ) {
      return this;
    }
    return getParent().findRoot();
  }

  @Override
  public String toString() {
    return getPath();
  }

  @Override
  public String getPathObjectCombination( String transName ) {
    if ( isRoot() ) {
      return getPath() + transName;
    } else {
      return getPath() + RepositoryDirectory.DIRECTORY_SEPARATOR + transName;
    }
  }

  @Override
  public boolean isVisible() {
    return visible;
  }

  public void setVisible( boolean visible ) {
    this.visible = visible;
  }

}
