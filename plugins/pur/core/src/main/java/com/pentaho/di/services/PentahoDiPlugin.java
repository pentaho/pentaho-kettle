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

package com.pentaho.di.services;

import org.pentaho.di.repository.IRepositoryService;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.client.ClientConfig;

@Generated( value = { "wadl|file:/C:/env/ws/pdi-ee-plugin/wadl2java/wadl-resource/application.wadl.xml" },
    comments = "wadl2java, http://wadl.java.net", date = "2015-04-30T15:20:38.668-04:00" )
public class PentahoDiPlugin {

  /**
   * The base URI for the resource represented by this proxy
   * 
   */
  public final static URI BASE_URI;

  static {
    URI originalURI = URI.create( "http://localhost:8080/pentaho/plugin/" );
    // Look up to see if we have any indirection in the local copy
    // of META-INF/java-rs-catalog.xml file, assuming it will be in the
    // oasis:name:tc:entity:xmlns:xml:catalog namespace or similar duck type
    java.io.InputStream is = PentahoDiPlugin.class.getResourceAsStream( "/META-INF/jax-rs-catalog.xml" );
    if ( is != null ) {
      try {
        // Ignore the namespace in the catalog, can't use wildcard until
        // we are sure we have XPath 2.0
        String found =
            javax.xml.xpath.XPathFactory.newInstance().newXPath().evaluate(
                "/*[name(.) = 'catalog']/*[name(.) = 'uri' and @name ='" + originalURI + "']/@uri",
                new org.xml.sax.InputSource( is ) );
        if ( found != null && found.length() > 0 ) {
          originalURI = java.net.URI.create( found );
        }

      } catch ( Exception ex ) {
        ex.printStackTrace();
      } finally {
        try {
          is.close();
        } catch ( java.io.IOException e ) {
        }
      }
    }
    BASE_URI = originalURI;
  }

  public static PentahoDiPlugin.PurRepositoryPluginApiRevision purRepositoryPluginApiRevision(
  javax.ws.rs.client.Client client, URI baseURI ) {
    return new PentahoDiPlugin.PurRepositoryPluginApiRevision( client, baseURI );
  }

  /**
   * Template method to allow tooling to customize the new Client
   * 
   */
  private static void customizeClientConfiguration( ClientConfig cc ) {
  }

  /**
   * Template method to allow tooling to override Client factory
   * 
   */
  private static javax.ws.rs.client.Client createClientInstance( ClientConfig cc ) {
    return javax.ws.rs.client.ClientBuilder.newClient( cc );
  }

  /**
   * Create a new Client instance
   * 
   */
  public static javax.ws.rs.client.Client createClient() {
    ClientConfig cc = new ClientConfig();
    customizeClientConfiguration( cc );
    return createClientInstance( cc );
  }

  public static PentahoDiPlugin.PurRepositoryPluginApiRevision purRepositoryPluginApiRevision() {
    return purRepositoryPluginApiRevision( createClient(), BASE_URI );
  }

  public static PentahoDiPlugin.PurRepositoryPluginApiRevision purRepositoryPluginApiRevision(
    javax.ws.rs.client.Client client ) {
    return purRepositoryPluginApiRevision( client, BASE_URI );
  }

  public static PentahoDiPlugin.PurRepositoryPluginApiPurge purRepositoryPluginApiPurge(
    javax.ws.rs.client.Client client, URI baseURI ) {
    return new PentahoDiPlugin.PurRepositoryPluginApiPurge( client, baseURI );
  }

  public static PentahoDiPlugin.PurRepositoryPluginApiPurge purRepositoryPluginApiPurge() {
    return purRepositoryPluginApiPurge( createClient(), BASE_URI );
  }

  public static PentahoDiPlugin.PurRepositoryPluginApiPurge purRepositoryPluginApiPurge(
      javax.ws.rs.client.Client client ) {
    return purRepositoryPluginApiPurge( client, BASE_URI );
  }

  public static class PurRepositoryPluginApiPurge implements IRepositoryService {

    private javax.ws.rs.client.Client _client;
    private UriBuilder _uriBuilder;
    private Map<String, Object> _templateAndMatrixParameterValues;

    private PurRepositoryPluginApiPurge( javax.ws.rs.client.Client client, UriBuilder uriBuilder,
        Map<String, Object> map ) {
      _client = client;
      _uriBuilder = uriBuilder.clone();
      _templateAndMatrixParameterValues = map;
    }

