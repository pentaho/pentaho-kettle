
package org.pentaho.di.trans.steps.zipfile;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;



/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class ZipFileData extends BaseStepData implements StepDataInterface
{
	public RowMetaInterface outputRowMeta;
	
	public int indexOfSourceFilename;
	public int indexOfZipFilename;
	public FileObject	sourceFile;
	public FileObject	zipFile;
	
	public int indexOfBaseFolder;
	public String baseFolder;
	
	public int indexOfMoveToFolder;
    
	/**
	 * 
	 */
	public ZipFileData()
	{
		super();
		indexOfSourceFilename=-1;
		indexOfZipFilename=-1;
		sourceFile=null;
		zipFile=null;
		indexOfBaseFolder=-1;
		baseFolder=null;
		indexOfMoveToFolder=-1;
	}

}
