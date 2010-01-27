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
package org.pentaho.di.core;

import java.util.Date;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.w3c.dom.Node;

/**
 * This is a result file: a file as a result of the execution of a job entry, a transformation step, etc.
 * 
 * @author matt
 *
 */
public class ResultFile implements Cloneable
{
	private static Class<?> PKG = Const.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final int FILE_TYPE_GENERAL   = 0;
	public static final int FILE_TYPE_LOG       = 1;
	public static final int FILE_TYPE_ERRORLINE = 2;
	public static final int FILE_TYPE_ERROR     = 3;
	public static final int FILE_TYPE_WARNING   = 4;
	
	public static final String[] fileTypeCode = { "GENERAL", "LOG", "ERRORLINE", "ERROR", "WARNING" };
	
	public static final String[] fileTypeDesc = {
                                                 BaseMessages.getString(PKG, "ResultFile.FileType.General"),
                                                 BaseMessages.getString(PKG, "ResultFile.FileType.Log"),
                                                 BaseMessages.getString(PKG, "ResultFile.FileType.ErrorLine"),
                                                 BaseMessages.getString(PKG, "ResultFile.FileType.Error"),
                                                 BaseMessages.getString(PKG, "ResultFile.FileType.Warning")
    };
	private static final String XML_TAG = "result-file";
    
	private int type;
	private FileObject file;
	private String originParent;
	private String origin;
	private String comment;
	private Date   timestamp;
	
	/**
	 * Construct a new result file
	 * @param type The type of file : FILE_TYPE_GENERAL, ...
	 * @param file The file to use
	 * @param originParent The transformation or job that has generated this result file
	 * @param origin The step or job entry that has generated this result file
	 */
	public ResultFile(int type, FileObject file, String originParent, String origin)
	{
		this.type = type;
		this.file = file;
		this.originParent = originParent;
		this.origin = origin;
		this.timestamp = new Date();
	}
	
	@Override
	public String toString() {
		return file.toString()+" - "+getTypeDesc()+" - "+XMLHandler.date2string(timestamp)+
			(origin==null ? "" : " - "+origin)+
			(originParent==null ? "" : " - "+originParent);
		
	}
	
	protected ResultFile clone() throws CloneNotSupportedException
	{
		return (ResultFile) super.clone();
	}

	/**
	 * @return Returns the comment.
	 */
	public String getComment()
	{
		return comment;
	}

	/**
	 * @param comment The comment to set.
	 */
	public void setComment(String comment)
	{
		this.comment = comment;
	}

	/**
	 * @return Returns the file.
	 */
	public FileObject getFile()
	{
		return file;
	}

	/**
	 * @param file The file to set.
	 */
	public void setFile(FileObject file)
	{
		this.file = file;
	}

	/**
	 * @return Returns the origin : the step or job entry that generated this result file
	 */
	public String getOrigin()
	{
		return origin;
	}

	/**
	 * @param origin The origin to set : the step or job entry that generated this result file
	 */
	public void setOrigin(String origin)
	{
		this.origin = origin;
	}

	/**
	 * @return Returns the originParent : the transformation or job that generated this result file
	 */
	public String getOriginParent()
	{
		return originParent;
	}

	/**
	 * @param originParent The originParent to set : the transformation or job that generated this result file
	 */
	public void setOriginParent(String originParent)
	{
		this.originParent = originParent;
	}

	/**
	 * @return Returns the type.
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * @param type The type to set.
	 */
	public void setType(int type)
	{
		this.type = type;
	}
	
	/**
	 * @return The description of this result files type.
	 */
	public String getTypeDesc()
	{
		return fileTypeDesc[type];
	}
	
	public String getTypeCode()
	{
		return fileTypeCode[type];
	}

	/**
	 * Search for the result file type, looking in both the descriptions (i18n depending) and the codes
	 * @param typeString the type string to search for
	 * @return the result file type
	 */
	public static final int getType(String typeString)
	{
		int idx = Const.indexOfString(typeString, fileTypeDesc);
		if (idx>=0) return idx;
		idx = Const.indexOfString(typeString, fileTypeCode);
		if (idx>=0) return idx;
		
		return FILE_TYPE_GENERAL;
	}
	
	/**
	 * @param fileType the result file type
	 * @return the result file type code
	 */
	public static final String getTypeCode(int fileType)
	{
		return fileTypeCode[fileType];
	}

	/**
	 * @param fileType the result file type
	 * @return the result file type description
	 */
	public static final String getTypeDesc(int fileType)
	{
		return fileTypeDesc[fileType];
	}
	
	public static final String[] getAllTypeDesc()
	{
		return fileTypeDesc;
	}

	/**
	 * @return Returns the timestamp.
	 */
	public Date getTimestamp()
	{
		return timestamp;
	}

	/**
	 * @param timestamp The timestamp to set.
	 */
	public void setTimestamp(Date timestamp)
	{
		this.timestamp = timestamp;
	}
	
	/**
	 * @return an output Row for this Result File object.
	 */
	public RowMetaAndData getRow()
	{
        RowMetaAndData row = new RowMetaAndData();

		// First the type
		row.addValue( new ValueMeta("type", ValueMetaInterface.TYPE_STRING), getTypeDesc());

		// The filename
		row.addValue( new ValueMeta("filename", ValueMetaInterface.TYPE_STRING), file.getName().getBaseName());

		// The path
		row.addValue( new ValueMeta("path", ValueMetaInterface.TYPE_STRING), file.getName().getURI());

		// The origin parent
		row.addValue( new ValueMeta("parentorigin", ValueMetaInterface.TYPE_STRING), originParent);

		// The origin
		row.addValue( new ValueMeta("origin", ValueMetaInterface.TYPE_STRING), origin);

		// The comment
		row.addValue( new ValueMeta("comment", ValueMetaInterface.TYPE_STRING), comment);

		// The timestamp
		row.addValue( new ValueMeta("timestamp", ValueMetaInterface.TYPE_DATE), timestamp);

		return row;
	}

	public String getXML() 
	{
		StringBuffer xml = new StringBuffer();
    	
        xml.append(XMLHandler.openTag(XML_TAG));
        
        xml.append( XMLHandler.addTagValue("type", getTypeCode()) );
        xml.append( XMLHandler.addTagValue("file", file.getName().toString()) );
        xml.append( XMLHandler.addTagValue("parentorigin", originParent) );
        xml.append( XMLHandler.addTagValue("origin", origin) );
        xml.append( XMLHandler.addTagValue("comment", comment) );
        xml.append( XMLHandler.addTagValue("timestamp", timestamp) );
        
        xml.append(XMLHandler.closeTag(XML_TAG));
        
        return xml.toString();
	}
	
	public ResultFile(Node node) throws KettleFileException
	{
		try {
			type = getType(XMLHandler.getTagValue(node, "type"));
			file = KettleVFS.getFileObject( XMLHandler.getTagValue(node, "file") );
			originParent = XMLHandler.getTagValue(node, "parentorigin");
			origin = XMLHandler.getTagValue(node, "origin");
			comment = XMLHandler.getTagValue(node, "comment");
			timestamp = XMLHandler.stringToDate( XMLHandler.getTagValue(node, "timestamp") );
		} catch(Exception e) {
			throw new KettleFileException(e);
		}
	}

}
