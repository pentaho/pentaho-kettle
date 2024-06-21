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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.compress.CompressionInputStream;
import org.pentaho.di.core.compress.CompressionProvider;
import org.pentaho.di.core.compress.CompressionProviderFactory;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.playlist.FilePlayListAll;
import org.pentaho.di.core.playlist.FilePlayListReplay;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
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

  @Override
  public JSONObject doAction( String fieldName, StepMetaInterface stepMetaInterface, TransMeta transMeta,
                              Trans trans, Map<String, String> queryParamToValues ) {
    JSONObject response = new JSONObject();
    try {
      Method actionMethod = TextFileInput.class.getDeclaredMethod( fieldName + "Action", Map.class );
      this.setStepMetaInterface( stepMetaInterface );
      response = (JSONObject) actionMethod.invoke( this, queryParamToValues );
      response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    } catch ( NoSuchMethodException | InvocationTargetException | IllegalAccessException e ) {
      log.logError( e.getMessage() );
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_METHOD_NOT_RESPONSE );
    }
    return response;
  }

  private JSONObject getEncodingsAction( Map<String, String> queryParamToValues ) {
    JSONObject response = new JSONObject();
    try {
      JSONArray encodings = new JSONArray();
      Charset.availableCharsets().values().forEach( charset -> {
        encodings.add( charset.displayName() );
      });
      response.put( "encodings", encodings );
    } catch ( Exception e ) {
      log.logError( e.getMessage() );
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
    }
    return response;
  }

  private JSONObject getAvailableLocalesAction( Map<String, String> queryParamToValues ) {
    JSONObject response = new JSONObject();
    try {
      Locale[] locale = Locale.getAvailableLocales();
      JSONArray dateLocales = new JSONArray();
      for ( int i = 0; i < locale.length; i++ ) {
        dateLocales.add( locale[i].toString() );
      }
      response.put( "availableLocales", dateLocales );
    } catch ( Exception e ) {
      log.logError( e.getMessage() );
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
    }
    return response;
  }

  private JSONObject getDefaultLocaleAction( Map<String, String> queryParamToValues ) {
    JSONObject response = new JSONObject();
    try {
      response.put( "defaultLocale", Locale.getDefault().toString() );
    } catch ( Exception e ) {
      log.logError( e.getMessage() );
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
    }
    return response;
  }

  private JSONObject showFilesAction( Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();

    String filter = queryParams.get( "filter" );
    String isRegex = queryParams.get( "isRegex" );

    TextFileInputMeta tfii = (TextFileInputMeta) getStepMetaInterface();
    String[] files =
            FileInputList.createFilePathList( getTransMeta().getBowl(), getTransMeta(), tfii.inputFiles.fileName,
                    tfii.inputFiles.fileMask, tfii.inputFiles.excludeFileMask, tfii.inputFiles.fileRequired,
                    tfii.inputFiles.includeSubFolderBoolean() );

    JSONArray filteredFiles = new JSONArray();
    for ( String file : files ) {
      if ( Boolean.TRUE.equals( isRegex ) ) {
        Matcher matcher = Pattern.compile( filter ).matcher( file );
        if ( matcher.matches() ) {
          filteredFiles.add( file );
        }
      } else if ( StringUtils.isBlank( filter ) || StringUtils.contains( file.toUpperCase(), filter.toUpperCase() ) ) {
        filteredFiles.add( file );
      }
    }
    try {
      response.put( "files", filteredFiles );
    } catch ( Exception e ) {
      log.logError( e.getMessage() );
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
    }
    return response;
  }

  public JSONObject showContentAction( Map<String, String> queryParams ) throws KettleException {
    JSONObject response = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    List<String> content = getFirst( Integer.valueOf( Objects.toString( queryParams.get( "nrlines" ), "0" ) ),
            Boolean.valueOf( Objects.toString( queryParams.get( "skipHeaders" ) , "false" ) ) );
    content.forEach( str -> {
      jsonArray.add( str );
    } );
    response.put ( "firstFileContent", jsonArray );
    return response;
  }

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
                    lineStringBuilder,  meta.content.nrLinesDocHeader - 1, meta.getEnclosure(), meta.getEscapeCharacter(), 0 );
          }

          // Skip the header lines first if more then one, it helps us position
          if ( meta.content.header && meta.content.nrHeaderLines > 0 ) {
            TextFileInputUtils.skipLines( log, reader, encodingType, fileFormatType,
                    lineStringBuilder,  meta.content.nrHeaderLines - 1, meta.getEnclosure(), meta.getEscapeCharacter(), 0 );
          }
        }

        String line = TextFileInputUtils.getLine( log, reader, encodingType, fileFormatType, lineStringBuilder, meta.getEnclosure(), meta.getEscapeCharacter() );
        while ( line != null && ( linenr < maxnr || nrlines == 0 ) ) {
          retval.add( line );
          linenr++;
          line = TextFileInputUtils.getLine( log, reader, encodingType, fileFormatType, lineStringBuilder, meta.getEnclosure(), meta.getEscapeCharacter() );
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
}
