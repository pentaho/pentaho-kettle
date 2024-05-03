/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.resource;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;



public class SimpleResourceNamingTest {

  @Test
  public void testCreateNewParameterName() {

    SimpleResourceNaming simpleResourceNaming1 = new SimpleResourceNaming();
    SimpleResourceNaming simpleResourceNaming2 = new SimpleResourceNaming();

    String prefix = UUID.randomUUID().toString();
    String originalFilePath = UUID.randomUUID().toString();
    String extension = UUID.randomUUID().toString();
    ResourceNamingInterface.FileNamingType namingType = ResourceNamingInterface.FileNamingType.DATA_FILE;

    simpleResourceNaming1.nameResource( prefix, originalFilePath, extension, namingType );
    assertEquals( 1, SimpleResourceNaming.getParameterNr() );
    simpleResourceNaming2.nameResource( prefix, originalFilePath, extension, namingType );
    assertEquals( 2, SimpleResourceNaming.getParameterNr() );
  }

}
