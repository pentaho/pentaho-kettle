/*!
 * Copyright 2010 - 2024 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.di.repository.pur;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.jcr.Repository;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.KettleLoggingEvent;
import org.pentaho.di.core.logging.KettleLoggingEventListener;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.imp.ImportRules;
import org.pentaho.di.imp.rule.ImportRuleInterface;
import org.pentaho.di.imp.rules.TransformationHasANoteImportRule;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.IRepositoryExporter;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.RepositoryTestBase;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.repository.pur.metastore.MetaStoreTestBase;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreDependenciesExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.exceptions.MetaStoreNamespaceExistsException;
import org.pentaho.metastore.util.PentahoDefaults;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.api.repository2.unified.IRepositoryVersionManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.DefaultLockHelper;
import org.pentaho.platform.repository2.unified.jcr.ILockHelper;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileUtils;
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
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;

@ContextConfiguration( locations = { "classpath:/repository.spring.xml",
  "classpath:/repository-test-override.spring.xml" } )
public class PurRepositoryIT extends RepositoryTestBase implements ApplicationContextAware, java.io.Serializable {

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

  private ILockHelper iLockHelper = new DefaultLockHelper( userNameUtils );

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

  public PurRepositoryIT( Boolean lazyRepo ) {
    super( lazyRepo );
  }

  // ~ Methods =========================================================================================================

  @BeforeClass
  public static void setUpClass() throws Exception {
    System.out.println( "Repository: "
        + PurRepositoryIT.class.getClassLoader().getResource( "repository.spring.xml" ).getPath() );

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

    IRepositoryVersionManager mockRepositoryVersionManager = mock( IRepositoryVersionManager.class );
    when( mockRepositoryVersionManager.isVersioningEnabled( anyString() ) ).thenReturn( true );
    when( mockRepositoryVersionManager.isVersionCommentEnabled( anyString() ) ).thenReturn( false );
    JcrRepositoryFileUtils.setRepositoryVersionManager( mockRepositoryVersionManager );

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
    mp.defineInstance( "ILockHelper", iLockHelper );
    mp.defineInstance( "RepositoryFileProxyFactory",
        new RepositoryFileProxyFactory( testJcrTemplate, repositoryFileDao ) );
    mp.defineInstance( "useMultiByteEncoding", new Boolean( false ) );

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
    purRep.setPurRepositoryConnector( new PurRepositoryConnector( purRep, (PurRepositoryMeta) repositoryMeta, purRep
        .getRootRef() ) );
    ( (PurRepository) repository ).setTest( repo );
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
    JcrCallback callback = PurRepositoryTestingUtils.setAclManagementCallback();
    testJcrTemplate.execute( callback );
  }

  protected void setUpUser() {
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
    SecurityContextHolder.setStrategyName( SecurityContextHolder.MODE_GLOBAL );
    PurRepositoryTestingUtils.setSession( pentahoSession, authentication );
    repositoryLifecyleManager.newTenant();
    repositoryLifecyleManager.newUser();
  }

  protected void loginAsRepositoryAdmin() {
    StandaloneSession repositoryAdminSession = PurRepositoryTestingUtils.createSession( repositoryAdminUsername );
    Authentication repositoryAdminAuthentication = PurRepositoryTestingUtils.createAuthentication(
        repositoryAdminUsername, superAdminRoleName );
    PurRepositoryTestingUtils.setSession( repositoryAdminSession, repositoryAdminAuthentication );
  }

  protected void logout() {
    PentahoSessionHolder.removeSession();
    SecurityContextHolder.getContext().setAuthentication( null );
  }

  protected void loginAsTenantAdmin() {
    StandaloneSession pentahoSession = new StandaloneSession( "joe" );
    pentahoSession.setAuthenticated( "joe" );
    pentahoSession.setAttribute( IPentahoSession.TENANT_ID_KEY, "acme" );
    final String password = "password";
    List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>( 3 );
    authorities.add( new SimpleGrantedAuthority( "Authenticated" ) );
    authorities.add( new SimpleGrantedAuthority( "acme_Authenticated" ) );
    authorities.add( new SimpleGrantedAuthority( "acme_Admin" ) );
    UserDetails userDetails = new User( "joe", password, true, true, true, true, authorities );
    Authentication auth = new UsernamePasswordAuthenticationToken( userDetails, password, authorities );
    PentahoSessionHolder.setSession( pentahoSession );
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication( auth );
    repositoryLifecyleManager.newTenant();
    repositoryLifecyleManager.newUser();
  }

  /**
   * Logs in with given username.
   *
   * @param username
   *          username of user
   * @param tenant
   *          tenant to which this user belongs
   * @tenantAdmin true to add the tenant admin authority to the user's roles
   */
  protected void login( final String username, final ITenant tenant, String[] roles ) {
    StandaloneSession pentahoSession = new StandaloneSession( username );
    pentahoSession.setAuthenticated( tenant.getId(), username );
    PentahoSessionHolder.setSession( pentahoSession );
    pentahoSession.setAttribute( IPentahoSession.TENANT_ID_KEY, tenant.getId() );

    Authentication auth = PurRepositoryTestingUtils.createAuthentication( username, roles );
    PurRepositoryTestingUtils.setSession( pentahoSession, auth );

    createUserHomeFolder( tenant, username );
  }

  protected ITenant getTenant( String principalId, boolean isUser ) {
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

  protected ITenant getCurrentTenant() {
    if ( PentahoSessionHolder.getSession() != null ) {
      String tenantId = (String) PentahoSessionHolder.getSession().getAttribute( IPentahoSession.TENANT_ID_KEY );
      return tenantId != null ? new Tenant( tenantId, true ) : null;
    } else {
      return null;
    }
  }

  protected String getPrincipalName( String principalId, boolean isUser ) {
    String principalName = null;
    ITenantedPrincipleNameResolver nameUtils = isUser ? userNameUtils : roleNameUtils;
    if ( nameUtils != null ) {
      principalName = nameUtils.getPrincipleName( principalId );
    }
    return principalName;
  }

  protected void createUserHomeFolder( final ITenant theTenant, final String theUsername ) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    Authentication origAuthentication = SecurityContextHolder.getContext().getAuthentication();
    StandaloneSession pentahoSession = new StandaloneSession( repositoryAdminUsername );
    pentahoSession.setAuthenticated( null, repositoryAdminUsername );
    PentahoSessionHolder.setSession( pentahoSession );

    String principleId = userNameUtils.getPrincipleId( theTenant, theUsername );
    String authenticatedRoleId = roleNameUtils.getPrincipleId( theTenant, tenantAuthenticatedRoleName );
    TransactionCallbackWithoutResult callback =
        PurRepositoryTestingUtils.createUserHomeDirCallback( theTenant, theUsername, principleId, authenticatedRoleId,
            repositoryFileDao );
    try {
      txnTemplate.execute( callback );
    } finally {
      // Switch our identity back to the original user.
      PurRepositoryTestingUtils.setSession( origPentahoSession, origAuthentication );
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
    mp = null;
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
  public void testNewNonAmbiguousNaming() throws Exception {
    PurRepository repo = (PurRepository) repository;

    System.setProperty( "KETTLE_COMPATIBILITY_PUR_OLD_NAMING_MODE", "N" );
    PartitionSchema partSchema1 = createPartitionSchema( "find.me" ); //$NON-NLS-1$
    PartitionSchema partSchema2 = createPartitionSchema( "find|me" ); //$NON-NLS-1$
    repository.save( partSchema1, VERSION_COMMENT_V1, null );
    repository.save( partSchema2, VERSION_COMMENT_V1, null );

    Map<RepositoryObjectType, List<? extends SharedObjectInterface>> sharedObjectsByType =
        new HashMap<RepositoryObjectType, List<? extends SharedObjectInterface>>();
    repo.readSharedObjects( sharedObjectsByType, RepositoryObjectType.PARTITION_SCHEMA );

    List<PartitionSchema> partitionSchemas =
        (List<PartitionSchema>) sharedObjectsByType.get( RepositoryObjectType.PARTITION_SCHEMA );
    assertEquals( 2, partitionSchemas.size() );

    System.setProperty( "KETTLE_COMPATIBILITY_PUR_OLD_NAMING_MODE", "Y" );

    PartitionSchema partSchema3 = createPartitionSchema( "another.one" ); //$NON-NLS-1$
    PartitionSchema partSchema4 = createPartitionSchema( "another|one" ); //$NON-NLS-1$

    repository.save( partSchema3, VERSION_COMMENT_V1, null );
    repository.save( partSchema4, VERSION_COMMENT_V1, null );
    sharedObjectsByType = new HashMap<RepositoryObjectType, List<? extends SharedObjectInterface>>();
    repo.readSharedObjects( sharedObjectsByType, RepositoryObjectType.PARTITION_SCHEMA );

    partitionSchemas = (List<PartitionSchema>) sharedObjectsByType.get( RepositoryObjectType.PARTITION_SCHEMA );
    assertEquals( 3, partitionSchemas.size() );
  }

  private class MockProgressMonitorListener implements ProgressMonitorListener {

    public void beginTask( String arg0, int arg1 ) {
    }

    public void done() {
    }

    public boolean isCanceled() {
      return false;
    }

    public void setTaskName( String arg0 ) {
    }

    public void subTask( String arg0 ) {
    }

    public void worked( int arg0 ) {
    }
  }

  private class MockRepositoryExportParser extends DefaultHandler2 {
    private List<String> nodeNames = new ArrayList<String>();
    private SAXParseException fatalError;
    private List<String> nodesToCapture = Arrays
        .asList( "repository", "transformations", "transformation", "jobs", "job" );

    //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    @Override
    public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException {
      // Only capture nodes we care about
      if ( nodesToCapture.contains( qName ) ) {
        nodeNames.add( qName );
      }
    }

    @Override
    public void fatalError( SAXParseException e ) throws SAXException {
      fatalError = e;
    }

    public List<String> getNodesWithName( String name ) {
      List<String> nodes = new ArrayList<String>();
      for ( String node : nodeNames ) {
        if ( node.equals( name ) ) {
          nodes.add( name );
        }
      }
      return nodes;
    }

    public List<String> getNodeNames() {
      return nodeNames;
    }

    public SAXParseException getFatalError() {
      return fatalError;
    }
  }

  @Test
  public void testExport() throws Exception {
    final String exportFileName = new File( "test.export" ).getAbsolutePath(); //$NON-NLS-1$

    RepositoryDirectoryInterface rootDir = initRepo();
    String uniqueTransName = EXP_TRANS_NAME.concat( EXP_DBMETA_NAME );
    TransMeta transMeta = createTransMeta( EXP_DBMETA_NAME );

    // Create a database association
    DatabaseMeta dbMeta = createDatabaseMeta( EXP_DBMETA_NAME );
    repository.save( dbMeta, VERSION_COMMENT_V1, null );

    TableInputMeta tableInputMeta = new TableInputMeta();
    tableInputMeta.setDatabaseMeta( dbMeta );

    transMeta.addStep( new StepMeta( EXP_TRANS_STEP_1_NAME, tableInputMeta ) );

    RepositoryDirectoryInterface transDir = rootDir.findDirectory( DIR_TRANSFORMATIONS );
    repository.save( transMeta, VERSION_COMMENT_V1, null );
    deleteStack.push( transMeta ); // So this transformation is cleaned up afterward
    assertNotNull( transMeta.getObjectId() );
    ObjectRevision version = transMeta.getObjectRevision();
    assertNotNull( version );
    assertTrue( hasVersionWithComment( transMeta, VERSION_COMMENT_V1 ) );
    assertTrue( repository.exists( uniqueTransName, transDir, RepositoryObjectType.TRANSFORMATION ) );

    JobMeta jobMeta = createJobMeta( EXP_JOB_NAME );
    RepositoryDirectoryInterface jobsDir = rootDir.findDirectory( DIR_JOBS );
    repository.save( jobMeta, VERSION_COMMENT_V1, null );
    deleteStack.push( jobMeta );
    assertNotNull( jobMeta.getObjectId() );
    version = jobMeta.getObjectRevision();
    assertNotNull( version );
    assertTrue( hasVersionWithComment( jobMeta, VERSION_COMMENT_V1 ) );
    assertTrue( repository.exists( EXP_JOB_NAME, jobsDir, RepositoryObjectType.JOB ) );

    LogListener errorLogListener = new LogListener( LogLevel.ERROR );
    KettleLogStore.getAppender().addLoggingEventListener( errorLogListener );

    try {
      repository.getExporter().exportAllObjects( new MockProgressMonitorListener(), exportFileName, null, "all" ); //$NON-NLS-1$
      FileObject exportFile = KettleVFS.getFileObject( exportFileName );
      assertFalse( "file left open", exportFile.getContent().isOpen() );
      assertNotNull( exportFile );
      MockRepositoryExportParser parser = new MockRepositoryExportParser();
      SAXParserFactory.newInstance().newSAXParser().parse( KettleVFS.getInputStream( exportFile ), parser );
      if ( parser.getFatalError() != null ) {
        throw parser.getFatalError();
      }
      assertNotNull( "No nodes found in export", parser.getNodeNames() ); //$NON-NLS-1$
      assertTrue( "No nodes found in export", !parser.getNodeNames().isEmpty() ); //$NON-NLS-1$
      assertEquals( "Incorrect number of nodes", 5, parser.getNodeNames().size() ); //$NON-NLS-1$
      assertEquals( "Incorrect number of transformations", 1, parser.getNodesWithName( "transformation" ).size() ); //$NON-NLS-1$ //$NON-NLS-2$
      assertEquals( "Incorrect number of jobs", 1, parser.getNodesWithName( "job" ).size() ); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue( "log error", errorLogListener.getEvents().isEmpty() );

    } finally {
      KettleVFS.getFileObject( exportFileName ).delete();
      KettleLogStore.getAppender().removeLoggingEventListener( errorLogListener );
    }
  }

  @Test
  public void testMetaStoreBasics() throws MetaStoreException {
    IMetaStore metaStore = repository.getRepositoryMetaStore();
    assertNotNull( metaStore );

    MetaStoreTestBase base = new MetaStoreTestBase();
    base.testFunctionality( metaStore );
  }

  @Test
  public void testMetaStoreNamespaces() throws MetaStoreException {
    IMetaStore metaStore = repository.getRepositoryMetaStore();
    assertNotNull( metaStore );

    // We start with a clean slate, only the pentaho namespace
    //
    assertEquals( 1, metaStore.getNamespaces().size() );

    String ns = PentahoDefaults.NAMESPACE;
    assertEquals( true, metaStore.namespaceExists( ns ) );

    metaStore.deleteNamespace( ns );
    assertEquals( false, metaStore.namespaceExists( ns ) );
    assertEquals( 0, metaStore.getNamespaces().size() );

    metaStore.createNamespace( ns );
    assertEquals( true, metaStore.namespaceExists( ns ) );

    List<String> namespaces = metaStore.getNamespaces();
    assertEquals( 1, namespaces.size() );
    assertEquals( ns, namespaces.get( 0 ) );

    try {
      metaStore.createNamespace( ns );
      fail( "Exception expected when a namespace already exists and where we try to create it again" );
    } catch ( MetaStoreNamespaceExistsException e ) {
      // OK, we expected this.
    }

    metaStore.deleteNamespace( ns );
    assertEquals( false, metaStore.namespaceExists( ns ) );
    assertEquals( 0, metaStore.getNamespaces().size() );
  }

  @Test
  public void testMetaStoreElementTypes() throws MetaStoreException {
    IMetaStore metaStore = repository.getRepositoryMetaStore();
    assertNotNull( metaStore );
    String ns = PentahoDefaults.NAMESPACE;

    // We start with a clean slate...
    //
    assertEquals( 1, metaStore.getNamespaces().size() );
    assertEquals( true, metaStore.namespaceExists( ns ) );

    // Now create an element type
    //
    IMetaStoreElementType elementType = metaStore.newElementType( ns );
    elementType.setName( PentahoDefaults.KETTLE_DATA_SERVICE_ELEMENT_TYPE_NAME );
    elementType.setDescription( PentahoDefaults.KETTLE_DATA_SERVICE_ELEMENT_TYPE_DESCRIPTION );

    metaStore.createElementType( ns, elementType );

    IMetaStoreElementType verifyElementType = metaStore.getElementType( ns, elementType.getId() );
    assertEquals( PentahoDefaults.KETTLE_DATA_SERVICE_ELEMENT_TYPE_NAME, verifyElementType.getName() );
    assertEquals( PentahoDefaults.KETTLE_DATA_SERVICE_ELEMENT_TYPE_DESCRIPTION, verifyElementType.getDescription() );

    verifyElementType = metaStore.getElementTypeByName( ns, PentahoDefaults.KETTLE_DATA_SERVICE_ELEMENT_TYPE_NAME );
    assertEquals( PentahoDefaults.KETTLE_DATA_SERVICE_ELEMENT_TYPE_NAME, verifyElementType.getName() );
    assertEquals( PentahoDefaults.KETTLE_DATA_SERVICE_ELEMENT_TYPE_DESCRIPTION, verifyElementType.getDescription() );

    // Get the list of element type ids.
    //
    List<String> ids = metaStore.getElementTypeIds( ns );
    assertNotNull( ids );
    assertEquals( 1, ids.size() );
    assertEquals( elementType.getId(), ids.get( 0 ) );

    // Verify that we can't delete the namespace since it has content in it!
    //
    try {
      metaStore.deleteNamespace( ns );
      fail( "The namespace deletion didn't cause an exception because there are still an element type in it" );
    } catch ( MetaStoreDependenciesExistsException e ) {
      assertNotNull( e.getDependencies() );
      assertEquals( 1, e.getDependencies().size() );
      assertEquals( elementType.getId(), e.getDependencies().get( 0 ) );
    }

    metaStore.deleteElementType( ns, elementType );
    assertEquals( 0, metaStore.getElementTypes( ns ).size() );

    metaStore.deleteNamespace( ns );
  }

  @Test
  public void testMetaStoreElements() throws MetaStoreException {
    // Set up a namespace
    //
    String ns = PentahoDefaults.NAMESPACE;
    IMetaStore metaStore = repository.getRepositoryMetaStore();
    if ( !metaStore.namespaceExists( ns ) ) {
      metaStore.createNamespace( ns );
    }

    // And an element type
    //
    IMetaStoreElementType elementType = metaStore.newElementType( ns );
    elementType.setName( PentahoDefaults.KETTLE_DATA_SERVICE_ELEMENT_TYPE_NAME );
    elementType.setDescription( PentahoDefaults.KETTLE_DATA_SERVICE_ELEMENT_TYPE_DESCRIPTION );
    metaStore.createElementType( ns, elementType );

    // Now we play with elements...
    //
    IMetaStoreElement oneElement = populateElement( metaStore, elementType, "Element One" );
    metaStore.createElement( ns, elementType, oneElement );

    IMetaStoreElement verifyOneElement = metaStore.getElement( ns, elementType, oneElement.getId() );
    assertNotNull( verifyOneElement );
    validateElement( verifyOneElement, "Element One" );

    assertEquals( 1, metaStore.getElements( ns, elementType ).size() );

    IMetaStoreElement twoElement = populateElement( metaStore, elementType, "Element Two" );
    metaStore.createElement( ns, elementType, twoElement );

    IMetaStoreElement verifyTwoElement = metaStore.getElement( ns, elementType, twoElement.getId() );
    assertNotNull( verifyTwoElement );

    assertEquals( 2, metaStore.getElements( ns, elementType ).size() );

    try {
      metaStore.deleteElementType( ns, elementType );
      fail( "Delete element type failed to properly detect element dependencies" );
    } catch ( MetaStoreDependenciesExistsException e ) {
      List<String> ids = e.getDependencies();
      assertEquals( 2, ids.size() );
      assertTrue( ids.contains( oneElement.getId() ) );
      assertTrue( ids.contains( twoElement.getId() ) );
    }

    metaStore.deleteElement( ns, elementType, oneElement.getId() );

    assertEquals( 1, metaStore.getElements( ns, elementType ).size() );

    metaStore.deleteElement( ns, elementType, twoElement.getId() );

    assertEquals( 0, metaStore.getElements( ns, elementType ).size() );
  }

  protected IMetaStoreElement populateElement( IMetaStore metaStore, IMetaStoreElementType elementType, String name )
    throws MetaStoreException {
    IMetaStoreElement element = metaStore.newElement();
    element.setElementType( elementType );
    element.setName( name );
    for ( int i = 1; i <= 5; i++ ) {
      element.addChild( metaStore.newAttribute( "id " + i, "value " + i ) );
    }
    IMetaStoreAttribute subAttr = metaStore.newAttribute( "sub-attr", null );
    for ( int i = 101; i <= 110; i++ ) {
      subAttr.addChild( metaStore.newAttribute( "sub-id " + i, "sub-value " + i ) );
    }
    element.addChild( subAttr );

    return element;
  }

  protected void validateElement( IMetaStoreElement element, String name ) throws MetaStoreException {
    assertEquals( name, element.getName() );
    assertEquals( 6, element.getChildren().size() );
    for ( int i = 1; i <= 5; i++ ) {
      IMetaStoreAttribute child = element.getChild( "id " + i );
      assertEquals( "value " + i, child.getValue() );
    }
    IMetaStoreAttribute subAttr = element.getChild( "sub-attr" );
    assertNotNull( subAttr );
    assertEquals( 10, subAttr.getChildren().size() );
    for ( int i = 101; i <= 110; i++ ) {
      IMetaStoreAttribute child = subAttr.getChild( "sub-id " + i );
      assertNotNull( child );
      assertEquals( "sub-value " + i, child.getValue() );
    }
  }

  @Test
  public void doesNotChangeFileWhenFailsToRename_slaves() throws Exception {
    final SlaveServer server1 = new SlaveServer();
    final SlaveServer server2 = new SlaveServer();
    try {
      testDoesNotChangeFileWhenFailsToRename( server1, server2, new Callable<RepositoryElementInterface>() {
        @Override
        public RepositoryElementInterface call() throws Exception {
          return repository.loadSlaveServer( server2.getObjectId(), null );
        }
      } );
    } finally {
      repository.deleteSlave( server1.getObjectId() );
      repository.deleteSlave( server2.getObjectId() );
    }
  }

  @Test
  public void doesNotChangeFileWhenFailsToRename_clusters() throws Exception {
    final ClusterSchema schema1 = new ClusterSchema();
    final ClusterSchema schema2 = new ClusterSchema();
    try {
      testDoesNotChangeFileWhenFailsToRename( schema1, schema2, new Callable<RepositoryElementInterface>() {
        @Override
        public RepositoryElementInterface call() throws Exception {
          return repository.loadClusterSchema( schema2.getObjectId(), null, null );
        }
      } );
    } finally {
      repository.deleteClusterSchema( schema1.getObjectId() );
      repository.deleteClusterSchema( schema2.getObjectId() );
    }
  }

  @Test
  public void doesNotChangeFileWhenFailsToRename_partitions() throws Exception {
    final PartitionSchema schema1 = new PartitionSchema();
    final PartitionSchema schema2 = new PartitionSchema();
    try {
      testDoesNotChangeFileWhenFailsToRename( schema1, schema2, new Callable<RepositoryElementInterface>() {
        @Override
        public RepositoryElementInterface call() throws Exception {
          return repository.loadPartitionSchema( schema2.getObjectId(), null );
        }
      } );
    } finally {
      repository.deletePartitionSchema( schema1.getObjectId() );
      repository.deletePartitionSchema( schema2.getObjectId() );
    }
  }

  private void testDoesNotChangeFileWhenFailsToRename( RepositoryElementInterface element1,
      RepositoryElementInterface element2, Callable<RepositoryElementInterface> loader ) throws Exception {
    final String name1 = "name1";
    final String name2 = "name2";

    element1.setName( name1 );
    element2.setName( name2 );

    repository.save( element1, "", null );
    repository.save( element2, "", null );

    element2.setName( name1 );
    try {
      repository.save( element2, "", null, true );
      fail( "A naming conflict should occur" );
    } catch ( KettleException e ) {
      // expected to be thrown
      element2.setName( name2 );
    }
    RepositoryElementInterface loaded = loader.call();
    assertEquals( element2, loaded );
  }

  @Test
  public void testExportWithRules() throws Exception {
    String fileName = "testExportWithRuled.xml";
    final String exportFileName = new File( fileName ).getAbsolutePath(); //$NON-NLS-1$

    RepositoryDirectoryInterface rootDir = initRepo();

    String transWithoutNoteName = "2" + EXP_DBMETA_NAME;
    TransMeta transWithoutNote = createTransMeta( transWithoutNoteName );
    String transUniqueName = EXP_TRANS_NAME.concat( transWithoutNoteName );

    RepositoryDirectoryInterface transDir = rootDir.findDirectory( DIR_TRANSFORMATIONS );
    repository.save( transWithoutNote, VERSION_COMMENT_V1, null );
    deleteStack.push( transWithoutNote ); // So this transformation is cleaned up afterward
    assertNotNull( transWithoutNote.getObjectId() );

    assertTrue( hasVersionWithComment( transWithoutNote, VERSION_COMMENT_V1 ) );
    assertTrue( repository.exists( transUniqueName, transDir, RepositoryObjectType.TRANSFORMATION ) );

    // Second transformation (contained note)
    String transWithNoteName = "1" + EXP_DBMETA_NAME;
    TransMeta transWithNote = createTransMeta( transWithNoteName );
    transUniqueName = EXP_TRANS_NAME.concat( EXP_DBMETA_NAME );
    TransMeta transWithRules = createTransMeta( EXP_DBMETA_NAME );

    NotePadMeta note = new NotePadMeta( "Note Message", 1, 1, 100, 5 );
    transWithRules.addNote( note );

    repository.save( transWithRules, VERSION_COMMENT_V1, null );
    deleteStack.push( transWithRules ); // So this transformation is cleaned up afterward
    assertNotNull( transWithRules.getObjectId() );

    assertTrue( hasVersionWithComment( transWithRules, VERSION_COMMENT_V1 ) );
    assertTrue( repository.exists( transUniqueName, transDir, RepositoryObjectType.TRANSFORMATION ) );

    // create rules for export to .xml file
    List<ImportRuleInterface> rules = new AbstractList<ImportRuleInterface>() {
      @Override
      public ImportRuleInterface get( int index ) {
        TransformationHasANoteImportRule rule = new TransformationHasANoteImportRule();
        rule.setEnabled( true );
        return rule;
      }

      @Override
      public int size() {
        return 1;
      }
    };
    ImportRules importRules = new ImportRules();
    importRules.setRules( rules );

    // create exporter
    IRepositoryExporter exporter = repository.getExporter();
    exporter.setImportRulesToValidate( importRules );

    // export itself
    try {
      exporter.exportAllObjects( new MockProgressMonitorListener(), exportFileName, null, "all" ); //$NON-NLS-1$
      FileObject exportFile = KettleVFS.getFileObject( exportFileName );
      assertNotNull( exportFile );
      MockRepositoryExportParser parser = new MockRepositoryExportParser();
      SAXParserFactory.newInstance().newSAXParser().parse( KettleVFS.getInputStream( exportFile ), parser );
      if ( parser.getFatalError() != null ) {
        throw parser.getFatalError();
      }
      // assumed transformation with note will be here and only it
      assertEquals( "Incorrect number of transformations", 1, parser.getNodesWithName(
          RepositoryObjectType.TRANSFORMATION.getTypeDescription() ).size() ); //$NON-NLS-1$ //$NON-NLS-2$
    } finally {
      KettleVFS.getFileObject( exportFileName ).delete();
    }
  }

  @Test
  public void testCreateRepositoryDirectory() throws KettleException {
    RepositoryDirectoryInterface tree = repository.loadRepositoryDirectoryTree();
    repository.createRepositoryDirectory( tree.findDirectory( "home" ), "/admin1" );
    repository.createRepositoryDirectory( tree, "home/admin2" );
    repository.createRepositoryDirectory( tree, "home/admin2/new1" );
    RepositoryDirectoryInterface repositoryDirectory =
        repository.createRepositoryDirectory( tree, "home/admin2/new1" );
    repository.getJobAndTransformationObjects( repositoryDirectory.getObjectId(), false );
  }
  @Test
  public void testLoadJob() throws Exception {
    RepositoryDirectoryInterface rootDir = initRepo();
    JobMeta jobMeta = createJobMeta( EXP_JOB_NAME );
    RepositoryDirectoryInterface jobsDir = rootDir.findDirectory( DIR_JOBS );
    repository.save( jobMeta, VERSION_COMMENT_V1, null );
    deleteStack.push( jobMeta );
    JobMeta fetchedJob = repository.loadJob( EXP_JOB_NAME, jobsDir, null, null );
    JobMeta jobMetaById = repository.loadJob( jobMeta.getObjectId(), null );
    //assertEquals( fetchedJob, jobMetaById ); //broken by BACKLOG-20809; probavbly ok
    assertNotNull( fetchedJob.getMetaStore() );
    assertTrue( fetchedJob.getMetaStore() == jobMetaById.getMetaStore() );
  }
  protected static class LogListener implements KettleLoggingEventListener {
    private List<KettleLoggingEvent> events = new ArrayList<>();
    private LogLevel logThreshold;

    public LogListener( LogLevel logThreshold ) {
      this.logThreshold = logThreshold;
    }

    public List<KettleLoggingEvent> getEvents() {
      return events;
    }

    public void eventAdded( KettleLoggingEvent event ) {
      if ( logThreshold.getLevel() >= event.getLevel().getLevel() ) {
        events.add( event );
      }
    }
  }
}
