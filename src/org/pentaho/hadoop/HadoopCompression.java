/*******************************************************************************
 *
 * Pentaho Data Integration
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

package org.pentaho.hadoop;

import java.io.*;
import java.lang.reflect.Method;

/**
 * Helper class for determining (via reflection) whether various 
 * Hadoop compression codecs (such as Snappy) are available on
 * the classpath and for returning Input/Output streams for 
 * reading/writing.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 */
public class HadoopCompression {

  public static final int IO_COMPRESSION_CODEC_SNAPPY_DEFAULT_BUFFERSIZE = 256 * 1024;

  private static final String HADOOP_CONFIG_UTIL_CLASS = "org.apache.hadoop.hive.jdbc.HadoopConfigurationUtil";

  private static final String GET_ACTIVE_CONFIGURATION_METHOD = "getActiveConfiguration";

  private static final String GET_SNAPPY_SHIM = "getSnappyShim";

  /**
   * Locate the Snappy Shim for the active Hadoop Configuration via the Hadoop Configuration Util
   * @return A {@link org.pentaho.hadoop.shim.spi.SnappyShim} to interact with Snappy
   * @throws Exception Error locating a valid Snappy shim:
   * <p>
   *   <ul>
   *     <li>{@link org.pentaho.hadoop.hive.jdbc.HadoopConfigurationUtil} could not be located</li>
   *     <li>No active Hadoop configuration</li>
   *     <li>Active Hadoop configuration doesn't support Snappy</li>
   *   </ul>
   * </p>
   */
  private static Object getActiveSnappyShim() throws Exception {
    Class<?> hadoopConfigUtilClass = Class.forName(HADOOP_CONFIG_UTIL_CLASS);
    Method getActiveConfiguration = hadoopConfigUtilClass.getMethod(GET_ACTIVE_CONFIGURATION_METHOD);
    Object hadoopConfiguration = getActiveConfiguration.invoke(null);
    Method getSnappyShim = hadoopConfiguration.getClass().getMethod(GET_SNAPPY_SHIM);
    return getSnappyShim.invoke(hadoopConfiguration);
  }

  /**
   * Tests whether hadoop-snappy (not to be confused with other java-based
   * snappy implementations such as jsnappy or snappy-java) plus the 
   * native snappy libraries are available.
   * 
   * @return true if hadoop-snappy is available on the classpath
   */
  public static boolean isHadoopSnappyAvailable() {
    try {
      Object snappyShim = getActiveSnappyShim();
      Method m = snappyShim.getClass().getMethod("isHadoopSnappyAvailable");
      return ((Boolean) m.invoke(snappyShim)).booleanValue();
    } catch (Exception ex) {
      return false;
    }
  }

  /**
   * Gets a CompressionInputStream that uses the snappy codec and 
   * wraps the supplied base input stream.
   * 
   * @param in the base input stream to wrap around
   * @return an InputStream that uses the Snappy codec
   * 
   * @throws Exception if snappy is not available or an error occurs during
   * reflection
   */
  public static InputStream getSnappyInputStream(InputStream in) throws Exception {
    return getSnappyInputStream(IO_COMPRESSION_CODEC_SNAPPY_DEFAULT_BUFFERSIZE, in);
  }

  /**
   * Gets an InputStream that uses the snappy codec and 
   * wraps the supplied base input stream.
   * 
   * @param the buffer size for the codec to use (in bytes)
   * @param in the base input stream to wrap around
   * @return an InputStream that uses the Snappy codec
   * 
   * @throws Exception if snappy is not available or an error occurs during
   * reflection
   */
  public static InputStream getSnappyInputStream(int bufferSize, InputStream in) throws Exception {
    if (!isHadoopSnappyAvailable()) {
      throw new Exception("Hadoop-snappy does not seem to be available");
    }

    Object snappyShim = getActiveSnappyShim();
    Method getSnappyInputStream = snappyShim.getClass().getMethod("getSnappyInputStream", int.class, InputStream.class);
    return (InputStream) getSnappyInputStream.invoke(snappyShim, bufferSize, in);
  }

  /**
   * Gets an OutputStream that uses the snappy codec and 
   * wraps the supplied base output stream.
   * 
   * @param the buffer size for the codec to use (in bytes)
   * @param out the base output stream to wrap around
   * @return a OutputStream that uses the Snappy codec
   * 
   * @throws Exception if snappy is not available or an error occurs during
   * reflection
   */
  public OutputStream getSnappyOutputStream(OutputStream out) throws Exception {
    return getSnappyOutputStream(IO_COMPRESSION_CODEC_SNAPPY_DEFAULT_BUFFERSIZE, out);
  }

  /**
   * Gets an OutputStream that uses the snappy codec and 
   * wraps the supplied base output stream.
   * 
   * @param the buffer size for the codec to use (in bytes)
   * 
   * @param out the base output stream to wrap around
   * @return a OutputStream that uses the Snappy codec
   * 
   * @throws Exception if snappy is not available or an error occurs during
   * reflection
   */
  public static OutputStream getSnappyOutputStream(int bufferSize, OutputStream out) throws Exception {
    if (!isHadoopSnappyAvailable()) {
      throw new Exception("Hadoop-snappy does not seem to be available");
    }

    Object snappyShim = getActiveSnappyShim();
    Method getSnappyOutputStream = snappyShim.getClass().getMethod("getSnappyOutputStream", int.class,
        OutputStream.class);
    return (OutputStream) getSnappyOutputStream.invoke(snappyShim, bufferSize, out);
  }
}
