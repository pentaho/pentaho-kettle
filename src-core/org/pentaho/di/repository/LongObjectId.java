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

public class LongObjectId implements ObjectId, Comparable<LongObjectId> {
	private long id;
	
	public LongObjectId(long id) {
		this.id = id;
	}
	
	public LongObjectId(ObjectId objectId) {
		if (objectId==null) {
			this.id=-1L; // backward compatible
		} else {
			if (objectId instanceof LongObjectId) {
				this.id = ((LongObjectId)objectId).longValue();
			} else {
				this.id = Long.valueOf(objectId.getId());
			}
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof LongObjectId)) return false;
		
		LongObjectId objectId = (LongObjectId) obj;
		
		return id == objectId.longValue();
	}
	
	@Override
	public int hashCode() {
		return Long.valueOf(id).hashCode();
	}
	
	public int compareTo(LongObjectId o) {
		return Long.valueOf(id).compareTo(Long.valueOf(o.longValue()));
	}
	
	@Override
	public String toString() {
		return Long.toString(id);
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return Long.toString(id);
	}
	
	/**
	 * @return the id in its original form.
	 */
	public Long longValue() {
		return Long.valueOf(id);
	}

}
