package org.pentaho.di.profiling.datacleaner;

import java.io.DataOutputStream;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;

public class DataCleanerKettleFileWriter extends RowAdapter {

    private TransMeta transMeta;
    private StepMeta stepMeta;

    private DataOutputStream outputStream;
    private Trans trans;
    private LogChannelInterface log;

    private String filename;

    public DataCleanerKettleFileWriter(Trans trans, StepMeta stepMeta) throws Exception {
        this.trans = trans;
        this.transMeta = trans.getTransMeta();
        this.stepMeta = stepMeta;
        this.log = trans.getLogChannel();
    }

    public void run() throws Exception {
        FileObject tempFile = KettleVFS.createTempFile("datacleaner", ".kettlestream",
                System.getProperty("java.io.tmpdir"));
        filename = KettleVFS.getFilename(tempFile);

        outputStream = new DataOutputStream(KettleVFS.getOutputStream(tempFile, false));
        log.logBasic("DataCleaner temp file created: " + filename);

        RowMetaInterface rowMeta = transMeta.getStepFields(stepMeta);

        log.logBasic("Opened an output stream to DataCleaner.");

        // Write the transformation name, the step name and the row metadata
        // first...
        //
        outputStream.writeUTF(transMeta.getName());
        log.logBasic("wrote the transformation name.");

        outputStream.writeUTF(stepMeta.getName());
        log.logBasic("wrote the step name.");

        rowMeta.writeMeta(outputStream);
        log.logBasic("Wrote the row metadata");

        // Add a row listener to the selected step...
        //
        List<StepInterface> steps = trans.findBaseSteps(stepMeta.getName());

        // Just do one step copy for the time being...
        //
        StepInterface step = steps.get(0);

        step.addRowListener(this);
        log.logBasic("Added the row listener to step: " + step.toString());

        // Now start the transformation...
        //
        trans.startThreads();
        log.logBasic("Started the transformation to profile... waiting until the transformation has finished");

        trans.waitUntilFinished();

        log.logBasic("The transformation to profile finished.");
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
        try {
            rowMeta.writeData(outputStream, row);
        } catch (KettleFileException e) {
            throw new KettleStepException(e);
        }
    }

    public void close() throws Exception {
        outputStream.flush();
        outputStream.close();
    }

}