    /**
     * Create new instance using existing Client instance, and a base URI and any parameters
     * 
     */
    public PurRepositoryPluginApiPurge( javax.ws.rs.client.Client client, URI baseUri ) {
      _client = client;
      _uriBuilder = UriBuilder.fromUri( baseUri );
      _uriBuilder = _uriBuilder.path( "/pur-repository-plugin/api/purge" );
      _templateAndMatrixParameterValues = new HashMap<String, Object>();
    }

    public PentahoDiPlugin.PurRepositoryPluginApiPurge.PathIdPurge pathIdPurge( String pathid ) {
      return new PentahoDiPlugin.PurRepositoryPluginApiPurge.PathIdPurge( _client, _uriBuilder
          .buildFromMap( _templateAndMatrixParameterValues ), pathid );
    }

    public static class PathIdPurge implements IRepositoryService {

      private javax.ws.rs.client.Client _client;
      private UriBuilder _uriBuilder;
      private Map<String, Object> _templateAndMatrixParameterValues;

      private PathIdPurge( javax.ws.rs.client.Client client, UriBuilder uriBuilder, Map<String, Object> map ) {
        _client = client;
        _uriBuilder = uriBuilder.clone();
        _templateAndMatrixParameterValues = map;
      }

      /**
       * Create new instance using existing Client instance, and a base URI and any parameters
       * 
       */
      public PathIdPurge( javax.ws.rs.client.Client client, URI baseUri, String pathid ) {
        _client = client;
        _uriBuilder = UriBuilder.fromUri( baseUri );
        _uriBuilder = _uriBuilder.path( "{pathId : .+}/purge" );
        _templateAndMatrixParameterValues = new HashMap<String, Object>();
        _templateAndMatrixParameterValues.put( "pathId", pathid );
      }

      /**
       * Create new instance using existing Client instance, and the URI from which the parameters will be extracted
       * 
       */
      public PathIdPurge( javax.ws.rs.client.Client client, URI uri ) {
        _client = client;
        StringBuilder template = new StringBuilder( BASE_URI.toString() );
        if ( template.charAt( ( template.length() - 1 ) ) != '/' ) {
          template.append( "/pur-repository-plugin/api/purge/{pathId : .+}/purge" );
        } else {
          template.append( "pur-repository-plugin/api/purge/{pathId : .+}/purge" );
        }
        _uriBuilder = UriBuilder.fromPath( template.toString() );
        _templateAndMatrixParameterValues = new HashMap<String, Object>();
        org.glassfish.jersey.uri.UriTemplate uriTemplate = new org.glassfish.jersey.uri.UriTemplate( template.toString() );
        HashMap<String, String> parameters = new HashMap<String, String>();
        uriTemplate.match( uri.toString(), parameters );
        _templateAndMatrixParameterValues.putAll( parameters );
      }

      /**
       * Get pathId
       * 
       */
      public String getPathid() {
        return ( (String) _templateAndMatrixParameterValues.get( "pathId" ) );
      }

      /**
       * Duplicate state and set pathId
       * 
       */
      public PentahoDiPlugin.PurRepositoryPluginApiPurge.PathIdPurge setPathid( String pathid ) {
        Map<String, Object> copyMap;
        copyMap = new HashMap<String, Object>( _templateAndMatrixParameterValues );
        UriBuilder copyUriBuilder = _uriBuilder.clone();
        copyMap.put( "pathId", pathid );
        return new PentahoDiPlugin.PurRepositoryPluginApiPurge.PathIdPurge( _client, copyUriBuilder, copyMap );
      }

      public <T> T postMultipartFormDataAs( Object input, javax.ws.rs.core.GenericType<T> returnType ) {
        UriBuilder localUriBuilder = _uriBuilder.clone();
        javax.ws.rs.client.WebTarget resource =
            _client.target( localUriBuilder.buildFromMap( _templateAndMatrixParameterValues ) );
        javax.ws.rs.client.Invocation.Builder resourceBuilder = resource.request();
        resourceBuilder = resourceBuilder.accept( "*/*" );
        resourceBuilder = resourceBuilder.header( "Content-Type" , "multipart/form-data" );
        javax.ws.rs.core.Response response;
        response = resourceBuilder.method( "POST" );
        if ( response.getStatus() >= 400 ) {
          throw new PentahoDiPlugin.WebApplicationExceptionMessage( Response
              .status( response.getStatus() ).build() );
        }
        return (T) response.getEntity();
      }

