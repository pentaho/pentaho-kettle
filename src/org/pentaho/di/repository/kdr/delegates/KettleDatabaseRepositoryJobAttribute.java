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
package org.pentaho.di.repository.kdr.delegates;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryAttributeInterface;

public class KettleDatabaseRepositoryJobAttribute implements RepositoryAttributeInterface {

	private KettleDatabaseRepositoryConnectionDelegate	connectionDelegate;
	private ObjectId	jobObjectId;

	public KettleDatabaseRepositoryJobAttribute(KettleDatabaseRepositoryConnectionDelegate connectionDelegate, ObjectId jobObjectId) {
		this.connectionDelegate = connectionDelegate;
		this.jobObjectId = jobObjectId;
	}
	
	public boolean getAttributeBoolean(String code) throws KettleException {
		return connectionDelegate.getJobAttributeBoolean(jobObjectId, 0, code);
	}

	public long getAttributeInteger(String code) throws KettleException {
		return connectionDelegate.getJobAttributeInteger(jobObjectId, 0, code);
	}

	public String getAttributeString(String code) throws KettleException {
		return connectionDelegate.getJobAttributeString(jobObjectId, 0, code);
	}

	public void setAttribute(String code, String value) throws KettleException {
		connectionDelegate.insertJobAttribute(jobObjectId, 0, code, 0, value);
	}

	public void setAttribute(String code, boolean value) throws KettleException {
		connectionDelegate.insertJobAttribute(jobObjectId, 0, code, 0, value?"Y":"N");
	}

	public void setAttribute(String code, long value) throws KettleException {
		connectionDelegate.insertJobAttribute(jobObjectId, 0, code, value, null);
	}
}
