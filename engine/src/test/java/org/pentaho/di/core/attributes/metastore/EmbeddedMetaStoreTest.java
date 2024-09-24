/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.attributes.metastore;

import com.google.common.collect.Maps;
import org.pentaho.di.core.AttributesInterface;
import org.pentaho.metastore.test.MetaStoreTestBase;

import java.util.Map;

/**
 * @author nhudak
 */
public class EmbeddedMetaStoreTest extends MetaStoreTestBase {
  private EmbeddedMetaStore metaStore;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    metaStore = new EmbeddedMetaStore( new TestStorage() );
    metaStore.setName( META_STORE_NAME );
  }

  public void test() throws Exception {
    super.testFunctionality( metaStore );
  }

  public void testParrallelRetrive() throws Exception {
    super.testParallelOneStore( metaStore );
  }

  private class TestStorage implements AttributesInterface {
    private Map<String, Map<String, String>> attributesMap = Maps.newHashMap();

    @Override
    public void setAttributesMap( Map<String, Map<String, String>> attributesMap ) {
      this.attributesMap = attributesMap;
    }

    @Override
    public Map<String, Map<String, String>> getAttributesMap() {
      return attributesMap;
    }

    @Override
    public void setAttribute( String groupName, String key, String value ) {
      Map<String, String> attributes = getAttributes( groupName );
      if ( attributes == null ) {
        attributes = Maps.newHashMap();
        attributesMap.put( groupName, attributes );
      }
      attributes.put( key, value );
    }

    @Override
    public void setAttributes( String groupName, Map<String, String> attributes ) {
      attributesMap.put( groupName, attributes );
    }

    @Override
    public Map<String, String> getAttributes( String groupName ) {
      return attributesMap.get( groupName );
    }

    @Override
    public String getAttribute( String groupName, String key ) {
      Map<String, String> attributes = attributesMap.get( groupName );
      if ( attributes == null ) {
        return null;
      }
      return attributes.get( key );
    }
  }
}
