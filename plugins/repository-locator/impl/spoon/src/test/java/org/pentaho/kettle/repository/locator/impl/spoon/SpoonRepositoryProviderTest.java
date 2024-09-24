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
package org.pentaho.kettle.repository.locator.impl.spoon;

import org.junit.Test;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.spoon.Spoon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 4/15/16.
 */
public class SpoonRepositoryProviderTest {
  @Test
  public void testNoArgConstructor() {
    assertNotNull( new SpoonRepositoryProvider() );
  }

  @Test
  public void testGetRepository() {
    Spoon spoon = mock( Spoon.class );
    Repository repository = mock( Repository.class );
    when( spoon.getRepository() ).thenReturn( repository );
    assertEquals( repository, new SpoonRepositoryProvider( () -> spoon ).getRepository() );
  }
}
