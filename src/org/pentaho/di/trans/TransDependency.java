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

package org.pentaho.di.trans;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.w3c.dom.Node;


/*
 * Created on 5-apr-2004
 *
 */

public class TransDependency implements XMLInterface, Cloneable
{
	private static Class<?> PKG = Trans.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final String XML_TAG = "dependency";
    
    private DatabaseMeta db;
	private String tablename;
	private String fieldname;
	
	private ObjectId id;
	
	public TransDependency(DatabaseMeta db, String tablename, String fieldname)
	{
		this.db = db;
		this.tablename = tablename;
		this.fieldname = fieldname;
	}

	public TransDependency()
	{
		this(null, null, null);
	}
    
    public String getXML()
    {
        StringBuilder xml = new StringBuilder(200);
        
        xml.append("      ").append(XMLHandler.openTag(XML_TAG)).append(Const.CR); //$NON-NLS-1$
        xml.append("        ").append(XMLHandler.addTagValue("connection", db==null?"":db.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        xml.append("        ").append(XMLHandler.addTagValue("table",      tablename)); //$NON-NLS-1$ //$NON-NLS-2$
        xml.append("        ").append(XMLHandler.addTagValue("field",      fieldname)); //$NON-NLS-1$ //$NON-NLS-2$
        xml.append("      ").append(XMLHandler.closeTag(XML_TAG)).append(Const.CR); //$NON-NLS-1$
        
        return xml.toString();
    }
    
	public TransDependency(Node depnode, List<DatabaseMeta> databases) throws KettleXMLException
	{
		try
		{
			String depcon    = XMLHandler.getTagValue(depnode, "connection"); //$NON-NLS-1$
			db = DatabaseMeta.findDatabase(databases, depcon);
			tablename        = XMLHandler.getTagValue(depnode, "table"); //$NON-NLS-1$
			fieldname        = XMLHandler.getTagValue(depnode, "field"); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "TransDependency.Exception.UnableToLoadTransformation"), e); //$NON-NLS-1$
		}
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public void setObjectId(ObjectId id)
	{
		this.id = id;
	}
	
	public ObjectId getObjectId()
	{
		return id;
	}
	
	public void setDatabase(DatabaseMeta db)
	{
		this.db = db;
	}
	
	public DatabaseMeta getDatabase()
	{
		return db;
	}
	
	public void setTablename(String tablename)
	{
		this.tablename = tablename;
	}
	
	public String getTablename()
	{
		return tablename;
	}
	
	public void setFieldname(String fieldname)
	{
		this.fieldname = fieldname;
	}
	
	public String getFieldname()
	{
		return fieldname;
	}
}
