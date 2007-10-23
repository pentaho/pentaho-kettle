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
 

package org.pentaho.di.trans.steps.sort;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;



/**
 * @author Matt
 * @since 24-jan-2005
 */
public class SortRowsData extends BaseStepData implements StepDataInterface
{
	public List<FileObject> files;
	public List<Object[]>   buffer;
    public int              getBufferIndex;

	public List<InputStream> fis;
    public List<GZIPInputStream> gzis;
    public List<DataInputStream> dis;
	public List<Object[]> rowbuffer;
    public List<Integer> bufferSizes;

	public int     fieldnrs[];      // the corresponding field numbers;
    public FileObject fil;
    public RowMetaInterface outputRowMeta;
	public int sortSize;
	public boolean compressFiles;
	public boolean convertKeysToNative;

	/**
	 * 
	 */
	public SortRowsData()
	{
		super();
		
		files= new ArrayList<FileObject>();
		fis  = new ArrayList<InputStream>();
        gzis  = new ArrayList<GZIPInputStream>();
        dis = new ArrayList<DataInputStream>();
        bufferSizes = new ArrayList<Integer>();
	}

}
