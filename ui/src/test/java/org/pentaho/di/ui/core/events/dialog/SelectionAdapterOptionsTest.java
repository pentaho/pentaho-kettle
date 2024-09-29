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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

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

    opts.setProviderFilters( new ProviderFilterType[] { ProviderFilterType.VFS, ProviderFilterType.LOCAL } );
    assertArrayEquals( new String[] {"vfs", "local" }, opts.getProviderFilters()  );

    opts.setSelectionOperation( SelectionOperation.FOLDER ).setUseSchemaPath( true );
    assertEquals( SelectionOperation.FOLDER, opts.getSelectionOperation() );
    assertTrue( opts.getUseSchemaPath() );
  }

  @Test
  public void toStringArray() {
    FilterType[] filterTypes = new FilterType[] { FilterType.ALL, FilterType.CSV, FilterType.TXT };
    String[] expectedFilterTypes =  new String[] { "ALL", "CSV", "TXT" };
    assertArrayEquals( expectedFilterTypes, SelectionAdapterOptions.toStringArray( filterTypes ) );
  }

  @Test
  public void setFilters() {
    FilterType[] filterTypes = new FilterType[] { FilterType.ALL, FilterType.CSV, FilterType.TXT };
    String[] expectedFilterTypes =  new String[] { "ALL", "CSV", "TXT" };

    SelectionAdapterOptions selectionAdapterOptions = new SelectionAdapterOptions( SelectionOperation.FILE );
    selectionAdapterOptions.setFilters( filterTypes );

    assertArrayEquals( expectedFilterTypes, selectionAdapterOptions.getFilters() );
  }

  @Test
  public void testSelectionAdapterOptions() {
    SelectionOperation selectionOperation = SelectionOperation.FILE;
    FilterType[] filterTypes = new FilterType[] { FilterType.ALL, FilterType.CSV, FilterType.TXT };
    FilterType defaultFilter = FilterType.CSV;
    String[] providerFilters = new String[] { "local" };
    boolean useSchemaPath = true;
    String[] expectedFilterTypes =  new String[] { "ALL", "CSV", "TXT" };

    SelectionAdapterOptions selectionAdapterOptions = new SelectionAdapterOptions( selectionOperation, filterTypes,
        defaultFilter, providerFilters, useSchemaPath );

    assertEquals( SelectionOperation.FILE, selectionAdapterOptions.getSelectionOperation() );
    assertArrayEquals( expectedFilterTypes, selectionAdapterOptions.getFilters() );
    assertEquals( defaultFilter.toString(), selectionAdapterOptions.getDefaultFilter() );
    assertArrayEquals( providerFilters , selectionAdapterOptions.getProviderFilters() );
    assertTrue( selectionAdapterOptions.getUseSchemaPath() );
  }

  @Test
  public void testSelectionAdapterOptions2() {
    SelectionOperation selectionOperation = SelectionOperation.FILE;
    String[] filterTypes =  new String[] { "ALL", "CSV", "TXT" };
    String defaultFilter = "TXT";
    String[] providerFilters = new String[] { "local" };
    boolean useSchemaPath = true;

    SelectionAdapterOptions selectionAdapterOptions = new SelectionAdapterOptions( selectionOperation, filterTypes,
      defaultFilter, providerFilters, useSchemaPath );

    assertEquals( SelectionOperation.FILE, selectionAdapterOptions.getSelectionOperation() );
    assertArrayEquals( filterTypes, selectionAdapterOptions.getFilters() );
    assertEquals( defaultFilter, selectionAdapterOptions.getDefaultFilter() );
    assertArrayEquals( providerFilters, selectionAdapterOptions.getProviderFilters() );
    assertTrue( selectionAdapterOptions.getUseSchemaPath() );
  }

  @Test
  public void testSelectionAdapterOptions3() {
    SelectionOperation selectionOperation = SelectionOperation.FILE;
    FilterType[] filterTypes = new FilterType[] { FilterType.ALL, FilterType.CSV, FilterType.TXT };
    FilterType defaultFilter = FilterType.CSV;
    ProviderFilterType[] providerFilters = new ProviderFilterType[] { ProviderFilterType.LOCAL };
    String[] expectedProviderFilters = new String[] { "local" };
    String[] expectedFilterTypes =  new String[] { "ALL", "CSV", "TXT" };

    SelectionAdapterOptions selectionAdapterOptions = new SelectionAdapterOptions( selectionOperation, filterTypes,
      defaultFilter, providerFilters );

    assertEquals( SelectionOperation.FILE, selectionAdapterOptions.getSelectionOperation() );
    assertArrayEquals( expectedFilterTypes, selectionAdapterOptions.getFilters() );
    assertEquals( defaultFilter.toString(), selectionAdapterOptions.getDefaultFilter() );
    assertArrayEquals( expectedProviderFilters, selectionAdapterOptions.getProviderFilters() );
    assertFalse( selectionAdapterOptions.getUseSchemaPath() );
  }

  @Test
  public void testToString_FilterType() {
    assertNull( SelectionAdapterOptions.toString( null ) );
    assertEquals( "TXT", SelectionAdapterOptions.toString( FilterType.TXT ) );
  }
}
