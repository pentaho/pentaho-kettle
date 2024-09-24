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

import org.junit.After;
import org.junit.Before;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.RepositoryTestLazySupport;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.IRepositoryVersionManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
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
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.jcr.Repository;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
@ContextConfiguration( locations = { "classpath:/repository.spring.xml",
  "classpath:/repository-test-override.spring.xml", "classpath:/pdi-pur-plugin-test-override.spring.xml" } )
@TestExecutionListeners( listeners = { DependencyInjectionTestExecutionListener.class,
  DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class } )
public abstract class PurRepositoryTestBase extends RepositoryTestLazySupport implements ApplicationContextAware {

  protected static final String TEST_LOGIN = "tester";
  protected static final String TEST_TENANT = "testTenant";

  protected static final String ANONYMOUS_ROLE = "Anonymous";

  // protected fields are useful for heirs
  protected IUnifiedRepository unifiedRepository;
  protected PurRepository purRepository;

  // these values are picked up from the Spring context to be used or injected into the micro platform
  private String superAdminRole;
  private String repositoryAdmin;
  private String systemAdmin;
  private String tenantAdminRole;
  private String tenantAuthenticatedRole;

  private ITenantManager tenantManager;
  private IUserRoleDao userRoleDao;
  private IAuthorizationPolicy authorizationPolicy;
  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDaoTarget;
  private JcrTemplate testJcrTemplate;
  private IRepositoryFileDao repositoryFileDao;
  private TransactionTemplate txnTemplate;

  // these two instances are just injected into the micro platform
  // they do not store anything except their settings, hence can be shared among different executions of tests
  private final ITenantedPrincipleNameResolver userNameUtils = new DefaultTenantedPrincipleNameResolver();
  private final ITenantedPrincipleNameResolver roleNameUtils = new DefaultTenantedPrincipleNameResolver(
      DefaultTenantedPrincipleNameResolver.ALTERNATE_DELIMETER );

  // these objects are created during environment initialisation
  private ITenant systemTenant;
  private ITenant testingTenant;

  // this block stores those values, that should be restored after test's execution
  private MicroPlatform mp;
  private IRepositoryVersionManager existingVersionManager;

  private TestContextManager testContextManager;

  public PurRepositoryTestBase( Boolean lazyRepo ) {
    super( lazyRepo );
  }

  @Before
  public void setUp() throws Exception {
    this.testContextManager = new TestContextManager( getClass() );
    this.testContextManager.prepareTestInstance( this );

    KettleEnvironment.init();
    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_GLOBAL );

    mockVersionManager();
    removePentahoRootFolder();
    startMicroPlatform();

    createSystemUser();
    createTester();