      public <T> T postMultipartFormDataAs( Object input, Class<T> returnType ) {
        UriBuilder localUriBuilder = _uriBuilder.clone();
        javax.ws.rs.client.WebTarget resource =
            _client.target( localUriBuilder.buildFromMap( _templateAndMatrixParameterValues ) );
        javax.ws.rs.client.Invocation.Builder resourceBuilder = resource.request();
        resourceBuilder = resourceBuilder.accept( "*/*" );
        resourceBuilder = resourceBuilder.header( "Content-Type" , "multipart/form-data" );
        javax.ws.rs.core.Response response;
        response = resourceBuilder.method( "POST" );
        if ( !javax.ws.rs.core.Response.class.isAssignableFrom( returnType ) ) {
          if ( response.getStatus() >= 400 ) {
            throw new PentahoDiPlugin.WebApplicationExceptionMessage( Response.status(
                response.getStatus() ).build() );
          }
        }
        if ( !javax.ws.rs.core.Response.class.isAssignableFrom( returnType ) ) {
          return (T) response.getEntity();
        } else {
          return returnType.cast( response );
        }
      }

    }

  }

  public static class PurRepositoryPluginApiRevision implements IRepositoryService {

    private javax.ws.rs.client.Client _client;
    private UriBuilder _uriBuilder;
    private Map<String, Object> _templateAndMatrixParameterValues;

    private PurRepositoryPluginApiRevision(javax.ws.rs.client.Client client, UriBuilder uriBuilder, Map<String, Object> map) {
      _client = client;
      _uriBuilder = uriBuilder.clone();
      _templateAndMatrixParameterValues = map;
    }

    /**
     * Create new instance using existing Client instance, and a base URI and any parameters
     * 
     */
    public PurRepositoryPluginApiRevision( javax.ws.rs.client.Client client, URI baseUri ) {
      _client = client;
      _uriBuilder = UriBuilder.fromUri( baseUri );
      _uriBuilder = _uriBuilder.path( "/pur-repository-plugin/api/revision" );
      _templateAndMatrixParameterValues = new HashMap<String, Object>();
    }

    public PentahoDiPlugin.PurRepositoryPluginApiRevision.PathIdRevisions pathIdRevisions( String pathid ) {
      return new PentahoDiPlugin.PurRepositoryPluginApiRevision.PathIdRevisions( _client, _uriBuilder
          .buildFromMap( _templateAndMatrixParameterValues ), pathid );
    }

    public PentahoDiPlugin.PurRepositoryPluginApiRevision.PathIdVersioningConfiguration pathIdVersioningConfiguration(
        String pathid ) {
      return new PentahoDiPlugin.PurRepositoryPluginApiRevision.PathIdVersioningConfiguration( _client, _uriBuilder
          .buildFromMap( _templateAndMatrixParameterValues ), pathid );
    }

    public static class PathIdRevisions implements IRepositoryService {

      private javax.ws.rs.client.Client _client;
      private UriBuilder _uriBuilder;
      private Map<String, Object> _templateAndMatrixParameterValues;

      private PathIdRevisions( javax.ws.rs.client.Client client, UriBuilder uriBuilder, Map<String, Object> map ) {
        _client = client;
        _uriBuilder = uriBuilder.clone();
        _templateAndMatrixParameterValues = map;
      }

      /**
       * Create new instance using existing Client instance, and a base URI and any parameters
       * 
       */
      public PathIdRevisions( javax.ws.rs.client.Client client, URI baseUri, String pathid ) {
        _client = client;
        _uriBuilder = UriBuilder.fromUri( baseUri );
        _uriBuilder = _uriBuilder.path( "{pathId : .+}/revisions" );
        _templateAndMatrixParameterValues = new HashMap<String, Object>();
        _templateAndMatrixParameterValues.put( "pathId", pathid );
      }

      /**
       * Create new instance using existing Client instance, and the URI from which the parameters will be extracted
       * 
       */
      public PathIdRevisions( javax.ws.rs.client.Client client, URI uri ) {
        _client = client;
        StringBuilder template = new StringBuilder( BASE_URI.toString() );
        if ( template.charAt( ( template.length() - 1 ) ) != '/' ) {
          template.append( "/pur-repository-plugin/api/revision/{pathId : .+}/revisions" );
        } else {
          template.append( "pur-repository-plugin/api/revision/{pathId : .+}/revisions" );
        }
        _uriBuilder = UriBuilder.fromPath( template.toString() );
        _templateAndMatrixParameterValues = new HashMap<String, Object>();
        org.glassfish.jersey.uri.UriTemplate uriTemplate = new org.glassfish.jersey.uri.UriTemplate( template.toString() );
        HashMap<String, String> parameters = new HashMap<String, String>();
        uriTemplate.match( uri.toString(), parameters );
        _templateAndMatrixParameterValues.putAll( parameters );
      }

