/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
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
