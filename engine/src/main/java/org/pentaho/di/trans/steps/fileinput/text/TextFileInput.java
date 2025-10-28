/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.fileinput.text;

import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.compress.CompressionInputStream;
import org.pentaho.di.core.compress.CompressionProvider;
import org.pentaho.di.core.compress.CompressionProviderFactory;
import org.pentaho.di.core.gui.TextFileInputFieldInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.playlist.FilePlayListAll;
import org.pentaho.di.core.playlist.FilePlayListReplay;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.common.CsvInputAwareMeta;
import org.pentaho.di.trans.steps.common.CsvInputAwareStep;
import org.pentaho.di.trans.steps.file.BaseFileField;
import org.pentaho.di.trans.steps.file.BaseFileInputStep;
import org.pentaho.di.trans.steps.file.IBaseFileInputReader;

/**
 * Read all sorts of text files, convert them to rows and writes these to one or more output streams.
 *
 * @author Matt
 * @since 4-apr-2003
 */
public class TextFileInput extends BaseFileInputStep<TextFileInputMeta, TextFileInputData> implements StepInterface,
  CsvInputAwareStep {
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

  public Vector<TextFileInputFieldInterface> getFields( TextFileInputMeta info, List<String> rows ) {
    Vector<TextFileInputFieldInterface> fields = new Vector<>();

    int maxsize = 0;
    for ( String row : rows ) {
      int len = row.length();
      if ( len > maxsize ) {
        maxsize = len;
      }
    }

    int prevEnd = 0;
    int dummynr = 1;

    for ( int i = 0; i < info.inputFields.length; i++ ) {
      BaseFileField f = info.inputFields[ i ];

      // See if positions are skipped, if this is the case, add dummy fields...
      if ( f.getPosition() != prevEnd ) { // gap

        BaseFileField field = new BaseFileField( "Dummy" + dummynr, prevEnd, f.getPosition() - prevEnd );
        field.setIgnored( true ); // don't include in result by default.
        fields.add( field );
        dummynr++;
      }

      BaseFileField field = new BaseFileField( f.getName(), f.getPosition(), f.getLength() );
      field.setType( f.getType() );
      field.setIgnored( false );
      field.setFormat( f.getFormat() );
      field.setPrecision( f.getPrecision() );
      field.setTrimType( f.getTrimType() );
      field.setDecimalSymbol( f.getDecimalSymbol() );
      field.setGroupSymbol( f.getGroupSymbol() );
      field.setCurrencySymbol( f.getCurrencySymbol() );
      field.setRepeated( f.isRepeated() );
      field.setNullString( f.getNullString() );

      fields.add( field );

      prevEnd = field.getPosition() + field.getLength();
    }

    if ( info.inputFields.length == 0 ) {
      BaseFileField field = new BaseFileField( "Field1", 0, maxsize );
      fields.add( field );
    } else {
      // Take the last field and see if it reached until the maximum...
      BaseFileField f = info.inputFields[ info.inputFields.length - 1 ];

      int pos = f.getPosition();
      int len = f.getLength();
      if ( pos + len < maxsize ) {
        // If not, add an extra trailing field!
        BaseFileField field = new BaseFileField( "Dummy" + dummynr, pos + len, maxsize - pos - len );
        field.setIgnored( true ); // don't include in result by default.
        fields.add( field );
      }
    }

    Collections.sort( fields );

    return fields;
  }

  public boolean isWaitingForData() {
    return true;
  }

  @Override
  public String massageFieldName( final String fieldName ) {
    // Replace all spaces and hyphens (-) with underscores (_)
    String massagedFieldName = fieldName;
    massagedFieldName = Const.replace( massagedFieldName, " ", "_" );
    massagedFieldName = Const.replace( massagedFieldName, "-", "_" );
    return massagedFieldName;
  }

  public InputStream getInputStream( final CsvInputAwareMeta meta ) {
    InputStream fileInputStream;
    CompressionInputStream inputStream = null;
    try {
      FileObject fileObject = meta.getHeaderFileObject( getTransMeta() );
      fileInputStream = KettleVFS.getInputStream( fileObject );
      CompressionProvider provider = CompressionProviderFactory.getInstance().createCompressionProviderInstance(
        ( (TextFileInputMeta) meta ).content.fileCompression );
      inputStream = provider.createInputStream( fileInputStream );
    } catch ( final Exception e ) {
      logError( BaseMessages.getString( "FileInputDialog.ErrorGettingFileDesc.DialogMessage" ), e );
    }
    return inputStream;
  }

  @Override
  public LogChannelInterface logChannel() {
    return getLogChannel();
  }

}
