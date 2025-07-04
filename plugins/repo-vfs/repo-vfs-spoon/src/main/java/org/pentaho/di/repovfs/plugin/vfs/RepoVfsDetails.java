package org.pentaho.di.repovfs.plugin.vfs;

import org.pentaho.di.repovfs.vfs.JCRSolutionFileProvider;

import org.pentaho.di.connections.annotations.Encrypted;
import org.pentaho.di.connections.vfs.BaseVFSConnectionDetails;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// even though we don't want to persist, this is still needed by ConnectionManager.clone
@MetaStoreElementType( name = "Pentaho repository connection", description = "A VFS connection to the Pentaho Repository" )
/** Information needed to connect to the repository */
public class RepoVfsDetails extends BaseVFSConnectionDetails {

  private static Logger log = LoggerFactory.getLogger( RepoVfsDetails.class );

  private VariableSpace vars;

  @MetaStoreAttribute
  private String name;

  @MetaStoreAttribute
  private String description = "";

  @MetaStoreAttribute
  private String url;

  @MetaStoreAttribute
  private String user;

  @MetaStoreAttribute
  @Encrypted
  private String pass;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName( String name ) {
    this.name = name;
  }

  @Override
  public String getType() {
    return JCRSolutionFileProvider.SCHEME;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public VariableSpace getSpace() {
    return vars;
  }

  @Override
  public void setSpace( VariableSpace space ) {
    vars = space;
  }

  public boolean hasBuckets() {
    return false;
  }


  public String getUrl() {
    return url;
  }

  public String getUser() {
    return user;
  }

  public String getPass() {
    return pass;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public void setUrl( String url ) {
    this.url = url;
  }

  public void setUser( String user ) {
    this.user = user;
  }

  public void setPass( String pass ) {
    this.pass = pass;
  }

  public Optional<URL> tryParseUrl() {
    if ( StringUtils.isEmpty( url ) ) {
      return Optional.empty();
    }
    try {
      return Optional.of( new URL( url ) );
    } catch ( MalformedURLException e ) {
      log.error( "Bad URL", e );
    }
    return Optional.empty();
  }

  @Override
  public String getDomain() {

    return tryParseUrl().map( url -> {
      String domain = url.getHost();
      int port = url.getPort();
      if ( port > 0 ) {
        return domain + ":" + port;
      } else {
        return domain;
      }
    } ).orElse( "" );

  }

  @Override
  public String getRootPath() {
    return tryParseUrl().map( URL::getPath ).orElse( "/" );
  }

  @Override
  public String toString() {
    return String.format( "name: '%s', url: '%s', user: '%s', pass: %s, description: '%s'", name, url, user,
      pass == null ? "<null>" : "***", description );
  }
}
