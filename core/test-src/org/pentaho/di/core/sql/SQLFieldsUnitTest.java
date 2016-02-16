/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.sql;

import com.google.common.collect.ImmutableList;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleSQLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings( "unchecked" )
public class SQLFieldsUnitTest {
  private static final String NO_SPACE = "nospace";
  private static final String WITH_SPACES = "with spaces";
  private static final String SELECT_CLAUSE = String.format( "%s, \"%s\"", NO_SPACE, WITH_SPACES );
  private static final String TABLE = "table";
  private static final String SELECT_CLAUSE_TABLE_QUALIFIER = String.format(
    "%s.%s, \"%s\".\"%s\"", TABLE, NO_SPACE, TABLE, WITH_SPACES );

  private RowMetaInterface serviceFields;

  @Before
  public void setup() {
    serviceFields = mock( RowMetaInterface.class );

    String nospace = NO_SPACE;
    String withSpaces = WITH_SPACES;
    ValueMetaInterface nospaceVmi = mock( ValueMetaInterface.class );
    ValueMetaInterface spaceVmi = mock( ValueMetaInterface.class );

    List<ValueMetaInterface> valueMetaList = Arrays.asList( nospaceVmi, spaceVmi );
    when( serviceFields.getValueMetaList() ).thenReturn( valueMetaList );
    when( nospaceVmi.getName() ).thenReturn( nospace );
    when( spaceVmi.getName() ).thenReturn( withSpaces );
    when( serviceFields.searchValueMeta( nospace ) ).thenReturn( nospaceVmi );
    when( serviceFields.searchValueMeta( withSpaces ) ).thenReturn( spaceVmi );
  }

  @Test
  public void testParseFields() throws KettleSQLException {
    SQLFields sqlFields = new SQLFields( TABLE, serviceFields, SELECT_CLAUSE );
    checkSqlFields( sqlFields, SELECT_CLAUSE );
  }

  @Test
  public void testParseFieldsWithTableQualifier() throws KettleSQLException {
    SQLFields sqlFields = new SQLFields( TABLE, serviceFields, SELECT_CLAUSE_TABLE_QUALIFIER );
    // same test as the preceding but with fields referenced like "table"."with space".
    checkSqlFields( sqlFields, SELECT_CLAUSE_TABLE_QUALIFIER );
  }

  private void checkSqlFields( SQLFields sqlFields, String selectClause ) {
    assertThat( sqlFields.getTableAlias(), sameInstance( TABLE ) );
    assertThat( sqlFields.getServiceFields(), sameInstance( serviceFields ) );
    assertThat( sqlFields.getFieldsClause(), equalTo( selectClause ) );

    assertThat( sqlFields.getFields(), validSqlFields() );
    assertThat( sqlFields.findByName( NO_SPACE ), sqlField( NO_SPACE ) );

    assertThat( sqlFields.getNonAggregateFields(), validSqlFields() );
    assertThat( sqlFields.getRegularFields(), validSqlFields() );

    assertThat( sqlFields.getAggregateFields(), empty() );
    assertThat( sqlFields.hasAggregates(), is( false ) );

    assertThat( sqlFields.getConstantFields(), empty() );
    assertThat( sqlFields.getIifFunctionFields(), empty() );
  }

  @Test
  public void testParseStar() throws KettleSQLException {
    SQLFields sqlFields = new SQLFields( TABLE, serviceFields, "*" );

    assertThat( sqlFields.getFields(), validSqlFields() );
  }

  @Test
  public void testDistinct() throws Exception {
    for ( SQLFields sqlFields : ImmutableList.of(
      new SQLFields( TABLE, serviceFields, "distinct " + SELECT_CLAUSE ),
      new SQLFields( TABLE, serviceFields, "Distinct " + SELECT_CLAUSE ),
      new SQLFields( TABLE, serviceFields, "DISTINCT " + SELECT_CLAUSE )
    ) ) {
      assertThat( sqlFields.getFieldsClause(), equalTo( SELECT_CLAUSE ) );
      assertThat( sqlFields.getFields(), validSqlFields() );
      assertThat( sqlFields.isDistinct(), is( true ) );
    }
  }


  public Matcher<Iterable<? extends SQLField>> validSqlFields() {
    return contains( sqlField( NO_SPACE ), sqlField( WITH_SPACES ) );
  }

  public Matcher<? super SQLField> sqlField( String name ) {
    return allOf(
      instanceOf( SQLField.class ),
      hasProperty( "name", equalTo( name ) )
    );
  }
}
