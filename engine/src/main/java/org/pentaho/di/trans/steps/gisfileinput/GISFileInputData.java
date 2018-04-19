package org.pentaho.di.trans.steps.gisfileinput;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import java.util.HashMap;

/**
 * Provides data for the GISFileInput step.
 *
 * @author etdub, jmathieu, tbadard
 * @since 29-sep-2008
 */
public class GISFileInputData extends BaseStepData implements StepDataInterface {
    public RowMetaInterface fields;
    public RowMetaInterface outputRowMeta;
    public FileInputList files;
    public HashMap<FileObject, Object[]> passThruFields;
    public Object[] currentPassThruFieldsRow;
    public int nrPassThruFields;
    public RowSet rowSet;
    public int fileIndex;
    public int readerRowSize;
    public int rowRead;

    public GISFileInputData() {
        super();
        fields = null;
        fileIndex = 0;
        rowRead = 0;
    }
}
