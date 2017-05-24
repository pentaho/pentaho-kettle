/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon.delegates;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.TransLogTable;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.ArrayList;
import java.util.List;


public class SpoonTransformationDelegateTest {

  private SpoonTransformationDelegate delegate;
  private Spoon spoon;

  private TransLogTable transLogTable;
  private TransMeta transMeta;
  private List<TransMeta> transformationMap;

  @Before
  public void before() {
    transformationMap = new ArrayList<TransMeta>();

    transMeta = mock( TransMeta.class );
    delegate = mock( SpoonTransformationDelegate.class );
    spoon = mock( Spoon.class );
    spoon.delegates = mock( SpoonDelegates.class );
    spoon.delegates.tabs = mock( SpoonTabsDelegate.class );

    doReturn( transformationMap ).when( delegate ).getTransformationList();
    doReturn( spoon ).when( delegate ).getSpoon();
    doCallRealMethod().when( delegate ).isLogTableDefined( any() );
    transLogTable = mock( TransLogTable.class );
  }

  @Test
  public void testIsLogTableDefinedLogTableDefined() {
    DatabaseMeta databaseMeta = mock( DatabaseMeta.class );
    doReturn( databaseMeta ).when( transLogTable ).getDatabaseMeta();
    doReturn( "test_table" ).when( transLogTable ).getTableName();

    assertTrue( delegate.isLogTableDefined( transLogTable ) );
  }

  @Test
  public void testIsLogTableDefinedLogTableNotDefined() {
    DatabaseMeta databaseMeta = mock( DatabaseMeta.class );
    doReturn( databaseMeta ).when( transLogTable ).getDatabaseMeta();

    assertFalse( delegate.isLogTableDefined( transLogTable ) );
  }

  @Test
  public void testAddAndCloseTransformation() {
    doCallRealMethod().when( delegate ).closeTransformation( any() );
    doCallRealMethod().when( delegate ).addTransformation( any() );
    assertTrue( delegate.addTransformation( transMeta ) );
    assertFalse( delegate.addTransformation( transMeta ) );
    delegate.closeTransformation( transMeta );
    assertTrue( delegate.addTransformation( transMeta ) );
  }
}
