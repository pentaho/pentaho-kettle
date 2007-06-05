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
 

package org.pentaho.di.trans.steps.update;

import java.sql.PreparedStatement;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;




/**
 * @author Matt
 * @since 28-feb-2005
 *
 */
public class UpdateData extends BaseStepData implements StepDataInterface
{
	public Database db;

	public int    keynrs[];         // nr of keylookup -value in row...
	public int    keynrs2[];        // nr of keylookup2-value in row...
	public int    valuenrs[];       // Stream valuename nrs to prevent searches.

    public String stringErrorKeyNotFound;

    public String stringFieldnames;

    public RowMetaInterface outputRowMeta;

    public String schemaTable;

    public PreparedStatement prepStatementLookup;
    public PreparedStatement prepStatementUpdate;

    public RowMetaInterface lookupParameterRowMeta;
    public RowMetaInterface lookupReturnRowMeta;
    public RowMetaInterface updateParameterRowMeta;
	
	/**
	 * 
	 */
	public UpdateData()
	{
		super();

		db=null;
	}

}
