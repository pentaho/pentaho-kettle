package org.pentaho.di.repository;

import org.pentaho.di.core.exception.KettleException;


/**
 * This interface allows you to pass a simple interface to an object to allow it 
 * to store or load itself from or to any type of repository in a generic fashion.
 * @author matt
 *
 */
public interface RepositoryAttributeInterface {

	/**
	 * Set a String attribute
	 * @param code
	 * @param value
	 * @throws KettleException
	 */
	public void setAttribute(String code, String value) throws KettleException;
	/**
	 * Get a string attribute.  If the attribute is not found, return null
	 * @param code
	 * @return
	 * @throws KettleException
	 */
	public String getAttributeString(String code) throws KettleException;
	
	/**
	 * Set a boolean attribute
	 * @param code
	 * @param value
	 * @throws KettleException
	 */
	public void setAttribute(String code, boolean value) throws KettleException;
	
	/**
	 * Get a boolean attribute, if the attribute is not found, return false;
	 * @param code
	 * @return
	 * @throws KettleException
	 */
	public boolean getAttributeBoolean(String code) throws KettleException;
	
	/**
	 * Set an integer attribute
	 * @param code
	 * @param value
	 * @throws KettleException
	 */
	public void setAttribute(String code, long value) throws KettleException;
	
	/**
	 * Get an integer attribute. If the attribute is not found, return 0;
	 * @param code
	 * @return
	 * @throws KettleException
	 */
	public long getAttributeInteger(String code) throws KettleException;
}
