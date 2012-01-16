/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core;

import java.util.Date;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementInterface;

public interface EngineMetaInterface extends RepositoryElementInterface {

	public void setFilename(String filename);
	
	public String getName();
	
	public void nameFromFilename();
	
	public void clearChanged();
	
	public String getXML() throws KettleException;
	
	public String getFileType();
	
    public String[] getFilterNames();

    public String[] getFilterExtensions();
    
    public String getDefaultExtension();
    
    public void setObjectId( ObjectId id );
 
    public Date getCreatedDate();
    
    public void setCreatedDate(Date date);
    
    public boolean canSave();
    
    public String getCreatedUser();
    
    public void setCreatedUser(String createduser);
    
    public Date getModifiedDate();
    
    public void setModifiedDate(Date date);
    
    public void setModifiedUser( String user );
    
    public String getModifiedUser( );
    
    public RepositoryDirectoryInterface getRepositoryDirectory();
    
    public String getFilename();
    
    public void saveSharedObjects() throws KettleException;
    
    public void setInternalKettleVariables();
}
