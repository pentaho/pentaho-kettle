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
package org.pentaho.di.core.listeners;

/**
 * A listener that will signal when the filename of an object changes.
 * 
 * @author Matt Casters (mcasters@pentaho.org)
 *
 */
public interface FilenameChangedListener {
	/**
	 * The method that is executed when the filename of an object changes
	 * @param object The object for which there is a filename change
	 * @param oldFilename the old filename
	 * @param newFilename the new filename
	 */
	public void filenameChanged(Object object, String oldFilename, String newFilename);
}
