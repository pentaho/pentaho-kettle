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


package org.pentaho.di.plugins.fileopensave.controllers.recent;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.plugins.fileopensave.providers.recents.model.RecentFile;
import org.pentaho.di.plugins.fileopensave.providers.recents.model.RecentUtils;

import java.util.Date;

public class RecentUtilsTest {

  private static final String NAMED_CONNECTION = "Named Connection";
  private static final String PVFS_FILENAME = "file.txt";
  private static final String PVFS_PARENT = "pvfs://" + NAMED_CONNECTION + "/path/to";
  private static final String PVFS_PATH = PVFS_PARENT + "/" + PVFS_FILENAME;

  private static final String UNIX_FILENAME = "file.txt";
  private static final String UNIX_PARENT = "/path/to";
  private static final String UNIX_PATH = UNIX_PARENT + "/" + UNIX_FILENAME;

  private static final String WINDOWS_FILENAME = "file.txt";
  private static final String WINDOWS_PARENT = "C:\\path\\to";
  private static final String WINDOWS_PATH = WINDOWS_PARENT + "\\" + WINDOWS_FILENAME;

  @Test
  public void setPathsVFSTest() {
    LastUsedFile lastUsedFile =
      new LastUsedFile( LastUsedFile.FILE_TYPE_TRANSFORMATION, PVFS_PATH, null, false, null, null, false, 1, new Date(),
        NAMED_CONNECTION );

    RecentFile recentFile = new RecentFile();
    RecentUtils.setPaths( lastUsedFile, recentFile );

    Assert.assertEquals( PVFS_FILENAME, recentFile.getName() );
    Assert.assertEquals( PVFS_PARENT, recentFile.getParent() );
    Assert.assertEquals( PVFS_PATH, recentFile.getPath() );
  }

  @Test
  public void setPathsUnixTest() {
    LastUsedFile lastUsedFile =
      new LastUsedFile( LastUsedFile.FILE_TYPE_TRANSFORMATION, UNIX_PATH, null, false, null, null, false, 1, new Date(),
        null );

    RecentFile recentFile = new RecentFile();
    RecentUtils.setPaths( lastUsedFile, recentFile );

    Assert.assertEquals( UNIX_FILENAME, recentFile.getName() );
    Assert.assertEquals( UNIX_PARENT, recentFile.getParent() );
    Assert.assertEquals( UNIX_PATH, recentFile.getPath() );
  }

  @Test
  public void setPathsWindowsTest() {
    LastUsedFile lastUsedFile =
      new LastUsedFile( LastUsedFile.FILE_TYPE_TRANSFORMATION, WINDOWS_PATH, null, false, null, null, false, 1,
        new Date(),
        null );

    RecentFile recentFile = new RecentFile();
    RecentUtils.setPaths( lastUsedFile, recentFile );

    Assert.assertEquals( WINDOWS_FILENAME, recentFile.getName() );
    Assert.assertEquals( WINDOWS_PARENT, recentFile.getParent() );
    Assert.assertEquals( WINDOWS_PATH, recentFile.getPath() );
  }

  @Test
  public void setPathsRepositoryTest() {
    LastUsedFile lastUsedFile =
      new LastUsedFile( LastUsedFile.FILE_TYPE_TRANSFORMATION, "file", "/home/admin", true, "repo", "admin", true, 1,
        new Date(), "" );

    RecentFile recentFile = new RecentFile();
    RecentUtils.setPaths( lastUsedFile, recentFile );

    Assert.assertEquals( "file", recentFile.getName() );
    Assert.assertEquals( "/home/admin", recentFile.getParent() );
    Assert.assertEquals( "/home/admin/file", recentFile.getPath() );
    Assert.assertEquals( "repo", recentFile.getRepository() );
    Assert.assertEquals( "admin", recentFile.getUsername() );
  }

}
