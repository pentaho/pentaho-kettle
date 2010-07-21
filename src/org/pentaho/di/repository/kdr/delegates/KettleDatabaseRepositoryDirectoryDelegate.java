/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.repository.kdr.delegates;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;

public class KettleDatabaseRepositoryDirectoryDelegate extends KettleDatabaseRepositoryBaseDelegate {
  private static Class<?> PKG = RepositoryDirectory.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  public KettleDatabaseRepositoryDirectoryDelegate(KettleDatabaseRepository repository) {
    super(repository);
  }

  public RowMetaAndData getDirectory(ObjectId id_directory) throws KettleException {
    return repository.connectionDelegate.getOneRow(quoteTable(KettleDatabaseRepository.TABLE_R_DIRECTORY),
        quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY), id_directory);
  }

  public RepositoryDirectoryInterface loadPathToRoot(ObjectId id_directory) throws KettleException {
    List<RepositoryDirectory> path = new ArrayList<RepositoryDirectory>();
    
    ObjectId directoryId = id_directory;
    
    RowMetaAndData directoryRow = getDirectory(directoryId);
    Long parentId= directoryRow.getInteger(1);
    
    // Do not load root itself, it doesn't exist.
    //
    while (parentId!=null && parentId>=0) { 
      RepositoryDirectory directory = new RepositoryDirectory();
      directory.setName(directoryRow.getString(2, null)); // Name of the directory
      directory.setObjectId(directoryId);
      path.add(directory);
      
      directoryId = new LongObjectId(parentId);
      directoryRow = getDirectory(directoryId);
      parentId = directoryRow.getInteger(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT);
    }

    RepositoryDirectory root = new RepositoryDirectory();
    root.setObjectId(new LongObjectId(0));
    path.add(root);

    // Connect the directories to each other.
    //
    for (int i=0;i<path.size()-1;i++) {
      RepositoryDirectory item = path.get(i);
      RepositoryDirectory parent = path.get(i+1);
      item.setParent(parent);
      parent.addSubdirectory(item);
    }
    
    RepositoryDirectory repositoryDirectory = path.get(0);
    return repositoryDirectory;
  }
  
  public RepositoryDirectoryInterface loadRepositoryDirectoryTree(RepositoryDirectoryInterface root) throws KettleException {
    try {
      root.clear();
      ObjectId subids[] = repository.getSubDirectoryIDs(root.getObjectId());
      for (int i = 0; i < subids.length; i++) {
        RepositoryDirectory subdir = new RepositoryDirectory();
        loadRepositoryDirectory(subdir, subids[i]);
        root.addSubdirectory(subdir);
      }

      return root;
    } catch (Exception e) {
      throw new KettleException("An error occured loading the directory tree from the repository", e);
    }
  }

  public void loadRepositoryDirectory(RepositoryDirectory repositoryDirectory, ObjectId id_directory)
      throws KettleException {
    if (id_directory == null) {
      // This is the root directory, id = OL
      id_directory = new LongObjectId(0L);
    }

    try {
      RowMetaAndData row = getDirectory(id_directory);
      if (row != null) {
        repositoryDirectory.setObjectId(id_directory);

        // Content?
        //
        repositoryDirectory.setName(row.getString("DIRECTORY_NAME", null));

        // The sub-directories?
        //
        ObjectId subids[] = repository.getSubDirectoryIDs(repositoryDirectory.getObjectId());
        for (int i = 0; i < subids.length; i++) {
          RepositoryDirectory subdir = new RepositoryDirectory();
          loadRepositoryDirectory(subdir, subids[i]);
          repositoryDirectory.addSubdirectory(subdir);
        }
      }
    } catch (Exception e) {
      throw new KettleException(BaseMessages
          .getString(PKG, "Repository.LoadRepositoryDirectory.ErrorLoading.Exception"), e);
    }
  }

  /*
    public synchronized RepositoryDirectory refreshRepositoryDirectoryTree() throws KettleException
    {
        try
        {
        	RepositoryDirectory tree = new RepositoryDirectory();
        	loadRepositoryDirectory(tree, tree.getID());
            repository.setDirectoryTree(tree);
            return tree;
        }
        catch (KettleException e)
        {
            repository.setDirectoryTree( new RepositoryDirectory() );
            throw new KettleException("Unable to read the directory tree from the repository!", e);
        }
    }
    */

  public synchronized int getNrDirectories(long id_directory) throws KettleException {
    int retval = 0;

    String sql = "SELECT COUNT(*) FROM " + quoteTable(KettleDatabaseRepository.TABLE_R_DIRECTORY) + " WHERE "
        + quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT) + " = " + id_directory;
    RowMetaAndData r = repository.connectionDelegate.getOneRow(sql);
    if (r != null) {
      retval = (int) r.getInteger(0, 0L);
    }

    return retval;
  }

  private synchronized ObjectId insertDirectory(ObjectId id_directory_parent, RepositoryDirectoryInterface dir)
      throws KettleException {
    ObjectId id = repository.connectionDelegate.getNextDirectoryID();

    String tablename = KettleDatabaseRepository.TABLE_R_DIRECTORY;
    RowMetaAndData table = new RowMetaAndData();
    table.addValue(
        new ValueMeta(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY, ValueMetaInterface.TYPE_INTEGER), id);
    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT,
        ValueMetaInterface.TYPE_INTEGER), id_directory_parent);
    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DIRECTORY_DIRECTORY_NAME,
        ValueMetaInterface.TYPE_STRING), dir.getName());

    repository.connectionDelegate.getDatabase().prepareInsert(table.getRowMeta(), tablename);
    repository.connectionDelegate.getDatabase().setValuesInsert(table);
    repository.connectionDelegate.getDatabase().insertRow();
    repository.connectionDelegate.getDatabase().closeInsert();

    return id;
  }

  
  public synchronized void deleteDirectory(ObjectId id_directory) throws KettleException {
    String sql = "DELETE FROM " + quoteTable(KettleDatabaseRepository.TABLE_R_DIRECTORY) + " WHERE "
        + quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY) + " = " + id_directory;
    repository.connectionDelegate.getDatabase().execStatement(sql);
    repository.connectionDelegate.getDatabase().commit();
  }

  public synchronized void deleteDirectory(RepositoryDirectoryInterface dir) throws KettleException {
    String trans[] = repository.getTransformationNames(dir.getObjectId(), false); // TODO : include or exclude deleted objects?
    String jobs[] = repository.getJobNames(dir.getObjectId(), false); // TODO : include or exclude deleted objects?
    ObjectId[] subDirectories = repository.getSubDirectoryIDs(dir.getObjectId());
    if (trans.length == 0 && jobs.length == 0 && subDirectories.length == 0) {
      repository.directoryDelegate.deleteDirectory(dir.getObjectId());
    } else {
      deleteDirectoryRecursively(dir);
    }
  }

  private synchronized void deleteDirectoryRecursively(RepositoryDirectoryInterface dir) throws KettleException {
      String trans[] = repository.getTransformationNames(dir.getObjectId(), false); // TODO : include or exclude deleted objects?
      String jobs[] = repository.getJobNames(dir.getObjectId(), false); // TODO : include or exclude deleted objects?
      for(String transformation:trans) {
        ObjectId id =  repository.getTransformationID(transformation, dir);
        repository.deleteTransformation(id);
      }
      for(String job:jobs) {
        ObjectId id =  repository.getJobId(job, dir);
        repository.deleteJob(id);
      } 
      for(RepositoryDirectoryInterface subDir : dir.getChildren()) {
        deleteDirectoryRecursively((RepositoryDirectory)subDir);   
      }
      repository.directoryDelegate.deleteDirectory(dir.getObjectId());
  }
  /**
   * Move / rename a directory in the repository
   * 
   * @param id_directory Id of the directory to be moved/renamed
   * @param id_directory_parent Id of the new parent directory (null if the parent does not change)
   * @param newName New name for this directory (null if the name does not change)
   * @throws KettleException
   */
  public synchronized void renameDirectory(ObjectId id_directory, ObjectId id_directory_parent, String newName)
      throws KettleException {
    if (id_directory.equals(id_directory_parent)) {
      // Make sure the directory cannot become its own parent
      throw new KettleException("Failed to copy directory into itself");
    } else {
      // Make sure the directory does not become a descendant of itself
      RepositoryDirectory rd = new RepositoryDirectory();
      loadRepositoryDirectory(rd, id_directory);
      if (rd.findDirectory(id_directory_parent) != null) {
        // The parent directory is a child of this directory. Do not proceed
        throw new KettleException("Directory cannot become a child to itself");
      } else {
        // Check for duplication
        RepositoryDirectory newParent = new RepositoryDirectory();
        loadRepositoryDirectory(newParent, id_directory_parent);
        RepositoryDirectory child = newParent.findChild(newName == null ? rd.getName() : newName);
        if (child != null) {
          throw new KettleException("Destination directory already contains a diectory with requested name");
        }
      }
    }

    if (id_directory_parent != null || newName != null) {
      RowMetaAndData r = new RowMetaAndData();

      String sql = "UPDATE " + quoteTable(KettleDatabaseRepository.TABLE_R_DIRECTORY) + " SET ";
      boolean additionalParameter = false;

      if (newName != null) {
        additionalParameter = true;
        sql += quote(KettleDatabaseRepository.FIELD_DIRECTORY_DIRECTORY_NAME) + " = ?";
        r.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DIRECTORY_DIRECTORY_NAME,
            ValueMetaInterface.TYPE_STRING), newName);
      }
      if (id_directory_parent != null) {
        // Add a parameter separator if the first parm was added
        if (additionalParameter) {
          sql += ", ";
        }
        sql += quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT) + " = ?";
        r.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT,
            ValueMetaInterface.TYPE_INTEGER), id_directory_parent);
      }

      sql += " WHERE " + quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY) + " = " + id_directory;

      log.logBasic("sql = [" + sql + "]");
      log.logBasic("row = [" + r + "]");

      repository.connectionDelegate.getDatabase().execStatement(sql, r.getRowMeta(), r.getData());
    }
  }

  public synchronized int getNrSubDirectories(ObjectId id_directory) throws KettleException {
    int retval = 0;

    String sql = "SELECT COUNT(*) FROM " + quoteTable(KettleDatabaseRepository.TABLE_R_DIRECTORY) + " WHERE "
        + quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT) + " = " + id_directory;
    RowMetaAndData r = repository.connectionDelegate.getOneRow(sql);
    if (r != null) {
      retval = (int) r.getInteger(0, 0);
    }

    return retval;
  }

  public synchronized ObjectId[] getSubDirectoryIDs(ObjectId id_directory) throws KettleException {
    return repository.connectionDelegate.getIDs("SELECT "
        + quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY) + " FROM "
        + quoteTable(KettleDatabaseRepository.TABLE_R_DIRECTORY) + " WHERE "
        + quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT) + " = " + id_directory + " ORDER BY "
        + quote(KettleDatabaseRepository.FIELD_DIRECTORY_DIRECTORY_NAME));
  }

  public void saveRepositoryDirectory(RepositoryDirectoryInterface dir) throws KettleException {
    try {
      ObjectId id_directory_parent = null;
      if (dir.getParent() != null) {
        id_directory_parent = dir.getParent().getObjectId();
      }

      dir.setObjectId(insertDirectory(id_directory_parent, dir));

      log.logDetailed("New id of directory = " + dir.getObjectId());

      repository.commit();
    } catch (Exception e) {
      throw new KettleException("Unable to save directory [" + dir + "] in the repository", e);
    }
  }

  public void delRepositoryDirectory(RepositoryDirectoryInterface dir, boolean deleteNonEmptyFolder) throws KettleException {
    try {
      if (!deleteNonEmptyFolder) {
        String trans[] = repository.getTransformationNames(dir.getObjectId(), false); // TODO : include or exclude deleted objects?
        String jobs[] = repository.getJobNames(dir.getObjectId(), false); // TODO : include or exclude deleted objects?
        ObjectId[] subDirectories = repository.getSubDirectoryIDs(dir.getObjectId());
        if (trans.length == 0 && jobs.length == 0 && subDirectories.length == 0) {
          repository.directoryDelegate.deleteDirectory(dir.getObjectId());
        } else {
          throw new KettleException("This directory is not empty!");
        }
      } else {
        repository.directoryDelegate.deleteDirectory(dir);
      }
    } catch (Exception e) {
      throw new KettleException("Unexpected error deleting repository directory:", e);
    }
  }

  @Deprecated
  public ObjectId renameRepositoryDirectory(RepositoryDirectory dir) throws KettleException {
    try {
      renameDirectory(dir.getObjectId(), null, dir.getName());
      return dir.getObjectId(); // doesn't change in this specific case.
    } catch (Exception e) {
      throw new KettleException("Unable to rename the specified repository directory [" + dir + "]", e);
    }
  }

  public ObjectId renameRepositoryDirectory(ObjectId id, RepositoryDirectoryInterface newParentDir, String newName)
      throws KettleException {
    ObjectId parentId = null;
    if (newParentDir != null) {
      parentId = newParentDir.getObjectId();
    }

    try {
      renameDirectory(id, parentId, newName);
      return id; // doesn't change in this specific case.
    } catch (Exception e) {
      throw new KettleException("Unable to rename the specified repository directory [" + id + "]", e);
    }
  }

  /**
   * Create a new directory, possibly by creating several sub-directies of / at the same time.
   * 
   * @param parentDirectory the parent directory
   * @param directoryPath The path to the new Repository Directory, to be created.
   * @return The created sub-directory
   * @throws KettleException In case something goes wrong
   */
  public RepositoryDirectoryInterface createRepositoryDirectory(RepositoryDirectoryInterface parentDirectory, String directoryPath)
      throws KettleException {
    
    // RepositoryDirectoryInterface refreshedParentDir = repository.loadRepositoryDirectoryTree().findDirectory(parentDirectory.getPath());
    
    RepositoryDirectoryInterface refreshedParentDir = parentDirectory;
    String path[] = Const.splitPath(directoryPath, RepositoryDirectory.DIRECTORY_SEPARATOR);

    RepositoryDirectoryInterface parent = refreshedParentDir;
    for (int level = 0; level < path.length; level++) {

      RepositoryDirectoryInterface rd = parent.findChild(path[level]);
      if (rd == null) {
        // This child directory doesn't exists, let's add it!
        //
        rd = new RepositoryDirectory(parent, path[level]);
        saveRepositoryDirectory(rd);

        // Don't forget to add this directory to the tree!
        //
        parent.addSubdirectory(rd);

        parent = rd;
      } else {
        parent = rd;
      }
    }
    return parent;
  }

}