      /**
       * Get pathId
       * 
       */
      public String getPathid() {
        return ( (String) _templateAndMatrixParameterValues.get( "pathId" ) );
      }

      /**
       * Duplicate state and set pathId
       * 
       */
      public PentahoDiPlugin.PurRepositoryPluginApiRevision.PathIdRevisions setPathid( String pathid ) {
        Map<String, Object> copyMap;
        copyMap = new HashMap<String, Object>( _templateAndMatrixParameterValues );
        UriBuilder copyUriBuilder = _uriBuilder.clone();
        copyMap.put( "pathId", pathid );
        return new PentahoDiPlugin.PurRepositoryPluginApiRevision.PathIdRevisions( _client, copyUriBuilder, copyMap );
      }

      public <T> T getAsXml( javax.ws.rs.core.GenericType<T> returnType ) {
        UriBuilder localUriBuilder = _uriBuilder.clone();
        javax.ws.rs.client.WebTarget resource =
            _client.target( localUriBuilder.buildFromMap( _templateAndMatrixParameterValues ) );
        javax.ws.rs.client.Invocation.Builder resourceBuilder = resource.request();
        resourceBuilder = resourceBuilder.accept( "application/xml" );
        javax.ws.rs.core.Response response;
        response = resourceBuilder.method( "GET" );
        if (response.getStatus() >= 400 ) {
          throw new PentahoDiPlugin.WebApplicationExceptionMessage(Response
              .status(response.getStatus() ).build() );
        }
        return (T) response.getEntity();
      }

      public <T> T getAsXml( Class<T> returnType ) {
        UriBuilder localUriBuilder = _uriBuilder.clone();
        javax.ws.rs.client.WebTarget resource =
            _client.target( localUriBuilder.buildFromMap( _templateAndMatrixParameterValues ) );
        javax.ws.rs.client.Invocation.Builder resourceBuilder = resource.request();
        resourceBuilder = resourceBuilder.accept( "application/xml" );
        javax.ws.rs.core.Response response;
        response = resourceBuilder.method( "GET" );
        if ( !javax.ws.rs.core.Response.class.isAssignableFrom(returnType)) {
          if ( response.getStatus() >= 400 ) {
            throw new PentahoDiPlugin.WebApplicationExceptionMessage(Response.status(
                response.getStatus() ).build() );
          }
        }
        if ( !javax.ws.rs.core.Response.class.isAssignableFrom( returnType ) ) {
          return (T) response.getEntity();
        } else {
          return returnType.cast( response );
        }
      }

      public <T> T getAsJson( javax.ws.rs.core.GenericType<T> returnType ) {
        UriBuilder localUriBuilder = _uriBuilder.clone();
        javax.ws.rs.client.WebTarget resource =
            _client.target(localUriBuilder.buildFromMap( _templateAndMatrixParameterValues ) );
        javax.ws.rs.client.Invocation.Builder resourceBuilder = resource.request();
        resourceBuilder = resourceBuilder.accept( "application/json" );
        javax.ws.rs.core.Response response;
        response = resourceBuilder.method( "GET" );
        if (response.getStatus() >= 400 ) {
          throw new PentahoDiPlugin.WebApplicationExceptionMessage( Response
              .status( response.getStatus() ).build() );
        }
        return (T) response.getEntity();
      }

      public <T> T getAsJson( Class<T> returnType ) {
        UriBuilder localUriBuilder = _uriBuilder.clone();
        javax.ws.rs.client.WebTarget resource =
            _client.target( localUriBuilder.buildFromMap( _templateAndMatrixParameterValues ) );
        javax.ws.rs.client.Invocation.Builder resourceBuilder = resource.request();
        resourceBuilder = resourceBuilder.accept( "application/json" );
        javax.ws.rs.core.Response response;
        response = resourceBuilder.method( "GET" );
        if ( !javax.ws.rs.core.Response.class.isAssignableFrom(returnType)) {
          if ( response.getStatus() >= 400 ) {
            throw new PentahoDiPlugin.WebApplicationExceptionMessage(Response.status(
                response.getStatus() ).build() );
          }
        }
        if ( !javax.ws.rs.core.Response.class.isAssignableFrom(returnType)) {
          return (T) response.getEntity( );
        } else {
          return returnType.cast( response );
        }
      }

    }

