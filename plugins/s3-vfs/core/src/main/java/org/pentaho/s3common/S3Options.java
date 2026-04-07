package org.pentaho.s3common;

import org.pentaho.amazon.s3.S3Details;
import org.pentaho.amazon.s3.S3Util;
import org.pentaho.di.core.encryption.Encr;

import java.util.Map;
import java.util.Optional;

import com.amazonaws.regions.Regions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3Options {

  private static final Logger log = LoggerFactory.getLogger( S3Options.class );

  private Optional<AuthKeys> authKeys;
  private final Optional<CredentialsFile> credFileAuth;
  private final BaseS3Options base;
  private final Optional<TrustStore> trustStoreFile;
  private final Optional<KeyStore> keyStoreFile;

  private S3Options( Optional<AuthKeys> authKeys,
                     Optional<CredentialsFile> credFileAuth,
                     BaseS3Options base,
                     Optional<TrustStore> trustStoreFile,
                     Optional<KeyStore> keyStoreFile ) {
    this.authKeys = authKeys;
    this.credFileAuth = credFileAuth;
    this.base = base;
    this.trustStoreFile = trustStoreFile;
    this.keyStoreFile = keyStoreFile;
  }

  public static S3Options from( S3CommonFileSystemConfigBuilder opts ) {
    return new S3Options(
      AuthKeys.from( opts ),
      CredentialsFile.from( opts ),
      BaseS3Options.from( opts ),
      TrustStore.from( opts ),
      KeyStore.from( opts ) );
  }

  public static S3Options from( Map<String, String> opts ) {
    return new S3Options(
      AuthKeys.from( opts ),
      CredentialsFile.from( opts ),
      BaseS3Options.from( opts ),
      TrustStore.from( opts ),
      KeyStore.from( opts ) );
  }

  public Optional<AuthKeys> authKeys() {
    return authKeys;
  }

  public Optional<CredentialsFile> credFileAuth() {
    return credFileAuth;
  }

  public BaseS3Options base() {
    return base;
  }

  public Optional<TrustStore> trustStore() {
    return trustStoreFile;
  }

  public Optional<KeyStore> keyStore() {
    return keyStoreFile;
  }

  public void setAuthKeys( AuthKeys keys ) {
    this.authKeys = Optional.of( keys );
  }

  public record AuthKeys( String accessKey, String secretKey, String sessionToken ) {

    public AuthKeys( S3Util.S3Keys keys ) {
      this( keys.getAccessKey(), keys.getSecretKey(), null );
    }

    static Optional<AuthKeys> from( Map<String, String> props ) {
      String accessKey = Encr.decryptPassword( props.get( S3Details.PROP_ACCESS_KEY ) );
      String secretKey = Encr.decryptPassword( props.get( S3Details.PROP_SECRET_KEY ) );
      String sessionToken = Encr.decryptPassword( props.get( S3Details.PROP_SESSION_TOKEN ) );
      return maybeAuth( accessKey, secretKey, sessionToken );
    }

    static Optional<AuthKeys> from( S3CommonFileSystemConfigBuilder s3CommonFileSystemConfigBuilder ) {
      String accessKey = s3CommonFileSystemConfigBuilder.getAccessKey();
      String secretKey = s3CommonFileSystemConfigBuilder.getSecretKey();
      String sessionToken = s3CommonFileSystemConfigBuilder.getSessionToken();
      return maybeAuth( accessKey, secretKey, sessionToken );
    }

    static Optional<AuthKeys> maybeAuth( String accessKey, String secretKey, String sessionToken ) {
      if ( StringUtils.isNoneEmpty( accessKey, secretKey ) ) {
        return Optional.of( new AuthKeys( accessKey, secretKey, StringUtils.isEmpty( sessionToken ) ? null :
          sessionToken ) );
      }
      return Optional.empty();
    }
  }

  public record CredentialsFile( String credentialsFile, String profileName ) {

    static Optional<CredentialsFile> from( S3CommonFileSystemConfigBuilder cfg ) {
      var credentialsFilePath = cfg.getCredentialsFile();
      var profileName = cfg.getProfileName();
      return StringUtils.isEmpty( credentialsFilePath ) ? Optional.empty() : Optional.of( new CredentialsFile(
        credentialsFilePath, profileName ) );
    }

    static Optional<CredentialsFile> from( Map<String, String> opts ) {
      var credentialsFilePath = opts.get( S3Details.PROP_CREDENTIALS_FILE_PATH );
      var profileName = opts.get( S3Details.PROP_PROFILE_NAME );
      return StringUtils.isEmpty( credentialsFilePath ) ? Optional.empty() : Optional.of( new CredentialsFile(
        credentialsFilePath, profileName ) );
    }

  }

  public record BaseS3Options( String endpoint, String region, String signatureVersion, boolean pathStyleAccess ) {

    static BaseS3Options from( S3CommonFileSystemConfigBuilder opts ) {
      return new BaseS3Options(
        opts.getEndpoint(),
        getRegion( opts.getRegion() ),
        opts.getSignatureVersion(),
        getPathStyleAccess( opts.getPathStyleAccess() ) );
    }

    static BaseS3Options from( Map<String, String> props ) {
      return new BaseS3Options(
        props.get( S3Details.PROP_ENDPOINT ),
        getRegion( props.get( S3Details.PROP_REGION ) ),
        props.get( S3Details.PROP_SIGNATURE_VERSION ),
        getPathStyleAccess( props.get( S3Details.PROP_PATHSTYLE_ACCESS ) ) );
    }

    private static String getRegion( String val ) {
      if ( StringUtils.isBlank( val ) ) {
        log.debug( "No region set, using default {}", Regions.DEFAULT_REGION.getName() );
        return Regions.DEFAULT_REGION.getName();
      }
      return val;
    }

    private static boolean getPathStyleAccess( String val ) {
      return Optional.ofNullable( val ).map( Boolean::parseBoolean ).orElse( true );
    }
  }


  public record TrustStore( String filePath, String pass, boolean trustAll ) {

    static Optional<TrustStore> from( S3CommonFileSystemConfigBuilder cfg ) {
      var filePath = cfg.getTrustStoreFilePath();
      var pass = cfg.getTrustStorePassword();
      var trustAll = Boolean.parseBoolean( cfg.getTrustAll() );
      return maybeTrustStore( filePath, pass, trustAll );
    }

    static Optional<TrustStore> from( Map<String, String> opts ) {
      var filePath = opts.get( S3Details.PROP_TRUST_STORE_FILE_PATH );
      var pass = Encr.decryptPassword( opts.get( S3Details.PROP_TRUST_STORE_PASSWORD ) );
      var trustAll = Boolean.parseBoolean( opts.get( S3Details.PROP_TRUST_ALL ) );
      return maybeTrustStore( filePath, pass, trustAll );
    }

    private static Optional<TrustStore> maybeTrustStore( String filePath, String pass, boolean trustAll ) {
      if ( StringUtils.isEmpty( pass ) ) {
        pass = null;
      }
      return StringUtils.isBlank( filePath ) && !trustAll
        ? Optional.empty()
        : Optional.of( new TrustStore( filePath, pass, trustAll ) );
    }
  }

  public record KeyStore( String filePath, String password ) {

    static Optional<KeyStore> from( S3CommonFileSystemConfigBuilder cfg ) {
      var filePath = cfg.getKeyStoreFilePath();
      var password = cfg.getKeyStorePassword();
      return maybeKeyStore( filePath, password );
    }

    static Optional<KeyStore> from( Map<String, String> opts ) {
      var filePath = opts.get( S3Details.PROP_KEY_STORE_FILE_PATH );
      var password = Encr.decryptPassword( opts.get( S3Details.PROP_KEY_STORE_PASSWORD ) );
      return maybeKeyStore( filePath, password );
    }

    private static Optional<KeyStore> maybeKeyStore( String filePath, String password ) {
      if ( StringUtils.isEmpty( password ) ) {
        password = null;
      }
      return StringUtils.isBlank( filePath )
        ? Optional.empty()
        : Optional.of( new KeyStore( filePath, password ) );
    }
  }
}
