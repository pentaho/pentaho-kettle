/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.extension.ExtensionPointPluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;

/**
 * @author Andrey Khayrutdinov
 */
@Ignore
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
      getters.add( new Getter( condition, type ) );
    }

    PluginTypeInterface type = mock( PluginTypeInterface.class );

    ConcurrencyTestRunner.runAndCheckNoExceptionRaised(
      Collections.singletonList( new Registrar( condition, type.getClass(), 1, "" ) ), getters, condition );
  }


  @Test
  public void getPlugins_WhenRegisteringPlugins() throws Exception {
    final int gettersAmount = 30;
    final int cycles = 100;

    AtomicBoolean condition = new AtomicBoolean( true );
    List<Getter> getters = new ArrayList<Getter>( gettersAmount );
    for ( int i = 0; i < gettersAmount; i++ ) {
      Class<? extends PluginTypeInterface> type = ( i % 2 == 0 ) ? type1 : type2;
      getters.add( new Getter( condition, type ) );
    }

    List<Registrar> registrars = Arrays.asList(
      new Registrar( condition, type1, cycles, type1.getName() ),
      new Registrar( condition, type2, cycles, type2.getName() )
    );

    ConcurrencyTestRunner.runAndCheckNoExceptionRaised( registrars, getters, condition );
  }


  private class Registrar extends StopOnErrorCallable<Object> {
    private final Class<? extends PluginTypeInterface> type;
    private final int cycles;
    private final String nameSeed;

    public Registrar( AtomicBoolean condition, Class<? extends PluginTypeInterface> type, int cycles,
                      String nameSeed ) {
      super( condition );
      this.type = type;
      this.cycles = cycles;
      this.nameSeed = nameSeed;
    }

    @Override
    Object doCall() throws Exception {
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
      } finally {
        // push up registered instances for future clean-up
        addUsedPlugins( type, registered );
      }
      return null;
    }
  }

  private static class Getter extends StopOnErrorCallable<Object> {
    private final Class<? extends PluginTypeInterface> type;

    public Getter( AtomicBoolean condition, Class<? extends PluginTypeInterface> type ) {
      super( condition );
      this.type = type;
    }

    @Override
    Object doCall() throws Exception {
      while ( condition.get() ) {
        PluginRegistry.getInstance().getPlugins( type );
      }
      return null;
    }
  }
}
