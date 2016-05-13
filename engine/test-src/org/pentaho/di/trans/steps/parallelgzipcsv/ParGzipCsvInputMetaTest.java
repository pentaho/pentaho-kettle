/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.parallelgzipcsv;

import org.junit.Test;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

import static org.junit.Assert.*;

public class ParGzipCsvInputMetaTest {

  @Test
  public void cloneTest() throws Exception {
    ParGzipCsvInputMeta meta = new ParGzipCsvInputMeta();
    meta.allocate( 2 );
    TextFileInputField tfi1 = new TextFileInputField( "fieldname1", 0, 10 );
    TextFileInputField tfi2 = new TextFileInputField( "fieldname2", 15, 30 );
    meta.setInputFields( new TextFileInputField[] { tfi1, tfi2 } );
    // scalars should be cloned using super.clone() - makes sure they're calling super.clone()
    meta.setFilename( "aFileName" );
    ParGzipCsvInputMeta aClone = (ParGzipCsvInputMeta) meta.clone();
    assertFalse( aClone == meta );
    TextFileInputField[] ctfi = aClone.getInputFields();
    assertTrue( ctfi[0].getName().equals( tfi1.getName() ) );
    assertTrue( ctfi[1].getName().equals( tfi2.getName() ) );
    assertEquals( meta.getFilename(), aClone.getFilename() );
  }

}
