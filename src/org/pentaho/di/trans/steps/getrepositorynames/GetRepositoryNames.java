/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.trans.steps.getrepositorynames;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Read all sorts of text files, convert them to rows and writes these to one or
 * more output streams.
 * 
 * @author Matt
 * @since 4-apr-2003
 */
public class GetRepositoryNames extends BaseStep implements StepInterface {
  private static Class<?>        PKG = GetRepositoryNamesMeta.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  private GetRepositoryNamesMeta meta;

  private GetRepositoryNamesData data;

  public GetRepositoryNames(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
  }

  /**
   * Build an empty row based on the meta-data...
   * 
   * @return
   */

  private Object[] buildEmptyRow() {
    Object[] rowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());

    return rowData;
  }

  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
    if (first) {
      first = false;

      data.outputRowMeta = new RowMeta();
      meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
    }
    
    if (data.filenr>=data.list.size()) {
      setOutputDone();
      return false;
    }
    
    // Get the next repository object from the list...
    //
    RepositoryElementMetaInterface repositoryObject = data.list.get(data.filenr++);
    
    Object[] outputRow = buildEmptyRow();
    int outputIndex = 0;
    
    String directory = repositoryObject.getRepositoryDirectory().getPath();
    String name = repositoryObject.getName();
    String path = directory.endsWith("/") ? directory+name : directory+"/"+name;

    outputRow[outputIndex++] = path;        // the directory and name of the object
    outputRow[outputIndex++] = directory;   // the directory
    outputRow[outputIndex++] = name     ;   // the name
    outputRow[outputIndex++] = repositoryObject.getObjectType().getTypeDescription();   // the object type
    outputRow[outputIndex++] = repositoryObject.getObjectId().toString();   // the object ID
    outputRow[outputIndex++] = repositoryObject.getModifiedUser();   // modified user
    outputRow[outputIndex++] = repositoryObject.getModifiedDate();   // modified date

    if (meta.isIncludeRowNumber()) {
      outputRow[outputIndex++] = Long.valueOf(data.rownr++);
    }

    // Finally, let's give this row of data to the next steps...
    //
    putRow(data.outputRowMeta, outputRow);
    if (checkFeedback(getLinesInput())) {
      if (log.isBasic())
        logBasic(BaseMessages.getString(PKG, "GetRepositoryNames.Log.NrLine", "" + getLinesInput()));
    }

    return true;
  }

  public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
    meta = (GetRepositoryNamesMeta) smi;
    data = (GetRepositoryNamesData) sdi;

    if (super.init(smi, sdi)) {

      try {
        // Get the repository objects from the repository...
        //
        data.list = getRepositoryObjects();

      } catch (Exception e) {
        logError("Error initializing step: ", e);
        return false;
      }

      data.rownr = 1L;
      data.filenr = 0;

      return true;
    }
    return false;
  }

  private List<RepositoryElementMetaInterface> getRepositoryObjects() throws KettleException {
    
    try {

      // The repository is available in the parent transformation metadata
      //
      Repository repository = getTransMeta().getRepository();
      
      // Get the repository directory tree.
      //
      RepositoryDirectoryInterface tree = repository.loadRepositoryDirectoryTree();
      
      // Now populate the list...
      //
      List<RepositoryElementMetaInterface> list = new ArrayList<RepositoryElementMetaInterface>();
      
      // Loop over the directories and add the discovered objects to the list...
      //
      for (int i=0;i<meta.getDirectory().length;i++) {
        
        RepositoryDirectoryInterface dir = tree.findDirectory(meta.getDirectory()[i]);
        if (dir!=null) {
          List<RepositoryElementMetaInterface> objects = getRepositoryObjects(repository, dir, meta.getIncludeSubFolders()[i], meta.getNameMask()[i], meta.getExcludeNameMask()[i]);
          list.addAll( objects );
        }
      }
      
      return list;
    } catch(Exception e) {
      throw new KettleException("Unable to get the list of repository objects from the repository", e);
    }
  }

  private List<RepositoryElementMetaInterface> getRepositoryObjects(Repository repository, RepositoryDirectoryInterface directory, boolean subdirs, String nameMask, String excludeNameMask) throws KettleException {
    List<RepositoryElementMetaInterface> list = new ArrayList<RepositoryElementMetaInterface>();
    List<RepositoryElementMetaInterface> objects = new ArrayList<RepositoryElementMetaInterface>();
    if (meta.getObjectTypeSelection().areTransformationsSelected()) {
      objects.addAll( repository.getTransformationObjects(directory.getObjectId(), false) );
    }
    if (meta.getObjectTypeSelection().areJobsSelected()) {
      objects.addAll( repository.getJobObjects(directory.getObjectId(), false) );
    }
    
    for (RepositoryElementMetaInterface object : objects) {
      boolean add = false;
      if (Const.isEmpty(nameMask) || object.getName().matches(nameMask)) {
        add=true;
      }
      if (!Const.isEmpty(excludeNameMask) && object.getName().matches(excludeNameMask)) {
        add=false;
      }
      if (add) {
        list.add(object);
      }
    }
    
    if (subdirs) {
      for (RepositoryDirectoryInterface child : directory.getChildren()) {
        list.addAll( getRepositoryObjects(repository, child, subdirs, nameMask, excludeNameMask) );
      }
    }
    
    return list;
  }
}