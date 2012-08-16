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

package org.pentaho.hadoop.shim.cdh3u4;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.SnappyCodec;
import org.pentaho.hadoop.shim.common.CommonSnappyShim;

public class SnappyShim extends CommonSnappyShim {
  /**
   * Tests whether hadoop-snappy (not to be confused with other java-based
   * snappy implementations such as jsnappy or snappy-java) plus the 
   * native snappy libraries are available.
   * 
   * @return true if hadoop-snappy is available on the classpath
   */
  public boolean isHadoopSnappyAvailable() {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    try {
      return SnappyCodec.isNativeSnappyLoaded(new Configuration());
    } catch (Throwable t) {
      return false;
    } finally {
      Thread.currentThread().setContextClassLoader(cl);
    }
  }
}
