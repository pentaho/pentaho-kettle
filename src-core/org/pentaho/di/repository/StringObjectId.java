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

public class StringObjectId implements ObjectId, Comparable<StringObjectId> {
	private String id;
	
	public StringObjectId(String id) {
		this.id = id;
	}
	
	public StringObjectId(ObjectId objectId) {
		if (objectId instanceof StringObjectId) {
			this.id = ((StringObjectId)objectId).id;
		} else {
			this.id = objectId.getId();
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		
		if(obj == null){
		  return false;
		}
		
		ObjectId objectId = (ObjectId) obj;
		
		return id.equalsIgnoreCase(objectId.getId());
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	public int compareTo(StringObjectId o) {
		return id.compareTo(o.id);
	}
	
	@Override
	public String toString() {
		return id;
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
}
