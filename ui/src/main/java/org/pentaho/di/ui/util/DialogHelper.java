/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2022-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.util;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.events.dialog.FilterType;
import org.pentaho.di.ui.core.events.dialog.ProviderFilterType;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterFileDialogTextVar;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterOptions;
import org.pentaho.di.ui.core.events.dialog.SelectionOperation;
import org.pentaho.di.ui.core.widget.TextVar;

/**
 * Created by bmorrise on 8/17/17.
 */
public class DialogHelper {
  public static SelectionAdapterFileDialogTextVar constructSelectionAdapterFileDialogTextVarForKettleFile( LogChannel log
                                                                                                           , TextVar wPath, AbstractMeta meta, SelectionOperation selectionOperation, FilterType filterType, Repository repository ) {
    if ( repository != null ) {
      ProviderFilterType[] providerFilterTypes = new ProviderFilterType[2];
      providerFilterTypes[0] = ProviderFilterType.RECENTS;
      providerFilterTypes[1] = ProviderFilterType.REPOSITORY;
      return new SelectionAdapterFileDialogTextVar( log, wPath, meta,
                                                    new SelectionAdapterOptions( meta.getBowl(), selectionOperation,
                                                                                 new FilterType[] { filterType,
        FilterType.XML, FilterType.ALL },
                                                                                 filterType, providerFilterTypes ) );
    }
    return new SelectionAdapterFileDialogTextVar( log, wPath, meta,
                                                  new SelectionAdapterOptions( meta.getBowl(), selectionOperation,
                                                                               new FilterType[] { filterType, FilterType.XML, FilterType.ALL }, filterType ) );
  }

  public static SelectionAdapterFileDialogTextVar constructSelectionAdapterFileDialogTextVarForUserFile( LogChannel log
                                                                                                         , TextVar wPath, AbstractMeta meta, SelectionOperation selectionOperation, FilterType[] filterTypes
                                                                                                         , FilterType defaultFilterType ) {
    ProviderFilterType[] providerFilterTypes = new ProviderFilterType[1];
    providerFilterTypes[0] = ProviderFilterType.DEFAULT;
    return new SelectionAdapterFileDialogTextVar( log, wPath, meta, new SelectionAdapterOptions( meta.getBowl(),
                                                                                                 selectionOperation, filterTypes, defaultFilterType, providerFilterTypes ) );
  }
}
