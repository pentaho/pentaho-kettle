/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.connections.common.domainbuckets;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;

import java.io.InputStream;

public class TestFileWithDomainAndBucketsObject extends AbstractFileObject<TestFileWithDomainAndBucketsSystem> {

  public TestFileWithDomainAndBucketsObject( AbstractFileName name,
                                             TestFileWithDomainAndBucketsSystem fs ) {
    super( name, fs );
  }

  @Override public boolean exists() throws FileSystemException {
    return true;
  }

  @Override protected long doGetContentSize() throws Exception {
    return 0;
  }

  @Override protected InputStream doGetInputStream() throws Exception {
    return null;
  }

  @Override protected FileType doGetType() throws Exception {
    return FileType.FILE;
  }

  @Override protected String[] doListChildren() throws Exception {
    return new String[] { "file1", "file2", "file3" };
  }
}
