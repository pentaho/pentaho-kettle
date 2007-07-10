package org.pentaho.di.resource;

public class ResourceDefinition {
	private String filename;
	private String content;
	
	/**
	 * @param filename
	 * @param content
	 */
	public ResourceDefinition(String filename, String content) {
		super();
		this.filename = filename;
		this.content = content;
	}
	
	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}
	
	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}
	
	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}
}
