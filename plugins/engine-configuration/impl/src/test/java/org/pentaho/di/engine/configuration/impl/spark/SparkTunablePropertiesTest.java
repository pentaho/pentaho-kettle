/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */
package org.pentaho.di.engine.configuration.impl.spark;

import org.junit.Test;

import java.util.List;

import static org.pentaho.di.core.util.Assert.assertFalse;
import static org.pentaho.di.core.util.Assert.assertTrue;

public class SparkTunablePropertiesTest {

  @Test
  public void testGenericDatasetTunable() {
    assertTrue( SparkTunableProperties.getProperties( "badID" ).contains( "cache.before" ) );
    assertFalse( SparkTunableProperties.getProperties( "badID" ).contains( "jdbc.columnName" ) );

  }

  @Test
  public void testJoinTunable() {
    assertTrue( SparkTunableProperties.getProperties( "MergeJoin" ).contains( "join.broadcast.stepName" ) );
    assertTrue( SparkTunableProperties.getProperties( "JoinRows" ).contains( "join.broadcast.stepName" ) );
    assertTrue( SparkTunableProperties.getProperties( "MergeRows" ).contains( "join.broadcast.stepName" ) );
    assertFalse( SparkTunableProperties.getProperties( "badID" ).contains( "join.broadcast.stepName" ) );

  }

  @Test
  public void testJdbcTunable() {
    List<String> properties = SparkTunableProperties.getProperties( "TableInput" );
    assertTrue( properties.contains( "jdbc.columnName" ) );
    assertFalse( properties.contains( "cache.before" ) );

  }

}
