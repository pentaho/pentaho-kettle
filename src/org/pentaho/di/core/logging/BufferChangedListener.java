package org.pentaho.di.core.logging;

/**
 * A listener to detect that content was added to a string buffer.
 * 
 * @author matt
 *
 */
public interface BufferChangedListener {
	public void contentWasAdded(StringBuffer content, String extra, int nrLines);
}
