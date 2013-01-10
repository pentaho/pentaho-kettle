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

import org.apache.commons.vfs.FileObject;

/**
 * Contains the occurrence of a key in a java source code file
 * 
 * @author matt
 * @since 2007-09-29
 *
 */

public class KeyOccurrence implements Comparable<KeyOccurrence> {
	/**
	 * The java source file
	 */
	private FileObject fileObject;
	
	/**
	 * The location of the messages file, derived from "^import .*Messages;"
	 */
	private String messagesPackage;
	
	/**
	 * The row on which the occurrence takes place
	 */
	private int row;
	
	/**
	 * The column on which the occurrence takes place
	 */
	private int column;
	
	/**
	 * The i18n key 
	 */
	private String key;
	
	/**
	 * The arguments from the source code
	 */
	private String arguments;
	
	/**
	 * The number of occurrences
	 */
	private int occurrences;
	
	/**
	 * line of source code on which the key occurs.
	 */
	private String sourceLine;
	/**
	 * 
	 */
	public KeyOccurrence() {
		occurrences=0;
	}
	
	/**
	 * @param fileObject The java source file
	 * @param messagesPackage The location of the messages file, derived from "^import .*Messages;"
	 * @param row The row on which the occurrence takes place
	 * @param column The column on which the occurrence takes place
	 * @param key The i18n key 
	 * @param arguments The arguments from the source code
	 */
	public KeyOccurrence(FileObject fileObject, String messagesPackage, int row, int column, String key, String arguments, String sourceLine) {
		this();
		this.fileObject = fileObject;
		this.messagesPackage = messagesPackage;
		this.row = row;
		this.column = column;
		this.key = key;
		this.arguments = arguments;
		this.occurrences = 1;
		this.sourceLine = sourceLine;
	}
	
	public String toString() {
		return "[key="+key+", messages package="+messagesPackage+"]";
	}

	public boolean equals(Object occ) {
		if (occ==null) return false;
		if (this==occ) return true;
		return key.equals(((KeyOccurrence)occ).key) && messagesPackage.equals(((KeyOccurrence)occ).messagesPackage);
	}
	 
    public int compareTo(KeyOccurrence occ) {
    	int cmp = key.compareTo(occ.key);
    	if (cmp!=0) return cmp;
    	
    	cmp = messagesPackage.compareTo(occ.messagesPackage);
    	return cmp;
    }

    /**
	 * @return The java source file
	 */
	public FileObject getFileObject() {
		return fileObject;
	}

	/**
	 * @param fileObject The java source file
	 */
	public void setFileObject(FileObject fileObject) {
		this.fileObject = fileObject;
	}

	/**
	 * @return The location of the messages file
	 */
	public String getMessagesPackage() {
		return messagesPackage;
	}

	/**
	 * @param messagesPackage The location of the messages file
	 */
	public void setMessagesPackage(String messagesPackage) {
		this.messagesPackage = messagesPackage;
	}

	/**
	 * @return The row on which the occurrence takes place
	 */
	public int getRow() {
		return row;
	}

	/**
	 * @param row The row on which the occurrence takes place
	 */
	public void setRow(int row) {
		this.row = row;
	}

	/**
	 * @return The column on which the occurrence takes place
	 */
	public int getColumn() {
		return column;
	}

	/**
	 * @param column The column on which the occurrence takes place
	 */
	public void setColumn(int column) {
		this.column = column;
	}

	/**
	 * @return The i18n key 
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key The i18n key 
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return The arguments from the source code
	 */
	public String getArguments() {
		return arguments;
	}

	/**
	 * @param arguments The arguments from the source code
	 */
	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	/**
	 * @return The number of occurrences
	 */
	public int getOccurrences() {
		return occurrences;
	}

	/**
	 * @param occurrences The number of occurrences
	 */
	public void setOccurrences(int occurrences) {
		this.occurrences = occurrences;
	}
	
	/**
	 * Increment the number of occurrences with one.
	 */
	public void incrementOccurrences() {
		this.occurrences++;
	}

	/**
	 * @return the line of source code on which the key occurs.
	 */
	public String getSourceLine() {
		return sourceLine;
	}

	/**
	 * @param sourceLine the line of source code on which the key occurs.
	 */
	public void setSourceLine(String sourceLine) {
		this.sourceLine = sourceLine;
	}
}
