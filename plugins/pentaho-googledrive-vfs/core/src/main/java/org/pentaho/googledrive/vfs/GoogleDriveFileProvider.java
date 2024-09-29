/*!
* Copyright (C) 2017 by Hitachi Vantara : http://www.pentaho.com
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.pentaho.googledrive.vfs;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;

public class GoogleDriveFileProvider extends AbstractOriginatingFileProvider {

  public static final String SCHEME = "googledrive";
  public static final String DISPLAY_NAME = "Google Drive";

  protected static final Collection<Capability>
      capabilities =
      Collections.unmodifiableCollection( Arrays.asList(
          new Capability[] { Capability.CREATE, Capability.DELETE, Capability.RENAME, Capability.GET_TYPE,
              Capability.LIST_CHILDREN, Capability.READ_CONTENT, Capability.URI, Capability.WRITE_CONTENT,
              Capability.GET_LAST_MODIFIED, Capability.RANDOM_ACCESS_READ } ) );

  public FileSystem doCreateFileSystem( FileName fileName, FileSystemOptions fileSystemOptions )
      throws FileSystemException {
    return new GoogleDriveFileSystem( fileName, fileSystemOptions );
  }

  public Collection<Capability> getCapabilities() {
    return capabilities;
  }
}
