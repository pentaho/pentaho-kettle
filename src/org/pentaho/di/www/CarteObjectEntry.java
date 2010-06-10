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
package org.pentaho.di.www;

import java.util.Comparator;

/**
 * A carte object entry in the transformation or job maps
 * 
 * @author matt
 *
 */
public class CarteObjectEntry implements Comparator<CarteObjectEntry>, Comparable<CarteObjectEntry> {
	private String name;
	private String id;
	
	public CarteObjectEntry(String name, String id) {
		this.name = name;
		this.id = id;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CarteObjectEntry)) return false;
		if (obj == this) return true;
		
		CarteObjectEntry entry = (CarteObjectEntry) obj;
		
		return entry.getId().equals(id);
	}
	
	public int hashCode() {
		return id.hashCode();
	}
	
	public int compare(CarteObjectEntry o1, CarteObjectEntry o2) {
		int cmpName = o1.getName().compareTo(o2.getName());
		if (cmpName!=0) return cmpName;
		
		return o1.getId().compareTo(o2.getId());
	}
	
	public int compareTo(CarteObjectEntry o) {
		return compare(this, o);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
}
