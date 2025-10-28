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
import org.pentaho.di.core.compress.CompressionInputStream;
import org.pentaho.di.core.compress.CompressionProvider;
import org.pentaho.di.core.compress.CompressionProviderFactory;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.file.BaseFileField;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Helper class to handle UI/actions for TextFileInput step (moved from step implementation).
 */
public class TextFileInputHelper extends BaseStepHelper {
  private static final Class<?> PKG = TextFileInputMeta.class;
  private static final String MESSAGE_KEY = "message";
  private static final String FIELDS = "fields";
  private static final String GET_FIELDS = "getFields";
  private static final String GET_FIELD_NAMES = "getFieldNames";
  private static final String SHOW_FILES = "showFiles";
  private static final String VALIDATE_SHOW_CONTENT = "validateShowContent";
  private static final String SHOW_CONTENT = "showContent";
  private static final String SET_MINIMAL_WIDTH = "setMinimalWidth";
  private static final String STEP_NAME = "stepName";

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

    StepMeta stepMeta = transMeta.findStep( stepName );
    if ( stepMeta == null || !( stepMeta.getStepMetaInterface() instanceof TextFileInputMeta tfii ) ) {
      return response;
    }

    String filter = queryParams.get( "filter" );
    boolean regexMode = Boolean.parseBoolean( queryParams.get( "isRegex" ) );
    JSONArray filteredFiles = getFilteredFiles( transMeta, tfii, filter, regexMode );

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


  @SuppressWarnings( "unchecked" )
  public JSONObject getFieldsAction( TransMeta transMeta, Map<String, String> queryParams )
    throws KettleException {
    JSONObject response = new JSONObject();
    JSONArray jsonArray = new JSONArray();

    String stepName = queryParams.get( STEP_NAME );
    if ( stepName == null || stepName.isEmpty() ) {
      response.put( FIELDS, jsonArray );
      return response;
    }

    StepMeta stepMeta = transMeta.findStep( stepName );
    if ( stepMeta == null || !( stepMeta.getStepMetaInterface() instanceof TextFileInputMeta tfii ) ) {
      response.put( FIELDS, jsonArray );
      return response;
    }

    if ( TextFileInputMeta.FILE_TYPE_CSV == tfii.getFileTypeNr() ) {
      return populateMeta( transMeta, tfii, queryParams );
    } else {
      List<String> rows = getFirst( tfii, transMeta, 50, false );
      jsonArray.addAll( rows );
    }

    response.put( FIELDS, jsonArray );
    response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    return response;
  }

  public LogChannelInterface logChannel() {
    return getLogChannel();
  }

