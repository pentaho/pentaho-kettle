/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.fileinput.text;

import java.util.Date;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.playlist.FilePlayListAll;
import org.pentaho.di.core.playlist.FilePlayListReplay;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.file.BaseFileInputStep;
import org.pentaho.di.trans.steps.file.IBaseFileInputReader;

/**
 * Read all sorts of text files, convert them to rows and writes these to one or more output streams.
 *
 * @author Matt
 * @since 4-apr-2003
 */
public class TextFileInput extends BaseFileInputStep<TextFileInputMeta, TextFileInputData> implements StepInterface {
  private static Class<?> PKG = TextFileInputMeta.class; // for i18n purposes, needed by Translator2!!

  public TextFileInput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  protected IBaseFileInputReader createReader( TextFileInputMeta meta, TextFileInputData data, FileObject file )
    throws Exception {
    return new TextFileInputReader( this, meta, data, file, log );
  }

  @Override
  public boolean init() {
    Date replayDate = getTrans().getReplayDate();
    if ( replayDate == null ) {
      data.filePlayList = FilePlayListAll.INSTANCE;
    } else {
      data.filePlayList =
          new FilePlayListReplay( replayDate, meta.errorHandling.lineNumberFilesDestinationDirectory,
              meta.errorHandling.lineNumberFilesExtension, meta.errorHandling.errorFilesDestinationDirectory,
              meta.errorHandling.errorFilesExtension, meta.content.encoding );
    }

    data.filterProcessor = new TextFileFilterProcessor( meta.getFilter(), this );

    // calculate the file format type in advance so we can use a switch
    data.fileFormatType = meta.getFileFormatTypeNr();

    // calculate the file type in advance CSV or Fixed?
    data.fileType = meta.getFileTypeNr();

    // Handle the possibility of a variable substitution
    data.separator = environmentSubstitute( meta.content.separator );
    data.enclosure = environmentSubstitute( meta.content.enclosure );
    data.escapeCharacter = environmentSubstitute( meta.content.escapeCharacter );
    // CSV without separator defined
    if ( meta.content.fileType.equalsIgnoreCase( "CSV" ) && ( meta.content.separator == null || meta.content.separator
        .isEmpty() ) ) {
      logError( BaseMessages.getString( PKG, "TextFileInput.Exception.NoSeparator" ) );
      return false;
    }

    return true;
  }

  public boolean isWaitingForData() {
    return true;
  }
}
