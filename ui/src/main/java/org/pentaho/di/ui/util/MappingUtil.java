/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.core.SourceToTargetMapping;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;

import java.util.ArrayList;
import java.util.List;

public class MappingUtil {

  private MappingUtil() {
  }

  public static List<SourceToTargetMapping> getCurrentMappings( List<String> sourceFields, List<String> targetFields, List<MappingValueRename> mappingValues ) {
    List<SourceToTargetMapping> sourceToTargetMapping = new ArrayList<>(  );

    if ( sourceFields == null || targetFields == null || mappingValues == null ) {
      return sourceToTargetMapping;
    }
    if ( !mappingValues.isEmpty() ) {
      for ( MappingValueRename mappingValue : mappingValues ) {
        String source = mappingValue.getSourceValueName();
        String target = mappingValue.getTargetValueName();
        int sourceIndex = sourceFields.indexOf( source );
        int targetIndex = targetFields.indexOf( target );
        sourceToTargetMapping.add( new SourceToTargetMapping( sourceIndex, targetIndex ) );
      }
    }
    return sourceToTargetMapping;
  }
}
