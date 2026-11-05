/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.amazon.s3.fileoutput;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.amazon.s3.S3Details;
import org.pentaho.amazon.s3.S3Util;
import org.pentaho.amazon.s3.provider.S3Provider;
import org.pentaho.di.connections.vfs.VFSRoot;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputFieldDTO;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
import org.pentaho.s3.vfs.S3FileName;
import org.pentaho.s3.vfs.S3FileProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class S3FileOutputHelper extends BaseStepHelper {
  private static final Class<?> PKG = S3FileOutputMeta.class;
  private static final String STEP_NAME = "stepName";
  private static final String ERROR = "error";
  private static final String SET_MINIMAL_WIDTH = "setMinimalWidth";
  private static final String SHOW_FILES = "showFiles";
  private static final String GET_EOL_FORMATS = "getFormats";
  private static final String LIST_S3_BUCKETS = "listS3Buckets";
  private static final String LIST_S3_CONTENTS = "listS3Contents";
  private static final String FILES_KEY = "files";
  private static final String FORMATS_KEY = "formats";
  private static final String UPDATED_DATA_KEY = "updatedData";
  private static final String BUCKETS_KEY = "buckets";
  private static final String CONTENTS_KEY = "contents";
  private static final String NAME_KEY = "name";
  private static final String TYPE_KEY = "type";
  private static final String PATH_KEY = "path";
  private static final String SIZE_KEY = "size";
  private static final String LAST_MODIFIED_KEY = "lastModified";
  private static final String CREATION_DATE_KEY = "creationDate";
  private static final String TYPE_BUCKET = "bucket";
  private static final String TYPE_FOLDER = "folder";
  private static final String TYPE_FILE = "file";


  private final S3Provider s3Provider;

  public S3FileOutputHelper() {
    super();
    this.s3Provider = new S3Provider();
  }

  @SuppressWarnings( "unchecked" )
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      switch ( method ) {
        case SET_MINIMAL_WIDTH:
          response = setMinimalWidthAction( transMeta, queryParams );
          break;
        case SHOW_FILES:
          response = showFilesAction( transMeta, queryParams );
          break;
        case GET_EOL_FORMATS:
          response = getEOLFormatsAction();
          break;
        case LIST_S3_BUCKETS:
          response = listS3BucketsAction( transMeta, queryParams );
          break;
        case LIST_S3_CONTENTS:
          response = listS3ContentsAction( transMeta, queryParams );
          break;
        default:
          response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
          break;
      }
    } catch ( Exception ex ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
      response.put( ERROR, ex.getMessage() );
    }
    return response;
  }

  // Called from Fields.tsx - Updates field formats to minimal width settings
  @SuppressWarnings( "unchecked" )
  public JSONObject setMinimalWidthAction( TransMeta transMeta, Map<String, String> queryParams )
    throws JsonProcessingException {
    JSONObject jsonObject = new JSONObject();
    JSONArray textFileFields = new JSONArray();
    ObjectMapper objectMapper = new ObjectMapper();

    String stepName = queryParams.get( STEP_NAME );
    for ( TextFileOutputFieldDTO textFileOutputFieldDTO : getUpdatedTextFields( transMeta, stepName ) ) {
      textFileFields.add( objectMapper.readTree( objectMapper.writeValueAsString( textFileOutputFieldDTO ) ) );
    }
    jsonObject.put( UPDATED_DATA_KEY, textFileFields );
    return jsonObject;
  }

  // Retrieves and updates text field configurations - TYPE_STRING: empty, TYPE_INTEGER: "0", TYPE_NUMBER: "0.#####"
  private List<TextFileOutputFieldDTO> getUpdatedTextFields( TransMeta transMeta, String stepName ) {
    List<TextFileOutputFieldDTO> textFileFields = new ArrayList<>();
    if ( !S3Util.isEmpty( stepName ) ) {
      StepMeta stepMeta = transMeta.findStep( stepName );
      if ( stepMeta != null && stepMeta.getStepMetaInterface() instanceof S3FileOutputMeta s3FileOutputMeta ) {
        for ( TextFileField textFileField : s3FileOutputMeta.getOutputFields() ) {
          TextFileOutputFieldDTO updatedTextFileField = new TextFileOutputFieldDTO();
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
              updatedTextFileField.setFormat( StringUtils.EMPTY );
              break;
          }
          updatedTextFileField.setLength( StringUtils.EMPTY );
          updatedTextFileField.setPrecision( StringUtils.EMPTY );
          updatedTextFileField.setCurrency( textFileField.getCurrencySymbol() );
          updatedTextFileField.setDecimal( textFileField.getDecimalSymbol() );
          updatedTextFileField.setGroup( textFileField.getGroupingSymbol() );
          updatedTextFileField.setTrimType( "both" );
          updatedTextFileField.setNullif( textFileField.getNullString() );
          textFileFields.add( updatedTextFileField );
        }
      }
    }
    return textFileFields;
  }

  // Called from Content.tsx - Returns line terminator format options (DOS, Unix, CR, None)
  @SuppressWarnings( "unchecked" )
