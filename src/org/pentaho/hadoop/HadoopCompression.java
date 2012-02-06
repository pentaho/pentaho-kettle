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
 * @version $Revision$
 */
public class HadoopCompression {
  
  public static String SNAPPY_CODEC_CLASS = "org.apache.hadoop.io.compress.SnappyCodec";
  public static final String IO_COMPRESSION_CODEC_SNAPPY_BUFFERSIZE_KEY =
    "io.compression.codec.snappy.buffersize";
  public static final int IO_COMPRESSION_CODEC_SNAPPY_DEFAULT_BUFFERSIZE =
    256 * 1024;

  /**
   * Tests whether hadoop-snappy (not to be confused with other java-based
   * snappy implementations such as jsnappy or snappy-java) plus the 
   * native snappy libraries are available.
   * 
   * @return true if hadoop-snappy is available on the classpath
   */
  public static boolean isHadoopSnappyAvailable() {
    try {
      Object snappyCodec = Class.forName(SNAPPY_CODEC_CLASS).newInstance();
      Class<?> confClass = Class.forName("org.apache.hadoop.conf.Configuration").newInstance().getClass();
      Class<?>[] paramClass = new Class[1];
      paramClass[0] = confClass;

      Method m = snappyCodec.getClass().getMethod("isNativeSnappyLoaded", paramClass);
      Object aConf = Class.forName("org.apache.hadoop.conf.Configuration").newInstance();
      Object result = m.invoke(snappyCodec, aConf);

      return ((Boolean)result).booleanValue();
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
  public static InputStream getSnappyInputStream(int bufferSize, InputStream in) 
    throws Exception {
    if (!isHadoopSnappyAvailable()) {
      throw new Exception("Hadoop-snappy does not seem to be available");
    }

    Object snappyCodec = Class.forName(SNAPPY_CODEC_CLASS).newInstance();
    Class<?> confClass = Class.forName("org.apache.hadoop.conf.Configuration").newInstance().getClass();
    Class<?>[] paramClass = new Class[1];
    paramClass[0] = confClass;
    
    Object newConf = Class.forName("org.apache.hadoop.conf.Configuration").newInstance();

    Object[] args = new Object[2];
    args[0] = IO_COMPRESSION_CODEC_SNAPPY_BUFFERSIZE_KEY;
    args[1] = "" + bufferSize;
    Method cm = confClass.getMethod("set", new Class[] {String.class, String.class});
    cm.invoke(newConf, args);
    
    Method m = snappyCodec.getClass().getMethod("setConf", paramClass);
    m.invoke(snappyCodec, newConf);

    paramClass[0] = Class.forName("java.io.InputStream");
    m = snappyCodec.getClass().getMethod("createInputStream", paramClass);
    Object result = m.invoke(snappyCodec, in);

    return (InputStream)result;
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
  public static OutputStream getSnappyOutputStream(int bufferSize, OutputStream out)
    throws Exception {
    if (!isHadoopSnappyAvailable()) {
      throw new Exception("Hadoop-snappy does not seem to be available");
    }
    
    Object snappyCodec = Class.forName(SNAPPY_CODEC_CLASS).newInstance();    
    Class<?> confClass = Class.forName("org.apache.hadoop.conf.Configuration").newInstance().getClass();
    Class<?>[] paramClass = new Class[1];
    paramClass[0] = confClass;
    
    Object newConf = Class.forName("org.apache.hadoop.conf.Configuration").newInstance();    
    
    Object[] args = new Object[2];
    args[0] = IO_COMPRESSION_CODEC_SNAPPY_BUFFERSIZE_KEY;
    args[1] = "" + bufferSize;
    Method cm = confClass.getMethod("set", new Class[] {String.class, String.class});
    cm.invoke(newConf, args);
    
    Method m = snappyCodec.getClass().getMethod("setConf", paramClass);
    m.invoke(snappyCodec, newConf);
    
    paramClass[0] = Class.forName("java.io.OutputStream");
    m = snappyCodec.getClass().getMethod("createOutputStream", paramClass);
    Object result = m.invoke(snappyCodec, out);
    
    return (OutputStream)result;
  }

  public static void main(String[] args) {
    try {
      FileInputStream fis = new FileInputStream(args[0]);
      //      Object newConf = Class.forName("org.apache.hadoop.conf.Configuration");    
      InputStream cis = HadoopCompression.getSnappyInputStream(1024 * 256, fis);
      BufferedInputStream bis = new BufferedInputStream(cis);
      InputStreamReader isr = new InputStreamReader(bis);

      // String tempLine = null;

      int c = 0;

      while ((c = isr.read()) >=0) {
        System.out.print((char)c);
      }      
      isr.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