    createPurRepository();
    loginAsTester();
  }

  private void mockVersionManager() {
    existingVersionManager = JcrRepositoryFileUtils.getRepositoryVersionManager();
    IRepositoryVersionManager versionManager = mock( IRepositoryVersionManager.class );
    when( versionManager.isVersioningEnabled( anyString() ) ).thenReturn( true );
    when( versionManager.isVersionCommentEnabled( anyString() ) ).thenReturn( false );
    JcrRepositoryFileUtils.setRepositoryVersionManager( versionManager );
  }

  private void removePentahoRootFolder() {
    loginAsRepositoryAdmin();
    SimpleJcrTestUtils.deleteItem( testJcrTemplate, ServerRepositoryPaths.getPentahoRootFolderPath() );
  }

  protected void loginAsRepositoryAdmin() {
    StandaloneSession repositoryAdminSession = PurRepositoryTestingUtils.createSession( repositoryAdmin );
    Authentication repositoryAdminAuthentication = PurRepositoryTestingUtils.createAuthentication( repositoryAdmin,
        superAdminRole );
    PurRepositoryTestingUtils.setSession( repositoryAdminSession, repositoryAdminAuthentication );
  }

  private void startMicroPlatform() throws Exception {
    mp = new MicroPlatform();
    mp.defineInstance( "tenantedUserNameUtils", userNameUtils );
    mp.defineInstance( "tenantedRoleNameUtils", roleNameUtils );
    mp.defineInstance( IAuthorizationPolicy.class, authorizationPolicy );
    mp.defineInstance( ITenantManager.class, tenantManager );
    mp.defineInstance( "roleAuthorizationPolicyRoleBindingDaoTarget", roleBindingDaoTarget );
    mp.defineInstance( "repositoryAdminUsername", repositoryAdmin );
    mp.defineInstance( "RepositoryFileProxyFactory",
        new RepositoryFileProxyFactory( testJcrTemplate, repositoryFileDao ) );
    mp.defineInstance( "useMultiByteEncoding", Boolean.FALSE );
    initMicroPlatform( mp );
    mp.start();
  }

  protected void initMicroPlatform( MicroPlatform mp ) {
    // override this method to inject your own values into the micro platform
  }

  private void createSystemUser() {
    loginAsRepositoryAdmin();
    setAclManagement();
    systemTenant =
        tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminRole,
            tenantAuthenticatedRole, ANONYMOUS_ROLE );
    userRoleDao.createUser( systemTenant, systemAdmin, "", "", new String[] { tenantAdminRole } );
  }

  private void setAclManagement() {
    testJcrTemplate.execute( PurRepositoryTestingUtils.setAclManagementCallback() );
  }

  private void createTester() {
    loginAsSystemAdmin();
    testingTenant =
        tenantManager
            .createTenant( systemTenant, TEST_TENANT, tenantAdminRole, tenantAuthenticatedRole, ANONYMOUS_ROLE );
    userRoleDao.createUser( testingTenant, TEST_LOGIN, "", "", new String[] { tenantAdminRole } );

    createUserHomeFolder( testingTenant, TEST_LOGIN );
  }

  private void loginAsSystemAdmin() {
    StandaloneSession session = PurRepositoryTestingUtils.createSession( systemTenant, systemAdmin );
    Authentication auth = PurRepositoryTestingUtils.createAuthentication( systemAdmin, tenantAdminRole,
        tenantAuthenticatedRole );
    PurRepositoryTestingUtils.setSession( session, auth );
  }

  private void createUserHomeFolder( final ITenant theTenant, final String theUsername ) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    Authentication origAuthentication = SecurityContextHolder.getContext().getAuthentication();

    String principleId = userNameUtils.getPrincipleId( theTenant, theUsername );
    String authenticatedRoleId = roleNameUtils.getPrincipleId( theTenant, tenantAuthenticatedRole );
    TransactionCallbackWithoutResult callback =
        PurRepositoryTestingUtils.createUserHomeDirCallback( theTenant, theUsername, principleId, authenticatedRoleId,
            repositoryFileDao );
    try {
      loginAsRepositoryAdmin();
      txnTemplate.execute( callback );
    } finally {
      PurRepositoryTestingUtils.setSession( origPentahoSession, origAuthentication );
    }
  }

  protected void createPurRepository() throws KettleException {
    PurRepositoryMeta purMeta = new PurRepositoryMeta();
    purMeta.setName( "JackRabbit" );
    purMeta.setDescription( "Jackrabbit test repository" );

    purRepository = new PurRepository();
    purRepository.init( purMeta );
    purRepository.setTest( unifiedRepository );
    purRepository.setPurRepositoryConnector( new PurRepositoryConnector( purRepository, purMeta, purRepository
        .getRootRef() ) );
    purRepository.connect( TEST_LOGIN, "" );
  }

  private void loginAsTester() throws Exception {
    Authentication authentication =
        PurRepositoryTestingUtils.createAuthentication( TEST_LOGIN, tenantAuthenticatedRole,
            roleNameUtils.getPrincipleId( testingTenant, ANONYMOUS_ROLE ) );

    StandaloneSession session = PurRepositoryTestingUtils.createSession( testingTenant, TEST_LOGIN );
    session.setAttribute( "SECURITY_PRINCIPAL", authentication );

    PurRepositoryTestingUtils.setSession( session, authentication );
  }

  @After
  public void tearDown() throws Exception {
    cleanupUserAndRoles( testingTenant );
    cleanupUserAndRoles( systemTenant );

    unifiedRepository = null;
    purRepository = null;
    superAdminRole = repositoryAdmin = systemAdmin = tenantAdminRole = tenantAuthenticatedRole = null;
    tenantManager = null;
    userRoleDao = null;
    authorizationPolicy = null;
    roleBindingDaoTarget = null;
    testJcrTemplate = null;
    repositoryFileDao = null;
    txnTemplate = null;
    systemTenant = testingTenant = null;

    mp.stop();
    mp = null;

    JcrRepositoryFileUtils.setRepositoryVersionManager( existingVersionManager );
    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_INHERITABLETHREADLOCAL );
  }

  private void cleanupUserAndRoles( final ITenant tenant ) {
    loginAsRepositoryAdmin();
    for ( IPentahoRole role : userRoleDao.getRoles( tenant ) ) {
      userRoleDao.deleteRole( role );
    }
    for ( IPentahoUser user : userRoleDao.getUsers( tenant ) ) {
      userRoleDao.deleteUser( user );
    }
  }

  @Override
  public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
    unifiedRepository = applicationContext.getBean( "unifiedRepository", IUnifiedRepository.class );
    tenantManager = applicationContext.getBean( "tenantMgrProxy", ITenantManager.class );
    userRoleDao = applicationContext.getBean( "userRoleDao", IUserRoleDao.class );
    authorizationPolicy = applicationContext.getBean( "authorizationPolicy", IAuthorizationPolicy.class );
    roleBindingDaoTarget =
        (IRoleAuthorizationPolicyRoleBindingDao) applicationContext
            .getBean( "roleAuthorizationPolicyRoleBindingDaoTarget" );

    SessionFactory jcrSessionFactory = (SessionFactory) applicationContext.getBean( "jcrSessionFactory" );
    testJcrTemplate = new JcrTemplate( jcrSessionFactory );
    testJcrTemplate.setAllowCreate( true );
    testJcrTemplate.setExposeNativeSession( true );
    txnTemplate = applicationContext.getBean( "jcrTransactionTemplate", TransactionTemplate.class );
    repositoryFileDao = (IRepositoryFileDao) applicationContext.getBean( "repositoryFileDao" );

    superAdminRole = applicationContext.getBean( "superAdminAuthorityName", String.class );
    repositoryAdmin = applicationContext.getBean( "repositoryAdminUsername", String.class );
    systemAdmin = (String) applicationContext.getBean( "superAdminUserName" );
    tenantAdminRole = applicationContext.getBean( "singleTenantAdminAuthorityName", String.class );
    tenantAuthenticatedRole = applicationContext.getBean( "singleTenantAuthenticatedAuthorityName", String.class );

    TestPrincipalProvider.userRoleDao = userRoleDao;
    TestPrincipalProvider.adminCredentialsStrategy =
        (CredentialsStrategy) applicationContext.getBean( "jcrAdminCredentialsStrategy" );
    TestPrincipalProvider.repository = (Repository) applicationContext.getBean( "jcrRepository" );

    doSetApplicationContext( applicationContext );
  }

  protected void doSetApplicationContext( ApplicationContext applicationContext ) {
    // override this method to pick up what is needed from the context
  }
}
