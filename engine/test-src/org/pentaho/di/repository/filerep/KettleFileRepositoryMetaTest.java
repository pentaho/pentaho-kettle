/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bmorrise on 4/26/16.
 */
public class KettleFileRepositoryMetaTest {

  public static final String NAME = "Name";
  public static final String DESCRIPTION = "Description";
  public static final String THIS_IS_THE_PATH = "/this/is/the/path";
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
    properties.put( "isDefaultOnStartup", true );
    properties.put( "location", THIS_IS_THE_PATH );
    properties.put( "doNotModify", true );

    kettleFileRepositoryMeta.populate( properties );

    Assert.assertEquals( NAME, kettleFileRepositoryMeta.getName() );
    Assert.assertEquals( true, kettleFileRepositoryMeta.isHidingHiddenFiles() );
    Assert.assertEquals( DESCRIPTION, kettleFileRepositoryMeta.getDescription() );
    Assert.assertEquals( THIS_IS_THE_PATH, kettleFileRepositoryMeta.getBaseDirectory() );
    Assert.assertEquals( true, kettleFileRepositoryMeta.isReadOnly() );
  }

}
