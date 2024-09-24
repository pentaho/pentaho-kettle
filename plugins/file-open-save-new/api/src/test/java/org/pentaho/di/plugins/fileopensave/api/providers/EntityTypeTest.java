/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2023 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.plugins.fileopensave.api.providers;

import org.junit.Test;

import static org.junit.Assert.*;

public class EntityTypeTest {

  @Test
  public void fromValue() {
    assertEquals( EntityType.UNKNOWN, EntityType.fromValue( 0 ) );
    assertEquals( EntityType.REPOSITORY_FILE, EntityType.fromValue( 5 ) );
  }

  @Test
  public void getValue() {
    assertEquals( 0, EntityType.UNKNOWN.getValue() );
    assertEquals( 13, EntityType.NAMED_CLUSTER_FILE.getValue() );
  }

  @Test
  public void valueOf() {
    assertEquals( EntityType.NAMED_CLUSTER_FILE, EntityType.valueOf( "NAMED_CLUSTER_FILE" ) );
  }
}