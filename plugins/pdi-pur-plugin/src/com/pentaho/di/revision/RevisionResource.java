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
package com.pentaho.di.revision;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.repository.pur.PurObjectRevision;
import org.pentaho.di.repository.pur.UnifiedRepositoryRevisionService;
import org.pentaho.di.ui.repository.pur.services.IRevisionService;
import org.pentaho.platform.api.repository2.unified.IRepositoryVersionManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.webservices.FileVersioningConfiguration;
import org.pentaho.platform.web.http.api.resources.utils.FileUtils;

/**
 * Created by pminutillo on 7/7/14.
 * 
 * Provide REST endpoints for revision API. These methods will provide the current status of the versioning and version
 * comments enabled flags
 */
@Path( "/pur-repository-plugin/api/revision" )
public class RevisionResource {

  IUnifiedRepository repository;

  IRevisionService revisionService = null;

  static IRepositoryVersionManager repositoryVersionManager;

  /**
   * 
   * @param unifiedRepository
   */
  public RevisionResource( IUnifiedRepository unifiedRepository ) {
    this.repository = unifiedRepository;
    // Is there a better way to get the revisionService
    this.revisionService = new UnifiedRepositoryRevisionService( unifiedRepository, null );
  }

  /**
   * Retrieves the version history of a selected repository file
   * 
   * <p>
   * <b>Example Request:</b><br>
   * GET /pur-repository-plugin/api/revision/path:to:file/revisions
   * </p>
   * 
   * @param pathId
   *          (colon separated path for the repository file)
   * 
   *          <pre function="syntax.xml">
   *    :path:to:file:id
   * </pre>
   * @return file revisions objects <code> purObjectRevisions </code>
   * 
   *         <pre function="syntax.xml">
   * &lt;purObjectRevisions&gt;
   * &lt;revision&gt;
   * &lt;versionId&gt;1.0&lt;/versionId&gt;
   * &lt;creationDate&gt;2014-07-22T14:42:46.029-04:00&lt;/creationDate&gt;
   * &lt;login&gt;admin&lt;/login&gt;
   * &lt;comment&gt;JMeter test&lt;/comment&gt;
   * &lt;/revision&gt;
   * &lt;/purObjectRevisions&gt;
   * </pre>
   */
  @GET
  @Path( "{pathId : .+}/revisions" )
  @StatusCodes( { @ResponseCode( code = 200, condition = "Successfully returns list of revisions" ),
    @ResponseCode( code = 500, condition = "Something failed when attempting to retrieve revisions" ),
    @ResponseCode( code = 404, condition = "Invalid path" ) } )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public Response doGetVersions( @PathParam( "pathId" ) String pathId ) {

    Serializable fileId = null;
    List<ObjectRevision> originalRevisions = null;

    RepositoryFile repositoryFile = repository.getFile( FileUtils.idToPath( pathId ) );
    if ( repositoryFile != null ) {
      fileId = repositoryFile.getId();
    }
    if ( fileId != null ) {
      try {
        originalRevisions = revisionService.getRevisions( new StringObjectId( fileId.toString() ) );
      } catch ( KettleException e ) {
        return Response.serverError().build();
      }

      List<PurObjectRevision> revisions = new ArrayList();
      for ( ObjectRevision revision : originalRevisions ) {
        revisions.add( (PurObjectRevision) revision );
      }

      GenericEntity<List<PurObjectRevision>> genericRevisionsEntity =
          new GenericEntity<List<PurObjectRevision>>( revisions ) {
          };

      return Response.ok( genericRevisionsEntity ).build();
    } else {
      return Response.serverError().build();
    }
  }

  /**
   * This method is used to determine whether versioning should be active for the given path
   * 
   * <p>
   * <b>Example Request:</b><br />
   * GET pentaho/api/repo/files/:jmeter-test:test_file_1.ktr/versioningConfiguration </pre>
   * </p>
   * 
   * @param pathId
   *          Colon separated path for the repository file.
   * 
   * @return The Versioning Configuration applicable to the path submitted
   * 
   *         <p>
   *         <b>Example Response:</b>
   *         </p>
   * 
   *         <pre function="syntax.xml">
   * &lt;fileVersioningConfiguration&gt;
   *   &lt;versionCommentEnabled&gt;true&lt;/versionCommentEnabled&gt;
   *   &lt;versioningEnabled&gt;true&lt;/versioningEnabled&gt;
   * &lt;/fileVersioningConfiguration&gt;
   * </pre>
   */
  @GET
  @Path( "{pathId}/versioningConfiguration" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes( { @ResponseCode( code = 200, condition = "Successfully returns the versioning configuation" ) } )
  public FileVersioningConfiguration doVersioningConfiguration( @PathParam( "pathId" ) String pathId ) {
    if ( RevisionResource.repositoryVersionManager == null ) {
      RevisionResource.repositoryVersionManager = PentahoSystem.get( IRepositoryVersionManager.class );
    }
    return new FileVersioningConfiguration( RevisionResource.repositoryVersionManager.isVersioningEnabled( FileUtils
        .idToPath( pathId ) ), repositoryVersionManager.isVersionCommentEnabled( FileUtils.idToPath( pathId ) ) );
  }

  /**
   * For use by junit tests
   * 
   * @param repositoryVersionManager
   */
  public static void setRepositoryVersionManager( IRepositoryVersionManager repositoryVersionManager ) {
    RevisionResource.repositoryVersionManager = repositoryVersionManager;
  }

}
