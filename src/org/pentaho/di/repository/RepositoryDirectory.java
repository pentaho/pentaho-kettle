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
 
package org.pentaho.di.repository;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Node;


/**
 * This class defines the location of a transformation, job or schema in the repository.
 * That means that it's just an extra parameter for recognising a transformation, job or schema.
 * It allows for subdirectories by linking back to itself.
 * 
 * @author Matt
 * @since  09-nov-2004
 *
 */
public class RepositoryDirectory
{
    public static final String DIRECTORY_SEPARATOR = "/";

	private RepositoryDirectory parent;
	private ArrayList<RepositoryDirectory>           children;
	
	private String directoryname;
	
	private long id;
    
	/**
	 * Create a new subdirectory in a certain other directory.
	 * @param parent The directory to create the subdirectory in
	 * @param directoryname The name of the new directory.
	 */
	public RepositoryDirectory(RepositoryDirectory parent, String directoryname)
	{
		this.parent        = parent;
		this.directoryname = directoryname;
		this.children      = new ArrayList<RepositoryDirectory>(); // default: no subdirectories...
		this.id            = 0L;              // The root directory!
	}
	
	/**
	 * Create an empty repository directory.
	 * With the name and parent set to empty, this is the root directory.
	 *
	 */
	public RepositoryDirectory()
	{
		this(null, (String)null);
	}
	
	public void clear()
	{
		this.parent        = null;
		this.directoryname = null;
		this.children      = new ArrayList<RepositoryDirectory>(); // default: no subdirectories...
		this.id            = 0L;              // The root directory!
	}
	
	/**
	 * Get the database ID in the repository for this object.
	 * @return the database ID in the repository for this object.
	 */
	public long getID()
	{
		return id;
	}

	/**
	 * Set the database ID for this object in the repository.
	 * @param id the database ID for this object in the repository.
	 */
	public void setID(long id)
	{
		this.id = id;
	}
	
	/**
	 * Change the parent of this directory. (move directory)
	 * 
	 * @param parent The new parent of this directory.
	 */
	public void setParent(RepositoryDirectory parent)
	{
		this.parent = parent;
	}

	/**
	 * get the parent directory for this directory.
	 * @return The parent directory of null if this is the root directory. 
	 */
	public RepositoryDirectory getParent()
	{
		return this.parent;
	}

	/**
	 * Set the directory name (rename)
	 * @param directoryname The new directory name
	 */
	public void setDirectoryName(String directoryname)
	{
		this.directoryname = directoryname;
	}
	
	/**
	 * Get the name of this directory...
	 * @return the name of this directory
	 */
	public String getDirectoryName()
	{
		if (directoryname==null) return DIRECTORY_SEPARATOR;
		return directoryname;
	}
	
	/**
	 * Check whether or not this is the root of the directory trees. (default)
	 * @return true if this is the root directory node.  False if it is not.
	 */
	public boolean isRoot()
	{
		return parent==null && directoryname==null;
	}
	
	/**
	 * Describe the complete path to ( and including) this directory, separated by the RepositoryDirectory.DIRECTORY_SEPARATOR property (slash).
	 * 
	 * @return The complete path to this directory.
	 */
	public String getPath()
	{
		if (getParent()==null) // Root! 
		{
			return DIRECTORY_SEPARATOR;
		}
		else
		{
			if (getParent().getParent()==null)
			{
				return DIRECTORY_SEPARATOR + getDirectoryName();
			}
			else
			{
				return getParent().getPath() + DIRECTORY_SEPARATOR + getDirectoryName();
			}
		}
	}
	
	/**
	 * Describe the complete path to ( and including) this directory, as an array of strings.
	 * 
	 * @return The complete path to this directory.
	 */
	public String[] getPathArray()
	{
		// First determine the depth of the tree...
		int depth=1;
		RepositoryDirectory follow = getParent();
		if (follow!=null)
		{
			depth++;
			follow=follow.getParent();
		}
		
		// Then put something in it...
		String retval[] = new String[depth];
		int level=depth-1;
		retval[level]=getDirectoryName();
		
		follow = getParent();
		if (follow!=null)
		{
			level--;
			retval[level]=follow.getDirectoryName();
			
			follow=follow.getParent();
		}
				
		return retval;
	}


