 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 
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

package org.pentaho.di.core.trans;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.value.Value;

public class DatabaseImpact
{
	public static final int TYPE_IMPACT_NONE       = 0;
	public static final int TYPE_IMPACT_READ       = 1;
	public static final int TYPE_IMPACT_WRITE      = 2;
	public static final int TYPE_IMPACT_READ_WRITE = 3;
	public static final int TYPE_IMPACT_TRUNCATE   = 4;
    public static final int TYPE_IMPACT_DELETE     = 5;
    public static final int TYPE_IMPACT_UPDATE     = 6;
	
	public static final String typeDesc[] = 
		{
		 Messages.getString("DatabaseImpact.TypeDesc.Label.None"), //$NON-NLS-1$
		 Messages.getString("DatabaseImpact.TypeDesc.Label.Read"), //$NON-NLS-1$
		 Messages.getString("DatabaseImpact.TypeDesc.Label.Write"), //$NON-NLS-1$
		 Messages.getString("DatabaseImpact.TypeDesc.Label.ReadOrWrite"), //$NON-NLS-1$
		 Messages.getString("DatabaseImpact.TypeDesc.Label.Truncate"), //$NON-NLS-1$
         Messages.getString("DatabaseImpact.TypeDesc.Label.Delete"), //$NON-NLS-1$
         Messages.getString("DatabaseImpact.TypeDesc.Label.Update") //$NON-NLS-1$
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
	
	public Row getRow()
	{
		Row r = new Row();
		r.addValue( new Value(Messages.getString("DatabaseImpact.RowDesc.Label.Type"), 			getTypeDesc()) ); //$NON-NLS-1$
		r.addValue( new Value(Messages.getString("DatabaseImpact.RowDesc.Label.Transformation"), getTransformationName()) ); //$NON-NLS-1$
		r.addValue( new Value(Messages.getString("DatabaseImpact.RowDesc.Label.Step"), 			getStepName()) ); //$NON-NLS-1$
		r.addValue( new Value(Messages.getString("DatabaseImpact.RowDesc.Label.Database"), 		getDatabaseName()) ); //$NON-NLS-1$
		r.addValue( new Value(Messages.getString("DatabaseImpact.RowDesc.Label.Table"), 			getTable()) ); //$NON-NLS-1$
		r.addValue( new Value(Messages.getString("DatabaseImpact.RowDesc.Label.Field"), 			getField()) ); //$NON-NLS-1$
		r.addValue( new Value(Messages.getString("DatabaseImpact.RowDesc.Label.Value"), 			getValue()) ); //$NON-NLS-1$
		r.addValue( new Value(Messages.getString("DatabaseImpact.RowDesc.Label.ValueOrigin"), 	getValueOrigin()) ); //$NON-NLS-1$
		r.addValue( new Value(Messages.getString("DatabaseImpact.RowDesc.Label.SQL"), 			getSQL()) ); //$NON-NLS-1$
		r.addValue( new Value(Messages.getString("DatabaseImpact.RowDesc.Label.Remarks"), 		getRemark()) ); //$NON-NLS-1$

		return r;
	}

}
