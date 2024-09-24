/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.common.basic;

import org.pentaho.di.connections.vfs.BaseVFSConnectionProvider;
import org.pentaho.di.connections.vfs.VFSRoot;

import java.util.List;

public class TestBasicConnectionProvider extends BaseVFSConnectionProvider<TestBasicConnectionDetails> {
  public static final String NAME = "Test";
  public static final String SCHEME = "test4";

  @Override public String getName() {
    return NAME;
  }

  @Override public String getKey() {
    return SCHEME;
  }

  @Override public List<VFSRoot> getLocations( TestBasicConnectionDetails vfsConnectionDetails ) {
    return null;
  }

  @Override public String getProtocol( TestBasicConnectionDetails vfsConnectionDetails ) {
    return SCHEME;
  }

  @Override public Class<TestBasicConnectionDetails> getClassType() {
    return TestBasicConnectionDetails.class;
  }

  @Override public boolean test( TestBasicConnectionDetails connectionDetails ) {
    return true;
  }
}
