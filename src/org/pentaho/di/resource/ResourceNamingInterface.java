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
package org.pentaho.di.resource;

public interface ResourceNamingInterface {
	/**
	 * Create a (file) name for a resource based on a prefix and an extension.
	 * @param prefix The prefix, usually the name of the object that is being exported
   * @param originalFilePath The original path to the file. This will be used in the naming of the resource to ensure that the same GUID will be returned for the same file.
	 * @param extension The extension of the filename to be created.  For now this also gives a clue as to what kind of data is being exported and named..
	 * @return The filename, typically including a GUID, but always the same when given the same prefix and extension as input.
	 */
	public String nameResource(String prefix, String originalFilePath, String extension);
}
