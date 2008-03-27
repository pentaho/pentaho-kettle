package org.pentaho.di.core.listeners;

/**
 * A listener that will signal when the filename of an object changes.
 * 
 * @author Matt Casters (mcasters@pentaho.org)
 *
 */
public interface FilenameChangedListener {
	/**
	 * The method that is executed when the filename of an object changes
	 * @param object The object for which there is a filename change
	 * @param oldFilename the old filename
	 * @param newFilename the new filename
	 */
	public void filenameChanged(Object object, String oldFilename, String newFilename);
}