	/**
	 * Add a subdirectory to this directory.
	 * @param subdir The subdirectory to add.
	 */
	public void addSubdirectory(RepositoryDirectory subdir)
	{
		subdir.setParent(this);
		children.add(subdir);
	}

	/**
	 * Counts the number of subdirectories in this directory.
	 * @return The number of subdirectories
	 */
	public int getNrSubdirectories()
	{
		return children.size();
	}
	
	/**
	 * Get a subdirectory on a certain position.
	 * @param i The subdirectory position
	 * @return The subdirectory with on a certain position
	 */
	public RepositoryDirectory getSubdirectory(int i)
	{
		if (children==null) return null;
		return (RepositoryDirectory)children.get(i);
	}
	
	/**
	 * Find the directory by following the path of strings
	 * @param path The path to the directory we're looking for.
	 * @return The directory if one can be found, null if no directory was found.
	 */
	public RepositoryDirectory findDirectory(String path[])
	{ 
	    // Is it root itself?
	    if (isRoot() && path.length==1 && path[0].equalsIgnoreCase(DIRECTORY_SEPARATOR))
	    {
	        return this;
	    }
	    
	    if (path.length<1) {
	    	return this;
	    }
	    	    
	    String[] directoryPath;
	    
	    // Skip the root directory, it doesn't really exist as such.
	    if (path.length>0 && path[0].equalsIgnoreCase(DIRECTORY_SEPARATOR))
	    {
	        // Copy the path exception the highest level, we go down one... 
	    	directoryPath = new String[path.length-1];
	    	for (int x=0;x<directoryPath.length;x++) directoryPath[x]=path[x+1];
	    }
	    else
	    {
	        directoryPath = path;
	    }
	   
		// The root directory?
		if (isRoot() && directoryPath.length==1 && directoryPath[0].equalsIgnoreCase(DIRECTORY_SEPARATOR))
		{
			return this;
		}
		else
		// This directory?    
	    if (directoryPath.length==1 && directoryPath[0].equalsIgnoreCase(getDirectoryName()))
		{
			return this;
		}
	    else
	    // A direct subdirectory?
	    if (directoryPath.length>=1) 
	    {
	        for (int i=0;i<getNrSubdirectories();i++)
	        {
	            RepositoryDirectory subdir = getSubdirectory(i);
	            if (subdir.getDirectoryName().equalsIgnoreCase(directoryPath[0]))
	            {
	                if (directoryPath.length==1) return subdir; // we arrived at the destination...
	             
	    	        // Copy the path exception the highest level, we go down one... 
	    	    	String subpath[] = new String[directoryPath.length-1];
	    	    	for (int x=0;x<subpath.length;x++) subpath[x]=directoryPath[x+1];

	    	    	// Perhaps the rest of the path is the same too?
	    	    	RepositoryDirectory look = subdir.findDirectory(subpath);
	    	    	if (look!=null) return look;
	            }
	        }
	    }

		return null;
	}
	
	/**
	 * Find a directory using the path to the directory with file.separator between the dir-names.

	 * @param path The path to the directory
	 * @return The directory if one was found, null if nothing was found.
	 */
	public RepositoryDirectory findDirectory(String path)
	{
		String newPath[] = Const.splitPath(path, DIRECTORY_SEPARATOR);
		
		String p[] = null;
		
		if (parent==null)
		{
			// This doesn't include the root:
			p = new String[newPath.length+1];
			p[0] = DIRECTORY_SEPARATOR;
			
			for (int i=0;i<newPath.length;i++)
			{
				p[i+1]=newPath[i];
			}
		}
		else
		{
		    p = newPath;
		}

		return findDirectory(p);
	}
	
	/**
	 * Find the sub-directory with a certain ID
	 * @param id_directory the directory ID to look for.
	 * @return The RepositoryDirectory if the ID was found, null if nothing could be found.
	 */
	public RepositoryDirectory findDirectory(long id_directory)
	{
		if (getID()==id_directory) return this;
		
		for (int i=0;i<getNrSubdirectories();i++)
		{
			RepositoryDirectory rd = getSubdirectory(i).findDirectory(id_directory);
			if (rd!=null) return rd;
		}
		
		return null;
	}
	
	
	/**
	 * Return the description of this directory & the subdirectories in XML.
	 * @return The XML describing this directory.
	 */
	public String getXML()
	{
		return getXML(0);
	}
	
