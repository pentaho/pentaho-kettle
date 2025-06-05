package com.pentaho.di.repovfs.vfs;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs2.provider.LayeredFileName;
import org.apache.commons.vfs2.provider.LayeredFileNameParser;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.provider.VfsComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pentaho.di.repovfs.cfg.JCRSolutionConfig;
import com.pentaho.di.repovfs.repo.BasicAuthentication;
import com.pentaho.di.repovfs.repo.RepositoryClient;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class JCRSolutionFileProvider extends AbstractOriginatingFileProvider {

  public static String SCHEME = "jcr-solution";

  private static final Logger log = LoggerFactory.getLogger( JCRSolutionFileProvider.class );

  private final JCRSolutionConfig cfg;

  public static final Collection<Capability> capabilities = Collections.unmodifiableCollection( Arrays.asList(
    Capability.GET_TYPE,
    Capability.GET_LAST_MODIFIED,
    Capability.LIST_CHILDREN,
    Capability.READ_CONTENT,
    Capability.WRITE_CONTENT,
    Capability.CREATE,
    Capability.FS_ATTRIBUTES,
    Capability.URI ) );

  public static final UserAuthenticationData.Type[] AUTHENTICATOR_TYPES = new UserAuthenticationData.Type[] {
    UserAuthenticationData.USERNAME, UserAuthenticationData.PASSWORD
  };

  public JCRSolutionFileProvider() {
    setFileNameParser( JcrLayeredFileNameParser.getInstance() );
    cfg = new JCRSolutionConfig();
  }

  /**
   * Creates a {@link org.apache.commons.vfs2.FileSystem}. If the returned FileSystem implements {@link
   * org.apache.commons.vfs2.provider.VfsComponent}, it will be initialised.
   *
   * @param rootName The name of the root file of the file system to create.
   */
  protected FileSystem doCreateFileSystem( final FileName rootName, final FileSystemOptions fileSystemOptions )
    throws FileSystemException {
    final LayeredFileName genericRootName = (LayeredFileName) rootName;
    log.debug( "doCreateFileSystem: jcr-solution" );
    return createJCRFileSystem( genericRootName, fileSystemOptions );
  }

  private FileSystem createJCRFileSystem( final LayeredFileName genericRootName,
                                          final FileSystemOptions fileSystemOptions ) {
    log.debug( "createJcrFileSystem({}, opts)", genericRootName );

    // be sure not to change any value here, config is used as a cache key
    // and this is called after the key lookup and before insert
    JCRSolutionFileSystem.ConfigBuilder configBuilder = JCRSolutionFileSystem.createConfigBuilder();
    String givenUsername = configBuilder.getUser( fileSystemOptions );
    String givenPassword = configBuilder.getPass( fileSystemOptions );
    String givenURI = configBuilder.getUrl( fileSystemOptions );

    BasicAuthentication auth = new BasicAuthentication( givenUsername, givenPassword );
    RepositoryClient client = new RepositoryClient( cfg, givenURI, auth );

    return new JCRSolutionFileSystem( genericRootName, fileSystemOptions, cfg, client );
  }


  /**
   * Get the filesystem capabilities.<br> These are the same as on the filesystem, but available before the first
   * filesystem was instanciated.
   */
  public Collection<Capability> getCapabilities() {
    return capabilities;
  }

  private static class JcrLayeredFileNameParser extends LayeredFileNameParser {

    private static final JcrLayeredFileNameParser INSTANCE = new JcrLayeredFileNameParser();

    public static JcrLayeredFileNameParser getInstance() {
      return INSTANCE;
    }

    /**
     * Parses the base and name into a FileName.
     *
     * @param context      The component context.
     * @param baseFileName The base FileName.
     * @param fileName     name The target file name.
     * @return The constructed FileName.
     * @throws FileSystemException if an error occurs.
     */
    @Override
    public FileName parseUri( final VfsComponentContext context, final FileName baseFileName, final String fileName )
      throws FileSystemException {
      // this is pretty much a copy-paste of LayeredFileNameParser's method, but calls a different extractRootName
      // and returns a different FileName impl
      final StringBuilder name = new StringBuilder();

      // Extract the scheme
      final String scheme = UriParser.extractScheme( context.getFileSystemManager().getSchemes(), fileName, name );

      // Extract the Layered file URI
      final String rootUriName = extractRootName( name );
      FileName rootUri = null;
      if ( rootUriName != null ) {
        rootUri = context.parseURI( rootUriName );
      }

      UriParser.fixSeparators( name );
      final FileType fileType = UriParser.normalisePath( name );
      final String path = name.toString();

      return new JcrLayeredFileName( scheme, rootUri, path, fileType );
    }

    @Override
    protected String extractRootName( StringBuilder uri ) {
      // this searches from the begginning of the string instead of the end,
      // to deal with the fact that pvfs provider can decode encoded '!' in the path
      int pos = uri.indexOf( "!" );
      final String root;
      if ( pos < 0 ) {
        root = uri.toString();
        uri.setLength( 0 );
      } else {
        root = uri.substring( 0, pos );
        uri.delete( 0, pos + 1 );
      }
      return root;
    }
  }

  /**
   * Workaround for issue where LayeredFileNameParser will unescape the '!' character and cause
   * path parsing to fail. This version is only meant to add '!' to list of characters to be reencoded.
   */
  private static class JcrLayeredFileName extends LayeredFileName {

    public JcrLayeredFileName( String scheme, FileName outerUri, String path, FileType type ) {
      super( scheme, outerUri, path, type );
    }

    private static final char[] RESERVED_URI_CHARS = { '#', ' ', '!' };

    protected String createURI() {
      return createURI( false, true );
    }

    /**
     * Returns the URI without a password.
     *
     * @return Returns the URI without a password.
     */
    @Override
    public String getFriendlyURI() {
      return createURI( false, false );
    }

    private String createURI( final boolean useAbsolutePath, final boolean usePassword ) {
      final StringBuilder buffer = new StringBuilder();
      appendRootUri( buffer, usePassword );
      buffer.append( handleURISpecialCharacters( getPath() ) );
      return buffer.toString();
    }

    private String handleURISpecialCharacters( String uri ) {
      if ( !StringUtils.isEmpty( uri ) ) {
        try {
          // VFS-325: Handle URI special characters in file name
          // Decode the base URI and re-encode with URI special characters
          // encode will allways encode '%', hence the first decode
          uri = UriParser.decode( uri );
          return UriParser.encode( uri, RESERVED_URI_CHARS );
        } catch ( final FileSystemException e ) {
          // Default to base URI value
        }
      }

      return uri;
    }
  }
}