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
package org.pentaho.di.core.plugins;

import org.pentaho.di.core.Const;

public class PluginVersion {
	public static final int UNKNOWN_MAJOR_VERSION = 2;
	public static final int UNKNOWN_MINOR_VERSION = 2;
	public static final int UNKNOWN_POINT_VERSION = 0;

	public static final int DEFAULT_MAJOR_VERSION = 3;
	public static final int DEFAULT_MINOR_VERSION = 0;
	public static final int DEFAULT_POINT_VERSION = 0;
	
	private int majorVersion;
	private int minorVersion;
	private int pointVersion;
	
	public static PluginVersion UNKNOWN_VERSION = new PluginVersion(UNKNOWN_MAJOR_VERSION, UNKNOWN_MINOR_VERSION, UNKNOWN_POINT_VERSION);
	public static PluginVersion DEFAULT_VERSION = new PluginVersion(DEFAULT_MAJOR_VERSION, DEFAULT_MINOR_VERSION, DEFAULT_POINT_VERSION);

	public PluginVersion(int majorVersion, int minorVersion, int pointVersion) {
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.pointVersion = pointVersion;
	}
	
	public String toString() {
		return majorVersion+"."+minorVersion+"."+pointVersion;
	}

	public static PluginVersion fromString(String versionString) {
		if (Const.isEmpty(versionString)) {
			// We have to assume it's an old plugin.xml file here.
			//
			return PluginVersion.UNKNOWN_VERSION;
		}
		else {
			String[] strings = versionString.split("\\.");
			int majorVersion = Integer.parseInt(strings[0]);
			int minorVersion = strings.length>1 ? Integer.parseInt(strings[1]) : 0;
			int pointVersion = strings.length>2 ? Integer.parseInt(strings[2]) : 0;
			
			return new PluginVersion(majorVersion, minorVersion, pointVersion);
		}
	}
	
	/**
	 * Verifies that this version is compatible with DEFAULT_VERSION
	 * @return true if this version is compatible with the default version.
	 */
	public boolean isCompatible() {
		if (majorVersion==DEFAULT_MAJOR_VERSION) return true;
		return false;
	}

	/**
	 * @param version the plugin version to check against
	 * @return true if this version is more recent than the version specified
	 */
	public boolean isMoreRecentThan(PluginVersion version) {
		if (majorVersion>version.majorVersion) {
			return true;
		}
		if (majorVersion==version.majorVersion) {
			if (minorVersion>version.minorVersion) {
				return true;
			}
			if (minorVersion==version.minorVersion) {
				if (pointVersion > version.pointVersion) return true;
			}
		}
		return false;
	}
	
	public boolean equals(PluginVersion version) {
		return 
			majorVersion==version.majorVersion && 
			minorVersion==version.minorVersion && 
			pointVersion==version.pointVersion;
	}

	/**
	 * @return the major version
	 */
	public int getMajorVersion() {
		return majorVersion;
	}

	/**
	 * @param majorVersion the major version to set
	 */
	public void setMajorVersion(int majorVersion) {
		this.majorVersion = majorVersion;
	}

	/**
	 * @return the minor version
	 */
	public int getMinorVersion() {
		return minorVersion;
	}

	/**
	 * @param minorVersion the minor version to set
	 */
	public void setMinorVersion(int minorVersion) {
		this.minorVersion = minorVersion;
	}

	/**
	 * @return the point version
	 */
	public int getPointVersion() {
		return pointVersion;
	}

	/**
	 * @param pointVersion the point version to set
	 */
	public void setPointVersion(int pointVersion) {
		this.pointVersion = pointVersion;
	}


	
	
}
