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
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Node;


/**
 * This class defines the location of a transformation, job or schema in the repository.
 * That means that it's just an extra parameter for recognizing a transformation, job or schema.
 * It allows for sub-directories by linking back to itself.
 * 
 * @author Matt
 * @since  09-nov-2004
 *
 */
public class RepositoryDirectory implements Directory
{
    public static final String DIRECTORY_SEPARATOR = "/";

	private RepositoryDirectory parent;
  private Repository repository;
	private List<Directory> children;
	
	private String directoryname;
	
	private ObjectId id;
    
	/**
	 * Create a new sub-directory in a certain other directory.
	 * @param parent The directory to create the sub-directory in
	 * @param directoryname The name of the new directory.
	 */
	public RepositoryDirectory(RepositoryDirectory parent, String directoryname)
	{
		this.parent        = parent;
		this.directoryname = directoryname;
		this.children      = new ArrayList<Directory>(); // default: no subdirectories...
		this.id            = null;              // The root directory!
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
	
	public List<Directory> getChildren() {
    return children;
  }

  public void setChildren(List<Directory> children) {
    this.children = children;
  }

  public void clear()
	{
		this.parent        = null;
		this.directoryname = null;
		this.children      = new ArrayList<Directory>(); // default: no subdirectories...
	}
	
	/**
	 * Get the database ID in the repository for this object.
	 * @return the database ID in the repository for this object.
	 */
	public ObjectId getObjectId()
	{
		return id;
	}

	/**
	 * Set the database ID for this object in the repository.
	 * @param id the database ID for this object in the repository.
	 */
	public void setObjectId(ObjectId id)
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
	public void setName(String directoryname)
	{
		this.directoryname = directoryname;
	}
	
	/**
	 * Get the name of this directory...
	 * @return the name of this directory
	 */
	public String getName()
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
				return DIRECTORY_SEPARATOR + getName();
			}
			else
			{
				return getParent().getPath() + DIRECTORY_SEPARATOR + getName();
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
		retval[level]=getName();
		
		follow = getParent();
		if (follow!=null)
		{
			level--;
			retval[level]=follow.getName();
			
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
	    if (directoryPath.length==1 && directoryPath[0].equalsIgnoreCase(getName()))
		{
			return this;
		}
	    else
	    // A direct subdirectory?
	    if (directoryPath.length>=1) 
	    {
	    	RepositoryDirectory follow = this;
	    	for (int i=0;i<directoryPath.length;i++) {
	    		RepositoryDirectory directory = follow.findChild(directoryPath[i]);
	    		if (directory==null) return null;
	    		follow=directory;
	    	}
	    	return follow;
	    	
	    	/*
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
	        */
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
	
	public RepositoryDirectory findChild(String name) {
		for (Directory child : children) {
			if (child.getName().equalsIgnoreCase(name)) 
			  return (RepositoryDirectory)child;
		}
		return null;
	}
	
	/**
	 * Find the sub-directory with a certain ID
	 * @param id_directory the directory ID to look for.
	 * @return The RepositoryDirectory if the ID was found, null if nothing could be found.
	 */
	public RepositoryDirectory findDirectory(ObjectId id_directory)
	{
		// Check for the root directory...
		//
		if (getObjectId()==null && id_directory==null) {
			return this; 
		}
		
		if (getObjectId()!=null && getObjectId().equals(id_directory)) {
			return this;
		}
		
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
		retval.append(spaces).append("  ").append(XMLHandler.addTagValue("name", getName() ));
		
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
	 * Get all the directory-id in this directory and the subdirectories.
	 * @return an array of all the directory id's (this directory & subdirectories) 
	 */
	public ObjectId[] getDirectoryIDs()
	{
		List<ObjectId> ids = new ArrayList<ObjectId>();
		getDirectoryIDs(ids);
		
		return ids.toArray(new ObjectId[ids.size()]);
	}
	
	/**
	 * Fill an arraylist with all the ID_DIRECTORY values in the tree below and including this directory.
	 *  
	 * @param ids The arraylist that will contain the directory IDs.
	 */
	private void getDirectoryIDs(List<ObjectId> ids)
	{
		if (getObjectId()!=null) {
			ids.add(getObjectId());
		}
		
		for (int i=0;i<getNrSubdirectories();i++) {
			getSubdirectory(i).getDirectoryIDs(ids);
		}
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

    public Repository getRepository() {
      return repository;
    }

    public void setRepository(Repository repository) {
      this.repository = repository;
    }
}
