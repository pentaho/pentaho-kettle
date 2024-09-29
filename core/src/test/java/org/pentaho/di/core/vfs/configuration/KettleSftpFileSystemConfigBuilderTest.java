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

package org.pentaho.di.core.vfs.configuration;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.lang.reflect.Method;

import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.sftp.IdentityInfo;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.springframework.util.ReflectionUtils;

/**
 * @author Andrey Khayrutdinov
 */
public class KettleSftpFileSystemConfigBuilderTest {

  @Test
  public void recognizesAndSetsUserHomeDirProperty() throws Exception {
    final String fullName = Const.VFS_USER_DIR_IS_ROOT;
    final String name = fullName.substring( "vfs.sftp.".length() );
    final String vfsInternalName = SftpFileSystemConfigBuilder.class.getName() + ".USER_DIR_IS_ROOT";

    final FileSystemOptions opts = new FileSystemOptions();
    KettleSftpFileSystemConfigBuilder builder = KettleSftpFileSystemConfigBuilder.getInstance();
    builder.setParameter( opts, name, "true", fullName, "sftp://fake-url:22" );

    Method getOption = ReflectionUtils.findMethod( opts.getClass(), "getOption", Class.class, String.class );
    getOption.setAccessible( true );
    Object value = ReflectionUtils.invokeMethod( getOption, opts, builder.getConfigClass(), vfsInternalName );
    assertEquals( true, value );
  }

  @Test
  public void recognizesAndSetsIdentityKeyFile() throws Exception {
    File tempFile = File.createTempFile( "KettleSftpFileSystemConfigBuilderTest", ".tmp" );
    tempFile.deleteOnExit();

    final String fullName = "vfs.sftp.identity";
    final String name = fullName.substring( "vfs.sftp.".length() );
    final String vfsInternalName = SftpFileSystemConfigBuilder.class.getName() + ".IDENTITIES";

    final FileSystemOptions opts = new FileSystemOptions();
    KettleSftpFileSystemConfigBuilder builder = KettleSftpFileSystemConfigBuilder.getInstance();
    builder.setParameter( opts, name, tempFile.getAbsolutePath(), fullName, "sftp://fake-url:22" );

    Method getOption = ReflectionUtils.findMethod( opts.getClass(), "getOption", Class.class, String.class );
    getOption.setAccessible( true );
    Object value = ReflectionUtils.invokeMethod( getOption, opts, builder.getConfigClass(), vfsInternalName );
    assertEquals( IdentityInfo[].class, value.getClass() );
    assertEquals( tempFile.getAbsolutePath(), ( (IdentityInfo[]) value )[0].getPrivateKey().getAbsolutePath() );
  }
}
