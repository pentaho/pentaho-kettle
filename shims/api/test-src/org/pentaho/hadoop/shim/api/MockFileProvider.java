/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.hadoop.shim.api;

import java.util.Collection;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.FileProvider;

public class MockFileProvider implements FileProvider {

  @Override
  public FileObject createFileSystem(String arg0, FileObject arg1, FileSystemOptions arg2) throws FileSystemException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FileObject findFile(FileObject arg0, String arg1, FileSystemOptions arg2) throws FileSystemException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection getCapabilities() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FileSystemConfigBuilder getConfigBuilder() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FileName parseUri(FileName arg0, String arg1) throws FileSystemException {
    // TODO Auto-generated method stub
    return null;
  }

}