public JSONObject getEOLFormatsAction() {
    JSONObject response = new JSONObject();
    JSONArray array = new JSONArray();
    for ( String format : TextFileOutputMeta.formatMapperLineTerminator ) {
      array.add( BaseMessages.getString( PKG, "S3FileOutputDialog.Format." + format ) );
    }
    response.put( FORMATS_KEY, array );
    return response;
  }

  // Called from File.tsx - Returns filtered list of output files (supports text search or regex)
  @SuppressWarnings( "unchecked" )
  public JSONObject showFilesAction( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    String stepName = queryParams.get( STEP_NAME );
    String filter = queryParams.get( "filter" );
    String isRegex = queryParams.get( "isRegex" );
    JSONArray filteredFiles = new JSONArray();

    if ( S3Util.isEmpty( stepName ) ) {
      response.put( FILES_KEY, filteredFiles );
      return response;
    }
    StepMeta stepMeta = transMeta.findStep( stepName );
    if ( stepMeta == null || !( stepMeta.getStepMetaInterface() instanceof S3FileOutputMeta s3FileOutputMeta ) ) {
      response.put( FILES_KEY, filteredFiles );
      return response;
    }
    for ( String file : s3FileOutputMeta.getFiles( transMeta ) ) {
      if ( fileMatchesFilter( file, filter, isRegex ) ) {
        filteredFiles.add( file );
      }
    }
    response.put( FILES_KEY, filteredFiles );
    return response;
  }

  // Checks if file path matches filter criteria (regex or case-insensitive substring)
  private boolean fileMatchesFilter( String file, String filter, String isRegex ) {
    if ( Boolean.parseBoolean( isRegex ) ) {
      if ( S3Util.isEmpty( filter ) ) {
        return true;
      }
      try {
        Matcher matcher = Pattern.compile( filter ).matcher( file );
        return matcher.matches();
      } catch ( PatternSyntaxException e ) {
        throw new IllegalArgumentException(
            BaseMessages.getString(
                PKG,
                "S3FileOutput.Error.InvalidRegex"
            ) + ": " + e.getDescription(),
            e
        );
      }
    } else {
      return S3Util.isEmpty( filter ) || Strings.CI.contains( file, filter );
    }
  }

  @SuppressWarnings( "unchecked" )
  public JSONObject listS3BucketsAction( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    JSONArray buckets = new JSONArray();
    try {
      S3Details s3Details = createS3DetailsFromParams( transMeta, queryParams );
      s3Details.setSpace( new Variables() );
      List<VFSRoot> bucketsTemp = s3Provider.getLocations( s3Details );
      String s3Prefix = S3FileProvider.SCHEME + "://";
      for ( VFSRoot bucket : bucketsTemp ) {
        JSONObject item = new JSONObject();
        item.put( NAME_KEY, bucket.getName() );
        item.put( TYPE_KEY, TYPE_BUCKET );
        item.put( PATH_KEY, s3Prefix + bucket.getName() + S3FileName.DELIMITER );
        if ( bucket.getModifiedDate() != null ) {
          item.put( CREATION_DATE_KEY, bucket.getModifiedDate().toString() );
        }
        buckets.add( item );
      }
      response.put( BUCKETS_KEY, buckets );
      response.put( ACTION_STATUS, SUCCESS_RESPONSE );
    } catch ( Exception e ) {
      response.put(
          ERROR,
          BaseMessages.getString( PKG, "S3FileOutput.Error.ListBucketsFailed" )
      );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }
  @SuppressWarnings( "unchecked" )
  public JSONObject listS3ContentsAction( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    JSONArray contents = new JSONArray();
    try {
      S3FileOutputMeta meta = (S3FileOutputMeta) transMeta
          .findStep( queryParams.get( STEP_NAME ) )
          .getStepMetaInterface();
      String path = meta.getFileName();
      String s3Prefix = S3FileProvider.SCHEME + "://";
      if ( path == null || !path.startsWith( s3Prefix ) ) {
        response.put( ERROR,
            BaseMessages.getString( PKG, "S3FileOutput.Error.InvalidS3Path" ) );
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        return response;
      }
      // Process and fill contents
      processS3Path( transMeta, queryParams, path, contents );
      response.put( CONTENTS_KEY, contents );
      response.put( ACTION_STATUS, SUCCESS_RESPONSE );
    } catch ( Exception e ) {
      response.put( ERROR,
          BaseMessages.getString( PKG, "S3FileOutput.Error.ListObjectsFailed" ) );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  public void processS3Path( TransMeta transMeta,
                             Map<String, String> queryParams,
                             String path,
                             JSONArray contents ) throws KettleException, FileSystemException {
    String s3Prefix = S3FileProvider.SCHEME + "://";
    S3Details s3Details = createS3DetailsFromParams( transMeta, queryParams );
    s3Details.setSpace( new Variables() );
    s3Details = s3Provider.prepare( s3Details );
    String cleanPath = Strings.CS.removeEnd( path.replace( s3Prefix, "" ), S3FileName.DELIMITER );
    FileObject folder = s3Provider.getDirectFile( transMeta.getBowl(), s3Details, cleanPath );
    if ( folder == null ) {
      throw new IllegalStateException(
          BaseMessages.getString( PKG, "S3FileOutput.Error.S3ClientInitFailed" )
      );
    }
    if ( folder.getType().hasChildren() ) {
      addFilesAndFolders( folder.getChildren(), s3Prefix + cleanPath + S3FileName.DELIMITER, contents );
    }
  }

  @SuppressWarnings( "unchecked" )
  public void addFilesAndFolders( FileObject[] children, String parentPath, JSONArray contents )
    throws FileSystemException {
    for ( FileObject child : children ) {
      if ( child.isFolder() ) {
        contents.add( toFolderItem( child, parentPath ) );
      }
    }
    for ( FileObject child : children ) {
      if ( child.isFile() ) {
        contents.add( toFileItem( child, parentPath ) );
      }
    }
  }

  @SuppressWarnings( "unchecked" )
  private JSONObject toFolderItem( FileObject folder, String parentPath ) {
    String name = folder.getName().getBaseName();
    JSONObject item = new JSONObject();
    item.put( NAME_KEY, name );
    item.put( TYPE_KEY, TYPE_FOLDER );
    item.put( PATH_KEY, parentPath + name + S3FileName.DELIMITER );
    return item;
  }

  @SuppressWarnings( "unchecked" )
  private JSONObject toFileItem( FileObject file, String parentPath ) throws FileSystemException {
    String name = file.getName().getBaseName();
    FileContent content = file.getContent();
    JSONObject item = new JSONObject();
    item.put( NAME_KEY, name );
    item.put( TYPE_KEY, TYPE_FILE );
    item.put( PATH_KEY, parentPath + name );
    item.put( SIZE_KEY, content.getSize() );
    long lastModified = content.getLastModifiedTime();
    if ( lastModified > 0 ) {
      item.put( LAST_MODIFIED_KEY, new Date( lastModified ).toString() );
    }
    return item;
  }

  /**
   * Creates S3Details object from step metadata and query parameters.
   * Auto-detects auth type: if accessKey/secretKey exist in meta -> access key auth,
   * otherwise -> credentials file auth.
   */
  public S3Details createS3DetailsFromParams( TransMeta transMeta, Map<String, String> queryParams ) {
    S3Details s3Details = new S3Details();
    S3FileOutputMeta s3FileOutputMeta = (S3FileOutputMeta) transMeta
        .findStep( queryParams.get( STEP_NAME ) )
        .getStepMetaInterface();
    // Read auth fields from meta
    String accessKey = null;
    String secretKey = null;
    if ( s3FileOutputMeta != null ) {
      accessKey = s3FileOutputMeta.getAccessKey();
      secretKey = s3FileOutputMeta.getSecretKey();
      if ( !S3Util.isEmpty( accessKey ) ) {
        s3Details.setAccessKey( accessKey );
      }
      if ( !S3Util.isEmpty( secretKey ) ) {
        s3Details.setSecretKey( secretKey );
      }
    }
    // Read optional sessionToken from queryParams
    String sessionToken = queryParams.get( "sessionToken" );
    if ( !S3Util.isEmpty( sessionToken ) ) {
      s3Details.setSessionToken( sessionToken );
    }
    // Auto-detect auth type based on whether access keys exist
    boolean hasAccessKeys = !S3Util.isEmpty( accessKey ) && !S3Util.isEmpty( secretKey );
    if ( hasAccessKeys ) {
      s3Details.setAuthType( "0" ); // Access Key auth
    } else {
      s3Details.setAuthType( "1" ); // Credentials File auth
      // Default credentials file path to ~/.aws/credentials
      s3Details.setCredentialsFilePath( System.getProperty( "user.home" ) + "/.aws/credentials" );
      // Read profile name from queryParams (default to "default")
      String profileName = queryParams.get( "profileName" );
      if ( !S3Util.isEmpty( profileName ) ) {
        s3Details.setProfileName( profileName );
      } else {
        s3Details.setProfileName( "default" );
      }
    }
    return s3Details;
  }
}
