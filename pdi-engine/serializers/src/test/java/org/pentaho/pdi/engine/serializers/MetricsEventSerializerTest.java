/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */

package org.pentaho.pdi.engine.serializers;

import org.junit.Test;
import org.pentaho.di.engine.api.events.MetricsEvent;
import org.pentaho.di.engine.api.reporting.Metrics;
import org.pentaho.di.engine.model.Transformation;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by nbaker on 3/6/17.
 */
public class MetricsEventSerializerTest {

  @Test
  public void testMetricsSerializer() throws Exception {

    Transformation transformation = mock( Transformation.class );
    when( transformation.getId() ).thenReturn( "foo" );

    Random random = new Random();
    Metrics metrics = new Metrics( random.nextInt(), random.nextInt(), random.nextInt(), random.nextInt() );
    MetricsEvent<Transformation> metricsEvent = new MetricsEvent<>( transformation, metrics );

    MetricsEventSerializer serializer = new MetricsEventSerializer();

    String serialized = serializer.serialize( metricsEvent );
    MetricsEvent deserialized = serializer.deserialize( serialized );

    assertTrue( serializer.getSupportedClasses().contains( MetricsEvent.class ) );

    assertEquals( metricsEvent, deserialized );

  }
}