/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.step.mqtt;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

/**
 * Helper class for converting SslConfig info between maps <=> lists
 */
public class SslConfigHelper {

  private List<String> sslKeys;
  private List<String> sslValues;

  static SslConfigHelper conf( List<String> sslKeys, List<String> sslValues ) {
    SslConfigHelper helper = new SslConfigHelper();
    helper.sslKeys = sslKeys;
    helper.sslValues = sslValues;
    return helper;
  }

  static SslConfigHelper conf( Map<String, String> sslConfig ) {
    checkNotNull( sslConfig );
    SslConfigHelper helper = new SslConfigHelper();
    helper.sslKeys = newArrayList(
      sslConfig.keySet().stream().sorted().collect( toList() ) );
    helper.sslValues = helper.sslKeys.stream()
      .map( sslConfig::get )
      .collect( toList() );
    return helper;
  }

  public Map<String, String> asMap() {
    checkState( sslKeys != null
      && sslValues != null
      && sslKeys.size() == sslValues.size() );
    return Maps.toMap( sslKeys, key -> sslValues.get( sslKeys.indexOf( key ) ) );
  }

  public List<String> keys() {
    return sslKeys;
  }

  public List<String> vals() {
    return sslValues;
  }
}
