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

import org.apache.hadoop.io.compress.CompressionInputStream;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.apache.hadoop.conf.Configuration;

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

  /**
   * Tests whether hadoop-snappy (not to be confused with other java-based
   * snappy implementations such as jsnappy or snappy-java) plus the 
   * native snappy libraries are available.
   * 
   * @return true if hadoop-snappy is available on the classpath
   */
  public static boolean isHadoopSnappyAvailable() {
    try {
      Object snappyCodec = Class.forName("org.apache.hadoop.io.compress.SnappyCodec").newInstance();
      Class confClass = new Configuration().getClass();
      Class[] paramClass = new Class[1];
      paramClass[0] = confClass;

      Method m = snappyCodec.getClass().getMethod("isNativeSnappyLoaded", paramClass);
      Object result = m.invoke(snappyCodec, new Configuration());

      return ((Boolean)result).booleanValue();
    } catch (Exception ex) {
      return false;
    }
  }

  /**
   * Gets a CompressionInputStream that uses the snappy codec and 
   * wraps the supplied base input stream.
   * 
   * @param conf a Hadoop Configuration object. The client can set the 
   * io.compression.codec.snappy.buffersize property here for controlling
   * the size of the buffer (in bytes) used by the Snappy codec. The default buffer
   * size is 256K.
   * 
   * @param in the base input stream to wrap around
   * @return a CompressionInputStream that uses the Snappy codec
   * 
   * @throws Exception if snappy is not available or an error occurs during
   * reflection
   */
  public static CompressionInputStream getSnappyInputStream(Configuration conf, InputStream in) 
    throws Exception {
    if (!isHadoopSnappyAvailable()) {
      throw new Exception("Hadoop-snappy does not seem to be available");
    }

    Object snappyCodec = Class.forName("org.apache.hadoop.io.compress.SnappyCodec").newInstance();
    Class confClass = new Configuration().getClass();
    Class[] paramClass = new Class[1];
    paramClass[0] = confClass;
    
    if (conf == null) {
      conf = new Configuration();
    }
    Method m = snappyCodec.getClass().getMethod("setConf", paramClass);
    m.invoke(snappyCodec, conf);

    paramClass[0] = Class.forName("java.io.InputStream");
    m = snappyCodec.getClass().getMethod("createInputStream", paramClass);
    Object result = m.invoke(snappyCodec, in);

    return (CompressionInputStream)result;
  }
  
  /**
   * Gets a CompressionOutputStream that uses the snappy codec and 
   * wraps the supplied base output stream.
   * 
   * @param conf a Hadoop Configuration object. The client can set the 
   * io.compression.codec.snappy.buffersize property here for controlling
   * the size of the buffer (in bytes) used by the Snappy codec. The default buffer
   * size is 256K.
   * 
   * @param out the base output stream to wrap around
   * @return a CompressionOutputStream that uses the Snappy codec
   * 
   * @throws Exception if snappy is not available or an error occurs during
   * reflection
   */
  public static CompressionOutputStream getSnappyOutputStream(Configuration conf, OutputStream out)
    throws Exception {
    if (!isHadoopSnappyAvailable()) {
      throw new Exception("Hadoop-snappy does not seem to be available");
    }
    
    Object snappyCodec = Class.forName("org.apache.hadoop.io.compress.SnappyCodec").newInstance();
    Class confClass = new Configuration().getClass();
    Class[] paramClass = new Class[1];
    paramClass[0] = confClass;
    
    if (conf == null) {
      conf = new Configuration();
    }
    Method m = snappyCodec.getClass().getMethod("setConf", paramClass);
    m.invoke(snappyCodec, conf);
    
    paramClass[0] = Class.forName("java.io.OutputStream");
    m = snappyCodec.getClass().getMethod("createOutputStream", paramClass);
    Object result = m.invoke(snappyCodec, out);
    
    return (CompressionOutputStream)result;
  }

  public static void main(String[] args) {
    try {
      FileInputStream fis = new FileInputStream(args[0]);
      CompressionInputStream cis = HadoopCompression.getSnappyInputStream(new Configuration(), fis);
      BufferedInputStream bis = new BufferedInputStream(cis);
      InputStreamReader isr = new InputStreamReader(bis);

      String tempLine = null;

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
