/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.core.widget;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TreeUtilTest {


  @Test
  public void testFindUniqueSuffix() {
    Set<String> existingNames = Set.of( "foo", "bar" );

    String newName = TreeUtil.findUniqueSuffix( "base", existingNames );
    assertEquals( "base 1", newName );

    existingNames = Set.of( "foo", "bar", "base 1" );
    newName = TreeUtil.findUniqueSuffix( "base", existingNames );
    assertEquals( "base 2", newName );

    existingNames = Set.of( "foo", "bar", "base 1", "base 2" );
    newName = TreeUtil.findUniqueSuffix( "base", existingNames );
    assertEquals( "base 3", newName );

    // gaps aren't checked, it's just the first available.
    existingNames = Set.of( "foo", "bar", "base 1", "base 3" );
    newName = TreeUtil.findUniqueSuffix( "base", existingNames );
    assertEquals( "base 2", newName );
  }

}
