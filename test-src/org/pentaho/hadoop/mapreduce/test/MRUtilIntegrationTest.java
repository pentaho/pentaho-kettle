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

package org.pentaho.hadoop.mapreduce.test;

import static org.junit.Assert.assertEquals;

import org.apache.hadoop.conf.Configuration;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.hadoop.mapreduce.MRUtil;

public class MRUtilIntegrationTest {

  @Test
  public void createTrans_normalEngine() throws Exception {
    KettleEnvironment.init();
    final Configuration c = new Configuration();
    final TransMeta transMeta = new TransMeta("./test-res/wordcount-reducer.ktr");
    final Trans trans = MRUtil.getTrans(c, transMeta.getXML(), false);
    assertEquals(TransMeta.TransformationType.Normal, trans.getTransMeta().getTransformationType());
  }

  @Test
  public void createTrans_singleThreaded() throws Exception {
    KettleEnvironment.init();
    final Configuration c = new Configuration();
    final TransMeta transMeta = new TransMeta("./test-res/wordcount-reducer.ktr");
    final Trans trans = MRUtil.getTrans(c, transMeta.getXML(), true);
    assertEquals(TransMeta.TransformationType.SingleThreaded, trans.getTransMeta().getTransformationType());
  }
}
