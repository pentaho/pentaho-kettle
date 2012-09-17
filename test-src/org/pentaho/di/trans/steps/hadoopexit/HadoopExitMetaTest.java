/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.hadoopexit;

import static org.junit.Assert.*;

import org.junit.Test;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;

public class HadoopExitMetaTest {

  @Test
  public void getFields() throws Throwable {
    HadoopExitMeta meta = new HadoopExitMeta();
    meta.setOutKeyFieldname("key");
    meta.setOutValueFieldname("value");

    RowMeta rowMeta = new RowMeta();
    ValueMeta valueMeta0 = new ValueMeta("key");
    ValueMeta valueMeta1 = new ValueMeta("value");
    rowMeta.addValueMeta(valueMeta0);
    rowMeta.addValueMeta(valueMeta1);

    meta.getFields(rowMeta, null, null, null, null);

    assertEquals(2, rowMeta.getValueMetaList().size());
    ValueMetaInterface vmi = rowMeta.getValueMeta(0);
    assertEquals("outKey", vmi.getName());
    vmi = rowMeta.getValueMeta(1);
    assertEquals("outValue", vmi.getName());
  }

  @Test
  public void getFields_invalid_key() throws Throwable {
    HadoopExitMeta meta = new HadoopExitMeta();
    meta.setOutKeyFieldname("invalid");
    meta.setOutValueFieldname("value");

    RowMeta rowMeta = new RowMeta();
    ValueMeta valueMeta0 = new ValueMeta("key");
    ValueMeta valueMeta1 = new ValueMeta("value");
    rowMeta.addValueMeta(valueMeta0);
    rowMeta.addValueMeta(valueMeta1);

    try {
      meta.getFields(rowMeta, null, null, null, null);
      fail("expected exception");
    } catch (Exception ex) {
      assertEquals(
          "\n" + BaseMessages.getString(HadoopExitMeta.class, "Error.InvalidKeyField", meta.getOutKeyFieldname())
              + "\n", ex.getMessage());
    }

    // Check that the meta was not modified
    assertEquals(2, rowMeta.getValueMetaList().size());
    ValueMetaInterface vmi = rowMeta.getValueMeta(0);
    assertEquals("key", vmi.getName());
    vmi = rowMeta.getValueMeta(1);
    assertEquals("value", vmi.getName());
  }

  @Test
  public void getFields_invalid_value() throws Throwable {
    HadoopExitMeta meta = new HadoopExitMeta();
    meta.setOutKeyFieldname("key");
    meta.setOutValueFieldname("invalid");

    RowMeta rowMeta = new RowMeta();
    ValueMeta valueMeta0 = new ValueMeta("key");
    ValueMeta valueMeta1 = new ValueMeta("value");
    rowMeta.addValueMeta(valueMeta0);
    rowMeta.addValueMeta(valueMeta1);

    try {
      meta.getFields(rowMeta, null, null, null, null);
      fail("expected exception");
    } catch (Exception ex) {
      assertEquals(
          "\n" + BaseMessages.getString(HadoopExitMeta.class, "Error.InvalidValueField", meta.getOutValueFieldname())
              + "\n", ex.getMessage());
    }

    // Check that the meta was not modified
    assertEquals(2, rowMeta.getValueMetaList().size());
    ValueMetaInterface vmi = rowMeta.getValueMeta(0);
    assertEquals("key", vmi.getName());
    vmi = rowMeta.getValueMeta(1);
    assertEquals("value", vmi.getName());
  }

}
