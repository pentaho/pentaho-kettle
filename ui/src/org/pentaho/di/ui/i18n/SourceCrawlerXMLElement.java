/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

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
