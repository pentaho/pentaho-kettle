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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.ui.repository.pur.services.IAbsSecurityProvider;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class AbsSecurityProviderTest {
  private AbsSecurityProvider provider;

  @Before
  public void setUp() {
    provider = new AbsSecurityProvider( new PurRepository(), new PurRepositoryMeta(), new UserInfo(),
      mock( ServiceManager.class ) );
    provider = spy( provider );
  }

  @Test( expected = KettleException.class )
  public void exceptionThrown_WhenOperationNotAllowed_ExecuteOperation() throws Exception {

    setOperationPermissions( IAbsSecurityProvider.EXECUTE_CONTENT_ACTION, false );
    provider.validateAction( RepositoryOperation.EXECUTE_TRANSFORMATION );
  }

  @Test( expected = KettleException.class )
  public void exceptionThrown_WhenOperationNotAllowed_ScheduleOperation() throws Exception {

    setOperationPermissions( IAbsSecurityProvider.SCHEDULE_CONTENT_ACTION, false );
    provider.validateAction( RepositoryOperation.SCHEDULE_JOB );
  }

  @Test( expected = KettleException.class )
  public void exceptionThrown_WhenOperationNotAllowed_ExecuteSchedulesOperation() throws Exception {

    setOperationPermissions( IAbsSecurityProvider.SCHEDULER_EXECUTE_ACTION, false );
    provider.validateAction( RepositoryOperation.SCHEDULER_EXECUTE );
  }

  @Test( expected = KettleException.class )
  public void exceptionThrown_WhenOperationNotAllowed_CreateOperation() throws Exception {

    setOperationPermissions( IAbsSecurityProvider.CREATE_CONTENT_ACTION, false );
    provider.validateAction( RepositoryOperation.MODIFY_JOB );
  }

  @Test( expected = KettleException.class )
  public void exceptionThrown_WhenOperationNotAllowed_DatasourcesOperation() throws Exception {
    setOperationPermissions( IAbsSecurityProvider.MODIFY_DATABASE_ACTION, false );
    provider.validateAction( RepositoryOperation.MODIFY_DATABASE );
  }

  @Test
  public void noExceptionThrown_WhenOperationIsAllowed_ScheduleOperation() throws Exception {

    setOperationPermissions( IAbsSecurityProvider.EXECUTE_CONTENT_ACTION, true );
    provider.validateAction( RepositoryOperation.EXECUTE_JOB );
  }

  @Test
  public void noExceptionThrown_WhenOperationIsAllowed_CreateOperation() throws Exception {

    setOperationPermissions( IAbsSecurityProvider.SCHEDULE_CONTENT_ACTION, true );
    provider.validateAction( RepositoryOperation.SCHEDULE_TRANSFORMATION );
  }

  @Test
  public void noExceptionThrown_WhenOperationIsAllowed_ExecuteSchedulesOperation() throws Exception {

    setOperationPermissions( IAbsSecurityProvider.SCHEDULER_EXECUTE_ACTION, true );
    provider.validateAction( RepositoryOperation.SCHEDULER_EXECUTE );
  }

  @Test
  public void noExceptionThrown_WhenOperationIsAllowed_ExecuteOperation() throws Exception {

    setOperationPermissions( IAbsSecurityProvider.CREATE_CONTENT_ACTION, true );
    provider.validateAction( RepositoryOperation.MODIFY_TRANSFORMATION );
  }

  @Test
  public void noExceptionThrown_WhenOperationNotAllowed_DatasourcesOperation() throws Exception {
    setOperationPermissions( IAbsSecurityProvider.MODIFY_DATABASE_ACTION, true );
    provider.validateAction( RepositoryOperation.MODIFY_DATABASE );
  }

  private void setOperationPermissions( String operation, boolean isAllowed ) throws Exception {
    doReturn( isAllowed ).when( provider ).isAllowed( operation );
  }
}
