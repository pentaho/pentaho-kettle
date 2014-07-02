/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.job.entry.loadSave;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.steps.loadsave.MemoryRepository;
import org.pentaho.di.trans.steps.loadsave.getter.Getter;
import org.pentaho.di.trans.steps.loadsave.setter.Setter;
import org.pentaho.di.trans.steps.loadsave.validator.DefaultFieldLoadSaveValidatorFactory;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidatorFactory;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.test.util.JavaBeanManipulator;

public class LoadSaveTester {
  private final Class<? extends JobEntryInterface> clazz;
  private final List<String> xmlAttributes;
  private final List<String> repoAttributes;
  private final JavaBeanManipulator<? extends JobEntryInterface> manipulator;
  private final FieldLoadSaveValidatorFactory fieldLoadSaveValidatorFactory;

  public LoadSaveTester( Class<? extends JobEntryInterface> clazz, List<String> commonAttributes,
      List<String> xmlAttributes, List<String> repoAttributes, Map<String, String> getterMap,
      Map<String, String> setterMap, Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap,
      Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorTypeMap ) {
    this.clazz = clazz;
    this.xmlAttributes = new ArrayList<String>( commonAttributes );
    this.xmlAttributes.addAll( xmlAttributes );
    this.repoAttributes = new ArrayList<String>( commonAttributes );
    this.repoAttributes.addAll( commonAttributes );
    List<String> combinedAttributes = new ArrayList<String>( commonAttributes );
    combinedAttributes.addAll( repoAttributes );
    combinedAttributes.addAll( xmlAttributes );
    manipulator = new JavaBeanManipulator<JobEntryInterface>( clazz, combinedAttributes, getterMap, setterMap );
    Map<Getter<?>, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorMethodMap =
        new HashMap<Getter<?>, FieldLoadSaveValidator<?>>();
    for ( Entry<String, FieldLoadSaveValidator<?>> entry : fieldLoadSaveValidatorAttributeMap.entrySet() ) {
      fieldLoadSaveValidatorMethodMap.put( manipulator.getGetter( entry.getKey() ), entry.getValue() );
    }
    fieldLoadSaveValidatorFactory =
        new DefaultFieldLoadSaveValidatorFactory( fieldLoadSaveValidatorMethodMap, fieldLoadSaveValidatorTypeMap );
  }

  public LoadSaveTester( Class<? extends JobEntryInterface> clazz, List<String> commonAttributes,
      Map<String, String> getterMap, Map<String, String> setterMap,
      Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap,
      Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorTypeMap ) {
    this( clazz, commonAttributes, Arrays.<String>asList(), Arrays.<String>asList(), getterMap, setterMap,
        fieldLoadSaveValidatorAttributeMap, fieldLoadSaveValidatorTypeMap );
  }

  public LoadSaveTester( Class<? extends JobEntryInterface> clazz, List<String> commonAttributes,
      List<String> xmlAttributes, List<String> repoAttributes, Map<String, String> getterMap,
      Map<String, String> setterMap ) {
    this( clazz, commonAttributes, xmlAttributes, repoAttributes, getterMap, setterMap,
        new HashMap<String, FieldLoadSaveValidator<?>>(), new HashMap<String, FieldLoadSaveValidator<?>>() );
  }

  public LoadSaveTester( Class<? extends JobEntryInterface> clazz, List<String> commonAttributes,
      Map<String, String> getterMap, Map<String, String> setterMap ) {
    this( clazz, commonAttributes, Arrays.<String>asList(), Arrays.<String>asList(), getterMap, setterMap,
        new HashMap<String, FieldLoadSaveValidator<?>>(), new HashMap<String, FieldLoadSaveValidator<?>>() );
  }

  public FieldLoadSaveValidatorFactory getFieldLoadSaveValidatorFactory() {
    return fieldLoadSaveValidatorFactory;
  }

