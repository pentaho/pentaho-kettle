/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.jsoninput.sampler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.trans.steps.jsoninput.sampler.node.ArrayNode;
import org.pentaho.di.trans.steps.jsoninput.sampler.node.ObjectNode;

import java.io.InputStream;

/**
 * Created by bmorrise on 8/7/18.
 */
public class JsonSamplerTest {

  @Test
  public void testDedupeArray() throws Exception {
    JsonSampler jsonSampler = new JsonSampler();
    InputStream inputStream = this.getClass().getResourceAsStream( "/org/pentaho/di/trans/steps/jsoninput/dedupe-test.json" );
    ArrayNode node = (ArrayNode) jsonSampler.sample( inputStream );
    Assert.assertEquals( node.output(), "[\n" +
            "{\n" +
            " item2:value2\n" +
            " item1:value1\n" +
            " itemarr:[\n" +
            "{\n" +
            " item2:value2\n" +
            " item1:value1\n" +
            " item3:value3\n" +
            "},\n" +
            "]\n" +
            " item3:{\n" +
            " item4:value4\n" +
            " item6:value6\n" +
            " item5:value5\n" +
            "},\n" +
            "\n" +
            "},\n" +
            "]" );
  }

  @Test
  public void testDedupeObject() throws Exception {
    JsonSampler jsonSampler = new JsonSampler();
    InputStream inputStream = this.getClass().getResourceAsStream( "/org/pentaho/di/trans/steps/jsoninput/dedupe-test2.json" );
    ObjectNode node = (ObjectNode) jsonSampler.sample( inputStream );
    Assert.assertEquals( node.output(), "{\n" +
            " data:[\n" +
            "{\n" +
            " item2:value2\n" +
            " item1:value1\n" +
            " itemarr:[\n" +
            "{\n" +
            " item2:value2\n" +
            " item1:value1\n" +
            " item3:value3\n" +
            "},\n" +
            "]\n" +
            " item3:{\n" +
            " item4:true\n" +
            " item6:value6\n" +
            " item5:1.0\n" +
            "},\n" +
            "\n" +
            "},\n" +
            "]\n" +
            "},\n" );
  }

  @Test
  public void testDedupeLines() throws Exception {
    Configuration configuration = new Configuration();
    configuration.setLines( 10 );
    JsonSampler jsonSampler = new JsonSampler( configuration );
    InputStream inputStream = this.getClass().getResourceAsStream( "/org/pentaho/di/trans/steps/jsoninput/dedupe-test2.json" );
    ObjectNode node = (ObjectNode) jsonSampler.sample( inputStream );
    Assert.assertEquals( node.output(), "{\n" +
            " data:[\n" +
            "{\n" +
            " item2:value2\n" +
            " item1:value1\n" +
            " itemarr:[\n" +
            "{\n" +
            " item1:value1\n" +
            " item3:value3\n" +
            "},\n" +
            "]\n" +
            " item3:{\n" +
            " item4:true\n" +
            " item5:1.0\n" +
            "},\n" +
            "\n" +
            "},\n" +
            "]\n" +
            "},\n" );
  }

}
