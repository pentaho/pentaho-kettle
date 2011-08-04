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
 

package org.pentaho.di.trans.steps.mailinput;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.job.entries.getpop.MailConnection;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class MailInputData extends BaseStepData implements StepDataInterface
{
    public RowMetaInterface inputRowMeta;
    public int totalpreviousfields;
    
	public RowMetaInterface outputRowMeta;
	public MailConnection mailConn;
	public int messagesCount;
    public long                rownr;
    public String folder;
    public String[] folders;
    public int folderenr;
    public boolean usePOP;
    public int indexOfFolderField;
	public Object[] readrow;
	public int rowlimit;
	public int nrFields;
	
	/**
	 * 
	 */
	public MailInputData()
	{
		super();
		mailConn=null;
		messagesCount=0;
		folder=null;
		folderenr=0;
		usePOP=true;
		indexOfFolderField=-1;
		readrow=null;
		totalpreviousfields=0;
		rowlimit=0;
	}

}
