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

import java.util.Arrays;

public class SelectionAdapterOptions {
  private SelectionOperation selectionOperation;
  private String[] filters;
  private String defaultFilter;
  private String[] providerFilters;
  private boolean useSchemaPath;

  public SelectionAdapterOptions( SelectionOperation selectionOperation, String[] filters, String defaultFilter,
                                  String[] providerFilters, boolean useSchemaPath  ) {
    this.selectionOperation = selectionOperation;
    this.filters = filters;
    this.defaultFilter = defaultFilter;
    this.providerFilters = providerFilters;
    this.useSchemaPath = useSchemaPath;
  }

  public SelectionAdapterOptions( SelectionOperation selectionOperation, String[] filters, String defaultFilter ) {
    this.selectionOperation = selectionOperation;
    this.filters = filters;
    this.defaultFilter = defaultFilter;
  }

  public SelectionAdapterOptions( SelectionOperation selectionOperation ) {
    this.selectionOperation = selectionOperation;
  }

  public SelectionOperation getSelectionOperation() {
    return selectionOperation;
  }

  public SelectionAdapterOptions setSelectionOperation( SelectionOperation selectionOperation ) {
    this.selectionOperation = selectionOperation;
    return this;
  }

  public String[] getFilters() {
    return filters;
  }

  public SelectionAdapterOptions setFilters( String[] filters ) {
    this.filters = filters;
    return this;
  }

  public SelectionAdapterOptions setFilters( SelectionAdapterFileDialog.FilterType[] filters ) {
    this.filters = Arrays.stream( filters ).map( Enum::toString ).toArray( String[]::new );
    return this;
  }

  public String getDefaultFilter() {
    return defaultFilter;
  }

  public SelectionAdapterOptions setDefaultFilter( String defaultFilter ) {
    this.defaultFilter = defaultFilter;
    return this;
  }

  public String[] getProviderFilters() {
    return providerFilters;
  }

  public SelectionAdapterOptions setProviderFilters( String[] providerFilters ) {
    this.providerFilters = providerFilters;
    return this;
  }

  public boolean getUseSchemaPath() {
    return useSchemaPath;
  }

  public SelectionAdapterOptions setUseSchemaPath( boolean useSchemaPath ) {
    this.useSchemaPath = useSchemaPath;
    return this;
  }
}