	public String getXML(int level)
	{
		String spaces = Const.rightPad(" ", level);
        StringBuffer retval = new StringBuffer(200);
		
		retval.append(spaces).append("<repdir>").append(Const.CR);
		retval.append(spaces).append("  ").append(XMLHandler.addTagValue("name", getDirectoryName() ));
		
		if (getNrSubdirectories()>0)
		{
			retval.append(spaces).append("    <subdirs>").append(Const.CR);
			for (int i=0;i<getNrSubdirectories();i++)
			{
				RepositoryDirectory subdir = getSubdirectory(i);
				retval.append(subdir.getXML(level+1));
			}
			retval.append(spaces).append("    </subdirs>").append(Const.CR);
		}
		
		retval.append(spaces).append("</repdir>").append(Const.CR);
        
		return retval.toString();
	}
	
	/**
	 * Load the directory & subdirectories from XML
	 * @param repdirnode The node in which the Repository directory information resides.
	 * @return True if all went well, false if an error occured.
	 */
	public boolean loadXML(Node repdirnode)
	{
		try
		{
			clear();
			
			directoryname = XMLHandler.getTagValue(repdirnode, "name");
			Node subdirsnode = XMLHandler.getSubNode(repdirnode, "subdirs");
			if (subdirsnode!=null)
			{
				int n = XMLHandler.countNodes(subdirsnode, "repdir");
				for (int i=0;i<n;i++)
				{
					Node subdirnode = XMLHandler.getSubNodeByNr(subdirsnode, "repdir", i);
					RepositoryDirectory subdir = new RepositoryDirectory();
					if (subdir.loadXML(subdirnode))
					{
						subdir.setParent(this);
						addSubdirectory(subdir);
					}
					else
					{
						return false;
					}
				}
			}
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}

	/**
	 * Load the complete directory tree from the repository.
	 * @param rep Repository
	 */
	public RepositoryDirectory(Repository rep) throws KettleException
	{
        loadDirectoryTreeFromRepository(rep);
	}
    
    private void loadDirectoryTreeFromRepository(Repository rep) throws KettleException
    {
        try
        {
            clear();
            
            long subids[] = rep.getSubDirectoryIDs( getID() );
            for (int i=0;i<subids.length;i++)
            {
                RepositoryDirectory subdir = new RepositoryDirectory();
                if (subdir.loadRep(rep, subids[i]))
                {
                    addSubdirectory(subdir);
                }
            }
        }
        catch(Exception e)
        {
            throw new KettleException("An error occured loading the directory tree from the repository", e);
        }
    }
	
	public boolean loadRep(Repository rep, long id_directory)
	{
		try
		{
			RowMetaAndData row = rep.getDirectory(id_directory);
			if (row!=null)
			{
				setID(id_directory);
				
				// Content?
				setDirectoryName( row.getString("DIRECTORY_NAME", null));
				
				// The subdirectories?
				long subids[] = rep.getSubDirectoryIDs( getID() );
				for (int i=0;i<subids.length;i++)
				{
					RepositoryDirectory subdir = new RepositoryDirectory();
					if (subdir.loadRep(rep, subids[i]))
					{
						addSubdirectory(subdir);
					}
				}
			}
			
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	public boolean addToRep(Repository rep)
	{
		try
		{
			long id_directory_parent = 0;
			if (getParent()!=null) id_directory_parent=getParent().getID();
			
			setID(rep.insertDirectory(id_directory_parent, this));
			
            LogWriter.getInstance().logDetailed(rep.getName(), "New id of directory = "+getID());
                        
			rep.commit();
            
            // Reload the complete directory tree from the parent down...
            findRoot().loadDirectoryTreeFromRepository(rep);

			return id>0;
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	public void delFromRep(Repository rep) throws KettleException
	{
		try
		{
			delFromRep(rep, true);
		}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error deleting repository directory", e);
		}
	}
	
	public void delFromRep(Repository rep, boolean deleteNonEmptyFolder) throws KettleException
	{
		try
		{
			String trans[]   = rep.getTransformationNames(getID());
			String jobs[]    = rep.getJobNames(getID());
			int subDirectories = getNrSubdirectories();
			if ((trans.length !=0 || jobs.length !=0 || subDirectories !=0) && !deleteNonEmptyFolder)
			{
				throw new KettleException("This directory is not empty!");
			}
			else
			{
				// delete all jobs in the current directory
                for (int i = 0; i < jobs.length; i++) {
                	rep.delAllFromJob(rep.getJobID(jobs[i], getID()));
                }
                // delete all transformations in the current directory
                for (int i = 0; i < trans.length; i++) {
                	rep.delAllFromTrans(rep.getTransformationID(trans[i], getID()));
                }
                // now delete every sub directory by calling this method for each sub dir
                for (int i = 0; i < subDirectories; i++) {
                	RepositoryDirectory nextDir = getSubdirectory(i);
                	nextDir.delFromRep(rep, deleteNonEmptyFolder);
                }
                // finally delete the directory itself
                rep.deleteDirectory(getID());
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error deleting repository directory", e);
		}
	}

	public boolean renameInRep(Repository rep)
	{
		try
		{
			rep.renameDirectory(getID(), getDirectoryName());
			
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}

	/**
	 * Get all the directory-id in this directory and the subdirectories.
	 * @return an array of all the directory id's (this directory & subdirectories) 
	 */
	public long[] getDirectoryIDs()
	{
		List<Long> ids = new ArrayList<Long>();
		getDirectoryIDs(ids);
		long retval[] = new long[ids.size()];
		for (int i=0;i<retval.length;i++) retval[i] = ((Long)ids.get(i)).longValue();
		
		return retval;
	}
	
	/**
	 * Fill an arraylist with all the ID_DIRECTORY values in the tree below and including this directory.
	 *  
	 * @param ids The arraylist that will contain the directory IDs.
	 */
	private void getDirectoryIDs(List<Long> ids)
	{
		Long lid = new Long(getID());
		ids.add(lid);
		
		for (int i=0;i<getNrSubdirectories();i++) getSubdirectory(i).getDirectoryIDs(ids);
	}
	
	/**
	 * Find the root of the directory tree starting from this directory.
	 * @return the root of the directory tree
	 */
	public RepositoryDirectory findRoot()
	{
		if (isRoot()) return this;
		return getParent().findRoot();
	}
	
	/**
	 * Create a new directory, possibly by creating several subdirecties of / at the same time.
	 * 
	 * @param directoryPath The path to the new Repository Directory, to be created.
	 * @return The created subdirectory
	 */
	public RepositoryDirectory createDirectory(Repository rep, String directoryPath) throws KettleDatabaseException
	{
	    String path[] = Const.splitPath(directoryPath, DIRECTORY_SEPARATOR);

	    RepositoryDirectory parent = this;
	    for (int level=0;level<path.length;level++)
	    {
	        RepositoryDirectory rd = parent.findChild(path[level]);
	        if (rd==null)
	        {
	            // This directory doesn't exists, let's add it!
	            rd = new RepositoryDirectory(parent, path[level]);
	            // System.out.println("New directory: ["+rd.getPath()+"]");
	            if (rd.addToRep(rep))
	            {
	                // Don't forget to add this directory to the tree!
	                parent.addSubdirectory(rd);
		            // System.out.println("Created directory ["+rd.getPath()+"], id = "+rd.getID());
	                parent = rd;
	            }
	            else
	            {
	                throw new KettleDatabaseException("Unable to create repository directory ["+path[level]+"] in directory ["+parent.getPath()+"]");
	            }
	        }
	        else
	        {
	            parent = rd;   
	        }
	    }
	    return parent;
	}

	public String toString()
	{
		return getPath();
	}

    public String getPathObjectCombination(String transName)
    {
        if (isRoot())
        {
            return getPath()+transName;
        }
        else
        {
            return getPath()+RepositoryDirectory.DIRECTORY_SEPARATOR+transName;
        }
    }
    
    public RepositoryDirectory findChild(String name) {
      for (RepositoryDirectory child : children) {
          if (child.getDirectoryName().equalsIgnoreCase(name)) 
            return (RepositoryDirectory)child;
      }
      return null;
  }
}
