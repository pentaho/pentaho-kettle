/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
