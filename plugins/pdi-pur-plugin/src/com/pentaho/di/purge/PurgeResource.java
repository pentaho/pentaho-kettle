/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.pentaho.di.purge;

import static javax.ws.rs.core.MediaType.WILDCARD;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Level;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.di.ui.repository.pur.services.IPurgeService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.util.RepositoryPathEncoder;

import com.sun.jersey.multipart.FormDataParam;

/**
 * Created by tkafalas 7/14/14.
 */
@Path( "/pur-repository-plugin/api/purge" )
public class PurgeResource {

  public static final String PATH_SEPARATOR = "/"; //$NON-NLS-1$

  IUnifiedRepository repository;

  public PurgeResource( IUnifiedRepository unifiedRepository ) {
    this.repository = unifiedRepository;
  }

  /**
   * Provides a utility for purging files and/or revision history for the DI server.
   * 
   * <p>
   * <b>Example Request:</b><br>
   * POST /pur-repository-plugin/api/purge/path:to:file/purge
   * </p>
   * 
   * @param pathId
   *          Colon separated path for the repository file. Processing of files will occur under this path. Exception:
   *          If purgeSharedObject=true other files may be affected as well.
   * @param purgeFiles
   *          If true, files will be purged completely. This options erases files and all history. This effectively
   *          disables all parameters effecting revisions since all revisions will be deleted unconditionally.
   * @param purgeRevisions
   *          If true, all revisions to the targeted files will be purged. The current state of the file will be
   *          retained.
   * @param purgeSharedObjects
   *          If true, Shared objects will also be targeted by the purge operation. This does not replace the pathId and
   *          fileFilter processing, but rather, is in addition to that processing. If it is desired to purge shared
   *          objects only without effecting other files, then set the pathId to a single space character. Some examples
   *          of shared objects database connections, Slave Servers, Cluster Schemas, and partition Schemas.
   * @param versionCount
   *          If present, the number of historical revisions to keep. If there are more revisions for a file than
   *          versionCount, the older ones will be removed.
   * @param purgeBeforeDate
   *          If set, remove all version history created prior to this date.
   * @param fileFilter
   *          The file filter to be applied when determining what files are affected by the purge. This filter is used
   *          by the <code>tree</code> endpoint to determine what files to return. The fileFilter is a list of allowed
   *          names of files separated by the pipe (|) character. Each file name in the filter may be a full name or a
   *          partial name with one or more wildcard characters ("*"). (eg: *.ktr|*.kjb returns all files with a ktr or
   *          kjb extension).
   * @param logLevelName
   *          The standard name for the log level (ie: INFO, DEBUG)
   * @return A text file containing a log of the service execution.
   */
  @POST
  @Path( "{pathId : .+}/purge" )
  @StatusCodes( { @ResponseCode( code = 200, condition = "Successfully purged specified target" ),
    @ResponseCode( code = 500, condition = "Something failed when attempting to purge " ),
    @ResponseCode( code = 404, condition = "Invalid path" ) } )
  @Consumes( MediaType.MULTIPART_FORM_DATA )
  @Produces( { WILDCARD } )
  public Response doDeleteRevisions( @PathParam( "pathId" ) String pathId,
      @DefaultValue( "false" ) @FormDataParam( "purgeFiles" ) boolean purgeFiles,
      @DefaultValue( "false" ) @FormDataParam( "purgeRevisions" ) boolean purgeRevisions,
      @DefaultValue( "false" ) @FormDataParam( "purgeSharedObjects" ) boolean purgeSharedObjects,
      @DefaultValue( "-1" ) @FormDataParam( "versionCount" ) int versionCount,
      @FormDataParam( "purgeBeforeDate" ) Date purgeBeforeDate,
      @DefaultValue( "*" ) @FormDataParam( "fileFilter" ) String fileFilter,
      @DefaultValue( "INFO" ) @FormDataParam( "logLevel" ) String logLevelName ) {

    // A version count of 0 is illegal.
    if ( versionCount == 0 ) {
      return Response.serverError().build();
    }

    if ( purgeRevisions && ( versionCount > 0 || purgeBeforeDate != null ) ) {
      purgeRevisions = false;
    }

    IPurgeService purgeService = new UnifiedRepositoryPurgeService( this.repository );
    Level logLevel = Level.toLevel( logLevelName );

    PurgeUtilitySpecification purgeSpecification = new PurgeUtilitySpecification();
    purgeSpecification.setPath( idToPath( pathId ) );
    purgeSpecification.setPurgeFiles( purgeFiles );
    purgeSpecification.setPurgeRevisions( purgeRevisions );
    purgeSpecification.setSharedObjects( purgeSharedObjects );
    purgeSpecification.setVersionCount( versionCount );
    purgeSpecification.setBeforeDate( purgeBeforeDate );
    purgeSpecification.setFileFilter( fileFilter );
    purgeSpecification.setLogLevel( logLevel );

    // Initialize the logger
    ByteArrayOutputStream purgeUtilityStream = new ByteArrayOutputStream();
    PurgeUtilityLogger.createNewInstance( purgeUtilityStream, purgeSpecification.getPath(), logLevel );

    try {
      purgeService.doDeleteRevisions( purgeSpecification );
    } catch ( Exception e ) {
      PurgeUtilityLogger.getPurgeUtilityLogger().error( e );
      return Response.ok( encodeOutput( purgeUtilityStream ), MediaType.TEXT_HTML ).build();
    }

    return Response.ok( encodeOutput( purgeUtilityStream ), MediaType.TEXT_HTML ).build();
  }

  public static String idToPath( String pathId ) {
    String path = pathId;
    path = RepositoryPathEncoder.decodeRepositoryPath( path );
    if ( path == null || path.trim().isEmpty() ) {
      path = "";
    } else {
      if ( !path.startsWith( PATH_SEPARATOR ) ) {
        path = PATH_SEPARATOR + path;
      }
    }
    return path;
  }

  private String encodeOutput( ByteArrayOutputStream purgeUtilityStream ) {
    String responseBody = null;
    try {
      responseBody = purgeUtilityStream.toString( "UTF-8" );
    } catch ( UnsupportedEncodingException e ) {
      responseBody = purgeUtilityStream.toString();
    }
    return responseBody;
  }
}
