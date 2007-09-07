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
 

package org.pentaho.di.trans.steps.getfilesrowscount;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import org.apache.commons.vfs.FileObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar Hassan
 * @since 06-Sept-2007
 */
public class GetFilesRowsCountData extends BaseStepData implements StepDataInterface 
{
	public String thisline;
	public RowMetaInterface outputRowMeta;
	public RowMetaInterface convertRowMeta;
	public Object[] previousRow;

	public List<FileObject>     files;
	public boolean              last_file;
	public FileObject           file;
	public long                 filenr;
	public long					filesnr;
	
	public InputStream fr;
	public InputStreamReader isr;
	public BufferedInputStream is;
    public Document            document;
    public Node                section;
    public String              itemElement;
    public int                 itemCount;
    public int                 itemPosition;
    public long                rownr;
    public int fileFormatType;
    public StringBuffer lineStringBuffer;

	/**
	 * 
	 */
	public GetFilesRowsCountData()
	{
		super();
		previousRow = null;
		thisline=null;
		previousRow=null;
		filenr = 0;
		
		fr=null;
		is=null;
		lineStringBuffer = new StringBuffer(256);
	}
}