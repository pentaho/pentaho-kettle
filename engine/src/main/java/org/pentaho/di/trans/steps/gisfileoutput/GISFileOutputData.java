package org.pentaho.di.trans.steps.gisfileoutput;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.geospatial.GeotoolsWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Provides data for the GISFileOutput step.
 * 
 * @author jmathieu, etdub
 * @since 29-sep-2008
 */
public class GISFileOutputData extends BaseStepData implements StepDataInterface {
    public RowMetaInterface outputRowMeta;
	public GeotoolsWriter gtWriter;
    public String charset;
    public FileObject file;
    public RowSet rowSet;
	
	public GISFileOutputData(){
		super();
		gtWriter=null;
	    file=null;
	}
}