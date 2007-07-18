package org.pentaho.di.resource;

public class ResourceEntry {
	public enum ResourceType { FILE, CONNECTION, SERVER, URL, DATABASENAME, ACTIONFILE, OTHER };
	
	private String resource;
	private ResourceType resourcetype;
	
	/**
	 * @param resource
	 * @param resourcetype
	 */
	public ResourceEntry(String resource, ResourceType resourcetype) {
		super();
		this.resource = resource;
		this.resourcetype = resourcetype;
	}

	/**
	 * @return the resource
	 */
	public String getResource() {
		return resource;
	}

	/**
	 * @param resource the resource to set
	 */
	public void setResource(String resource) {
		this.resource = resource;
	}

	/**
	 * @return the resourcetype
	 */
	public ResourceType getResourcetype() {
		return resourcetype;
	}

	/**
	 * @param resourcetype the resourcetype to set
	 */
	public void setResourcetype(ResourceType resourcetype) {
		this.resourcetype = resourcetype;
	}
	
	
}
