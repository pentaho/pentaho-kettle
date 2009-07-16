package org.pentaho.di.repository;

import java.util.Map;

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
	private String versionBrowserClassName;
	
	protected final PropertySetter psetter = new PropertySetter();
	private String[]	jarFiles;
	private Map<String, String>	localizedNames;
	private Map<String, String>	localizedDescriptions;

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
	 * @param localizedDescriptions 
	 * @param localizedNames 
	 * @param jarfiles 
	 */
	public RepositoryPluginMeta(String id, String name, String description, String className, String metaClassName, String dialogClassName, String versionBrowserClassName, String[] jarfiles, Map<String, String> localizedNames, Map<String, String> localizedDescriptions ) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.className = className;
		this.metaClassName = metaClassName;
		this.dialogClassName = dialogClassName;
		this.versionBrowserClassName = versionBrowserClassName;
		this.jarFiles = jarfiles;
		this.localizedNames = localizedNames;
		this.localizedDescriptions = localizedDescriptions;
	}

	public boolean equals(Object obj) {
		return id.equals(((RepositoryPluginMeta)obj).getId());
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


	/**
	 * @return the jarFiles
	 */
	public String[] getJarFiles() {
		return jarFiles;
	}


	/**
	 * @param jarFiles the jarFiles to set
	 */
	public void setJarFiles(String[] jarFiles) {
		this.jarFiles = jarFiles;
	}


	/**
	 * @return the localizedNames
	 */
	public Map<String, String> getLocalizedNames() {
		return localizedNames;
	}


	/**
	 * @param localizedNames the localizedNames to set
	 */
	public void setLocalizedNames(Map<String, String> localizedNames) {
		this.localizedNames = localizedNames;
	}


	/**
	 * @return the localizedDescriptions
	 */
	public Map<String, String> getLocalizedDescriptions() {
		return localizedDescriptions;
	}


	/**
	 * @param localizedDescriptions the localizedDescriptions to set
	 */
	public void setLocalizedDescriptions(Map<String, String> localizedDescriptions) {
		this.localizedDescriptions = localizedDescriptions;
	}


	/**
	 * @return the versionBrowserClassName
	 */
	public String getVersionBrowserClassName() {
		return versionBrowserClassName;
	}


	/**
	 * @param versionBrowserClassName the versionBrowserClassName to set
	 */
	public void setVersionBrowserClassName(String versionBrowserClassName) {
		this.versionBrowserClassName = versionBrowserClassName;
	}	
}
