/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

/**
 * <p>
 * Util class to handle StepMock creation in generic way.
 * </p>
 * <p>
 * Usage example: 
 * <pre>
 * Mapping step = StepMockUtil.getStep( Mapping.class, MappingMeta.class, "junit" );
 * </pre>
 * 
 * 
 * </p>
 * 
 */
public class StepMockUtil {

  public static <T extends StepMetaInterface, V extends BaseStep> StepMockHelper<T, StepDataInterface> getStepMockHelper( Class<T> meta, String name ) {
    StepMockHelper<T, StepDataInterface> stepMockHelper = new StepMockHelper<T, StepDataInterface>( name, meta, StepDataInterface.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn( stepMockHelper.logChannelInterface );
    when( stepMockHelper.logChannelInterfaceFactory.create( any() ) ).thenReturn( stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    return stepMockHelper;
  }

  public static <T extends BaseStep, K extends StepMetaInterface, V extends StepDataInterface> T getStep( Class<T> klass, StepMockHelper<K, V> mock )
      throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Constructor<T> kons = klass.getConstructor( StepMeta.class, StepDataInterface.class, int.class, TransMeta.class, Trans.class );
    T step = kons.newInstance( mock.stepMeta, mock.stepDataInterface, 0, mock.transMeta, mock.trans );
    return step;
  }

  public static <T extends BaseStep, K extends StepMetaInterface> T getStep( Class<T> stepClass, Class<K> stepMetaClass, String stepName )
      throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    return StepMockUtil.getStep( stepClass, StepMockUtil.getStepMockHelper( stepMetaClass, stepName ) );
  }

}
