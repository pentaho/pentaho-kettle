/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
