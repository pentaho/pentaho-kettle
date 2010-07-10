
package org.pentaho.di.trans.steps.ssh;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import com.trilead.ssh2.Connection;


/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class SSHData extends BaseStepData implements StepDataInterface
{
	public int indexOfCommand;
	public Connection conn;
	public boolean wroteOneRow;
	public String commands;
	public int nrInputFields;
	public int nrOutputFields;
	
	// Output fields
	public String stdOutField;
	public String stdTypeField;
	
    public RowMetaInterface outputRowMeta;

	/**
	 * 
	 */
	public SSHData()
	{
		super();
		this.indexOfCommand=-1;
		this.conn=null;
		this.wroteOneRow=false;
		this.commands=null;
		this.stdOutField=null;
		this.stdTypeField=null;
	}

}
