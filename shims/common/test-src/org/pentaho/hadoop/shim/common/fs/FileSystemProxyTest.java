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

package org.pentaho.hadoop.shim.common.fs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.junit.Test;
import org.pentaho.hadoop.shim.api.fs.Path;

public class FileSystemProxyTest {

  @Test(expected=NullPointerException.class)
  public void instantiation_null_delegate() {
    new FileSystemProxy(null);
  }
  
  private Configuration getLocalFileSystemConfiguration() {
    Configuration c = new Configuration();
    c.set("fs.default.name", "file:///");
    return c;
  }
  
  @Test
  public void getDelegate() throws IOException {
    FileSystem delegate = FileSystem.get(new Configuration());
    FileSystemProxy proxy = new FileSystemProxy(delegate);
    assertEquals(delegate, proxy.getDelegate());
  }
  
  @Test
  public void asPath_String() throws IOException, URISyntaxException { 
    Configuration c = getLocalFileSystemConfiguration();
    FileSystem delegate = FileSystem.get(c);
    FileSystemProxy proxy = new FileSystemProxy(delegate);
    Path p = proxy.asPath("/");
    assertNotNull(p);
    assertEquals(new URI("/"), p.toUri());
  }
  
  @Test
  public void asPath_Path_String() throws IOException, URISyntaxException { 
    Configuration c = getLocalFileSystemConfiguration();
    FileSystem delegate = FileSystem.get(c);
    FileSystemProxy proxy = new FileSystemProxy(delegate);
    Path p = proxy.asPath("/");
    assertNotNull(p);
    assertEquals(new URI("/"), p.toUri());
    
    Path test = proxy.asPath(p, "test");
    assertNotNull(test);
    assertEquals(new URI("/test"), test.toUri());
  }

  @Test
  public void asPath_String_String() throws IOException, URISyntaxException {
    Configuration c = getLocalFileSystemConfiguration();
    FileSystem delegate = FileSystem.get(c);
    FileSystemProxy proxy = new FileSystemProxy(delegate);
    Path p = proxy.asPath("/", "test");
    assertNotNull(p);
    assertEquals(new URI("/test"), p.toUri());
  }
  
  @Test
  public void asPath_exists() throws IOException, URISyntaxException {
    Configuration c = getLocalFileSystemConfiguration();
    FileSystem delegate = FileSystem.get(c);
    FileSystemProxy proxy = new FileSystemProxy(delegate);
    assertTrue(proxy.exists(proxy.asPath("/")));
  }
  
  @Test
  public void asPath_delete() throws IOException, URISyntaxException {
    Configuration c = getLocalFileSystemConfiguration();
    
    File tmp = File.createTempFile(FileSystemProxyTest.class.getSimpleName(), null);
    
    FileSystem delegate = FileSystem.get(c);
    FileSystemProxy proxy = new FileSystemProxy(delegate);
    Path p = proxy.asPath(tmp.getAbsolutePath());
    assertTrue(proxy.exists(p));
    proxy.delete(p, true);
    assertFalse(proxy.exists(p));
    assertFalse(tmp.exists());
  }
}
