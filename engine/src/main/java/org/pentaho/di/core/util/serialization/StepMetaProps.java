/*
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
 *
 * **************************************************************************
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
 */

package org.pentaho.di.core.util.serialization;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.injection.bean.BeanInjectionInfo;
import org.pentaho.di.core.injection.bean.BeanInjector;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.step.StepMetaInterface;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Stream.concat;
import static org.pentaho.di.core.util.serialization.StepMetaProps.STEP_TAG;

/**
 * A slim representation of StepMetaInterface properties, used as a
 * way to leverage alternative serialization strategies (e.g. {@link MetaXmlSerializer}
 * <p>
 * Public methods allow conversion:
 * {@link #from(StepMetaInterface)} and
 * {@link #to(StepMetaInterface)}
 * <p>
 * <p>
 * Internally, the StepMetaProps holds a list of {@link PropGroup} elements, each corresponding
 * to a MetadataInjection group.
 * <p>
 * InjectionDeep not yet supported.
 */
@XmlRootElement ( name = STEP_TAG )
public class StepMetaProps {

  static final String STEP_TAG = "step-props";

  @XmlAttribute ( name = "secure" )
  private List<String> secureFields;

  private StepMetaInterface stepMeta;
  private VariableSpace variableSpace = new Variables();

  @SuppressWarnings ( "unused" )
  private StepMetaProps() {
  }

  private StepMetaProps( StepMetaInterface smi ) {
    secureFields = sensitiveFields( smi.getClass() );
  }

  @XmlElement ( name = "group" )
  private List<PropGroup> groups = new ArrayList<>();

  /**
   * Retuns an instance of this class with stepMeta properties mapped
   * to a list of {@link PropGroup}
   */
  public static StepMetaProps from( StepMetaInterface stepMeta ) {
    StepMetaProps propMap = new StepMetaProps( stepMeta );
    propMap.stepMeta = stepMeta;

    // use metadata injection to extract properties
    BeanInjectionInfo info = new BeanInjectionInfo( stepMeta.getClass() );
    BeanInjector injector = new BeanInjector( info );

    propMap.populateGroups( stepMeta, info, injector );

    return propMap;
  }

  /**
   * Sets the properties of this StepMetaProps on {@param stepMetaInterface}
   * <p>
   * This method mutates the stepMeta, as opposed to returning a new instance, to match
   * more cleanly to Kettle's {@link StepMetaInterface#loadXML} design, which loads props into
   * an instance.
   */
  public StepMetaInterface to( StepMetaInterface stepMetaInterface ) {
    BeanInjectionInfo info = new BeanInjectionInfo( stepMetaInterface.getClass() );

    BeanInjector injector = new BeanInjector( info );
    info.getProperties().values().forEach( property -> assignValueForProp( property, stepMetaInterface, injector ) );
    return stepMetaInterface;
  }

  /**
   * Allows specifying a variable space to be used when applying property values to
   * a stepMeta.
   */
  public StepMetaProps withVariables( VariableSpace space ) {
    StepMetaProps propCopy = from( this.stepMeta );
    propCopy.variableSpace = space;
    return propCopy;
  }

  private void populateGroups( StepMetaInterface stepMeta, BeanInjectionInfo info,
                               BeanInjector injector ) {
    groups = info.getGroups().stream()  // get metadata injection groups
      .flatMap( group -> group.getGroupProperties().stream() ) // expand to all properties
      .map( getProp( stepMeta, injector ) ) // map to  property/value
      .collect( groupingBy( Prop::getGroup ) ).entrySet().stream()  // group by group name
      .map( entry -> new PropGroup( entry.getKey(), entry.getValue() ) ) // map the set of properties to a group
      .collect( Collectors.toList() );
  }

  /**
   * Collects the list of declared fields with the {@link Sensitive} annotation
   * for the class.  Values for these fields will be encrypted.
   * <p>
   * Checks the top level fields of clazz, and recurses into
   * {@link InjectionDeep} classes.
   */
  @VisibleForTesting
  static List<String> sensitiveFields( Class clazz ) {
    Field[] declaredFields = clazz.getDeclaredFields();

    return concat( stream( declaredFields ), recurseDeep( declaredFields ) )
      .filter( field -> field.getAnnotation( Sensitive.class ) != null )
      .filter( field -> field.getAnnotation( Injection.class ) != null )
      .map( field -> field.getAnnotation( Injection.class ).name() )
      .collect( Collectors.toList() );
  }

  private static Stream<Field> recurseDeep( Field[] topLevelFields ) {
    Stream<Field> deepInjectionFields = Stream.empty();
    if ( stream( topLevelFields ).anyMatch( isInjectionDeep() ) ) {
      deepInjectionFields = stream( topLevelFields )
        .filter( isInjectionDeep() )
        .flatMap( field -> stream( field.getType().getDeclaredFields() ) );
      List<Field> deepFields = deepInjectionFields.collect( Collectors.toList() );
      return concat( deepFields.stream(), recurseDeep( deepFields.toArray( new Field[ 0 ] ) ) );
    }
    return deepInjectionFields;
  }

