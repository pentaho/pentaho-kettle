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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.repository.RepositoriesMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bmorrise on 4/26/16.
 */
@RunWith( MockitoJUnitRunner.class )
public class KettleDatabaseRepositoryMetaTest {

  public static final String JSON_OUTPUT =
    "{\"isDefault\":true,\"displayName\":\"Name\",\"description\":\"Description\","
      + "\"databaseConnection\":\"Database Connection\",\"id\":\"KettleDatabaseRepository\"}";

  @Mock
  RepositoriesMeta repositoriesMeta;

  @Mock
  DatabaseMeta databaseMeta;

  public static final String NAME = "Name";
  public static final String DESCRIPTION = "Description";
  public static final String DATABASE_CONNECTION = "Database Connection";
  KettleDatabaseRepositoryMeta kettleDatabaseRepositoryMeta;

  @Before
  public void setup() {
    kettleDatabaseRepositoryMeta = new KettleDatabaseRepositoryMeta();
  }

  @Test
  public void testPopulate() {
    kettleDatabaseRepositoryMeta.setConnection( databaseMeta );
    when( databaseMeta.getName() ).thenReturn( DATABASE_CONNECTION );
    when( repositoriesMeta.searchDatabase( DATABASE_CONNECTION ) ).thenReturn( databaseMeta );

    Map<String, Object> properties = new HashMap<>();
    properties.put( "displayName", NAME );
    properties.put( "description", DESCRIPTION );
    properties.put( "databaseConnection", DATABASE_CONNECTION );
    properties.put( "isDefault", true );

    kettleDatabaseRepositoryMeta.populate( properties, repositoriesMeta );

    assertEquals( NAME, kettleDatabaseRepositoryMeta.getName() );
    assertEquals( DESCRIPTION, kettleDatabaseRepositoryMeta.getDescription() );
    assertEquals( DATABASE_CONNECTION, kettleDatabaseRepositoryMeta.getConnection().getName() );
    assertEquals( true, kettleDatabaseRepositoryMeta.isDefault() );
  }

  @Test
  public void testToJSONString() {
    when( databaseMeta.getName() ).thenReturn( DATABASE_CONNECTION );

    kettleDatabaseRepositoryMeta.setName( NAME );
    kettleDatabaseRepositoryMeta.setDescription( DESCRIPTION );
    kettleDatabaseRepositoryMeta.setConnection( databaseMeta );
    kettleDatabaseRepositoryMeta.setDefault( true );

    JSONObject json = kettleDatabaseRepositoryMeta.toJSONObject();

    assertEquals( JSON_OUTPUT, json.toString() );
  }

}
