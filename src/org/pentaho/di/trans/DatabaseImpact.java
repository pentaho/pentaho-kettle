 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
/*
 * Created on 13-aug-2004
 *
 * @author Matt
 *
 * This class gives info on the impact that a step has on 
 * 	 a database,
 *   a table 
 *   a field, 
 *   etc.
 */

package org.pentaho.di.trans;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;

public class DatabaseImpact
{
	private static Class<?> PKG = Trans.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final int TYPE_IMPACT_NONE       = 0;
	public static final int TYPE_IMPACT_READ       = 1;
	public static final int TYPE_IMPACT_WRITE      = 2;
	public static final int TYPE_IMPACT_READ_WRITE = 3;
	public static final int TYPE_IMPACT_TRUNCATE   = 4;
    public static final int TYPE_IMPACT_DELETE     = 5;
    public static final int TYPE_IMPACT_UPDATE     = 6;
	
	public static final String typeDesc[] = 
		{
		 BaseMessages.getString(PKG, "DatabaseImpact.TypeDesc.Label.None"), //$NON-NLS-1$
		 BaseMessages.getString(PKG, "DatabaseImpact.TypeDesc.Label.Read"), //$NON-NLS-1$
		 BaseMessages.getString(PKG, "DatabaseImpact.TypeDesc.Label.Write"), //$NON-NLS-1$
		 BaseMessages.getString(PKG, "DatabaseImpact.TypeDesc.Label.ReadOrWrite"), //$NON-NLS-1$
		 BaseMessages.getString(PKG, "DatabaseImpact.TypeDesc.Label.Truncate"), //$NON-NLS-1$
         BaseMessages.getString(PKG, "DatabaseImpact.TypeDesc.Label.Delete"), //$NON-NLS-1$
         BaseMessages.getString(PKG, "DatabaseImpact.TypeDesc.Label.Update") //$NON-NLS-1$
		};
	
	private String transname;
	private String stepname;
	private String dbname;
	private String table;
	private String field;
	private String valuename;
	private String valueorigin;
	private String sql;
	private String remark;
	private int type;

	public DatabaseImpact(int type, String transname, String stepname, String dbname, String table, String field, String valuename, String valueorigin, String sql, String remark)
	{
		this.type        = type;
		this.transname   = transname;
		this.stepname    = stepname;
		this.dbname      = dbname;
		this.table       = table;
		this.field       = field;
		this.valuename   = valuename;
		this.valueorigin = valueorigin;
		this.sql         = sql;
		this.remark      = remark;
	}
	
	public String getTransformationName()
	{
		return transname;
	}
	
	public String getStepName()
	{
		return stepname;
	}
	
	public String getValueOrigin()
	{
		return valueorigin;
	}
	
	public String getDatabaseName()
	{
		return dbname;
	}
	
	public String getTable()
	{
		return table;
	}
	
	public String getField()
	{
		return field;
	}
	
	public String getValue()
	{
		return valuename;
	}
	
	public String getSQL()
	{
		return sql;
	}
	
	public String getRemark()
	{
		return remark;
	}
	
	public String getTypeDesc()
	{
		return typeDesc[type];
	}
	
	public static final int getTypeDesc(String typedesc)
	{
		for (int i=1;i<typeDesc.length;i++)
		{
			if (typeDesc[i].equalsIgnoreCase(typedesc)) return i;
		}
		return TYPE_IMPACT_NONE;
	}
	
	public int getType()
	{
		return type;
	}
	
	public RowMetaAndData getRow()
	{
        RowMetaAndData r = new RowMetaAndData();
		r.addValue( new ValueMeta(BaseMessages.getString(PKG, "DatabaseImpact.RowDesc.Label.Type"), ValueMetaInterface.TYPE_STRING), getTypeDesc() ); //$NON-NLS-1$
		r.addValue( new ValueMeta(BaseMessages.getString(PKG, "DatabaseImpact.RowDesc.Label.Transformation"), ValueMetaInterface.TYPE_STRING), getTransformationName() ); //$NON-NLS-1$
		r.addValue( new ValueMeta(BaseMessages.getString(PKG, "DatabaseImpact.RowDesc.Label.Step"), ValueMetaInterface.TYPE_STRING), getStepName() ); //$NON-NLS-1$
		r.addValue( new ValueMeta(BaseMessages.getString(PKG, "DatabaseImpact.RowDesc.Label.Database"), ValueMetaInterface.TYPE_STRING), getDatabaseName() ); //$NON-NLS-1$
		r.addValue( new ValueMeta(BaseMessages.getString(PKG, "DatabaseImpact.RowDesc.Label.Table"), ValueMetaInterface.TYPE_STRING), getTable() ); //$NON-NLS-1$
		r.addValue( new ValueMeta(BaseMessages.getString(PKG, "DatabaseImpact.RowDesc.Label.Field"), ValueMetaInterface.TYPE_STRING), getField() ); //$NON-NLS-1$
		r.addValue( new ValueMeta(BaseMessages.getString(PKG, "DatabaseImpact.RowDesc.Label.Value"), ValueMetaInterface.TYPE_STRING), getValue() ); //$NON-NLS-1$
		r.addValue( new ValueMeta(BaseMessages.getString(PKG, "DatabaseImpact.RowDesc.Label.ValueOrigin"), ValueMetaInterface.TYPE_STRING), getValueOrigin() ); //$NON-NLS-1$
		r.addValue( new ValueMeta(BaseMessages.getString(PKG, "DatabaseImpact.RowDesc.Label.SQL"), ValueMetaInterface.TYPE_STRING), getSQL() ); //$NON-NLS-1$
		r.addValue( new ValueMeta(BaseMessages.getString(PKG, "DatabaseImpact.RowDesc.Label.Remarks"), ValueMetaInterface.TYPE_STRING), getRemark() ); //$NON-NLS-1$

		return r;
	}

}
