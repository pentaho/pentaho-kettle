package org.pentaho.di.repovfs.plugin.server;

import org.pentaho.di.repository.Repository;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shenanigans for determining if we should be using a local repository instance */
public class RepositoryTypeGuesser {

  private static Logger log = LoggerFactory.getLogger( RepositoryTypeGuesser.class );

  public boolean useEmbeddedServer() {
    // there are many variants of determining if the repository to use is the local embedded one,
    // they all go something like this
    return isSingleDiServerInstance() && ( !isRemoteDiServerInstance() && isServer() );
  }

  public boolean canUseLocalRepository( Repository diRepo ) {
    if ( log.isDebugEnabled() ) {
      logFlags();
    }
    try {
      if ( useEmbeddedServer() && isRepoLocal( diRepo ) ) {
        return hasLocalRepository();
      }
    } catch ( Exception e ) {
      log.warn( "Unable to check for local repository", e );
    }
    return false;
  }

  public boolean hasLocalRepository() {
    return PentahoSystem.get( IUnifiedRepository.class ) != null;
  }

  private static boolean isRepoLocal( Repository diRepo ) {
    // kinda shady but this might be the best guess
    boolean hasPhonyPass = "ignore".equals( diRepo.getUserInfo().getPassword() );
    log.debug( "repo named {} {}", diRepo.getName(), hasPhonyPass ? "has phony pass" : "" );
    return diRepo.getName().equals( "singleDiServerInstance" ) && hasPhonyPass;
  }

  private boolean isServer() {
    try {
      IApplicationContext appCtx = PentahoSystem.getApplicationContext();
      return appCtx != null && appCtx.getFullyQualifiedServerURL() != null;
    } catch ( Exception e ) {
      return false;
    }
  }

  /**
   * "If this option is set, then you cannot load transformations or jobs from anywhere but the local server."
   * -- IRepositoryFactory, connect:124, not necessarily true
   */
  private boolean isSingleDiServerInstance() {
    // various places use this with default=true to determine if local (or "embedded") repository is in use
    // but worker-nodes-ee setting it to false in its pentaho.xml is the only write I could find
    return getSystemFlag( "singleDiServerInstance", true );
  }

  private boolean isRemoteDiServerInstance() {
    // found no evidence of where this is set, but it is checked in a few places to determine
    // if the embedded repository should be used
    return getSystemFlag( "remoteDiServerInstance", false );
  }

  private static boolean getSystemFlag( String property, boolean defaultValue ) {
    String prop = PentahoSystem.getSystemSetting( property, null );
    if ( prop != null ) {
      return BooleanUtils.toBoolean( prop );
    }
    return defaultValue;
  }

  private void logFlags() {
    log.debug( "flags: single? {}, remote? {}, server? {}", isRemoteDiServerInstance(), isRemoteDiServerInstance(), isServer() );
  }

}
