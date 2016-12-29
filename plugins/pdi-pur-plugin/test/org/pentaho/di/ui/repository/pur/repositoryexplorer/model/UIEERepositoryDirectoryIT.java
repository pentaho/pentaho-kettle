/*!
 * Copyright 2010 - 2016 Pentaho Corporation.  All rights reserved.
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
package org.pentaho.di.ui.repository.pur.repositoryexplorer.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.security.AccessControlException;

import org.apache.commons.io.FileUtils;
import org.apache.jackrabbit.api.JackrabbitWorkspace;
import org.apache.jackrabbit.api.security.authorization.PrivilegeManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryTestBase;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.repository.pur.PurRepository;
import org.pentaho.di.repository.pur.PurRepositoryConnector;
import org.pentaho.di.repository.pur.PurRepositoryMeta;
import org.pentaho.di.ui.repository.pur.services.IAclService;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl.Builder;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid.Type;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.repository2.unified.jcr.RepositoryFileProxyFactory;
import org.pentaho.platform.repository2.unified.jcr.SimpleJcrTestUtils;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.TestPrincipalProvider;
import org.pentaho.platform.repository2.unified.jcr.sejcr.CredentialsStrategy;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.userroledao.DefaultTenantedPrincipleNameResolver;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@ContextConfiguration( locations = { "classpath:/repository.spring.xml",
  "classpath:/repository-test-override.spring.xml" } )
public class UIEERepositoryDirectoryIT extends RepositoryTestBase implements ApplicationContextAware,
    java.io.Serializable {

  static final long serialVersionUID = 2064159405078106703L; /* EESOURCE: UPDATE SERIALVERUID */

  private IUnifiedRepository repo;

  private ITenantedPrincipleNameResolver userNameUtils = new DefaultTenantedPrincipleNameResolver();
  private ITenantedPrincipleNameResolver roleNameUtils = new DefaultTenantedPrincipleNameResolver(
      DefaultTenantedPrincipleNameResolver.ALTERNATE_DELIMETER );

  private ITenantManager tenantManager;

  private ITenant systemTenant;

  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDaoTarget;

  private String repositoryAdminUsername;

  private JcrTemplate testJcrTemplate;

  private MicroPlatform mp;
  IUserRoleDao testUserRoleDao;

  IUserRoleDao userRoleDao;

  private String singleTenantAdminRoleName;
  private String tenantAuthenticatedRoleName;
  private String sysAdminUserName;
  private String superAdminRoleName;
  private TransactionTemplate txnTemplate;
  private IRepositoryFileDao repositoryFileDao;
  private final String TENANT_ID_ACME = "acme";
  private IBackingRepositoryLifecycleManager repositoryLifecyleManager;
  private final String TENANT_ID_DUFF = "duff";

  private static IAuthorizationPolicy authorizationPolicy;

  private TestContextManager testContextManager;

  public UIEERepositoryDirectoryIT( Boolean lazyRepo ) {
    super( lazyRepo );
  }

  // ~ Methods =========================================================================================================

  @BeforeClass
  public static void setUpClass() throws Exception {
    System.out.println( "Repository: "
        + UIEERepositoryDirectoryIT.class.getClassLoader().getResource( "repository.spring.xml" ).getPath() );

    // folder cannot be deleted at teardown shutdown hooks have not yet necessarily completed
    // parent folder must match jcrRepository.homeDir bean property in repository-test-override.spring.xml
    FileUtils.deleteDirectory( new File( "/tmp/jackrabbit-test-TRUNK" ) );
    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_GLOBAL );
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_INHERITABLETHREADLOCAL );
  }

  @Before
  public void setUp() throws Exception {
    this.testContextManager = new TestContextManager( getClass() );
    this.testContextManager.prepareTestInstance( this );

    loginAsRepositoryAdmin();
    SimpleJcrTestUtils.deleteItem( testJcrTemplate, ServerRepositoryPaths.getPentahoRootFolderPath() );
    mp = new MicroPlatform();
    // used by DefaultPentahoJackrabbitAccessControlHelper
    mp.defineInstance( "tenantedUserNameUtils", userNameUtils );
    mp.defineInstance( "tenantedRoleNameUtils", roleNameUtils );
    mp.defineInstance( IAuthorizationPolicy.class, authorizationPolicy );
    mp.defineInstance( ITenantManager.class, tenantManager );
    mp.defineInstance( "roleAuthorizationPolicyRoleBindingDaoTarget", roleBindingDaoTarget );
    mp.defineInstance( "repositoryAdminUsername", repositoryAdminUsername );
    mp.defineInstance( "RepositoryFileProxyFactory",
        new RepositoryFileProxyFactory( testJcrTemplate, repositoryFileDao ) );
    mp.defineInstance( "useMultiByteEncoding", new Boolean( false ) );
    mp.defineInstance( IAclService.class, new Boolean( false ) );

    // Start the micro-platform
    mp.start();
    loginAsRepositoryAdmin();
    setAclManagement();
    systemTenant =
        tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), singleTenantAdminRoleName,
            tenantAuthenticatedRoleName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[] { singleTenantAdminRoleName } );
    logout();

    super.setUp();

    KettleEnvironment.init();

    // programmatically register plugins, annotation based plugins do not get loaded unless
    // they are in kettle's plugins folder.
    JobEntryPluginType.getInstance().registerCustom( JobEntryAttributeTesterJobEntry.class, "test",
        "JobEntryAttributeTester", "JobEntryAttributeTester", "JobEntryAttributeTester", "" );
    StepPluginType.getInstance().registerCustom( TransStepAttributeTesterTransStep.class, "test",
        "StepAttributeTester", "StepAttributeTester", "StepAttributeTester", "" );

    repositoryMeta = new PurRepositoryMeta();
    repositoryMeta.setName( "JackRabbit" );
    repositoryMeta.setDescription( "JackRabbit test repository" );
    userInfo = new UserInfo( EXP_LOGIN, "password", EXP_USERNAME, "Apache Tomcat user", true );

    repository = new PurRepository();
    repository.init( repositoryMeta );

    login( sysAdminUserName, systemTenant, new String[] { singleTenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, EXP_TENANT, singleTenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, EXP_LOGIN, "password", "", new String[] { singleTenantAdminRoleName } );
    logout();

    setUpUser();

    PurRepository purRep = (PurRepository) repository;
    final PurRepositoryConnector purRepositoryConnector =
        new PurRepositoryConnector( purRep, (PurRepositoryMeta) repositoryMeta, purRep.getRootRef() );
    purRep.setPurRepositoryConnector( purRepositoryConnector );
    purRep.setTest( repo );
    repository.connect( EXP_LOGIN, "password" );
    login( EXP_LOGIN, tenantAcme, new String[] { singleTenantAdminRoleName, tenantAuthenticatedRoleName } );

    System.out.println( "PUR NAME!!!: " + repo.getClass().getCanonicalName() );
    RepositoryFile repositoryFile = repo.getFile( ClientRepositoryPaths.getPublicFolderPath() );
    Serializable repositoryFileId = repositoryFile.getId();
    List<RepositoryFile> files = repo.getChildren( repositoryFileId );
    StringBuilder buf = new StringBuilder();
    for ( RepositoryFile file : files ) {
      buf.append( "\n" ).append( file );
    }
    assertTrue( "files not deleted: " + buf, files.isEmpty() );
  }

  private void setAclManagement() {
    testJcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( Session session ) throws IOException, RepositoryException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        Workspace workspace = session.getWorkspace();
        PrivilegeManager privilegeManager = ( (JackrabbitWorkspace) workspace ).getPrivilegeManager();
        try {
          privilegeManager.getPrivilege( pentahoJcrConstants.getPHO_ACLMANAGEMENT_PRIVILEGE() );
        } catch ( AccessControlException ace ) {
          privilegeManager.registerPrivilege( pentahoJcrConstants.getPHO_ACLMANAGEMENT_PRIVILEGE(), false,
              new String[0] );
        }
        session.save();
        return null;
      }
    } );
  }

  private void setUpUser() {
    StandaloneSession pentahoSession = new StandaloneSession( userInfo.getLogin() );
    pentahoSession.setAuthenticated( userInfo.getLogin() );
    pentahoSession.setAttribute( IPentahoSession.TENANT_ID_KEY, "/pentaho/" + EXP_TENANT );
    List<GrantedAuthority> authorities = new ArrayList<>( 2 );
    authorities.add( new SimpleGrantedAuthority( "Authenticated" ) );
    authorities.add( new SimpleGrantedAuthority( "acme_Authenticated" ) );
    final String password = "ignored"; //$NON-NLS-1$
    UserDetails userDetails = new User( userInfo.getLogin(), password, true, true, true, true, authorities );
    Authentication authentication = new UsernamePasswordAuthenticationToken( userDetails, password, authorities );
    // next line is copy of SecurityHelper.setPrincipal
    pentahoSession.setAttribute( "SECURITY_PRINCIPAL", authentication );
    PentahoSessionHolder.setSession( pentahoSession );
    SecurityContextHolder.setStrategyName( SecurityContextHolder.MODE_GLOBAL );
    SecurityContextHolder.getContext().setAuthentication( authentication );
    repositoryLifecyleManager.newTenant();
    repositoryLifecyleManager.newUser();
  }

  private void loginAsRepositoryAdmin() {
    StandaloneSession pentahoSession = new StandaloneSession( repositoryAdminUsername );
    pentahoSession.setAuthenticated( repositoryAdminUsername );
    List<GrantedAuthority> repositoryAdminAuthorities = new ArrayList<>();
    repositoryAdminAuthorities.add( new SimpleGrantedAuthority( superAdminRoleName ) );
    final String password = "ignored";
    UserDetails repositoryAdminUserDetails =
        new User( repositoryAdminUsername, password, true, true, true, true, repositoryAdminAuthorities );
    Authentication repositoryAdminAuthentication =
        new UsernamePasswordAuthenticationToken( repositoryAdminUserDetails, password, repositoryAdminAuthorities );
    PentahoSessionHolder.setSession( pentahoSession );
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication( repositoryAdminAuthentication );
  }

  private void logout() {
    PentahoSessionHolder.removeSession();
    SecurityContextHolder.getContext().setAuthentication( null );
  }

  /**
   * Logs in with given username.
   * 
   * @param username
   *          username of user
   * @param tenantId
   *          tenant to which this user belongs
   * @tenantAdmin true to add the tenant admin authority to the user's roles
   */
  private void login( final String username, final ITenant tenant, String[] roles ) {
    StandaloneSession pentahoSession = new StandaloneSession( username );
    pentahoSession.setAuthenticated( tenant.getId(), username );
    PentahoSessionHolder.setSession( pentahoSession );
    pentahoSession.setAttribute( IPentahoSession.TENANT_ID_KEY, tenant.getId() );
    final String password = "password";

    List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

    if ( roles != null ) {
      for ( String roleName : roles ) {
        authorities.add( new SimpleGrantedAuthority( roleName ) );
      }
    }
    UserDetails userDetails = new User( username, password, true, true, true, true, authorities );
    Authentication auth = new UsernamePasswordAuthenticationToken( userDetails, password, authorities );
    PentahoSessionHolder.setSession( pentahoSession );
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication( auth );

    createUserHomeFolder( tenant, username );
  }

  private ITenant getTenant( String principalId, boolean isUser ) {
    ITenant tenant = null;
    ITenantedPrincipleNameResolver nameUtils = isUser ? userNameUtils : roleNameUtils;
    if ( nameUtils != null ) {
      tenant = nameUtils.getTenant( principalId );
    }
    if ( tenant == null || tenant.getId() == null ) {
      tenant = getCurrentTenant();
    }
    return tenant;
  }

  private ITenant getCurrentTenant() {
    if ( PentahoSessionHolder.getSession() != null ) {
      String tenantId = (String) PentahoSessionHolder.getSession().getAttribute( IPentahoSession.TENANT_ID_KEY );
      return tenantId != null ? new Tenant( tenantId, true ) : null;
    } else {
      return null;
    }
  }

  private String getPrincipalName( String principalId, boolean isUser ) {
    String principalName = null;
    ITenantedPrincipleNameResolver nameUtils = isUser ? userNameUtils : roleNameUtils;
    if ( nameUtils != null ) {
      principalName = nameUtils.getPrincipleName( principalId );
    }
    return principalName;
  }

  private void createUserHomeFolder( final ITenant theTenant, final String theUsername ) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    Authentication origAuthentication = SecurityContextHolder.getContext().getAuthentication();
    StandaloneSession pentahoSession = new StandaloneSession( repositoryAdminUsername );
    pentahoSession.setAuthenticated( null, repositoryAdminUsername );
    PentahoSessionHolder.setSession( pentahoSession );
    try {
      txnTemplate.execute( new TransactionCallbackWithoutResult() {
        public void doInTransactionWithoutResult( final TransactionStatus status ) {
          Builder aclsForUserHomeFolder = null;
          Builder aclsForTenantHomeFolder = null;
          ITenant tenant = null;
          String username = null;
          if ( theTenant == null ) {
            tenant = getTenant( username, true );
            username = getPrincipalName( theUsername, true );
          } else {
            tenant = theTenant;
            username = theUsername;
          }
          if ( tenant == null || tenant.getId() == null ) {
            tenant = getCurrentTenant();
          }
          if ( tenant == null || tenant.getId() == null ) {
            tenant = JcrTenantUtils.getDefaultTenant();
          }
          RepositoryFile userHomeFolder = null;
          String userId = userNameUtils.getPrincipleId( theTenant, username );
          final RepositoryFileSid userSid = new RepositoryFileSid( userId );
          RepositoryFile tenantHomeFolder = null;
          RepositoryFile tenantRootFolder = null;
          // Get the Tenant Root folder. If the Tenant Root folder does not exist then exit.
          tenantRootFolder =
              repositoryFileDao.getFileByAbsolutePath( ServerRepositoryPaths.getTenantRootFolderPath( theTenant ) );
          if ( tenantRootFolder != null ) {
            // Try to see if Tenant Home folder exist
            tenantHomeFolder =
                repositoryFileDao.getFileByAbsolutePath( ServerRepositoryPaths.getTenantHomeFolderPath( theTenant ) );
            if ( tenantHomeFolder == null ) {
              String ownerId = userNameUtils.getPrincipleId( theTenant, username );
              RepositoryFileSid ownerSid = new RepositoryFileSid( ownerId, Type.USER );

              String tenantAuthenticatedRoleId = roleNameUtils.getPrincipleId( theTenant, tenantAuthenticatedRoleName );
              RepositoryFileSid tenantAuthenticatedRoleSid =
                  new RepositoryFileSid( tenantAuthenticatedRoleId, Type.ROLE );

              aclsForTenantHomeFolder =
                  new RepositoryFileAcl.Builder( userSid ).ace( tenantAuthenticatedRoleSid, EnumSet
                      .of( RepositoryFilePermission.READ ) );

              aclsForUserHomeFolder =
                  new RepositoryFileAcl.Builder( userSid ).ace( ownerSid, EnumSet.of( RepositoryFilePermission.ALL ) );
              tenantHomeFolder =
                  repositoryFileDao.createFolder( tenantRootFolder.getId(), new RepositoryFile.Builder(
                      ServerRepositoryPaths.getTenantHomeFolderName() ).folder( true ).build(), aclsForTenantHomeFolder
                      .build(), "tenant home folder" );
            } else {
              String ownerId = userNameUtils.getPrincipleId( theTenant, username );
              RepositoryFileSid ownerSid = new RepositoryFileSid( ownerId, Type.USER );
              aclsForUserHomeFolder =
                  new RepositoryFileAcl.Builder( userSid ).ace( ownerSid, EnumSet.of( RepositoryFilePermission.ALL ) );
            }

            // now check if user's home folder exist
            userHomeFolder =
                repositoryFileDao.getFileByAbsolutePath( ServerRepositoryPaths.getUserHomeFolderPath( theTenant,
                    username ) );
            if ( userHomeFolder == null ) {
              userHomeFolder =
                  repositoryFileDao.createFolder( tenantHomeFolder.getId(), new RepositoryFile.Builder( username )
                      .folder( true ).build(), aclsForUserHomeFolder.build(), "user home folder" ); //$NON-NLS-1$
            }
          }
        }
      } );
    } finally {
      // Switch our identity back to the original user.
      PentahoSessionHolder.setSession( origPentahoSession );
      SecurityContextHolder.getContext().setAuthentication( origAuthentication );
    }
  }

  private void cleanupUserAndRoles( final ITenant tenant ) {
    loginAsRepositoryAdmin();
    for ( IPentahoRole role : testUserRoleDao.getRoles( tenant ) ) {
      testUserRoleDao.deleteRole( role );
    }
    for ( IPentahoUser user : testUserRoleDao.getUsers( tenant ) ) {
      testUserRoleDao.deleteUser( user );
    }
  }

  @After
  public void tearDown() throws Exception {
    // null out fields to get back memory
    authorizationPolicy = null;
    login( sysAdminUserName, systemTenant, new String[] { singleTenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenant =
        tenantManager.getTenant( "/" + ServerRepositoryPaths.getPentahoRootFolderName() + "/" + TENANT_ID_ACME );
    if ( tenant != null ) {
      cleanupUserAndRoles( tenant );
    }
    login( sysAdminUserName, systemTenant, new String[] { singleTenantAdminRoleName, tenantAuthenticatedRoleName } );
    tenant = tenantManager.getTenant( "/" + ServerRepositoryPaths.getPentahoRootFolderName() + "/" + TENANT_ID_DUFF );
    if ( tenant != null ) {
      cleanupUserAndRoles( tenant );
    }
    cleanupUserAndRoles( systemTenant );
    SimpleJcrTestUtils.deleteItem( testJcrTemplate, ServerRepositoryPaths.getPentahoRootFolderPath() );
    logout();

    repositoryAdminUsername = null;
    singleTenantAdminRoleName = null;
    tenantAuthenticatedRoleName = null;
    // roleBindingDao = null;
    authorizationPolicy = null;
    testJcrTemplate = null;

    // null out fields to get back memory
    tenantManager = null;
    repo = null;
    mp.stop();
  }

  @Override
  protected void delete( ObjectId id ) {
    if ( id != null ) {
      repo.deleteFile( id.getId(), true, null );
    }
  }

  /**
   * Tries twice to delete files. By not failing outright on the first pass, we hopefully eliminate files that are
   * holding references to the files we cannot delete.
   */
  protected void safelyDeleteAll( final ObjectId[] ids ) throws Exception {
    Exception firstException = null;

    List<String> frozenIds = new ArrayList<String>();
    for ( ObjectId id : ids ) {
      frozenIds.add( id.getId() );
    }

    List<String> remainingIds = new ArrayList<String>();
    for ( ObjectId id : ids ) {
      remainingIds.add( id.getId() );
    }

    try {
      for ( int i = 0; i < frozenIds.size(); i++ ) {
        repo.deleteFile( frozenIds.get( i ), true, null );
        remainingIds.remove( frozenIds.get( i ) );
      }
    } catch ( Exception e ) {
      e.printStackTrace();
    }

    if ( !remainingIds.isEmpty() ) {

      List<String> frozenIds2 = remainingIds;

      List<String> remainingIds2 = new ArrayList<String>();
      for ( String id : frozenIds2 ) {
        remainingIds2.add( id );
      }

      try {
        for ( int i = 0; i < frozenIds2.size(); i++ ) {
          repo.deleteFile( frozenIds2.get( i ), true, null );
          remainingIds2.remove( frozenIds2.get( i ) );
        }
      } catch ( Exception e ) {
        if ( firstException == null ) {
          firstException = e;
        }
      }
      if ( !remainingIds2.isEmpty() ) {
        throw firstException;
      }
    }
  }

  @Override
  public void setApplicationContext( final ApplicationContext applicationContext ) throws BeansException {
    SessionFactory jcrSessionFactory = (SessionFactory) applicationContext.getBean( "jcrSessionFactory" );
    testJcrTemplate = new JcrTemplate( jcrSessionFactory );
    testJcrTemplate.setAllowCreate( true );
    testJcrTemplate.setExposeNativeSession( true );
    repositoryAdminUsername = (String) applicationContext.getBean( "repositoryAdminUsername" );
    superAdminRoleName = (String) applicationContext.getBean( "superAdminAuthorityName" );
    sysAdminUserName = (String) applicationContext.getBean( "superAdminUserName" );
    tenantAuthenticatedRoleName = (String) applicationContext.getBean( "singleTenantAuthenticatedAuthorityName" );
    singleTenantAdminRoleName = (String) applicationContext.getBean( "singleTenantAdminAuthorityName" );
    tenantManager = (ITenantManager) applicationContext.getBean( "tenantMgrProxy" );
    roleBindingDaoTarget =
        (IRoleAuthorizationPolicyRoleBindingDao) applicationContext
            .getBean( "roleAuthorizationPolicyRoleBindingDaoTarget" );
    authorizationPolicy = (IAuthorizationPolicy) applicationContext.getBean( "authorizationPolicy" );
    repo = (IUnifiedRepository) applicationContext.getBean( "unifiedRepository" );
    userRoleDao = (IUserRoleDao) applicationContext.getBean( "userRoleDao" );
    repositoryFileDao = (IRepositoryFileDao) applicationContext.getBean( "repositoryFileDao" );
    testUserRoleDao = userRoleDao;
    repositoryLifecyleManager =
        (IBackingRepositoryLifecycleManager) applicationContext.getBean( "defaultBackingRepositoryLifecycleManager" );
    txnTemplate = (TransactionTemplate) applicationContext.getBean( "jcrTransactionTemplate" );
    TestPrincipalProvider.userRoleDao = testUserRoleDao;
    TestPrincipalProvider.adminCredentialsStrategy =
        (CredentialsStrategy) applicationContext.getBean( "jcrAdminCredentialsStrategy" );
    TestPrincipalProvider.repository = (Repository) applicationContext.getBean( "jcrRepository" );
  }

  @Override
  protected RepositoryDirectoryInterface loadStartDirectory() throws Exception {
    RepositoryDirectoryInterface rootDir = repository.loadRepositoryDirectoryTree();
    RepositoryDirectoryInterface startDir = rootDir.findDirectory( "public" );
    assertNotNull( startDir );
    return startDir;
  }

  /**
   * Allow PentahoSystem to create this class but it in turn delegates to the authorizationPolicy fetched from Spring's
   * ApplicationContext.
   */
  public static class DelegatingAuthorizationPolicy implements IAuthorizationPolicy {

    public List<String> getAllowedActions( final String actionNamespace ) {
      return authorizationPolicy.getAllowedActions( actionNamespace );
    }

    public boolean isAllowed( final String actionName ) {
      return authorizationPolicy.isAllowed( actionName );
    }

  }

  @Test
  public void testUiDelete() throws Exception {
    RepositoryDirectoryInterface rootDir = repository.loadRepositoryDirectoryTree();
    final String startDirName = "home";
    final String testDirName = "testdir";
    final String startDirPath = "/" + startDirName;
    final String testDirPath = startDirPath + "/" + testDirName;

    RepositoryDirectoryInterface startDir = rootDir.findDirectory( startDirName );
    final RepositoryDirectoryInterface testDirCreated = repository.createRepositoryDirectory( startDir, testDirName );
    assertNotNull( testDirCreated );
    assertNotNull( testDirCreated.getObjectId() );

    rootDir = repository.loadRepositoryDirectoryTree();

    final RepositoryDirectoryInterface startDirFound = repository.findDirectory( startDirPath );
    final RepositoryDirectoryInterface testDirFound = repository.findDirectory( testDirPath );
    Assert.assertNotNull( testDirFound );

    final UIEERepositoryDirectory startDirUi = new UIEERepositoryDirectory( startDirFound, null, repository );
    final UIEERepositoryDirectory testDirUi = new UIEERepositoryDirectory( testDirFound, startDirUi, repository );

    testDirUi.delete( true );
    RepositoryDirectoryInterface testDirFound2 = repository.findDirectory( testDirPath );
    Assert.assertNull( testDirFound2 );
  }

  @Test
  public void testUiDeleteNotEmpty() throws Exception {
    RepositoryDirectoryInterface rootDir = repository.loadRepositoryDirectoryTree();
    final String startDirName = "home";
    final String testDirName = "testdir";
    final String testDir2Name = "testdir2";
    final String startDirPath = "/" + startDirName;
    final String testDirPath = startDirPath + "/" + testDirName;
    final String testDir2Path = testDirPath + "/" + testDir2Name;

    RepositoryDirectoryInterface startDir = rootDir.findDirectory( startDirName );
    final RepositoryDirectoryInterface testDirCreated = repository.createRepositoryDirectory( startDir, testDirName );
    final RepositoryDirectoryInterface testDir2Created =
        repository.createRepositoryDirectory( testDirCreated, testDir2Name );
    assertNotNull( testDirCreated );
    assertNotNull( testDirCreated.getObjectId() );
    assertNotNull( testDir2Created );

    rootDir = repository.loadRepositoryDirectoryTree();
    startDir = rootDir.findDirectory( startDirName );

    final RepositoryDirectoryInterface startDirFound = repository.findDirectory( startDirPath );

    final RepositoryDirectoryInterface testDirFound = repository.findDirectory( testDirPath );
    Assert.assertNotNull( testDirFound );

    final RepositoryDirectoryInterface testDir2Found = repository.findDirectory( testDir2Path );
    Assert.assertNotNull( testDir2Found );

    final UIEERepositoryDirectory startDirUi = new UIEERepositoryDirectory( startDirFound, null, repository );
    final UIEERepositoryDirectory testDirUi = new UIEERepositoryDirectory( testDirFound, startDirUi, repository );

    testDirUi.delete( true );
    RepositoryDirectoryInterface testDirFound2 = repository.findDirectory( testDirPath );
    Assert.assertNull( testDirFound2 );
  }

  @Override
  @Test
  @Ignore
  public void testVarious() throws Exception {
  }

  @Override
  @Test
  @Ignore
  public void testDirectories() throws Exception {
  }

  @Override
  @Test
  @Ignore
  public void testJobs() throws Exception {
  }

  @Override
  @Test
  @Ignore
  public void testTransformations() throws Exception {
  }

  @Override
  @Test
  @Ignore
  public void testPartitionSchemas() throws Exception {
  }

  @Override
  @Test
  @Ignore
  public void testClusterSchemas() throws Exception {
  }

  @Override
  @Test
  @Ignore
  public void testDatabases() throws Exception {
  }

  @Override
  @Test
  @Ignore
  public void testSlaves() throws Exception {
  }

  @Override
  @Test
  @Ignore
  public void testRenameAndUndelete() throws Exception {
  }

  @Override
  @Test
  @Ignore
  public void testVersions() throws Exception {
  }

  @Override
  @Test
  @Ignore
  public void testGetSecurityProvider() throws Exception {
  }

  @Override
  @Test
  @Ignore
  public void testGetVersionRegistry() throws Exception {
  }

  @Override
  @Test
  @Ignore
  public void testInsertJobEntryDatabase() throws Exception {
  }

  @Override
  @Test
  @Ignore
  public void testInsertLogEntry() throws Exception {
  }

  @Override
  @Test
  @Ignore
  public void testInsertStepDatabase() throws Exception {
  }

  @Override
  @Test
  @Ignore
  public void testLoadConditionFromStepAttribute() throws Exception {
  }

  @Override
  @Test
  @Ignore
  public void testLoadDatabaseMetaFromJobEntryAttribute() throws Exception {
  }

  @Override
  @Test
  @Ignore
  public void testLoadDatabaseMetaFromStepAttribute() throws Exception {
  }

  @Override
  @Test
  @Ignore
  public void testReadJobMetaSharedObjects() throws Exception {
  }

  @Override
  @Test
  @Ignore
  public void testReadTransSharedObjects() throws Exception {
  }

  @Override
  @Test
  @Ignore
  public void testSaveConditionStepAttribute() throws Exception {
  }

  @Override
  @Test
  @Ignore
  public void testGetAcl() throws Exception {
  }

  @Override
  @Test
  @Ignore
  public void testSetAcl() throws Exception {
  }

  @Override
  @Test
  @Ignore
  public void testSaveDatabaseMetaJobEntryAttribute() throws Exception {
  }

  @Override
  @Test
  @Ignore
  public void testSaveDatabaseMetaStepAttribute() throws Exception {
  }

}
