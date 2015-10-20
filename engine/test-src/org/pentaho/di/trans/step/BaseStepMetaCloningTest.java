/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.step;

import org.junit.Test;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.repository.Repository;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author Andrey Khayrutdinov
 */
public class BaseStepMetaCloningTest {

  @Test
  public void cloningKeepsAllExceptIoMeta() throws Exception {
    final Database db1 = mock( Database.class );
    final Database db2 = mock( Database.class );
    final Repository repository = mock( Repository.class );
    final StepMeta stepMeta = mock( StepMeta.class );

    BaseStepMeta meta = new BaseStepMeta();
    meta.setChanged( true );
    meta.databases = new Database[] { db1, db2 };
    meta.ioMeta = new StepIOMeta( true, false, false, false, false, false );
    meta.repository = repository;
    meta.parentStepMeta = stepMeta;

    BaseStepMeta clone = (BaseStepMeta) meta.clone();
    assertTrue( clone.hasChanged() );

    // is it OK ?
    assertTrue( clone.databases == meta.databases );
    assertArrayEquals( meta.databases, clone.databases );

    assertEquals( meta.repository, clone.repository );
    assertEquals( meta.parentStepMeta, clone.parentStepMeta );

    assertNull( clone.ioMeta );
  }
}
