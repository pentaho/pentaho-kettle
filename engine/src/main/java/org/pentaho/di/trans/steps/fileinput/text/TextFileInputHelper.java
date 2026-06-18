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
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.steps.common.CsvInputAwareHelper;
import org.pentaho.di.trans.steps.common.CsvInputAwareMeta;
import org.pentaho.di.trans.steps.file.BaseFileField;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

public class TextFileInputHelper extends BaseStepHelper implements CsvInputAwareHelper {

  private static final Class<?> PKG = TextFileInputHelper.class;

  private static final String MESSAGE_KEY = "message";
  private static final String FIELDS = "fields";
  private static final String GET_FIELDS = "getFields";
  private static final String GET_FIELD_NAMES = "getFieldNames";
  private static final String SHOW_FILES = "showFiles";
  private static final String VALIDATE_SHOW_CONTENT = "validateShowContent";
  private static final String SHOW_CONTENT = "showContent";
  private static final String SET_MINIMAL_WIDTH = "setMinimalWidth";
  private static final String STEP_NAME = "stepName";

  TextFileInputMeta meta;

  public TextFileInputHelper( TextFileInputMeta textFileInputMeta ) {
    this.meta = textFileInputMeta;
  }

  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      switch ( method ) {
        case GET_FIELDS:
          response = getFieldsAction( transMeta, queryParams );
          break;
        case GET_FIELD_NAMES:
          response = getFieldNamesAction( transMeta, queryParams );
          break;
        case SHOW_FILES:
          response = showFilesAction( transMeta, queryParams );
          break;
        case VALIDATE_SHOW_CONTENT:
          response = validateShowContentAction( transMeta, queryParams );
          break;
        case SHOW_CONTENT:
          response = showContentAction( transMeta, queryParams );
          break;
        case SET_MINIMAL_WIDTH:
          response = setMinimalWidthAction( transMeta, queryParams );
          break;
        default:
          response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
      }
    } catch ( Exception e ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  @SuppressWarnings( "unchecked" )
  public JSONObject showFilesAction( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );

    String stepName = queryParams.get( STEP_NAME );
    if ( StringUtils.isBlank( stepName ) ) {
      return response;
    }

    String filter = queryParams.get( "filter" );
    boolean regexMode = Boolean.parseBoolean( queryParams.get( "isRegex" ) );
    JSONArray filteredFiles = getFilteredFiles( transMeta, meta, filter, regexMode );

    if ( filteredFiles.isEmpty() ) {
      response.put( MESSAGE_KEY,
        BaseMessages.getString( PKG, "TextFileInputDialog.NoFilesFound.DialogMessage" ) );
    }

    response.put( "files", filteredFiles );
    return response;
  }

  private JSONArray getFilteredFiles( TransMeta transMeta, TextFileInputMeta tfii,
                                      String filter, boolean regexMode ) {
    JSONArray filteredFiles = new JSONArray();
    String[] files = tfii.getFilePaths( transMeta.getBowl(), transMeta );

    if ( files == null ) {
      return filteredFiles;
    }

    for ( String file : files ) {
      if ( shouldIncludeFile( file, filter, regexMode ) ) {
        filteredFiles.add( file );
      }
    }

    return filteredFiles;
  }

  private boolean shouldIncludeFile( String file, String filter, boolean regexMode ) {
    if ( regexMode ) {
      return file.matches( filter );
    }
    return StringUtils.isBlank( filter ) || StringUtils.containsIgnoreCase( file, filter );
  }

  @SuppressWarnings( "java:S1144" )
  public JSONObject getFieldsAction( TransMeta transMeta, Map<String, String> queryParams )
    throws KettleException, JsonProcessingException {
    JSONObject response = new JSONObject();
    JSONArray jsonArray = new JSONArray();

    if ( TextFileInputMeta.FILE_TYPE_CSV == meta.getFileTypeNr() ) {
      return populateMeta( transMeta, meta, queryParams );
    } else {
      List<String> rows = getFirst( meta, transMeta, 50, false );
      jsonArray.addAll( rows );
    }

    response.put( FIELDS, jsonArray );
    response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    return response;
  }

  @Override
  public LogChannelInterface logChannel() {
    return log;
  }

  // This method contains code extracted from fileinput/text/TextFileCSVImportProgressDialog class
  // to get Fields data and Fields summary statistics
  public JSONObject populateMeta( TransMeta transMeta, TextFileInputMeta meta, Map<String, String> queryParams )
    throws KettleException, JsonProcessingException {
    JSONObject response = new JSONObject();
    String isSampleSummary = queryParams.get( "isSampleSummary" );
    int samples = Integer.parseInt( Objects.toString( queryParams.get( "noOfFields" ), "0" ) );
    CsvInputAwareMeta csvInputAwareMeta =
      (CsvInputAwareMeta) transMeta.findStep( queryParams.get( STEP_NAME ) ).getStepMetaInterface();
    final InputStream inputStream = getInputStream( transMeta, csvInputAwareMeta );
    final BufferedInputStreamReader reader = getBufferedReader( transMeta, csvInputAwareMeta, inputStream );
    String[] fieldNames = getFieldNames( transMeta, csvInputAwareMeta );
    meta.setFields( fieldNames );

    TextFileCsvFileTypeImportProcessor processor =
      new TextFileCsvFileTypeImportProcessor( meta, transMeta, reader, samples,
        Boolean.parseBoolean( isSampleSummary ) );
    String summary = processor.analyzeFile( true );

    response.put( FIELDS, convertFieldsToJsonArray( processor.getInputFieldsDto() ) );
    response.put( "summary", summary );
    return response;
  }

  @SuppressWarnings( { "java:S1144", "java:S1905", "java:S1172" } ) // Using reflection this method is being invoked
  public JSONObject getFieldNamesAction( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    String[] fieldNames = getFieldNames( transMeta, meta );
    Collections.addAll( jsonArray, fieldNames );
    response.put( "fieldNames", jsonArray );
    return response;
  }

  @SuppressWarnings("java:S1172")
  public JSONObject validateShowContentAction( TransMeta transMeta, Map<String, String> queryParams )  {
    JSONObject response = new JSONObject();
    FileInputList textFileList = meta.getFileInputList( transMeta.getBowl(), transMeta );
    if ( textFileList != null && textFileList.nrOfFiles() <= 0 ) {
      response.put( MESSAGE_KEY,
        BaseMessages.getString( PKG, "TextFileInputDialog.NoValidFileFound.DialogMessage" ) );
    }
    response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    return response;
  }

  @SuppressWarnings( "unchecked" )
  public JSONObject showContentAction( TransMeta transMeta, Map<String, String> queryParams ) throws KettleException {
    JSONObject response = new JSONObject();
    JSONArray jsonArray = new JSONArray();

    int nrlines = Integer.parseInt( Objects.toString( queryParams.get( "nrlines" ), "0" ) );
    boolean skipHeaders = Boolean.parseBoolean( Objects.toString( queryParams.get( "skipHeaders" ), "false" ) );
    List<String> content = getFirst( meta, transMeta, nrlines, skipHeaders );
    content.forEach( jsonArray::add );

    response.put( "firstFileContent", jsonArray );
    response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    return response;
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  public JSONObject setMinimalWidthAction( TransMeta transMeta, Map<String, String> queryParams )
    throws JsonProcessingException {
    JSONObject jsonObject = new JSONObject();
    JSONArray textFileFields = new JSONArray();
    ObjectMapper objectMapper = new ObjectMapper();

    for ( TextFileInputFieldDTO dto : getUpdatedTextFields( meta ) ) {
      textFileFields.add( objectMapper.readTree( objectMapper.writeValueAsString( dto ) ) );

    }

    jsonObject.put( "updatedData", textFileFields );
    jsonObject.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    return jsonObject;
  }

  public TextFileInputFieldDTO[] getUpdatedTextFields( TextFileInputMeta tfii ) {
    List<TextFileInputFieldDTO> list = new ArrayList<>();
    for ( BaseFileField textFileField : tfii.getInputFields() ) {
      TextFileInputFieldDTO updated = new TextFileInputFieldDTO();
      updated.setName( textFileField.getName() );
      updated.setType( textFileField.getTypeDesc() );

      switch ( textFileField.getType() ) {
        case ValueMetaInterface.TYPE_STRING:
          updated.setFormat( StringUtils.EMPTY );
          break;
        case ValueMetaInterface.TYPE_INTEGER:
          updated.setFormat( "0" );
          break;
        case ValueMetaInterface.TYPE_NUMBER:
          updated.setFormat( "0.#####" );
          break;
        default:
          break;
      }
      updated.setLength( StringUtils.EMPTY );
      updated.setPrecision( StringUtils.EMPTY );
      updated.setCurrency( textFileField.getCurrencySymbol() );
      updated.setDecimal( textFileField.getDecimalSymbol() );
      updated.setGroup( textFileField.getGroupSymbol() );
      updated.setTrimType( "both" );
      updated.setNullif( textFileField.getNullString() );
      updated.setIfnull( textFileField.getIfNullValue() );
      int position = textFileField.getPosition();
      updated.setPosition( position == -1 ? StringUtils.EMPTY : String.valueOf( position ) );
      updated.setRepeat( textFileField.isRepeated() ? "Y" : "N" );

      list.add( updated );
    }
    return list.toArray( new TextFileInputFieldDTO[ 0 ] );
  }

  @Override
  public InputStream getInputStream( TransMeta transMeta, final CsvInputAwareMeta meta ) {
    InputStream fileInputStream = null;
    CompressionInputStream inputStream = null;
    try {
      FileObject fileObject = meta.getHeaderFileObject( transMeta );
      fileInputStream = KettleVFS.getInputStream( fileObject );
      CompressionProvider provider = CompressionProviderFactory.getInstance().createCompressionProviderInstance(
        ( (TextFileInputMeta) meta ).content.fileCompression );
      inputStream = provider.createInputStream( fileInputStream );
    } catch ( final Exception e ) {
      logError( BaseMessages.getString( "FileInputDialog.ErrorGettingFileDesc.DialogMessage" ), e );
    }
    return inputStream;
  }

  // Logic to Get the first x lines is moved from TextFileInputDialog to step class
  public List<String> getFirst( TextFileInputMeta meta, TransMeta transMeta, int nrlines, boolean skipHeaders )
    throws KettleException {
    FileInputList textFileList = meta.getFileInputList( transMeta.getBowl(), transMeta );
    List<String> retval = new ArrayList<>();

    if ( textFileList == null || textFileList.nrOfFiles() == 0 ) {
      return retval;
    }

    FileObject file = textFileList.getFile( 0 );
    CompressionInputStream f = null;
    StringBuilder lineStringBuilder = new StringBuilder( 256 );
    try ( InputStream fi = KettleVFS.getInputStream( file ) ) {
      CompressionProvider provider = CompressionProviderFactory.getInstance()
        .createCompressionProviderInstance( meta.content.fileCompression );
      f = provider.createInputStream( fi );
      try (BufferedInputStreamReader reader =
             (meta.getEncoding() != null && !meta.getEncoding().isEmpty())
               ? new BufferedInputStreamReader(new InputStreamReader(f, meta.getEncoding()))
               : new BufferedInputStreamReader(new InputStreamReader(f))) {

        EncodingType encodingType = EncodingType.guessEncodingType(reader.getEncoding());
        int maxnr = nrlines + (meta.content.header ? meta.content.nrHeaderLines : 0);
        if (skipHeaders) {
          skipHeaderLines(meta, reader, encodingType, lineStringBuilder);
        }
        readFileLines(meta, retval, reader, encodingType, nrlines, maxnr, lineStringBuilder);
      }
    } catch ( Exception e ) {
      throw new KettleException(
        BaseMessages.getString( PKG, "TextFileInputDialog.Exception.ErrorGettingFirstLines", "" + nrlines,
          file.getName().getURI() ), e );
    } finally {
      try {
        if ( f != null ) {
          f.close();
        }
      } catch ( Exception e ) {
        // Ignore errors
      }
    }

    return retval;
  }

  /**
   * Skips the required header lines if present.
   */
  private void skipHeaderLines( TextFileInputMeta meta, BufferedInputStreamReader reader,
                                EncodingType encodingType, StringBuilder lineStringBuilder ) throws KettleException {
    // Skip the header lines first if more then one, it helps us position
    if ( meta.content.layoutPaged && meta.content.nrLinesDocHeader > 0 ) {
      TextFileInputUtils.skipLines( logChannel(), reader, encodingType, meta.getFileFormatTypeNr(),
        lineStringBuilder,
        meta.content.nrLinesDocHeader - 1, meta.getEnclosure(), meta.getEscapeCharacter(), 0 );
    }
    // Skip the header lines first if more then one, it helps us position
    if ( meta.content.header && meta.content.nrHeaderLines > 0 ) {
      TextFileInputUtils.skipLines( logChannel(), reader, encodingType, meta.getFileFormatTypeNr(),
        lineStringBuilder,
        meta.content.nrHeaderLines - 1, meta.getEnclosure(), meta.getEscapeCharacter(), 0 );
    }
  }

  /**
   * Reads lines from the file until the desired number or end of file.
   */
  private void readFileLines( TextFileInputMeta meta, List<String> retval,
                              BufferedInputStreamReader reader, EncodingType encodingType,
                              int nrlines, int maxnr, StringBuilder lineStringBuilder ) throws KettleException {
    String line =
      TextFileInputUtils.getLine( logChannel(), reader, encodingType, meta.getFileFormatTypeNr(), lineStringBuilder,
        meta.getEnclosure(), meta.getEscapeCharacter() );
    int linenr = 0;
    while ( line != null && ( linenr < maxnr || nrlines == 0 ) ) {
      retval.add( line );
      linenr++;
      line =
        TextFileInputUtils.getLine( logChannel(), reader, encodingType, meta.getFileFormatTypeNr(), lineStringBuilder,
          meta.getEnclosure(), meta.getEscapeCharacter() );
    }
  }

  @SuppressWarnings({ "java:S1149" })
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

  @Override
  public String massageFieldName( final String fieldName ) {
    // Replace all spaces and hyphens (-) with underscores (_)
    String massagedFieldName = fieldName;
    massagedFieldName = Const.replace( massagedFieldName, " ", "_" );
    massagedFieldName = Const.replace( massagedFieldName, "-", "_" );
    return massagedFieldName;
  }
}
