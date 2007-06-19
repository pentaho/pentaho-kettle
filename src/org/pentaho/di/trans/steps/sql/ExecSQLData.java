 package org.pentaho.di.trans.steps.sql;

import java.util.List;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;




/**
 * @author Matt
 * @since 20-jan-2005
 */
public class ExecSQLData extends BaseStepData implements StepDataInterface
{
	public Database db;
    public Result   result;
    public int[]    argumentIndexes;
    public List     markerPositions;
    public RowMetaInterface outputRowMeta;
	
	public ExecSQLData()
	{
		super();
		
		db              = null;
        result          = null;
        argumentIndexes = null;
        markerPositions = null;
	}


	
}
