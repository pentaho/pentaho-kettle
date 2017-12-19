/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.repository.filerep;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.repository.RepositoriesMeta;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by bmorrise on 4/26/16.
 */
@RunWith( MockitoJUnitRunner.class )
public class KettleFileRepositoryMetaTest {

  public static final String NAME = "Name";
  public static final String DESCRIPTION = "Description";
  public static final String THIS_IS_THE_PATH = "/this/is/the/path";
  public static final String JSON_OUTPUT = "{\"isDefault\":true,\"displayName\":\"Name\",\"showHiddenFolders\":true,"
    + "\"description\":\"Description\",\"location\":\"\\/this\\/is\\/the\\/path\",\"id\":\"KettleFileRepository\","
    + "\"doNotModify\":true}";

  @Mock
  private RepositoriesMeta repositoriesMeta;

  KettleFileRepositoryMeta kettleFileRepositoryMeta;

  @Before
  public void setup() {
    kettleFileRepositoryMeta = new KettleFileRepositoryMeta();
  }

  @Test
  public void testPopulate() throws Exception {
    Map<String, Object> properties = new HashMap<>();
    properties.put( "displayName", NAME );
    properties.put( "showHiddenFolders", true );
    properties.put( "description", DESCRIPTION );
    properties.put( "location", THIS_IS_THE_PATH );
    properties.put( "doNotModify", true );
    properties.put( "isDefault", true );

    kettleFileRepositoryMeta.populate( properties, repositoriesMeta );

    assertEquals( NAME, kettleFileRepositoryMeta.getName() );
    assertEquals( true, kettleFileRepositoryMeta.isHidingHiddenFiles() );
    assertEquals( DESCRIPTION, kettleFileRepositoryMeta.getDescription() );
    assertEquals( THIS_IS_THE_PATH, kettleFileRepositoryMeta.getBaseDirectory() );
    assertEquals( true, kettleFileRepositoryMeta.isReadOnly() );
    assertEquals( true, kettleFileRepositoryMeta.isDefault() );
  }

  @Test
  public void testToJSONString() {
    kettleFileRepositoryMeta.setName( NAME );
    kettleFileRepositoryMeta.setHidingHiddenFiles( true );
    kettleFileRepositoryMeta.setDescription( DESCRIPTION );
    kettleFileRepositoryMeta.setBaseDirectory( THIS_IS_THE_PATH );
    kettleFileRepositoryMeta.setReadOnly( true );
    kettleFileRepositoryMeta.setDefault( true );

    JSONObject json = kettleFileRepositoryMeta.toJSONObject();

    assertEquals( JSON_OUTPUT, json.toString() );
  }

}
