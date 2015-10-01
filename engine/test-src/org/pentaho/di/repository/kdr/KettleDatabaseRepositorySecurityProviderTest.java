/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.repository.kdr;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryUserDelegate;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Andrey Khayrutdinov
 */
public class KettleDatabaseRepositorySecurityProviderTest {

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

  @Test
  public void saveUserInfo_NormalizesInfo_PassesIfNoViolations() throws Exception {
    UserInfo info = new UserInfo( "login    " );

    ArgumentCaptor<UserInfo> captor = ArgumentCaptor.forClass( UserInfo.class );
    provider.saveUserInfo( info );
    verify( repository.userDelegate ).saveUserInfo( captor.capture() );

    info = captor.getValue();
    assertEquals( "Spaces should be trimmed", "login", info.getLogin() );
  }

  @Test( expected = KettleException.class )
  public void saveUserInfo_NormalizesInfo_FailsIfStillBreaches() throws Exception {
    UserInfo info = new UserInfo( "    " );
    provider.saveUserInfo( info );
  }
}
