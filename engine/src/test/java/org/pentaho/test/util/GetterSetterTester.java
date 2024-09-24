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

package org.pentaho.test.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.pentaho.di.trans.steps.loadsave.setter.Setter;

public class GetterSetterTester<T> {
  private final Map<String, ObjectTester<?>> objectTesterMap;
  private final Class<? extends T> clazz;
  private final Map<String, String> getterMap;
  private final Map<String, String> setterMap;

  public GetterSetterTester( Class<? extends T> clazz ) {
    this( clazz, new HashMap<String, String>(), new HashMap<String, String>() );
  }

  public GetterSetterTester( Class<? extends T> clazz, Map<String, String> getterMap, Map<String, String> setterMap ) {
    this.clazz = clazz;
    this.getterMap = getterMap;
    this.setterMap = setterMap;
    objectTesterMap = new HashMap<String, ObjectTester<?>>();
  }

  public void test( Object objectUnderTest ) {
    JavaBeanManipulator<T> manipulator =
        new JavaBeanManipulator<T>( clazz, new ArrayList<String>( objectTesterMap.keySet() ), getterMap, setterMap );
    for ( Entry<String, ObjectTester<?>> entry : objectTesterMap.entrySet() ) {
      String attribute = entry.getKey();
      @SuppressWarnings( "unchecked" )
      ObjectTester<Object> tester = (ObjectTester<Object>) entry.getValue();
      for ( Object testObject : tester.getTestObjects() ) {
        @SuppressWarnings( "unchecked" )
        Setter<Object> setter = (Setter<Object>) manipulator.getSetter( attribute );
        setter.set( objectUnderTest, testObject );
        tester.validate( testObject, manipulator.getGetter( attribute ).get( objectUnderTest ) );
      }
    }
  }

  public void addObjectTester( String attribute, ObjectTester<?> objectTester ) {
    objectTesterMap.put( attribute, objectTester );
  }
}
