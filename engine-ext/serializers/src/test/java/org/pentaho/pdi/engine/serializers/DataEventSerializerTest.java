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
import org.pentaho.di.engine.api.events.DataEvent;
import org.pentaho.di.engine.api.model.Row;
import org.pentaho.di.engine.api.model.Rows;
import org.pentaho.di.engine.model.Operation;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by nbaker on 3/6/17.
 */
public class DataEventSerializerTest {


  @Test
  public void testDataEventSerializer() throws Exception {

    Operation operation = mock( Operation.class );
    when( operation.getId() ).thenReturn( "foo" );

    Date date = new Date();
    URI uri = new URI( "http://www.pentaho.com" );

    List<Object> objects = new ArrayList<>();
    objects.add( 100 );
    objects.add( 100.50 );
    objects.add( new BigDecimal( "10000000000000000000.50" ) );
    objects.add( true );
    objects.add( date );
    objects.add( "A String" );
    objects.add( uri );

    List<String> names = new ArrayList<>();
    names.add( "some int" );
    names.add( "some Double" );
    names.add( "some Decimal" );
    names.add( "some Boolean" );
    names.add( "some Date" );
    names.add( "some String" );
    names.add( "some Serializable" );

    List<Class> classes = Arrays
      .asList( Integer.class, Double.class, BigDecimal.class, Boolean.class, Date.class, String.class, Object.class );

    Row row = new DeserializedRow( names, classes, objects );

    List<Row> rowsList = Collections.singletonList( row );
    Rows rows = new Rows( rowsList, Rows.TYPE.OUT, Rows.STATE.ACTIVE );
    DataEvent<Operation> dataEvent = new DataEvent<>( operation, rows );

    DataEventSerializer serializer = new DataEventSerializer();

    String serialized = serializer.serialize( dataEvent );
    System.out.println( serialized );
    DataEvent deserialized = serializer.deserialize( serialized );
    assertTrue( serializer.getSupportedClasses().contains( DataEvent.class ) );
    assertEquals( dataEvent, deserialized );
  }
}