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

package org.pentaho.di.trans.steps.rowgenerator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.steps.loadsave.MemoryRepository;
import org.pentaho.metastore.api.IMetaStore;

import java.util.Collections;

public class RowGeneratorMetaTest {

  private Repository rep;

  private ObjectId id_step;

  private final String launchVariable = "${ROW_LIMIT}";

  private final String rowGeneratorRowLimitCode = "limit";

  @Before
  public void setUp() throws KettleException {
    rep = new MemoryRepository();
    id_step = new StringObjectId( "aStringObjectID" );
    rep.saveStepAttribute( new StringObjectId( "transId" ), id_step, rowGeneratorRowLimitCode, launchVariable );
  }

  /**
   * If we can read row limit as string from repository then we can run row generator.
   * @see RowGeneratorTest
   * @throws KettleException
   */
  @Test
  public void testReadRowLimitAsStringFromRepository() throws KettleException {
    RowGeneratorMeta rowGeneratorMeta = new RowGeneratorMeta();
    IMetaStore metaStore = Mockito.mock( IMetaStore.class );
    DatabaseMeta dbMeta = Mockito.mock( DatabaseMeta.class );
    rowGeneratorMeta.readRep( rep, metaStore, id_step, Collections.singletonList( dbMeta ) );
    assertEquals( rowGeneratorMeta.getRowLimit(),  launchVariable );
  }

}
