/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.concurrency;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.logging.LoggingObject;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.core.logging.SimpleLoggingObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoggingRegistryConcurrencyTest {

  @Before
  public void setup() {
    LoggingRegistry.getInstance();
  }

  @Test
  public void testChildren() throws Exception {
    int amount = 50;
    List<Modifier> modifiers = new ArrayList<>( amount );
    List<Traverser> traversers = new ArrayList<>( amount );
    AtomicBoolean condition = new AtomicBoolean( true );

    for ( int i = 0; i < amount; i++ ) {
      modifiers.add( new Modifier( condition ) );
      traversers.add( new Traverser( condition ) );
    }

    ConcurrencyTestRunner<?, ?> runner = new ConcurrencyTestRunner<Object, Object>( modifiers, traversers, condition );
    runner.runConcurrentTest();

    runner.checkNoExceptionRaised();

  }

  private class Modifier extends StopOnErrorCallable<LoggingRegistry> {
    boolean addOrRemove;
    String name;
    String id;

    Modifier( AtomicBoolean condition ) {
      super( condition );
      name = Thread.currentThread().getId() + " name ";
      id = Thread.currentThread().getId() + " id ";
    }

    @Override
    LoggingRegistry doCall() throws Exception {
      Random random = new Random();
      int cycles = 0;
      // Add a new logging source
      while ( ( cycles < 100 ) && ( condition.get() ) ) {
        cycles++;
        if ( !addOrRemove ) {
          LoggingObject addObj = new LoggingObject( new SimpleLoggingObject( Thread.currentThread().getId() + " name ", LoggingObjectType.TRANS, null ) );
          addObj.setLogChannelId( Thread.currentThread().getId() + "" );
          LoggingRegistry.getInstance().registerLoggingSource( addObj );
          addOrRemove = !addOrRemove;
        } else {
          LoggingRegistry.getInstance().removeIncludingChildren( id );
          addOrRemove = !addOrRemove;
        }
        Thread.sleep( random.nextInt( 100 ) );
      }
      return null;
    }
  }

  private class Traverser extends StopOnErrorCallable<LoggingRegistry> {
    Traverser( AtomicBoolean condition ) {
      super( condition );
    }

    @Override
    LoggingRegistry doCall() throws Exception {
      Random random = new Random();
      int cycles = 0;
      while ( ( cycles < 100 ) && ( condition.get() ) ) {
        List<String> kids = LoggingRegistry.getInstance().getLogChannelChildren( "DoesntExist" );
        Thread.sleep( random.nextInt( 100 ) );
      }
      return null;
    }
  }
}
