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

package org.pentaho.di.ui.repo.util;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.RepositoryMeta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class PurRepositoryUtilsTest {

  @BeforeClass
  public static void setUpClass() throws KettleException {
    KettleClientEnvironment.init();
  }

  // ===== isPurRepository =====

  @Test
  public void isPurRepositoryReturnsTrueForPurRepository() {
    RepositoryMeta meta = mock( RepositoryMeta.class );
    when( meta.getId() ).thenReturn( "PentahoEnterpriseRepository" );

    assertTrue( PurRepositoryUtils.isPurRepository( meta ) );
  }

  @Test
  public void isPurRepositoryReturnsFalseForNonPurRepository() {
    RepositoryMeta meta = mock( RepositoryMeta.class );
    when( meta.getId() ).thenReturn( "SomeOtherRepository" );

    assertFalse( PurRepositoryUtils.isPurRepository( meta ) );
  }

  @Test
  public void isPurRepositoryReturnsFalseForNullMeta() {
    assertFalse( PurRepositoryUtils.isPurRepository( null ) );
  }

  @Test
  public void isPurRepositoryReturnsFalseWhenIdIsNull() {
    RepositoryMeta meta = mock( RepositoryMeta.class );
    when( meta.getId() ).thenReturn( null );

    assertFalse( PurRepositoryUtils.isPurRepository( meta ) );
  }


  @Test
  public void getServerUrlReturnsNullForNullMeta() {
    assertNull( PurRepositoryUtils.getServerUrl( null ) );
  }

  @Test
  public void getServerUrlReturnsUrlFromRepositoryLocation() {
    RepositoryMeta meta = new FakePurRepoMeta( "http://pentaho.example.com:8080/pentaho", "USERNAME_PASSWORD" ) {
    };
    assertEquals( "http://pentaho.example.com:8080/pentaho", PurRepositoryUtils.getServerUrl( meta ) );
  }

  @Test
  public void getServerUrlReturnsNullWhenLocationIsNull() {
    RepositoryMeta meta = new FakePurRepoMeta( null, "USERNAME_PASSWORD" ) {
    };
    assertNull( PurRepositoryUtils.getServerUrl( meta ) );
  }

  // ===== hasServerUrl =====

  @Test
  public void hasServerUrlReturnsTrueWhenUrlIsConfigured() {
    RepositoryMeta meta = new FakePurRepoMeta( "http://server:8080/pentaho", "USERNAME_PASSWORD" ) {
    };
    assertTrue( PurRepositoryUtils.hasServerUrl( meta ) );
  }

  @Test
  public void hasServerUrlReturnsFalseWhenUrlIsNull() {
    RepositoryMeta meta = new FakePurRepoMeta( null, "USERNAME_PASSWORD" ) {
    };
    assertFalse( PurRepositoryUtils.hasServerUrl( meta ) );
  }


  @Test
  public void hasServerUrlReturnsFalseForNullMeta() {
    assertFalse( PurRepositoryUtils.hasServerUrl( null ) );
  }

  // ===== getAuthMethod =====

  @Test
  public void getAuthMethodReturnsSsoWhenConfigured() {
    RepositoryMeta meta = new FakePurRepoMeta( "http://server:8080/pentaho", "SSO" ) {
    };
    assertEquals( "SSO", PurRepositoryUtils.getAuthMethod( meta ) );
  }

  @Test
  public void getAuthMethodReturnsUsernamePasswordWhenConfigured() {
    RepositoryMeta meta = new FakePurRepoMeta( "http://server:8080/pentaho", "USERNAME_PASSWORD" ) {
    };
    assertEquals( "USERNAME_PASSWORD", PurRepositoryUtils.getAuthMethod( meta ) );
  }

  @Test
  public void getAuthMethodReturnsDefaultWhenNotPurRepository() {
    RepositoryMeta meta = mock( RepositoryMeta.class );
    when( meta.getId() ).thenReturn( "SomeOtherRepository" );

    assertEquals( "USERNAME_PASSWORD", PurRepositoryUtils.getAuthMethod( meta ) );
  }

  @Test
  public void getAuthMethodReturnsDefaultWhenNullMeta() {
    assertEquals( "USERNAME_PASSWORD", PurRepositoryUtils.getAuthMethod( null ) );
  }

  @Test
  public void getAuthMethodReturnsDefaultWhenReflectionFails() {
    RepositoryMeta meta = mock( RepositoryMeta.class );
    when( meta.getId() ).thenReturn( "PentahoEnterpriseRepository" );

    assertEquals( "USERNAME_PASSWORD", PurRepositoryUtils.getAuthMethod( meta ) );
  }

  @Test
  public void getAuthMethodReturnsDefaultWhenMethodReturnsNull() {
    RepositoryMeta meta = new FakePurRepoMeta( "http://server:8080/pentaho", null ) {
    };
    assertEquals( "USERNAME_PASSWORD", PurRepositoryUtils.getAuthMethod( meta ) );
  }

  // ===== supportsBrowserAuth =====

  @Test
  public void supportsBrowserAuthReturnsFalseWhenNotPurRepository() {
    RepositoryMeta meta = mock( RepositoryMeta.class );
    when( meta.getId() ).thenReturn( "SomeOtherRepository" );

    assertFalse( PurRepositoryUtils.supportsBrowserAuth( meta ) );
  }

  @Test
  public void supportsBrowserAuthReturnsFalseWhenNoServerUrl() {
    RepositoryMeta meta = mock( RepositoryMeta.class );
    when( meta.getId() ).thenReturn( "PentahoEnterpriseRepository" );

    assertFalse( PurRepositoryUtils.supportsBrowserAuth( meta ) );
  }

  @Test
  public void supportsBrowserAuthReturnsFalseForNullMeta() {
    assertFalse( PurRepositoryUtils.supportsBrowserAuth( null ) );
  }

  @Test
  public void supportsBrowserAuthReturnsTrueWhenAllConditionsMet() {
    RepositoryMeta meta = new FakePurRepoMeta( "http://server:8080/pentaho", "SSO" ) {
    };
    assertTrue( PurRepositoryUtils.supportsBrowserAuth( meta ) );
  }

  @Test
  public void supportsBrowserAuthReturnsFalseWhenAuthMethodIsNotSso() {
    RepositoryMeta meta = new FakePurRepoMeta( "http://server:8080/pentaho", "USERNAME_PASSWORD" ) {
    };
    assertFalse( PurRepositoryUtils.supportsBrowserAuth( meta ) );
  }

  @Test
  public void supportsBrowserAuthIsCaseSensitiveForSso() {
    RepositoryMeta meta = new FakePurRepoMeta( "http://server:8080/pentaho", "sso" ) {
    };
    assertFalse( PurRepositoryUtils.supportsBrowserAuth( meta ) );
  }

  @Test
  public void supportsBrowserAuthReturnsFalseWhenUrlIsEmpty() {
    RepositoryMeta meta = new FakePurRepoMeta( "", "SSO" ) {
    };
    assertFalse( PurRepositoryUtils.supportsBrowserAuth( meta ) );
  }

  @Test
  public void supportsBrowserAuthReturnsFalseWhenUrlIsWhitespace() {
    RepositoryMeta meta = new FakePurRepoMeta( "   ", "SSO" ) {
    };
    assertFalse( PurRepositoryUtils.supportsBrowserAuth( meta ) );
  }

  @Test
  public void supportsBrowserAuthReturnsFalseWhenAuthMethodIsNull() {
    RepositoryMeta meta = new FakePurRepoMeta( "http://server:8080/pentaho", null ) {
    };
    assertFalse( PurRepositoryUtils.supportsBrowserAuth( meta ) );
  }

  @Test
  public void getSsoAuthorizationUriReturnsConfiguredValue() {
    RepositoryMeta meta = new FakePurRepoMeta(
      "http://server:8080/pentaho", "SSO", "oauth2/authorization/azure", "Microsoft", "azure" ) {
    };

    assertEquals( "oauth2/authorization/azure", PurRepositoryUtils.getSsoAuthorizationUri( meta ) );
  }

  @Test
  public void getSsoProviderNameReturnsConfiguredValue() {
    RepositoryMeta meta = new FakePurRepoMeta(
      "http://server:8080/pentaho", "SSO", "oauth2/authorization/azure", "Microsoft", "azure" ) {
    };

    assertEquals( "Microsoft", PurRepositoryUtils.getSsoProviderName( meta ) );
  }

  @Test
  public void getSsoRegistrationIdReturnsConfiguredValue() {
    RepositoryMeta meta = new FakePurRepoMeta(
      "http://server:8080/pentaho", "SSO", "oauth2/authorization/azure", "Microsoft", "azure" ) {
    };

    assertEquals( "azure", PurRepositoryUtils.getSsoRegistrationId( meta ) );
  }

  @Test
  public void getSsoAuthorizationUriReturnsNullForNonPurRepository() {
    RepositoryMeta meta = mock( RepositoryMeta.class );
    when( meta.getId() ).thenReturn( "SomeOtherRepository" );

    assertNull( PurRepositoryUtils.getSsoAuthorizationUri( meta ) );
  }

  @Test
  public void getSsoAuthorizationUriReturnsNullForNullMeta() {
    assertNull( PurRepositoryUtils.getSsoAuthorizationUri( null ) );
  }

  @Test
  public void getSsoProviderNameReturnsNullForNonPurRepository() {
    RepositoryMeta meta = mock( RepositoryMeta.class );
    when( meta.getId() ).thenReturn( "SomeOtherRepository" );

    assertNull( PurRepositoryUtils.getSsoProviderName( meta ) );
  }

  @Test
  public void getSsoProviderNameReturnsNullForNullMeta() {
    assertNull( PurRepositoryUtils.getSsoProviderName( null ) );
  }

  @Test
  public void getSsoRegistrationIdReturnsNullForNonPurRepository() {
    RepositoryMeta meta = mock( RepositoryMeta.class );
    when( meta.getId() ).thenReturn( "SomeOtherRepository" );

    assertNull( PurRepositoryUtils.getSsoRegistrationId( meta ) );
  }

  @Test
  public void getSsoRegistrationIdReturnsNullForNullMeta() {
    assertNull( PurRepositoryUtils.getSsoRegistrationId( null ) );
  }

  abstract static class FakePurRepoMeta implements RepositoryMeta {
    protected final String url;
    protected final String authMethod;
    protected final String ssoAuthorizationUri;
    protected final String ssoProviderName;
    protected final String ssoRegistrationId;

    FakePurRepoMeta( String url, String authMethod ) {
      this( url, authMethod, null, null, null );
    }

    FakePurRepoMeta( String url, String authMethod, String ssoAuthorizationUri,
                     String ssoProviderName, String ssoRegistrationId ) {
      this.url = url;
      this.authMethod = authMethod;
      this.ssoAuthorizationUri = ssoAuthorizationUri;
      this.ssoProviderName = ssoProviderName;
      this.ssoRegistrationId = ssoRegistrationId;
    }

    @SuppressWarnings( "unused" )
    public FakeLocation getRepositoryLocation() {
      return url != null ? new FakeLocation( url ) : null;
    }

    @SuppressWarnings( "unused" )
    public String getAuthMethod() {
      return authMethod;
    }

    @SuppressWarnings( "unused" )
    public String getSsoAuthorizationUri() {
      return ssoAuthorizationUri;
    }

    @SuppressWarnings( "unused" )
    public String getSsoProviderName() {
      return ssoProviderName;
    }

    @SuppressWarnings( "unused" )
    public String getSsoRegistrationId() {
      return ssoRegistrationId;
    }

    @Override
    public String getId() {
      return "PentahoEnterpriseRepository";
    }

    @Override
    public String getName() {
      return "Fake PUR";
    }

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public void setId( String id ) {
    }

    @Override
    public void setName( String name ) {
    }

    @Override
    public void setDescription( String description ) {
    }

    @Override
    public String getDialogClassName() {
      return null;
    }

    @Override
    public String getRevisionBrowserDialogClassName() {
      return null;
    }

    @Override
    public void loadXML( org.w3c.dom.Node repnode,
                         java.util.List<org.pentaho.di.core.database.DatabaseMeta> databases ) {
    }

    @Override
    public String getXML() {
      return null;
    }

    @Override
    public Boolean isDefault() {
      return false;
    }

    @Override
    public void setDefault( Boolean is ) {
    }

    @Override
    public org.pentaho.di.repository.RepositoryCapabilities getRepositoryCapabilities() {
      return null;
    }

    @Override
    public void populate( java.util.Map<String, Object> properties,
                          org.pentaho.di.repository.RepositoriesMeta repositoriesMeta ) {
    }

    @Override
    public RepositoryMeta clone() {
      return new FakePurRepoMeta( url, authMethod, ssoAuthorizationUri, ssoProviderName, ssoRegistrationId ) {
      };
    }

    @Override
    public org.json.simple.JSONObject toJSONObject() {
      return null;
    }
  }

  record FakeLocation(String url) {
    @SuppressWarnings( "unused" )
    public String getUrl() {
      return url;
    }
  }
}

