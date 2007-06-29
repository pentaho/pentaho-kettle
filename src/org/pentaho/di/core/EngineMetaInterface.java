package org.pentaho.di.core;

import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.spoon.Spoon;

public interface EngineMetaInterface {

	public void setFilename(String filename);
	
	public String getName();
	
	public void nameFromFilename();
	
	public void clearChanged();
	
	public String getXML();
	
	public String getFileType();
	
    public String[] getFilterNames();

    public String[] getFilterExtensions();
    
    public String getDefaultExtension();
    
    public void setID( long id );
 
    public Date getCreatedDate();
    
    public void setCreatedDate(Date date);
    
    public String getCreatedUser();
    
    public void setCreatedUser(String createduser);
    
    public Date getModifiedDate();
    
    public void setModifiedDate(Date date);
    
    public void setModifiedUser( String user );
    
    public String getModifiedUser( );
    
    public RepositoryDirectory getDirectory();
    
    public boolean editProperties(Spoon spoon, Repository rep);
    
    public boolean showReplaceWarning(Repository rep);
    
    public void saveRep(Repository rep, IProgressMonitor monitor) throws KettleException;
    
    public String getFilename();
    
    public boolean saveSharedObjects();
    
    public void setInternalKettleVariables();
}
