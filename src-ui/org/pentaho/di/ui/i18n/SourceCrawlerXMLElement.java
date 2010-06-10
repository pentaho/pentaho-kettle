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
package org.pentaho.di.ui.i18n;

public class SourceCrawlerXMLElement {
	private String searchElement;
	private String keyTag;
	private String keyAttribute;
	
	/**
	 * @param searchElement
	 * @param keyTag
	 * @param keyAttribute
	 */
	public SourceCrawlerXMLElement(String searchElement, String keyTag, String keyAttribute) {
		this.searchElement = searchElement;
		this.keyTag = keyTag;
		this.keyAttribute = keyAttribute;
	}
	/**
	 * @return the searchElement
	 */
	public String getSearchElement() {
		return searchElement;
	}
	/**
	 * @param searchElement the searchElement to set
	 */
	public void setSearchElement(String searchElement) {
		this.searchElement = searchElement;
	}
	/**
	 * @return the keyTag
	 */
	public String getKeyTag() {
		return keyTag;
	}
	/**
	 * @param keyTag the keyTag to set
	 */
	public void setKeyTag(String keyTag) {
		this.keyTag = keyTag;
	}
	/**
	 * @return the keyAttribute
	 */
	public String getKeyAttribute() {
		return keyAttribute;
	}
	/**
	 * @param keyAttribute the keyAttribute to set
	 */
	public void setKeyAttribute(String keyAttribute) {
		this.keyAttribute = keyAttribute;
	}
	

}
