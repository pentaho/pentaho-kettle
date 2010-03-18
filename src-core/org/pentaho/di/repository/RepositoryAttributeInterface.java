package org.pentaho.di.repository;

import org.pentaho.di.core.exception.KettleException;


/**
 * This interface allows you to pass a simple interface to an object to allow it 
 * to store or load itself from or to any type of repository in a generic fashion.
 * @author matt
 *
 */
public interface RepositoryAttributeInterface {

	public void setAttribute(String code, String value) throws KettleException;
	public String getAttribute(String code) throws KettleException;
	public void setAttribute(String code, boolean value) throws KettleException;
	public boolean getAttributeBoolean(String code, boolean defaultValue) throws KettleException;
	public void setAttribute(String code, long value) throws KettleException;
	public long getAttributeInteger(String code, long defaultValue) throws KettleException;
}
