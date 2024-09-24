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

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs2.provider.local.GenericFileNameParser;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class TestBasicFileProvider extends AbstractOriginatingFileProvider {

  public static String SCHEME = "test4";
  public static String SCHEME_NAME = "Test File System";

  protected static final Collection<Capability>
    capabilities = Collections.unmodifiableCollection( Arrays.asList( Capability.CREATE, Capability.DELETE,
    Capability.RENAME, Capability.GET_TYPE, Capability.LIST_CHILDREN, Capability.READ_CONTENT, Capability.URI,
    Capability.WRITE_CONTENT, Capability.GET_LAST_MODIFIED, Capability.RANDOM_ACCESS_READ ) );

  public TestBasicFileProvider() {
    setFileNameParser( new GenericFileNameParser() );
  }

  @Override protected FileSystem doCreateFileSystem( FileName fileName, FileSystemOptions fileSystemOptions )
    throws FileSystemException {
    return new TestBasicFileSystem( fileName, fileSystemOptions );
  }

  @Override public Collection<Capability> getCapabilities() {
    return capabilities;
  }
}
