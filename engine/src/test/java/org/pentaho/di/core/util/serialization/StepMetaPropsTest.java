/*
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import com.google.common.base.Objects;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.streaming.common.BaseStreamStepMeta;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class StepMetaPropsTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();


  @InjectionSupported ( localizationPrefix = "stuff", groups = { "stuffGroup" } ) static class FooMeta
    extends BaseStreamStepMeta {

    @Sensitive
    @Injection ( name = "FIELD1", group = "stuffGroup" ) String field1 = "default";
    @Injection ( name = "FIELD2", group = "stuffGroup" ) int field2 = 123;

    @Sensitive
    @Injection ( name = "PassVerd" ) String password = "should.be.encrypted";


    @Injection ( name = "ALIST" ) List<String> alist = new ArrayList<>();

    @Sensitive
    @Injection ( name = "SECURELIST" ) List<String> securelist = new ArrayList<>();

    @Injection ( name = "BOOLEANLIST" ) List<Boolean> blist = new ArrayList<>();
    @Injection ( name = "IntList" ) List<Integer> ilist = new ArrayList<>();

    @InjectionDeep SoooDeep deep = new SoooDeep();

    static class SoooDeep {
      @Injection ( name = "DEEP_FLAG" ) boolean isItDeep = true;
      @Injection ( name = "DEPTH" ) int howDeep = 1000;
      @Injection ( name = "DEEP_LIST " ) List<String> deepList = new ArrayList<>();

      @Sensitive
      @Injection ( name = "DEEP_PASSWORD" ) String password = "p@ssword";

      @Override public boolean equals( Object o ) {
        if ( this == o ) {
          return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
          return false;
        }
        SoooDeep soooDeep = (SoooDeep) o;
        return isItDeep == soooDeep.isItDeep && howDeep == soooDeep.howDeep;
      }

      @Override public int hashCode() {
        return Objects.hashCode( isItDeep, howDeep );
      }
    }


    @Override
    public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
                                  TransMeta transMeta,
                                  Trans trans ) {
      return null;
    }

    @Override public StepDataInterface getStepData() {
      return null;
    }

    @Override public boolean equals( Object o ) {
      if ( this == o ) {
        return true;
      }
      if ( o == null || getClass() != o.getClass() ) {
        return false;
      }
      FooMeta fooMeta = (FooMeta) o;
      return field2 == fooMeta.field2
        && Objects.equal( field1, fooMeta.field1 )
        && Objects.equal( password, fooMeta.password )
        && Objects.equal( alist, fooMeta.alist )
        && Objects.equal( securelist, fooMeta.securelist )
        && Objects.equal( blist, fooMeta.blist )
        && Objects.equal( ilist, fooMeta.ilist )
        && Objects.equal( deep, fooMeta.deep );
    }

    @Override public int hashCode() {
      return Objects.hashCode( field1, field2, password, alist, securelist, blist, ilist, deep );
    }

    @Override public String toString() {
      return String
        .format( "FooMeta{%nfield1='%s', %nfield2=%d, %nalist=%s, %nblist=%s, %nilist=%s}",
          field1, field2, alist, blist, ilist );
    }

    @Override public RowMeta getRowMeta( String origin, VariableSpace space ) {
      return null;
    }
  }


  @Before
  public void before() throws KettleException {
    KettleEnvironment.init();
  }

  @Test
  public void testToAndFrom() {
    FooMeta foo = getTestFooMeta();

    StepMetaProps fromMeta = StepMetaProps.from( foo );

    FooMeta toMeta = new FooMeta();
    fromMeta.to( toMeta );

    assertThat( foo, equalTo( toMeta ) );
  }

  @Test
  public void testEncrypt() {
    FooMeta foo = getTestFooMeta();
    foo.password = "p@ssword";
    StepMetaProps stepMetaProps = StepMetaProps.from( foo );

    assertThat( "password field should be encrypted, so should not be present in the .toString of the props",
      stepMetaProps.toString(), not( containsString( "p@ssword" ) ) );

    FooMeta toMeta = new FooMeta();
    stepMetaProps.to( toMeta );

    assertThat( foo, equalTo( toMeta ) );
    assertThat( "p@ssword", equalTo( toMeta.password ) );
  }

  @Test
  public void testEncryptedList() {
    FooMeta foo = getTestFooMeta();

    foo.securelist = asList( "shadow", "substance" );
    StepMetaProps stepMetaProps = StepMetaProps.from( foo );


    assertThat(
      "secureList should be encrypted, so raw values should not be present in the .toString of the props",
      stepMetaProps.toString(), not( containsString( "expectedString" ) ) );
    asList( "shadow", "substance" ).forEach( val ->
      assertThat( val + " should be encrypted, so should not be present in the .toString of the props",
        stepMetaProps.toString(), not( containsString( val ) ) ) );

    FooMeta toMeta = new FooMeta();
    stepMetaProps.to( toMeta );

    assertThat( foo, equalTo( toMeta ) );
    assertThat( asList( "shadow", "substance" ), equalTo( toMeta.securelist ) );
  }

  @Test
  public void testInjectionDeep() {
    FooMeta fooMeta = getTestFooMeta();
    fooMeta.deep.howDeep = 50;
    fooMeta.deep.isItDeep = false;

    FooMeta toMeta = new FooMeta();
    StepMetaProps.from( fooMeta ).to( toMeta );
    assertThat( 50, equalTo( toMeta.deep.howDeep ) );
    assertThat( false, equalTo( toMeta.deep.isItDeep ) );
  }


  @Test
  public void sensitiveFieldsCheckedAtMultipleLevels() {
    // verifies that fields below the top level can be correctly identified as Sensitive.
    class DeeperContainer {
      @Sensitive @Injection ( name = "Sensitive" ) String sensitive = "very sensitive";
      @Injection ( name = "NotSensitive" ) String notSensitive = "cold and unfeeling";
    }
    class DeepContainer {
      @InjectionDeep DeeperContainer deeperObj = new DeeperContainer();
    }
    Object topLevelObject = new Object() {
      @InjectionDeep DeepContainer deepObj = new DeepContainer();
    };
    List<String> sensitiveFields = StepMetaProps.sensitiveFields( topLevelObject.getClass() );

    assertThat( sensitiveFields, equalTo( singletonList( "Sensitive" ) ) );
  }

  @Test
  public void variableSubstitutionHappens() {
    // Tests that the .withVariables method allows creation of a copy of
    // the step meta with all variables substituted, both in lists and field strings,
    // and in deep meta injection
    FooMeta fooMeta = getTestFooMeta();

    fooMeta.field1 = "${field1Sub}";
    fooMeta.alist = asList( "noSub", "${listEntrySub}", "${listEntrySub2}", "noSubAgain" );
    fooMeta.password = "${encryptedSub}";
    fooMeta.deep.deepList = asList( "deepNotSubbed", "${deepListSub}" );

    FooMeta newFoo = new FooMeta();

    VariableSpace variables = new Variables();
    variables.setVariable( "field1Sub", "my substituted value" );
    variables.setVariable( "listEntrySub", "list sub" );
    variables.setVariable( "listEntrySub2", "list sub 2" );
    variables.setVariable( "encryptedSub", "encrypted sub" );
    variables.setVariable( "deepListSub", "deep list sub" );

    StepMetaProps
      .from( fooMeta )
      .withVariables( variables )
      .to( newFoo );

    assertThat( "my substituted value", equalTo( newFoo.field1 ) );
    assertThat( "list sub", equalTo( newFoo.alist.get( 1 ) ) );
    assertThat( "list sub 2", equalTo( newFoo.alist.get( 2 ) ) );
    assertThat( "encrypted sub", equalTo( newFoo.password ) );
    assertThat( "deep list sub", equalTo( newFoo.deep.deepList.get( 1 ) ) );
    assertThat( "noSub", equalTo( newFoo.alist.get( 0 ) ) );
  }


  static FooMeta getTestFooMeta() {
    FooMeta foo = new FooMeta();

    foo.field1 = "expectedString";
    foo.field2 = 42;
    foo.alist = asList( "one", "two", "three", "four" );
    foo.blist = asList( true, false, false );
    foo.ilist = asList( 1, 4, 26 );
    return foo;
  }


}
