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
package org.pentaho.di.repository;

/**
 * The RepositoryObjectInterface represents all objects that can come out of a repository, including
 * directories (RepositoryDirectoryInterface), elements such as TransMeta and JobMeta 
 * (RepositoryElementMetaInterface), and metadata about elements (RepositoryElementMetaInterface).
 * 
 * All repository objects have a name and id.
 */
public interface RepositoryObjectInterface {

  /**
   * The name of the repository object
   * 
   * @return the name of the object
   */
  public String getName();

  /**
   * The id of the object
   * 
   * @return the id of the object
   */
  public ObjectId getObjectId();
  
}
