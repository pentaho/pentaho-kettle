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
 * A repository element is an object that can be saved or loaded from the repository.
 * As such, we need to be able to identify it.
 * It needs a RepositoryDirectory, a name and an ID.
 * 
 * We also need to identify the type of the element.
 *   
 * Finally, we need to be able to optionally identify the revision of the element. 
 * 
 * @author matt
 *
 */
public interface RepositoryElementInterface extends RepositoryObjectInterface {
  
  public RepositoryDirectoryInterface getRepositoryDirectory();
  public void setRepositoryDirectory(RepositoryDirectoryInterface repositoryDirectory);
	
	public String getName();
	public void setName(String name);

	public String getDescription();
	public void setDescription(String description);

	public ObjectId getObjectId();
	public void setObjectId(ObjectId id);
			
	public RepositoryObjectType getRepositoryElementType();
	
  public ObjectRevision getObjectRevision();	
	public void setObjectRevision(ObjectRevision objectRevision);

}
