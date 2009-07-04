package org.pentaho.di.repository;

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Node;

public class BaseRepositoryMeta {
		
	protected String id;
	protected String name;
	protected String description;
	
	public BaseRepositoryMeta(String id) {
		this.id = id;
	}

	/**
	 * @param id
	 * @param name
	 * @param description
	 */
	public BaseRepositoryMeta(String id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}
	
	/* (non-Javadoc)
	 * @see org.pentaho.di.repository.RepositoryMeta#loadXML(org.w3c.dom.Node, java.util.List)
	 */
	public void loadXML(Node repnode, List<DatabaseMeta> databases) throws KettleException
	{
		try
		{
                          // Fix for PDI-2508: migrating from 3.2 to 4.0 causes NPE on startup.
			id          = Const.NVL(XMLHandler.getTagValue(repnode, "id"), id) ;
			name        = XMLHandler.getTagValue(repnode, "name") ;
			description = XMLHandler.getTagValue(repnode, "description");
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to load repository meta object", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.pentaho.di.repository.RepositoryMeta#getXML()
	 */
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(100);
		
		retval.append("    ").append(XMLHandler.addTagValue("id",          id));
		retval.append("    ").append(XMLHandler.addTagValue("name",        name));
		retval.append("    ").append(XMLHandler.addTagValue("description", description));
        
		return retval.toString();
	}


	/* (non-Javadoc)
	 * @see org.pentaho.di.repository.RepositoryMeta#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.repository.RepositoryMeta#setId(java.lang.String)
	 */
	public void setId(String id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.repository.RepositoryMeta#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.repository.RepositoryMeta#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.repository.RepositoryMeta#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.repository.RepositoryMeta#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		this.description = description;
	}

}