  // This method contains code extracted from fileinput/text/TextFileCSVImportProgressDialog class
  // to get Fields data and Fields summary statistics
  private JSONObject populateMeta( TransMeta transMeta, TextFileInputMeta meta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      String isSampleSummary = queryParams.get( "isSampleSummary" );
      int samples = Integer.parseInt( Objects.toString( queryParams.get( "noOfFields" ), "0" ) );

      final FileObject headerFile = meta.getHeaderFileObject( transMeta );
      final InputStream fileInputStream = headerFile == null ? null : KettleVFS.getInputStream( headerFile );

      if ( fileInputStream == null ) {
        throw new KettleFileException( "Header file input stream is null" );
      }

      CompressionProvider provider = CompressionProviderFactory.getInstance()
        .createCompressionProviderInstance( meta.content.fileCompression );

      try ( InputStream fis = fileInputStream;
            CompressionInputStream f = provider.createInputStream( fis );
            BufferedInputStreamReader reader = meta.getEncoding() != null && meta.getEncoding().length() > 0
              ? new BufferedInputStreamReader( new InputStreamReader( f, meta.getEncoding() ) )
              : new BufferedInputStreamReader( new InputStreamReader( f ) ) ) {

        TextFileCsvFileTypeImportProcessor processor =
          new TextFileCsvFileTypeImportProcessor( meta, transMeta, reader, samples,
            Boolean.parseBoolean( isSampleSummary ) );
        String summary = processor.analyzeFile( true );

  response.put( FIELDS, convertFieldsToJsonArray( processor.getInputFieldsDto() ) );
        response.put( "summary", summary );
        response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
        return response;
      }
    } catch ( Exception e ) {
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
      response.put( MESSAGE_KEY, e.getMessage() );
      return response;
    }
  }

  public LogChannelInterface getLogChannel() {
    return log;
  }

  @SuppressWarnings( "unchecked" )
  public JSONObject getFieldNamesAction( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    response.put( "fieldNames", jsonArray );
    response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );

    String stepName = queryParams.get( STEP_NAME );
    if ( StringUtils.isBlank( stepName ) ) {
      return response;
    }

    StepMeta stepMeta = transMeta.findStep( stepName );
    if ( stepMeta == null || !( stepMeta.getStepMetaInterface() instanceof TextFileInputMeta tfii ) ) {
      return response;
    }

    FileObject headerFile = tfii.getHeaderFileObject( transMeta );
    if ( headerFile == null ) {
      return response;
    }

    try ( InputStream fi = KettleVFS.getInputStream( headerFile );
          CompressionInputStream f = createCompressionStream( tfii, fi );
          BufferedInputStreamReader reader = createReader( tfii, f ) ) {

      String line = readFirstLine( transMeta, tfii, reader );
      String[] fieldNames = parseFieldNames( transMeta, tfii, line );

      if ( fieldNames == null || fieldNames.length == 0 ) {
        response.put( MESSAGE_KEY, BaseMessages.getString( PKG, "Dialog.ErrorGettingFields.Message" ) );
      } else {
        Collections.addAll( jsonArray, fieldNames );
      }

    } catch ( Exception e ) {
      response.put( MESSAGE_KEY, BaseMessages.getString( PKG, "Dialog.ErrorGettingFields.Message" ) );
    }

    return response;
  }

  private CompressionInputStream createCompressionStream( TextFileInputMeta tfii, InputStream fi )
 throws IOException {
    return CompressionProviderFactory.getInstance()
      .createCompressionProviderInstance( tfii.content.fileCompression )
      .createInputStream( fi );
  }

  private BufferedInputStreamReader createReader( TextFileInputMeta tfii, CompressionInputStream f ) throws
    UnsupportedEncodingException {
    return ( tfii.getEncoding() != null && !tfii.getEncoding().isEmpty() )
      ? new BufferedInputStreamReader( new InputStreamReader( f, tfii.getEncoding() ) )
      : new BufferedInputStreamReader( new InputStreamReader( f ) );
  }

  private String readFirstLine( TransMeta transMeta, TextFileInputMeta tfii, BufferedInputStreamReader reader )
    throws KettleFileException {
    String enclosure = transMeta.environmentSubstitute( tfii.getEnclosure() );
    String escape = transMeta.environmentSubstitute( tfii.getEscapeCharacter() );

    return TextFileInputUtils.getLine(
      logChannel(),
      reader,
      EncodingType.guessEncodingType( reader.getEncoding() ),
      tfii.getFileFormatTypeNr(),
      new StringBuilder( 1000 ),
      enclosure,
      escape
    );
  }

  private String[] parseFieldNames( TransMeta transMeta, TextFileInputMeta tfii, String line ) throws KettleException {
    if ( StringUtils.isBlank( line ) ) {
      return new String[] {};
    }
    return TextFileInputUtils.guessStringsFromLine(
      transMeta.getParentVariableSpace(),
      logChannel(),
      line,
      tfii,
      transMeta.environmentSubstitute( tfii.getDelimiter() ),
      transMeta.environmentSubstitute( tfii.getEnclosure() ),
      tfii.getEscapeCharacter()
    );
  }


  public JSONObject validateShowContentAction( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    String stepName = queryParams.get( STEP_NAME );
    if ( stepName != null && !stepName.isEmpty() ) {
      StepMeta stepMeta = transMeta.findStep( stepName );
      if ( stepMeta != null && stepMeta.getStepMetaInterface() instanceof TextFileInputMeta tfii ) {
        FileInputList textFileList = tfii.getFileInputList( transMeta.getBowl(), transMeta );
        if ( textFileList != null && textFileList.nrOfFiles() <= 0 ) {
          response.put( MESSAGE_KEY,
            BaseMessages.getString( PKG, "TextFileInputDialog.NoValidFileFound.DialogMessage" ) );
        }
      }
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
    String stepName = queryParams.get( STEP_NAME );

    if ( stepName != null && !stepName.isEmpty() ) {
      StepMeta stepMeta = transMeta.findStep( stepName );
      if ( stepMeta != null && stepMeta.getStepMetaInterface() instanceof TextFileInputMeta tfii ) {
        List<String> content = getFirst( tfii, transMeta, nrlines, skipHeaders );
        content.forEach( jsonArray::add );
      }
    }

    response.put( "firstFileContent", jsonArray );
    response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    return response;
  }

  @SuppressWarnings( "unchecked" )
  public JSONObject setMinimalWidthAction( TransMeta transMeta, Map<String, String> queryParams )
    throws JsonProcessingException {
    JSONObject jsonObject = new JSONObject();
    JSONArray textFileFields = new JSONArray();
    ObjectMapper objectMapper = new ObjectMapper();

    String stepName = queryParams.get( STEP_NAME );
    if ( stepName != null && !stepName.isEmpty() ) {
      StepMeta stepMeta = transMeta.findStep( stepName );
      if ( stepMeta != null && stepMeta.getStepMetaInterface() instanceof TextFileInputMeta tfii ) {
        for ( TextFileInputFieldDTO dto : getUpdatedTextFields( tfii ) ) {
          textFileFields.add( objectMapper.readTree( objectMapper.writeValueAsString( dto ) ) );
        }
      }
    }

    jsonObject.put( "updatedData", textFileFields );
    jsonObject.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    return jsonObject;
  }

  private TextFileInputFieldDTO[] getUpdatedTextFields( TextFileInputMeta tfii ) {
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

  private JSONArray convertFieldsToJsonArray( TextFileInputFieldDTO[] textFileInputFields )
    throws JsonProcessingException {
    JSONArray jsonArray = new JSONArray();
    ObjectMapper objectMapper = new ObjectMapper();
    for ( TextFileInputFieldDTO field : textFileInputFields ) {
      jsonArray.add( objectMapper.readTree( objectMapper.writeValueAsString( field ) ) );
    }
    return jsonArray;
  }

  @SuppressWarnings( { "java:S2093", "java:S3776" } )
  // Logic to Get the first x lines is moved from TextFileInputDialog to step class
  private List<String> getFirst( TextFileInputMeta meta, TransMeta transMeta, int nrlines, boolean skipHeaders )
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

      String line =
        TextFileInputUtils.getLine( logChannel(), reader, encodingType, meta.getFileFormatTypeNr(), lineStringBuilder,
          meta.getEnclosure(), meta.getEscapeCharacter() );
      while ( line != null && ( linenr < maxnr || nrlines == 0 ) ) {
        retval.add( line );
        linenr++;
        line =
          TextFileInputUtils.getLine( logChannel(), reader, encodingType, meta.getFileFormatTypeNr(), lineStringBuilder,
            meta.getEnclosure(), meta.getEscapeCharacter() );
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
}