  private static Predicate<Field> isInjectionDeep() {
    return field -> field.getAnnotation( InjectionDeep.class ) != null;
  }

  private Function<BeanInjectionInfo.Property, Prop> getProp( StepMetaInterface stepMeta,
                                                              BeanInjector injector ) {
    return prop ->
      new Prop( prop.getName(),
        getPropVal( stepMeta, injector, prop ),
        prop.getGroupName() );
  }

  @SuppressWarnings ( "unchecked" )
  private List<Object> getPropVal( StepMetaInterface stepMeta, BeanInjector injector,
                                   BeanInjectionInfo.Property prop ) {
    try {
      List ret;

      Object o = injector.getPropVal( stepMeta, prop.getName() );
      if ( o instanceof List ) {
        ret = (List<Object>) o;
      } else if ( o instanceof Object[] ) {
        ret = asList( (Object[]) o );
      } else {
        ret = singletonList( o );
      }
      return maybeEncrypt( prop.getName(), ret );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  private List<Object> maybeEncrypt( String name, List<Object> ret ) {
    if ( secureFields.contains( name ) ) {
      return ret.stream()
        .map( val ->
          ( val == null ) || ( val.toString().isEmpty() )
            ? "" : Encr.encryptPasswordIfNotUsingVariables( val.toString() ) )
        .collect( Collectors.toList() );
    }
    return ret;
  }

  private void assignValueForProp( BeanInjectionInfo.Property beanInfoProp, StepMetaInterface stepMetaInterface,
                                   BeanInjector injector ) {
    List<Prop> props = groups.stream()
      .filter( group -> beanInfoProp.getGroupName().equals( group.name ) )
      .flatMap( group -> group.props.stream() )
      .filter( prop -> beanInfoProp.getName().equals( prop.name ) )
      .collect( Collectors.toList() );

    decryptVals( props );
    props.forEach( entry -> injectVal( beanInfoProp, entry, stepMetaInterface, injector ) );
  }

  private void decryptVals( List<Prop> props ) {
    props.stream()
      .filter( prop -> secureFields.contains( prop.getName() ) )
      .forEach( prop -> prop.value = prop.value.stream()
        .map( Object::toString )
        .map( Encr::decryptPasswordOptionallyEncrypted )
        .collect( Collectors.toList() ) );
  }

  private void injectVal( BeanInjectionInfo.Property beanInfoProp, Prop prop,
                          StepMetaInterface stepMetaInterface,
                          BeanInjector injector ) {

    if ( prop.value == null || prop.value.size() == 0 ) {
      prop.value = singletonList( null );
    }
    try {
      injector.setProperty( stepMetaInterface,
        beanInfoProp.getName(),
        prop.value.stream()
          .map( value -> {
            RowMetaAndData rmad = new RowMetaAndData();
            rmad.addValue( new ValueMetaString( prop.getName() ), envSubs( value ) );
            return rmad;
          } ).collect( Collectors.toList() ),
        beanInfoProp.getName() );
    } catch ( KettleException e ) {
      throw new RuntimeException( e );
    }
  }

  private Object envSubs( Object value ) {
    if ( value instanceof String ) {
      return variableSpace.environmentSubstitute( value.toString() );
    }
    return value;
  }

  @Override public String toString() {
    return "StepMetaProps{" + "groups=" + groups + '}';
  }

  public List<Object> getPropertyValue( String group, String property ) {
    PropGroup propGroup = groups.stream().filter( pg -> pg.name.equals( group ) ).findFirst()
      .orElseThrow( () -> new IllegalArgumentException( "Group " + group + " not found" ) );
    Prop prop = propGroup.props.stream().filter( p -> p.name.equals( property ) ).findFirst()
      .orElseThrow( () -> new IllegalArgumentException( "Property " + property + " not found" ) );
    return prop.value;
  }

  /**
   * Represents a named grouping of properties, corresponding to a metadata injection group.
   */
  private static class PropGroup {
    @XmlAttribute String name;

    @XmlElement ( name = "property" ) List<Prop> props;

    @SuppressWarnings ( "unused" )
    public PropGroup() {
    } // needed for deserialization

    PropGroup( String name, List<Prop> propList ) {
      this.name = name;
      this.props = propList;
    }

    @Override public String toString() {
      return "PropGroup{" + "name='" + name + '\'' + ", props=" + props + '}';
    }
  }

  /**
   * Represents a single property from a StepMetaInterface impl.
   * Values are captured as a List<Object> to consistently handle both List properties and single items.
   */
  private static class Prop {
    @XmlAttribute String group;
    @XmlAttribute String name;

    @XmlElement ( name = "value" ) List<Object> value = new ArrayList<>();

    @SuppressWarnings ( "unused" )
    public Prop() {
    } // needed for deserialization

    private Prop( String name, List<Object> value, String group ) {
      this.group = group;
      this.name = name;
      this.value = value;
    }

    String getName() {
      return name;
    }


    String getGroup() {
      return group;
    }

    @Override public String toString() {
      return "\n  Prop{" + "group='" + group + '\'' + ", name='" + name + '\'' + ", value=" + value + '}';
    }
  }


}
