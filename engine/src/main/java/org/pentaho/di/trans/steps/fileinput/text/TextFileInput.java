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
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.compress.CompressionInputStream;
import org.pentaho.di.core.compress.CompressionProvider;
import org.pentaho.di.core.compress.CompressionProviderFactory;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.gui.TextFileInputFieldInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.playlist.FilePlayListAll;
import org.pentaho.di.core.playlist.FilePlayListReplay;
import org.pentaho.di.core.row.ValueMetaInterface;
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

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  public JSONObject setMinimalWidthAction( Map<String, String> queryParams ) throws JsonProcessingException {
    JSONObject jsonObject = new JSONObject();
    JSONArray textFileFields = new JSONArray();
    ObjectMapper objectMapper = new ObjectMapper();
    for ( TextFileInputFieldDTO textFileInputFieldDTO : getUpdatedTextFields() ) {
      textFileFields.add( objectMapper.readTree( objectMapper.writeValueAsString( textFileInputFieldDTO ) ) );
    }
    jsonObject.put( "updatedData", textFileFields );
    return jsonObject;
  }

  public List<TextFileInputFieldDTO> getUpdatedTextFields() {
    TextFileInputMeta tfii = (TextFileInputMeta) getStepMetaInterface();
    List<TextFileInputFieldDTO> textFileFields = new ArrayList<>();

    for ( BaseFileField textFileField : tfii.getInputFields() ) {
      TextFileInputFieldDTO updatedTextFileField = new TextFileInputFieldDTO();
      updatedTextFileField.setName( textFileField.getName() );
      updatedTextFileField.setType( textFileField.getTypeDesc() );

      switch ( textFileField.getType() ) {
        case ValueMetaInterface.TYPE_STRING:
          updatedTextFileField.setFormat( StringUtils.EMPTY );
          break;
        case ValueMetaInterface.TYPE_INTEGER:
          updatedTextFileField.setFormat( "0" );
          break;
        case ValueMetaInterface.TYPE_NUMBER:
          updatedTextFileField.setFormat( "0.#####" );
          break;
        default:
          break;
      }
      updatedTextFileField.setLength( StringUtils.EMPTY );
      updatedTextFileField.setPrecision( StringUtils.EMPTY );
      updatedTextFileField.setCurrency( textFileField.getCurrencySymbol() );
      updatedTextFileField.setDecimal( textFileField.getDecimalSymbol() );
      updatedTextFileField.setGroup( textFileField.getGroupSymbol() );
      updatedTextFileField.setTrimType( "both" );
      updatedTextFileField.setNullif( textFileField.getNullString() );
      updatedTextFileField.setIfnull( textFileField.getIfNullValue() );
      int position = textFileField.getPosition();
      updatedTextFileField.setPosition( position == -1 ? StringUtils.EMPTY : String.valueOf( position ) );
      updatedTextFileField.setRepeat( textFileField.isRepeated() ? "Y" : "N" );

      textFileFields.add( updatedTextFileField );
    }
    return textFileFields;
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  public JSONObject getFieldsAction( Map<String, String> queryParams )
    throws KettleException, JsonProcessingException {
    JSONObject response = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    ObjectMapper objectMapper = new ObjectMapper();

    TextFileInputMeta tfii = (TextFileInputMeta) getStepMetaInterface();

    if ( TextFileInputMeta.FILE_TYPE_CSV == tfii.getFileTypeNr() ) {
      return populateMeta( queryParams );
    } else {
      List<String> rows = this.getFirst( 50, false );
      jsonArray.addAll( rows );
    }

    response.put( "fields", jsonArray );
    return response;
  }

  // This method contains code extracted from fileinput/text/TextFileCSVImportProgressDialog class
  // to get Fields data and Fields summary statistics
  private JSONObject populateMeta( Map<String, String> queryParams ) throws KettleException, JsonProcessingException {
    JSONObject response = new JSONObject();
    String isSampleSummary = queryParams.get( "isSampleSummary" );
    int samples = Integer.parseInt( Objects.toString( queryParams.get( "noOfFields" ), "0" ) );

    TransMeta transMeta = getTransMeta();
    CsvInputAwareMeta csvInputAwareMeta = (CsvInputAwareMeta) getStepMetaInterface();
    final InputStream inputStream = getInputStream( csvInputAwareMeta );
    final BufferedInputStreamReader reader = getBufferedReader( csvInputAwareMeta, inputStream );
    meta = (TextFileInputMeta) getStepMetaInterface();
    String[] fieldNames = getFieldNames( csvInputAwareMeta );
    meta.setFields( fieldNames );

    TextFileCsvFileTypeImportProcessor processor =
      new TextFileCsvFileTypeImportProcessor( meta, transMeta, reader, samples, Boolean.parseBoolean( isSampleSummary ) );
    String summary = processor.analyzeFile( true );

    response.put( "fields", convertFieldsToJsonArray( processor.getInputFieldsDto() ) );
    response.put( "summary", summary );
    return response;
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

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  public JSONObject getFieldNamesAction( Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    String[] fieldNames = getFieldNames( (CsvInputAwareMeta) getStepMetaInterface() );
    Collections.addAll( jsonArray, fieldNames );
    response.put( "fieldNames", jsonArray );
    return response;
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  public JSONObject showFilesAction( Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();

    String filter = queryParams.get( "filter" );
    String isRegex = queryParams.get( "isRegex" );

    TextFileInputMeta tfii = (TextFileInputMeta) getStepMetaInterface();
    String[] files =
      FileInputList.createFilePathList( getTransMeta().getBowl(), getTransMeta(), tfii.inputFiles.fileName,
        tfii.inputFiles.fileMask, tfii.inputFiles.excludeFileMask, tfii.inputFiles.fileRequired,
        tfii.inputFiles.includeSubFolderBoolean() );

    JSONArray filteredFiles = new JSONArray();

    if ( files == null || files.length == 0 ) {
      response.put( "message", BaseMessages.getString( PKG, "TextFileInputDialog.NoFilesFound.DialogMessage" ) );
    } else {
      for ( String file : files ) {
        if ( Boolean.parseBoolean( isRegex ) ) {
          Matcher matcher = Pattern.compile( filter ).matcher( file );
          if ( matcher.matches() ) {
            filteredFiles.add( file );
          }
        } else if ( StringUtils.isBlank( filter ) || StringUtils.contains( file.toUpperCase(),
          filter.toUpperCase() ) ) {
          filteredFiles.add( file );
        }
      }
    }
    response.put( "files", filteredFiles );
    return response;
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  public JSONObject validateShowContentAction( Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    TextFileInputMeta meta = (TextFileInputMeta) getStepMetaInterface();

    FileInputList textFileList = meta.getFileInputList( getTransMeta().getBowl(), getTransMeta() );

    if ( Objects.nonNull( textFileList ) && textFileList.nrOfFiles() <= 0 ) {
      response.put( "message", BaseMessages.getString( PKG, "TextFileInputDialog.NoValidFileFound.DialogMessage" ) );
    }

    return response;
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  public JSONObject showContentAction( Map<String, String> queryParams ) throws KettleException {
    JSONObject response = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    List<String> content = getFirst( Integer.valueOf( Objects.toString( queryParams.get( "nrlines" ), "0" ) ),
      Boolean.valueOf( Objects.toString( queryParams.get( "skipHeaders" ), "false" ) ) );
    content.forEach( jsonArray::add );
    response.put( "firstFileContent", jsonArray );
    return response;
  }

  @SuppressWarnings( { "java:S2093", "java:S3776" } )
  // Logic to Get the first x lines is moved from TextFileInputDialog to step class
  public List<String> getFirst( int nrlines, boolean skipHeaders ) throws KettleException {
    TextFileInputMeta meta = (TextFileInputMeta) getStepMetaInterface();

    FileInputList textFileList = meta.getFileInputList( getTransMeta().getBowl(), getTransMeta() );

    InputStream fi;
    CompressionInputStream f = null;
    StringBuilder lineStringBuilder = new StringBuilder( 256 );
    int fileFormatType = meta.getFileFormatTypeNr();

    List<String> retval = new ArrayList<>();

    if ( textFileList.nrOfFiles() > 0 ) {
      FileObject file = textFileList.getFile( 0 );
      try {
        fi = KettleVFS.getInputStream( file );

        CompressionProvider provider =
          CompressionProviderFactory.getInstance().createCompressionProviderInstance( meta.content.fileCompression );
        f = provider.createInputStream( fi );

        BufferedInputStreamReader reader;
        if ( meta.getEncoding() != null && meta.getEncoding().length() > 0 ) {
          reader = new BufferedInputStreamReader( new InputStreamReader( f, meta.getEncoding() ) );
        } else {
          reader = new BufferedInputStreamReader( new InputStreamReader( f ) );
        }
        EncodingType encodingType = EncodingType.guessEncodingType( reader.getEncoding() );

        int linenr = 0;

        int maxnr = nrlines + ( meta.content.header ? meta.content.nrHeaderLines : 0 );

        if ( skipHeaders ) {
          // Skip the header lines first if more then one, it helps us position
          if ( meta.content.layoutPaged && meta.content.nrLinesDocHeader > 0 ) {
            TextFileInputUtils.skipLines( log, reader, encodingType, fileFormatType,
              lineStringBuilder, meta.content.nrLinesDocHeader - 1, meta.getEnclosure(), meta.getEscapeCharacter(), 0 );
          }

          // Skip the header lines first if more then one, it helps us position
          if ( meta.content.header && meta.content.nrHeaderLines > 0 ) {
            TextFileInputUtils.skipLines( log, reader, encodingType, fileFormatType,
              lineStringBuilder, meta.content.nrHeaderLines - 1, meta.getEnclosure(), meta.getEscapeCharacter(), 0 );
          }
        }

        String line =
          TextFileInputUtils.getLine( log, reader, encodingType, fileFormatType, lineStringBuilder, meta.getEnclosure(),
            meta.getEscapeCharacter() );
        while ( line != null && ( linenr < maxnr || nrlines == 0 ) ) {
          retval.add( line );
          linenr++;
          line = TextFileInputUtils.getLine( log, reader, encodingType, fileFormatType, lineStringBuilder,
            meta.getEnclosure(), meta.getEscapeCharacter() );
        }
      } catch ( Exception e ) {
        throw new KettleException( BaseMessages.getString( PKG, "TextFileInputDialog.Exception.ErrorGettingFirstLines",
          "" + nrlines, file.getName().getURI() ), e );
      } finally {
        try {
          if ( f != null ) {
            f.close();
          }
        } catch ( Exception e ) {
          // Ignore errors
        }
      }
    }

    return retval;
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
