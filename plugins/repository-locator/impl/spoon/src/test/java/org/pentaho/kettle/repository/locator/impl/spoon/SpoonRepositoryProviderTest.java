/*!
 * Copyright 2010 - 2022 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
