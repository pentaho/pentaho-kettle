/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