    public static class PathIdVersioningConfiguration implements IRepositoryService {

      private javax.ws.rs.client.Client _client;
      private UriBuilder _uriBuilder;
      private Map<String, Object> _templateAndMatrixParameterValues;

      private PathIdVersioningConfiguration( javax.ws.rs.client.Client client, UriBuilder uriBuilder,
          Map<String, Object> map ) {
        _client = client;
        _uriBuilder = uriBuilder.clone();
        _templateAndMatrixParameterValues = map;
      }

      /**
       * Create new instance using existing Client instance, and a base URI and any parameters
       * 
       */
      public PathIdVersioningConfiguration( javax.ws.rs.client.Client client, URI baseUri, String pathid ) {
        _client = client;
        _uriBuilder = UriBuilder.fromUri( baseUri );
        _uriBuilder = _uriBuilder.path( "{pathId}/versioningConfiguration" );
        _templateAndMatrixParameterValues = new HashMap<String, Object>();
        _templateAndMatrixParameterValues.put( "pathId", pathid );
      }

      /**
       * Create new instance using existing Client instance, and the URI from which the parameters will be extracted
       * 
       */
      public PathIdVersioningConfiguration( javax.ws.rs.client.Client client, URI uri ) {
        _client = client;
        StringBuilder template = new StringBuilder( BASE_URI.toString() );
        if ( template.charAt( ( template.length() - 1 ) ) != '/' ) {
          template.append( "/pur-repository-plugin/api/revision/{pathId}/versioningConfiguration" );
        } else {
          template.append( "pur-repository-plugin/api/revision/{pathId}/versioningConfiguration" );
        }
        _uriBuilder = UriBuilder.fromPath( template.toString() );
        _templateAndMatrixParameterValues = new HashMap<String, Object>();
        org.glassfish.jersey.uri.UriTemplate uriTemplate = new org.glassfish.jersey.uri.UriTemplate( template.toString() );
        HashMap<String, String> parameters = new HashMap<String, String>();
        uriTemplate.match( uri.toString(), parameters );
        _templateAndMatrixParameterValues.putAll( parameters );
      }

      /**
       * Get pathId
       * 
       */
      public String getPathid() {
        return ( (String) _templateAndMatrixParameterValues.get( "pathId" ) );
      }

      /**
       * Duplicate state and set pathId
       * 
       */
      public PentahoDiPlugin.PurRepositoryPluginApiRevision.PathIdVersioningConfiguration setPathid( String pathid ) {
        Map<String, Object> copyMap;
        copyMap = new HashMap<String, Object>( _templateAndMatrixParameterValues );
        UriBuilder copyUriBuilder = _uriBuilder.clone();
        copyMap.put( "pathId", pathid );
        return new PentahoDiPlugin.PurRepositoryPluginApiRevision.PathIdVersioningConfiguration( _client,
            copyUriBuilder, copyMap );
      }

      public FileVersioningConfiguration getAsFileVersioningConfigurationXml() {
        UriBuilder localUriBuilder = _uriBuilder.clone();
        javax.ws.rs.client.WebTarget resource =
            _client.target( localUriBuilder.buildFromMap( _templateAndMatrixParameterValues ) );
        javax.ws.rs.client.Invocation.Builder resourceBuilder = resource.request();
        resourceBuilder = resourceBuilder.accept( "application/xml" );
        javax.ws.rs.core.Response response;
        response = resourceBuilder.method( "GET" );
        if ( response.getStatus() >= 400 ) {
          throw new PentahoDiPlugin.WebApplicationExceptionMessage( javax.ws.rs.core.Response
              .status( response.getStatus() ).build() );
        }
        return ( FileVersioningConfiguration ) response.getEntity( );
      }

      public <T> T getAsXml( javax.ws.rs.core.GenericType<T> returnType ) {
        UriBuilder localUriBuilder = _uriBuilder.clone();
        javax.ws.rs.client.WebTarget resource =
            _client.target( localUriBuilder.buildFromMap( _templateAndMatrixParameterValues ) );
        javax.ws.rs.client.Invocation.Builder resourceBuilder = resource.request();
        resourceBuilder = resourceBuilder.accept( "application/xml" );
        javax.ws.rs.core.Response response;
        response = resourceBuilder.method( "GET" );
        if ( response.getStatus() >= 400 ) {
          throw new PentahoDiPlugin.WebApplicationExceptionMessage( javax.ws.rs.core.Response
              .status( response.getStatus() ).build() );
        }
        return (T) response.getEntity();
      }

