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

public class SourceCrawlerPackageException {
	private String startsWith;
	private String packageName;

	/**
	 * @param startsWiths
	 * @param packageName
	 */
	public SourceCrawlerPackageException(String startsWith, String packageName) {
		this.startsWith = startsWith;
		this.packageName = packageName;
	}

	/**
	 * @return the startsWith
	 */
	public String getStartsWith() {
		return startsWith;
	}

	/**
	 * @param startsWith
	 *            the startsWith to set
	 */
	public void setStartsWith(String startsWith) {
		this.startsWith = startsWith;
	}

	/**
	 * @return the packageName
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @param packageName
	 *            the packageName to set
	 */
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

}
