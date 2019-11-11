/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.ui.core.events.dialog;

import org.junit.Test;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterFileDialog.FilterType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class SelectionAdapterOptionsTest {

  @Test
  public void testOptionsSetter() {

    SelectionAdapterOptions opts = new SelectionAdapterOptions( SelectionOperation.FILE, new String[] { }, "",
      new String[] {}, false );

    opts.setFilters( new FilterType[] { FilterType.ALL, FilterType.CUBE, FilterType.TXT} ).setDefaultFilter( FilterType.CUBE.toString() );

    assertArrayEquals( new String[] {"ALL", "CUBE", "TXT"}, opts.getFilters() );
    assertEquals( "CUBE", opts.getDefaultFilter() );

    opts.setFilters( new String[] { FilterType.ALL.toString(), FilterType.CUBE.toString(), FilterType.TXT.toString()} );
    assertArrayEquals( new String[] {"ALL", "CUBE", "TXT"}, opts.getFilters() );

    opts.setProviderFilters( new String[] { "VFS", "LOCAL" } );
    assertArrayEquals( new String[] {"VFS", "LOCAL"}, opts.getProviderFilters() );

    opts.setSelectionOperation( SelectionOperation.FOLDER ).setUseSchemaPath( true );
    assertEquals( SelectionOperation.FOLDER, opts.getSelectionOperation() );
    assertTrue( opts.getUseSchemaPath() );
  }
}
