/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.xul;


public class XulObject implements XulItem {

    private String id;
    private XulItem parent;

	public XulObject( ) {
		this( null, null );
	}

	public XulObject( String id, XulItem parent ) {
		super();
		this.id = id;
		this.parent = parent;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public XulItem getParent() {
		return parent;
	}

	public void setParent(XulItem parent) {
		this.parent = parent;
	}
	
	public Object getNativeObject() {
		return null;
	}
}
