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


package org.pentaho.di.trans.steps.mailinput;

/**
 * Tests for MailInputMeta class
 *
 * @author Marc Batchelor - removed useless test case, added load/save tests
 * @see MailInputMeta
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.util.HttpClientManager;
import org.pentaho.di.job.entries.getpop.MailConnectionMeta;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class MailInputMetaTest implements InitializerInterface<StepMetaInterface> {
  LoadSaveTester loadSaveTester;

  @Mock
  private CloseableHttpClient mockHttpClient;
  @Mock
  private CloseableHttpResponse mockResponse;

  @Mock
  private HttpClientManager httpClientManager;

  private MailInputMeta mailInputMeta;
  Class<MailInputMeta> testMetaClass = MailInputMeta.class;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    MockitoAnnotations.initMocks( this );
    mailInputMeta = new MailInputMeta();
    KettleEnvironment.init();
    PluginRegistry.init( false );
    when( httpClientManager.createDefaultClient() ).thenReturn( mockHttpClient );
    List<String> attributes =
        Arrays.asList( "conditionReceivedDate", "valueimaplist", "serverName", "userName", "password", "useSSL", "port",
            "firstMails", "retrievemails", "delete", "protocol", "firstIMAPMails", "IMAPFolder", "senderSearchTerm",
            "notTermSenderSearch", "recipientSearch", "subjectSearch", "receivedDate1", "receivedDate2",
            "notTermSubjectSearch", "notTermRecipientSearch", "notTermReceivedDateSearch", "includeSubFolders", "useProxy",
            "proxyUsername", "folderField", "dynamicFolder", "rowLimit", "useBatch", "start", "end", "batchSize",
            "stopOnError", "inputFields" );

    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "inputFields",
        new ArrayLoadSaveValidator<MailInputField>( new MailInputFieldLoadSaveValidator(), 5 ) );
    attrValidatorMap.put( "batchSize", new IntLoadSaveValidator( 1000 ) );
    attrValidatorMap.put( "conditionReceivedDate", new IntLoadSaveValidator( MailConnectionMeta.conditionDateCode.length ) );
    attrValidatorMap.put( "valueimaplist", new IntLoadSaveValidator( MailConnectionMeta.valueIMAPListCode.length ) );
    attrValidatorMap.put( "port", new StringIntLoadSaveValidator( 65534 ) );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  @Override
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof MailInputMeta ) {
      ( (MailInputMeta) someMeta ).allocate( 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  @Test
  public void testProtocolType() {
    MailInputMeta mailInputMeta = new MailInputMeta();
    String expectedProtocol = "IMAP";
    mailInputMeta.setProtocol( expectedProtocol );
    assertEquals( expectedProtocol, mailInputMeta.getProtocol() );
  }

  @Test
  public void testAuthenticationTypeNone() {
    mailInputMeta.setUsingAuthentication( MailInputMeta.AUTENTICATION_NONE );
    assertEquals( MailInputMeta.AUTENTICATION_NONE, mailInputMeta.isUsingAuthentication() );
  }

  @Test
  public void testAuthenticationTypeBasic() {
    mailInputMeta.setUsingAuthentication( MailInputMeta.AUTENTICATION_BASIC );
    assertEquals( MailInputMeta.AUTENTICATION_BASIC, mailInputMeta.isUsingAuthentication() );
  }

  @Test
  public void testAuthenticationTypeOAuth() {
    mailInputMeta.setUsingAuthentication( MailInputMeta.AUTENTICATION_OAUTH );
    assertEquals( MailInputMeta.AUTENTICATION_OAUTH, mailInputMeta.isUsingAuthentication() );
  }

  @Test
  public void testGrantTypeClientCredentials() {
    MailInputMeta mailInputMeta = new MailInputMeta();
    mailInputMeta.setGrantType( MailInputMeta.GRANTTYPE_CLIENTCREDENTIALS );
    assertEquals( MailInputMeta.GRANTTYPE_CLIENTCREDENTIALS, mailInputMeta.getGrantType() );
  }


  @Test( expected = RuntimeException.class )
  public void getOauthToken_invalidResponse_throwsException() throws Exception {
    String tokenUrl = "http://example.com/token";
    String scope = "scope";
    String clientId = "clientId";
    String secretKey = "secretKey";
    String grantType = MailInputMeta.GRANTTYPE_AUTHORIZATION_CODE;
    String authorizationCode = "authCode";
    String redirectUri = "redirectUri";

    when( httpClientManager.createDefaultClient() ).thenReturn( mockHttpClient );
    when( mockHttpClient.execute( any( HttpPost.class) ) ).thenReturn( mockResponse );
    when( mockResponse.getStatusLine().getStatusCode() ).thenReturn( HttpStatus.SC_BAD_REQUEST );

    mailInputMeta.getOauthToken( tokenUrl, scope, clientId, secretKey, grantType, null, authorizationCode, redirectUri );
  }

  @Test( expected = RuntimeException.class )
  public void getOauthToken_httpClientExecuteThrowsIOException_throwsRuntimeException() throws Exception {
    String tokenUrl = "http://example.com/token";
    String scope = "scope";
    String clientId = "clientId";
    String secretKey = "secretKey";
    String grantType = MailInputMeta.GRANTTYPE_AUTHORIZATION_CODE;
    String authorizationCode = "authCode";
    String redirectUri = "redirectUri";

    when( httpClientManager.createDefaultClient() ).thenReturn( mockHttpClient );
    when( mockHttpClient.execute( any( HttpPost.class ) ) ).thenThrow( new IOException( "IO error" ) );

    mailInputMeta.getOauthToken( tokenUrl, scope, clientId, secretKey, grantType, null, authorizationCode, redirectUri );
  }

  @Test( expected = RuntimeException.class )
  public void getOauthToken_invalidGrantType_throwsRuntimeException() throws Exception {
    String tokenUrl = "http://example.com/token";
    String scope = "scope";
    String clientId = "clientId";
    String secretKey = "secretKey";
    String grantType = "invalid_grant_type";
    String authorizationCode = "authCode";
    String redirectUri = "redirectUri";

    when( httpClientManager.createDefaultClient() ).thenReturn( mockHttpClient );

    mailInputMeta.getOauthToken( tokenUrl, scope, clientId, secretKey, grantType, null, authorizationCode, redirectUri );
  }

  @Test
  public void testGrantTypeAuthorizationCode() {
    MailInputMeta mailInputMeta = new MailInputMeta();
    mailInputMeta.setGrantType( MailInputMeta.GRANTTYPE_AUTHORIZATION_CODE );
    assertEquals( MailInputMeta.GRANTTYPE_AUTHORIZATION_CODE, mailInputMeta.getGrantType() );
  }

  @Test
  public void testGrantTypeRefreshToken() {
    MailInputMeta mailInputMeta = new MailInputMeta();
    mailInputMeta.setGrantType(MailInputMeta.GRANTTYPE_REFRESH_TOKEN);
    assertEquals(MailInputMeta.GRANTTYPE_REFRESH_TOKEN, mailInputMeta.getGrantType());
  }

  @Test
  public void testAuthorizationCodeAndRedirectUri() {
    MailInputMeta meta = new MailInputMeta();
    String authorizationCode = "testAuthCode";
    String redirectUri = "http://test.redirect.uri";

    meta.setAuthorization_code( authorizationCode );
    meta.setRedirectUri( redirectUri );

    assertEquals( authorizationCode, meta.getAuthorization_code() );
    assertEquals( redirectUri, meta.getRedirectUri() );
  }

  @Test( expected = RuntimeException.class )
  public void testgetOauthTokenThrowsExceptionOnHttpClientExecuteFailure() throws IOException {
    String tokenUrl = "http://example.com/token";

    Mockito.when( httpClientManager.createDefaultClient() ).thenReturn( mockHttpClient );
    Mockito.when( mockHttpClient.execute( any( HttpPost.class ) ) ).thenThrow( new IOException() );
    mailInputMeta.getOauthToken("token", "scope", "clientId", "secretKey", "authorization_code", null, "authCode", "redirectUri");
  }

  public class MailInputFieldLoadSaveValidator implements FieldLoadSaveValidator<MailInputField> {
    final Random rand = new Random();
    @Override
    public MailInputField getTestObject() {
      MailInputField rtn = new MailInputField();
      rtn.setName( UUID.randomUUID().toString() );
      rtn.setColumn( rand.nextInt( MailInputField.ColumnDesc.length ) );
      return rtn;
    }

    @Override
    public boolean validateTestObject( MailInputField testObject, Object actual ) {
      if ( !( actual instanceof MailInputField ) ) {
        return false;
      }
      MailInputField another = (MailInputField) actual;
      return new EqualsBuilder()
          .append( testObject.getName(), another.getName() )
          .append( testObject.getColumn(), another.getColumn() )
      .isEquals();
    }
  }

  public class StringIntLoadSaveValidator implements FieldLoadSaveValidator<String> {
    final Random rand = new Random();
    int intBound;

    public StringIntLoadSaveValidator( ) {
      intBound = 0;
    }

    public StringIntLoadSaveValidator( int bounds ) {
      if ( bounds <= 0 ) {
        throw new IllegalArgumentException( "Bad boundary for StringIntLoadSaveValidator" );
      }
      this.intBound = bounds;
    }

    @Override
    public String getTestObject() {
      int someInt = 0;
      if ( intBound > 0 ) {
        someInt = rand.nextInt( intBound );
      } else {
        someInt = rand.nextInt();
      }
      return Integer.toString( someInt );
    }

    @Override
    public boolean validateTestObject( String testObject, Object actual ) {
      return ( actual.equals( testObject ) );
    }
  }
}