  @SuppressWarnings( "unchecked" )
  private Map<String, FieldLoadSaveValidator<?>> createValidatorMapAndInvokeSetters( List<String> attributes,
      JobEntryInterface metaToSave ) {
    Map<String, FieldLoadSaveValidator<?>> validatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    for ( String attribute : attributes ) {
      Getter<?> getter = manipulator.getGetter( attribute );
      @SuppressWarnings( "rawtypes" )
      Setter setter = manipulator.getSetter( attribute );
      FieldLoadSaveValidator<?> validator = fieldLoadSaveValidatorFactory.createValidator( getter );
      try {
        setter.set( metaToSave, validator.getTestObject() );
      } catch ( Exception e ) {
        throw new RuntimeException( "Unable to invoke setter for " + attribute, e );
      }
      validatorMap.put( attribute, validator );
    }
    return validatorMap;
  }

  private JobEntryInterface createMeta() {
    try {
      return clazz.newInstance();
    } catch ( Exception e ) {
      throw new RuntimeException( "Unable to create meta of class " + clazz.getCanonicalName(), e );
    }
  }

  private void validateLoadedMeta( List<String> attributes, Map<String, FieldLoadSaveValidator<?>> validatorMap,
      JobEntryInterface metaSaved, JobEntryInterface metaLoaded ) {
    for ( String attribute : attributes ) {
      try {
        Getter<?> getterMethod = manipulator.getGetter( attribute );
        Object originalValue = getterMethod.get( metaSaved );
        Object value = getterMethod.get( metaLoaded );
        FieldLoadSaveValidator<?> validator = validatorMap.get( attribute );
        Method[] validatorMethods = validator.getClass().getMethods();
        Method validatorMethod = null;
        for ( Method method : validatorMethods ) {
          if ( "validateTestObject".equals( method.getName() ) ) {
            Class<?>[] types = method.getParameterTypes();
            if ( types.length == 2 ) {
              if ( types[1] == Object.class
                  && ( originalValue == null || types[0].isAssignableFrom( originalValue.getClass() ) ) ) {
                validatorMethod = method;
                break;
              }
            }
          }
        }
        if ( validatorMethod == null ) {
          throw new RuntimeException( "Couldn't find proper validateTestObject method on "
              + validator.getClass().getCanonicalName() );
        }
        if ( !( (Boolean) validatorMethod.invoke( validator, originalValue, value ) ) ) {
          throw new KettleException( "Attribute " + attribute + " started with value "
              + validatorMap.get( attribute ).getTestObject() + " ended with value " + value );
        }
      } catch ( Exception e ) {
        throw new RuntimeException( "Error validating " + attribute, e );
      }
    }
  }

  public void testXmlRoundTrip() throws KettleException {
    JobEntryInterface metaToSave = createMeta();
    Map<String, FieldLoadSaveValidator<?>> validatorMap =
        createValidatorMapAndInvokeSetters( xmlAttributes, metaToSave );
    JobEntryInterface metaLoaded = createMeta();
    String xml = "<step>" + metaToSave.getXML() + "</step>";
    InputStream is = new ByteArrayInputStream( xml.getBytes() );
    metaLoaded.loadXML( XMLHandler.getSubNode( XMLHandler.loadXMLFile( is, null, false, false ), "step" ),
        (List<DatabaseMeta>) null, null, null, (IMetaStore) null );
    validateLoadedMeta( xmlAttributes, validatorMap, metaToSave, metaLoaded );
  }

  public void testRepoRoundTrip() throws KettleException {
    JobEntryInterface metaToSave = createMeta();
    Map<String, FieldLoadSaveValidator<?>> validatorMap =
        createValidatorMapAndInvokeSetters( repoAttributes, metaToSave );
    JobEntryInterface metaLoaded = createMeta();
    Repository rep = new MemoryRepository();
    metaToSave.saveRep( rep, null, null );
    metaLoaded.loadRep( rep, null, null, null, null );
    validateLoadedMeta( repoAttributes, validatorMap, metaToSave, metaLoaded );
  }
}
