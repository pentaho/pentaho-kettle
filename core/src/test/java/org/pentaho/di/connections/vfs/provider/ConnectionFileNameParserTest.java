package org.pentaho.di.connections.vfs.provider;

import org.junit.Assert;
import org.junit.Test;

public class ConnectionFileNameParserTest {

  private static final String CONNECTION_TEST = "Connection Test";
  private static final String SOME_FILE_PATH_TXT = "/some/file/path.txt";

  @Test
  public void testExtractToPath() throws Exception {
    ConnectionFileNameParser connectionFileNameParser = new ConnectionFileNameParser();
    ConnectionFileName fileName = (ConnectionFileName) connectionFileNameParser.parseUri( null, null,
      "pvfs://" + CONNECTION_TEST + SOME_FILE_PATH_TXT );
    String connection = fileName.getConnection();

    Assert.assertEquals( CONNECTION_TEST, connection );
    Assert.assertEquals( SOME_FILE_PATH_TXT, fileName.getPath() );
  }

}
