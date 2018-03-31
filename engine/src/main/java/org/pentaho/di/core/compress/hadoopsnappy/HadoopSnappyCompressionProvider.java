/*******************************************************************************
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

package org.pentaho.di.core.compress.hadoopsnappy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import org.pentaho.di.core.compress.CompressionProvider;

public class HadoopSnappyCompressionProvider implements CompressionProvider {

  public static final int IO_COMPRESSION_CODEC_SNAPPY_DEFAULT_BUFFERSIZE = 256 * 1024;

  private static final String HADOOP_CONFIG_UTIL_CLASS_PROPERTY = "hadoop.config.util.class";

  private static final String[] HADOOP_CONFIG_UTIL_CLASS = { "org.apache.hadoop.hive.jdbc.HadoopConfigurationUtil",
    "org.pentaho.hadoop.hive.jdbc.HadoopConfigurationUtil", };

  private static final String GET_ACTIVE_CONFIGURATION_METHOD = "getActiveConfiguration";

  private static final String GET_SNAPPY_SHIM = "getSnappyShim";

  /**
   * Locate the Snappy Shim for the active Hadoop Configuration via the Hadoop Configuration Util
   *
   * @return A {@link org.pentaho.hadoop.shim.spi.SnappyShim} to interact with Snappy
   * @throws Exception
   *           Error locating a valid Snappy shim:
   *           <p>
   *           <ul>
   *           <li>{@link org.pentaho.hadoop.hive.jdbc.HadoopConfigurationUtil} could not be located</li>
   *           <li>No active Hadoop configuration</li>
   *           <li>Active Hadoop configuration doesn't support Snappy</li>
   *           </ul>
   *           </p>
   */
  public static Object getActiveSnappyShim() throws Exception {
    Class<?> hadoopConfigUtilClass = null;
    String hadoopConfigUtilClassName = System.getProperty( HADOOP_CONFIG_UTIL_CLASS_PROPERTY );
    if ( hadoopConfigUtilClassName != null ) {
      hadoopConfigUtilClass = Class.forName( hadoopConfigUtilClassName );
    } else {
      for ( int i = 0; hadoopConfigUtilClass == null && i < HADOOP_CONFIG_UTIL_CLASS.length; i++ ) {
        try {
          hadoopConfigUtilClass = Class.forName( HADOOP_CONFIG_UTIL_CLASS[i] );
        } catch ( ClassNotFoundException cnfe ) {
          // Nothing to do here but try again
        }
      }
      if ( hadoopConfigUtilClass == null ) {
        throw new Exception( "No Hadoop Configuration Utilities class found, unable to get active Snappy shim" );
      }
    }

    Method getActiveConfiguration = hadoopConfigUtilClass.getMethod( GET_ACTIVE_CONFIGURATION_METHOD );
    Object hadoopConfiguration = getActiveConfiguration.invoke( hadoopConfigUtilClass.newInstance() );
    Method getSnappyShim = hadoopConfiguration.getClass().getMethod( GET_SNAPPY_SHIM );
    return getSnappyShim.invoke( hadoopConfiguration );
  }

  /**
   * Tests whether hadoop-snappy (not to be confused with other java-based snappy implementations such as jsnappy or
   * snappy-java) plus the native snappy libraries are available.
   *
   * @return true if hadoop-snappy is available on the classpath
   */
  public static boolean isHadoopSnappyAvailable() {
    try {
      Object snappyShim = getActiveSnappyShim();
      Method m = snappyShim.getClass().getMethod( "isHadoopSnappyAvailable" );
      return ( (Boolean) m.invoke( snappyShim ) ).booleanValue();
    } catch ( Exception ex ) {
      return false;
    }
  }

  @Override
  public HadoopSnappyCompressionInputStream createInputStream( InputStream in ) throws IOException {
    return new HadoopSnappyCompressionInputStream( in, this );
  }

  @Override
  public boolean supportsInput() {
    return true;
  }

  @Override
  public HadoopSnappyCompressionOutputStream createOutputStream( OutputStream out ) throws IOException {
    return new HadoopSnappyCompressionOutputStream( out, this );
  }

  @Override
  public boolean supportsOutput() {
    return true;
  }

  @Override
  public String getDescription() {
    return "Hadoop Snappy compression";
  }

  @Override
  public String getName() {
    return "Hadoop-snappy";
  }

  @Override
  public String getDefaultExtension() {
    return null;
  }
}
