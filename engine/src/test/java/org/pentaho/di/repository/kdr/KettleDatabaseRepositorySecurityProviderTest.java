/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.repository.kdr;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryUserDelegate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
/**
 * @author Andrey Khayrutdinov
 */
public class KettleDatabaseRepositorySecurityProviderTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }


  private KettleDatabaseRepository repository;
  private KettleDatabaseRepositorySecurityProvider provider;

  @Before
  public void setUp() {
    repository = new KettleDatabaseRepository();
    repository.userDelegate = mock( KettleDatabaseRepositoryUserDelegate.class );
    provider =
      new KettleDatabaseRepositorySecurityProvider( repository, new KettleDatabaseRepositoryMeta(), new UserInfo() );
  }


  @Test( expected = KettleException.class )
  public void saveUserInfo_NormalizesInfo_FailsIfStillBreaches() throws Exception {
    provider.saveUserInfo( new UserInfo( "    " ) );
  }

  @Test( expected = KettleException.class )
  public void saveUserInfo_CheckDuplication_FailsIfFoundSame() throws Exception {
    testSaveUserInfo_Passes( "login", "login", "login" );
  }


  @Test
  public void saveUserInfo_CheckDuplication_PassesIfFoundDifferenceInCase() throws Exception {
    testSaveUserInfo_Passes( "login", "login", "LOGIN" );
  }

  @Test
  public void saveUserInfo_NormalizesInfo_PassesIfNoViolations() throws Exception {
    testSaveUserInfo_Passes( "login    ", "login" );
  }

  @Test
  public void saveUserInfo_CheckDuplication_PassesIfFoundNothing() throws Exception {
    testSaveUserInfo_Passes( "login", "login" );
  }

  private void testSaveUserInfo_Passes( String login, String expectedLogin ) throws Exception {
    testSaveUserInfo_Passes( login, expectedLogin, "prefix_" + login );
  }

  private void testSaveUserInfo_Passes( String login, String expectedLogin, String existing ) throws Exception {
    doReturn( new StringObjectId( existing ) ).when( repository.userDelegate ).getUserID( eq( existing ) );

    provider.saveUserInfo( new UserInfo( login ) );

    ArgumentCaptor<UserInfo> captor = ArgumentCaptor.forClass( UserInfo.class );
    verify( repository.userDelegate ).saveUserInfo( captor.capture() );

    assertEquals( "UserInfo should be passed", expectedLogin, captor.getValue().getLogin() );
  }
}
