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

package org.pentaho.di.trans.steps.xbaseinput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;

public class XBaseInputIntTest {

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  List<RowMetaAndData> getEmptyRowMetaAndData() {
    return new ArrayList<RowMetaAndData>();
  }

  /*
   * Timeout is needed, as the transformation may never stop if PDI-8846 regresses
   */
  @Test(timeout=10000)
  public void testFilenameFromFieldNoFiles() throws KettleException {
    String stepName = "XBase Input";
    XBaseInputMeta meta = new XBaseInputMeta();
    meta.setAcceptingFilenames( true );
    meta.setAcceptingField( "filename" );
    meta.setAcceptingStepName( TransTestFactory.INJECTOR_STEPNAME );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, stepName );
    List<RowMetaAndData> inputList = getEmptyRowMetaAndData();
    List<RowMetaAndData> ret =
      TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, stepName,
        TransTestFactory.DUMMY_STEPNAME, inputList );

    assertNotNull( ret );
    assertEquals( 0, ret.size() );
  }
}
