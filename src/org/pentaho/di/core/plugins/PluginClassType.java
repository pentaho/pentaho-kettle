package org.pentaho.di.core.plugins;

public enum PluginClassType {

	MainClassType("Main class", false),
	MetaClassType("Meta class", true),
	DialogClassType("Dialog class", true),
	RepositoryVersionBrowserClassType("Version browser class", true),

	;
	
	private String description;
	private boolean uiClass;
	
	/**
	 * @param description
	 * @param uiClass
	 */
	private PluginClassType(String description, boolean uiClass) {
		this.description = description;
		this.uiClass = uiClass;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean isUiClass() {
		return uiClass;
	}
	
}
