package org.pentaho.di.repository;

import org.pentaho.di.core.config.PropertySetter;
import org.pentaho.di.core.exception.KettleConfigException;

/**
 * This class describes the properties of a repository factory.
 * 
 * @author matt
 *
 */
public class RepositoryPluginMeta {
	private String id;
	private String name;
	private String description;
	private String className;
	private String metaClassName;
	private String dialogClassName;
	
	protected final PropertySetter psetter = new PropertySetter();

	/**
	 * Empty constructor, called by the ConfigManager (reflection from repositories-rules.xml and all that)
	 */
	public RepositoryPluginMeta() {
	}
	

	/**
	 * @param id
	 * @param name
	 * @param description
	 * @param className
	 * @param metaClassName
	 * @param dialogClassName
	 */
	public RepositoryPluginMeta(String id, String name, String description, String className, String metaClassName, String dialogClassName) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.className = className;
		this.metaClassName = metaClassName;
		this.dialogClassName = dialogClassName;
	}


	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param className the className to set
	 */
	public void setClassName(String className) {
		this.className = className;
	}
	
	
	/**
	 * This method is called by the BasicConfigurationManager upon parsing of the repositories-rules.xml file
	 * @param id
	 */
	public void setIdWithStr(String id)
	{
		this.id = id;
	}

	/**
	 * This method is called by the BasicConfigurationManager upon parsing of the repositories-rules.xml file
	 */
	public void set(String property, String value) throws KettleConfigException
	{
		psetter.setProperty(this, property, value);
	}

	/**
	 * @return the dialogClassName
	 */
	public String getDialogClassName() {
		return dialogClassName;
	}

	/**
	 * @param dialogClassName the dialogClassName to set
	 */
	public void setDialogClassName(String dialogClassName) {
		this.dialogClassName = dialogClassName;
	}

	/**
	 * @return the metaClassName
	 */
	public String getMetaClassName() {
		return metaClassName;
	}

	/**
	 * @param metaClassName the metaClassName to set
	 */
	public void setMetaClassName(String metaClassName) {
		this.metaClassName = metaClassName;
	}	
}
