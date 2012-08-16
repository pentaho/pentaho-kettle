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

package org.pentaho.hadoop.shim.hadoop20;

import static org.junit.Assert.assertTrue;

import java.util.ServiceLoader;

import org.junit.Test;
import org.pentaho.hadoop.shim.common.CommonPigShim;
import org.pentaho.hadoop.shim.common.CommonSqoopShim;
import org.pentaho.hadoop.shim.spi.PigShim;
import org.pentaho.hadoop.shim.spi.SqoopShim;
import org.pentaho.hadoop.shim.spi.SnappyShim;
import org.pentaho.hbase.shim.common.CommonHBaseShim;
import org.pentaho.hbase.shim.spi.HBaseShim;

/**
 * Validate that our Shim service providers have been registered properly
 */
public class ShimRegistrationTest {

  /**
   * Make sure we've registered our Hadoop Shim
   */
  @Test
  public void hadoopShimRegistered() {
    ServiceLoader<org.pentaho.hadoop.shim.spi.HadoopShim> l = ServiceLoader.load(org.pentaho.hadoop.shim.spi.HadoopShim.class);
    org.pentaho.hadoop.shim.spi.HadoopShim s = l.iterator().next();
    assertTrue(org.pentaho.hadoop.shim.common.CommonHadoopShim.class.isAssignableFrom(s.getClass()));
  }
  
  /**
   * Make sure we've registered our Pig Shim
   */
  @Test
  public void pigShimRegistered() {
    ServiceLoader<PigShim> l = ServiceLoader.load(PigShim.class);
    PigShim s = l.iterator().next();
    assertTrue(CommonPigShim.class.isAssignableFrom(s.getClass()));
  }
  
  /**
   * Make sure we've registered our Pig Shim
   */
  @Test
  public void sqoopShimRegistered() {
    ServiceLoader<SqoopShim> l = ServiceLoader.load(SqoopShim.class);
    SqoopShim s = l.iterator().next();
    assertTrue(CommonSqoopShim.class.isAssignableFrom(s.getClass()));
  }
  
  /**
   * Make sure we've registered our Snappy Shim
   */
  @Test
  public void snappyShimRegistered() {
    ServiceLoader<SnappyShim> l = ServiceLoader.load(SnappyShim.class);
    SnappyShim s = l.iterator().next();
    assertTrue(org.pentaho.hadoop.shim.hadoop20.SnappyShim.class.isAssignableFrom(s.getClass()));
  }

  /**
   * Make sure we've registered our HBase Shim
   */
  @Test
  public void hbaseShimRegistered() {
    ServiceLoader<HBaseShim> l = ServiceLoader.load(HBaseShim.class);
    HBaseShim s = l.iterator().next();
    assertTrue(CommonHBaseShim.class.isAssignableFrom(s.getClass()));
  }
}