      public <T> T getAsXml( Class<T> returnType ) {
        UriBuilder localUriBuilder = _uriBuilder.clone();
        javax.ws.rs.client.WebTarget resource =
            _client.target( localUriBuilder.buildFromMap( _templateAndMatrixParameterValues ) );
        javax.ws.rs.client.Invocation.Builder resourceBuilder = resource.request();
        resourceBuilder = resourceBuilder.accept( "application/xml" );
        javax.ws.rs.core.Response response;
        response = resourceBuilder.method( "GET" );
        if ( !javax.ws.rs.core.Response.class.isAssignableFrom( returnType ) ) {
          if ( response.getStatus() >= 400 ) {
            throw new PentahoDiPlugin.WebApplicationExceptionMessage( javax.ws.rs.core.Response.status(
                response.getStatus() ).build() );
          }
        }
        if ( !javax.ws.rs.core.Response.class.isAssignableFrom( returnType ) ) {
          return (T) response.getEntity();
        } else {
          return returnType.cast( response );
        }
      }

      public FileVersioningConfiguration getAsFileVersioningConfigurationJson() {
        UriBuilder localUriBuilder = _uriBuilder.clone();
        javax.ws.rs.client.WebTarget resource =
            _client.target( localUriBuilder.buildFromMap( _templateAndMatrixParameterValues ) );
        javax.ws.rs.client.Invocation.Builder resourceBuilder = resource.request();
        resourceBuilder = resourceBuilder.accept( "application/json" );
        javax.ws.rs.core.Response response;
        response = resourceBuilder.method( "GET" );
        if ( response.getStatus() >= 400 ) {
          throw new PentahoDiPlugin.WebApplicationExceptionMessage( javax.ws.rs.core.Response
              .status( response.getStatus() ).build() );
        }
        return ( FileVersioningConfiguration ) response.getEntity();
      }

      public <T> T getAsJson( javax.ws.rs.core.GenericType<T> returnType ) {
        UriBuilder localUriBuilder = _uriBuilder.clone();
        javax.ws.rs.client.WebTarget resource =
            _client.target( localUriBuilder.buildFromMap( _templateAndMatrixParameterValues ) );
        javax.ws.rs.client.Invocation.Builder resourceBuilder = resource.request();
        resourceBuilder = resourceBuilder.accept( "application/json" );
        javax.ws.rs.core.Response response;
        response = resourceBuilder.method( "GET" );
        if ( response.getStatus() >= 400 ) {
          throw new PentahoDiPlugin.WebApplicationExceptionMessage( javax.ws.rs.core.Response
              .status( response.getStatus() ).build() );
        }
        return (T) response.getEntity();
      }

      public <T> T getAsJson( Class<T> returnType ) {
        UriBuilder localUriBuilder = _uriBuilder.clone();
        javax.ws.rs.client.WebTarget resource =
            _client.target( localUriBuilder.buildFromMap( _templateAndMatrixParameterValues ) );
        javax.ws.rs.client.Invocation.Builder resourceBuilder = resource.request();
        resourceBuilder = resourceBuilder.accept( "application/json" );
        javax.ws.rs.core.Response response;
        response = resourceBuilder.method( "GET" );
        if ( !javax.ws.rs.core.Response.class.isAssignableFrom( returnType ) ) {
          if ( response.getStatus() >= 400 ) {
            throw new PentahoDiPlugin.WebApplicationExceptionMessage( javax.ws.rs.core.Response.status(
                response.getStatus() ).build() );
          }
        }
        if ( !javax.ws.rs.core.Response.class.isAssignableFrom( returnType ) ) {
          return (T) response.getEntity();
        } else {
          return returnType.cast( response );
        }
      }

    }

  }

  /**
   * Workaround for JAX_RS_SPEC-312
   * 
   */
  private static class WebApplicationExceptionMessage extends WebApplicationException {

    private WebApplicationExceptionMessage( Response response ) {
      super( response );
    }

    /**
     * Workaround for JAX_RS_SPEC-312
     * 
     */
    public String getMessage() {
      Response response = getResponse();
      Response.Status status = Response.Status.fromStatusCode( response.getStatus() );
      if ( status != null ) {
        return ( response.getStatus() + ( " " + status.getReasonPhrase() ) );
      } else {
        return Integer.toString( response.getStatus() );
      }
    }

    public String toString() {
      String s = "javax.ws.rs.WebApplicationException";
      String message = getLocalizedMessage();
      return ( s + ( ": " + message ) );
    }

  }

}
