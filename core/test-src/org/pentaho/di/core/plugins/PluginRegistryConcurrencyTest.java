/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.plugins;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.extension.ExtensionPointPluginType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class PluginRegistryConcurrencyTest {

  private static final Class<? extends PluginTypeInterface> type1 = TwoWayPasswordEncoderPluginType.class;
  private static final Class<? extends PluginTypeInterface> type2 = ExtensionPointPluginType.class;

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleClientEnvironment.init();
  }

  private final Map<Class<? extends PluginTypeInterface>, List<PluginInterface>> plugins = new HashMap<Class<? extends
    PluginTypeInterface>, List<PluginInterface>>();

  @Before
  public void setUp() {
    plugins.clear();
  }

  @After
  public void tearDown() {
    PluginRegistry registry = PluginRegistry.getInstance();
    for ( Map.Entry<Class<? extends PluginTypeInterface>, List<PluginInterface>> entry : plugins.entrySet() ) {
      Class<? extends PluginTypeInterface> type = entry.getKey();
      for ( PluginInterface plugin : entry.getValue() ) {
        registry.removePlugin( type, plugin );
      }
    }
  }

  private synchronized void addUsedPlugins( Class<? extends PluginTypeInterface> type, List<PluginInterface> list ) {
    List<PluginInterface> existing = plugins.get( type );
    if ( existing == null ) {
      existing = new ArrayList<PluginInterface>();
      plugins.put( type, existing );
    }
    existing.addAll( list );
  }


  @Test
  public void getPlugins_WhenRegisteringPluginTypes() throws Exception {
    final int gettersAmount = 30;

    AtomicBoolean condition = new AtomicBoolean( true );
    List<Getter> getters = new ArrayList<Getter>( gettersAmount );
    for ( int i = 0; i < gettersAmount; i++ ) {
      Class<? extends PluginTypeInterface> type = ( i % 2 == 0 ) ? type1 : type2;
      getters.add( new Getter( type, condition ) );
    }

    PluginTypeInterface type = mock( PluginTypeInterface.class );

    TestRunner runner =
      new TestRunner( Collections.singletonList( new Registrar( type.getClass(), 1, "" ) ), getters, condition );
    runner.runConcurrentTest();

    List<Exception> exceptions = runner.getExceptions();
    if ( !exceptions.isEmpty() ) {
      for ( Exception exception : exceptions ) {
        exception.printStackTrace();
      }
      fail( "There is expected no exceptions during the test" );
    }
  }


  @Test
  public void getPlugins_WhenRegisteringPlugins() throws Exception {
    final int gettersAmount = 30;
    final int cycles = 100;

    AtomicBoolean condition = new AtomicBoolean( true );
    List<Getter> getters = new ArrayList<Getter>( gettersAmount );
    for ( int i = 0; i < gettersAmount; i++ ) {
      Class<? extends PluginTypeInterface> type = ( i % 2 == 0 ) ? type1 : type2;
      getters.add( new Getter( type, condition ) );
    }

    List<Registrar> registrars = Arrays.asList(
      new Registrar( type1, cycles, type1.getName() ),
      new Registrar( type2, cycles, type2.getName() )
    );

    TestRunner runner = new TestRunner( registrars, getters, condition );
    runner.runConcurrentTest();

    List<Exception> exceptions = runner.getExceptions();
    if ( !exceptions.isEmpty() ) {
      for ( Exception exception : exceptions ) {
        exception.printStackTrace();
      }
      fail( "There is expected no exceptions during the test" );
    }
  }


  private class Registrar implements Callable<Exception> {
    private final Class<? extends PluginTypeInterface> type;
    private final int cycles;
    private final String nameSeed;

    public Registrar( Class<? extends PluginTypeInterface> type, int cycles, String nameSeed ) {
      this.type = type;
      this.cycles = cycles;
      this.nameSeed = nameSeed;
    }

    @Override
    public Exception call() throws Exception {
      Exception exception = null;
      List<PluginInterface> registered = new ArrayList<PluginInterface>( cycles );
      try {
        for ( int i = 0; i < cycles; i++ ) {
          String id = nameSeed + '_' + i;
          PluginInterface mock = mock( PluginInterface.class );
          when( mock.getName() ).thenReturn( id );
          when( mock.getIds() ).thenReturn( new String[] { id } );
          when( mock.getPluginType() ).thenAnswer( new Answer<Object>() {
            @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
              return type;
            }
          } );

          registered.add( mock );

          PluginRegistry.getInstance().registerPlugin( type, mock );
        }
      } catch ( Exception e ) {
        exception = e;
      } finally {
        // push up registered instances for future clean-up
        addUsedPlugins( type, registered );
      }
      return exception;
    }
  }

  private static class Getter implements Callable<Exception> {
    private final Class<? extends PluginTypeInterface> type;
    private final AtomicBoolean condition;

    public Getter( Class<? extends PluginTypeInterface> type, AtomicBoolean condition ) {
      this.type = type;
      this.condition = condition;
    }

    @Override
    public Exception call() throws Exception {
      Exception exception = null;
      while ( condition.get() ) {
        try {
          PluginRegistry.getInstance().getPlugins( type );
        } catch ( Exception e ) {
          condition.set( false );
          exception = e;
          break;
        }
      }
      return exception;
    }
  }


  private static class TestRunner {
    private final List<? extends Callable<Exception>> monitoredTasks;
    private final List<? extends Callable<Exception>> backgroundTasks;
    private final AtomicBoolean condition;

    private final List<Exception> exceptions;

    public TestRunner( List<? extends Callable<Exception>> monitoredTasks,
                       List<? extends Callable<Exception>> backgroundTasks, AtomicBoolean condition ) {
      this.monitoredTasks = monitoredTasks;
      this.backgroundTasks = backgroundTasks;
      this.condition = condition;
      this.exceptions = new ArrayList<Exception>();
    }

    public void runConcurrentTest() throws Exception {
      final int tasksAmount = monitoredTasks.size() + backgroundTasks.size();
      final ExecutorService executors = Executors.newFixedThreadPool( tasksAmount );
      try {
        CompletionService<Exception> service = new ExecutorCompletionService<Exception>( executors );
        for ( Callable<Exception> task : backgroundTasks ) {
          service.submit( task );
        }
        List<Future<Exception>> monitored = new ArrayList<Future<Exception>>( monitoredTasks.size() );
        for ( Callable<Exception> task : monitoredTasks ) {
          monitored.add( service.submit( task ) );
        }

        while ( condition.get() && !isDone( monitored ) ) {
          // wait
        }
        condition.set( false );

        for ( int i = 0; i < tasksAmount; i++ ) {
          Exception exception = service.take().get();
          if ( exception != null ) {
            exceptions.add( exception );
          }
        }
      } finally {
        executors.shutdown();
      }
    }

    private boolean isDone( List<Future<Exception>> futures ) {
      for ( Future<Exception> future : futures ) {
        if ( !future.isDone() ) {
          return false;
        }
      }
      return true;
    }

    public List<Exception> getExceptions() {
      return exceptions;
    }
  }
}