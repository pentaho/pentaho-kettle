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

public class XBaseInputIntIT {

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
