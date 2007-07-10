package org.pentaho.di.resource;

public interface ResourceNamingInterface {
	/**
	 * Create a (file) name for a resource based on a prefix and an extension.
	 * @param prefix The prefix, usually the name of the object that is being exported
   * @param originalFilePath The original path to the file. This will be used in the naming of the resource to ensure that the same GUID will be returned for the same file.
	 * @param extension The extension of the filename to be created.  For now this also gives a clue as to what kind of data is being exported and named..
	 * @return The filename, typically including a GUID, but always the same when given the same prefix and extension as input.
	 */
	public String nameResource(String prefix, String originalFilePath, String extension);
}
