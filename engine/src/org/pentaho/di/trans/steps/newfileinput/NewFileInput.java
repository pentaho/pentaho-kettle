package org.pentaho.di.trans.steps.newfileinput;

import java.util.Date;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.playlist.FilePlayListAll;
import org.pentaho.di.core.playlist.FilePlayListReplay;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.baseinput.BaseInputStep;
import org.pentaho.di.trans.steps.baseinput.IBaseInputReader;

/**
 * Replacement for TextFileInput step.
 */
public class NewFileInput extends BaseInputStep<NewFileInputMeta, NewFileInputData>implements StepInterface {
    private static Class<?> PKG = NewFileInputMeta.class; // for i18n purposes, needed by Translator2!!

    public NewFileInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
            TransMeta transMeta, Trans trans) {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }

    @Override
    protected IBaseInputReader createReader(NewFileInputMeta meta, NewFileInputData data, FileObject file)
            throws Exception {
        return new NewFileInputReader(this, meta, data, file, log);
    }

    @Override
    public boolean init() {
        Date replayDate = getTrans().getReplayDate();
        if (replayDate == null) {
            data.filePlayList = FilePlayListAll.INSTANCE;
        } else {
            data.filePlayList = new FilePlayListReplay(replayDate,
                    meta.errorHandling.lineNumberFilesDestinationDirectory,
                    meta.errorHandling.lineNumberFilesExtension,
                    meta.errorHandling.errorFilesDestinationDirectory, meta.errorHandling.errorFilesExtension,
                    meta.content.encoding);
        }

        data.filterProcessor = new NewFileFilterProcessor(meta.getFilter());

        // calculate the file format type in advance so we can use a switch
        data.fileFormatType = meta.getFileFormatTypeNr();

        // calculate the file type in advance CSV or Fixed?
        data.fileType = meta.getFileTypeNr();

        // Handle the possibility of a variable substitution
        data.separator = environmentSubstitute(meta.content.separator);
        data.enclosure = environmentSubstitute(meta.content.enclosure);
        data.escapeCharacter = environmentSubstitute(meta.content.escapeCharacter);

        return true;
    }

    public boolean isWaitingForData() {
        return true;
    }
}
