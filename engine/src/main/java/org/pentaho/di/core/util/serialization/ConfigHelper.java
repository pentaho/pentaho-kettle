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


package org.pentaho.di.core.util.serialization;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

/**
 * Helper class for converting Config info between maps <=> lists
 */
public class ConfigHelper {

  private List<String> keys;
  private List<String> values;

  public static ConfigHelper conf( List<String> keys, List<String> values ) {
    ConfigHelper helper = new ConfigHelper();
    helper.keys = keys;
    helper.values = values;
    return helper;
  }

  public static ConfigHelper conf( Map<String, String> config ) {
    checkNotNull( config );
    ConfigHelper helper = new ConfigHelper();
    helper.keys = newArrayList(
      config.keySet().stream().sorted().collect( toList() ) );
    helper.values = helper.keys.stream()
      .map( config::get )
      .collect( toList() );
    return helper;
  }

  public Map<String, String> asMap() {
    checkState( keys != null
      && values != null
      && keys.size() == values.size() );
    return keys.stream().collect( Collectors.toMap( Function.identity(), key -> values.get( keys.indexOf( key ) ) ) );
  }

  public List<String> keys() {
    return keys;
  }

  public List<String> vals() {
    return values;
  }
}
