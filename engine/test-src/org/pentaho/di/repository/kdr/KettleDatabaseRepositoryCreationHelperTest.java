/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.OracleDatabaseMeta;

/**
 * @author Tatsiana_Kasiankova
 * 
 */
public class KettleDatabaseRepositoryCreationHelperTest extends TestCase {

  /**
   * 
   */
  private static final int EXPECTED_ORACLE_DB_REPO_STRING = 1999;
  private static final int EXPECTED_DEFAULT_DB_REPO_STRING = KettleDatabaseRepository.REP_ORACLE_STRING_LENGTH;
  private KettleDatabaseRepositoryMeta repositoryMeta;
  private KettleDatabaseRepository repository;

  public void testOracleDBRepoStringLength() throws Exception {

    KettleEnvironment.init();
    DatabaseMeta databaseMeta = new DatabaseMeta( "OraRepo", "ORACLE", "JDBC", null, "test", null, null, null );
    repositoryMeta =
        new KettleDatabaseRepositoryMeta( "KettleDatabaseRepository", "OraRepo", "Ora Repository", databaseMeta );
    repository = new KettleDatabaseRepository();
    repository.init( repositoryMeta );
    KettleDatabaseRepositoryCreationHelper helper = new KettleDatabaseRepositoryCreationHelper( repository );
    int repoStringLength = helper.getRepoStringLength();
    assertEquals( EXPECTED_ORACLE_DB_REPO_STRING, repoStringLength );
  }

  public void testDefaultDBRepoStringLength() throws Exception {

    KettleEnvironment.init();
    DatabaseMeta databaseMeta = new DatabaseMeta();
    databaseMeta.setDatabaseInterface( new TestDatabaseMeta() );
    repositoryMeta =
        new KettleDatabaseRepositoryMeta( "KettleDatabaseRepository", "TestRepo", "Test Repository", databaseMeta );
    repository = new KettleDatabaseRepository();
    repository.init( repositoryMeta );
    KettleDatabaseRepositoryCreationHelper helper = new KettleDatabaseRepositoryCreationHelper( repository );
    int repoStringLength = helper.getRepoStringLength();
    assertEquals( EXPECTED_DEFAULT_DB_REPO_STRING, repoStringLength );
  }

  class TestDatabaseMeta extends OracleDatabaseMeta {

    @Override
    public int getMaxVARCHARLength() {
      return 1;
    }
  }

}
