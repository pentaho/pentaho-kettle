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

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.mockito.Mockito;

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
    serviceFields = Mockito.mock( RowMetaInterface.class );

    String nospace = NO_SPACE;
    String withSpaces = WITH_SPACES;
    ValueMetaInterface nospaceVmi = Mockito.mock( ValueMetaInterface.class );
    ValueMetaInterface spaceVmi = Mockito.mock( ValueMetaInterface.class );

    List<ValueMetaInterface> valueMetaList = Arrays.asList( nospaceVmi, spaceVmi );
    Mockito.when( serviceFields.getValueMetaList() ).thenReturn( valueMetaList );
    Mockito.when( nospaceVmi.getName() ).thenReturn( nospace );
    Mockito.when( spaceVmi.getName() ).thenReturn( withSpaces );
    Mockito.when( serviceFields.searchValueMeta( nospace ) ).thenReturn( nospaceVmi );
    Mockito.when( serviceFields.searchValueMeta( withSpaces ) ).thenReturn( spaceVmi );
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
    Assert.assertThat( sqlFields.getTableAlias(), CoreMatchers.sameInstance( TABLE ) );
    Assert.assertThat( sqlFields.getServiceFields(), CoreMatchers.sameInstance( serviceFields ) );
    Assert.assertThat( sqlFields.getFieldsClause(), CoreMatchers.equalTo( selectClause ) );

    Assert.assertThat( sqlFields.getFields(), validSqlFields() );
    Assert.assertThat( sqlFields.findByName( NO_SPACE ), sqlField( NO_SPACE ) );

    Assert.assertThat( sqlFields.getNonAggregateFields(), validSqlFields() );
    Assert.assertThat( sqlFields.getRegularFields(), validSqlFields() );

    Assert.assertThat( sqlFields.getAggregateFields(), Matchers.empty() );
    Assert.assertThat( sqlFields.hasAggregates(), Matchers.is( false ) );

    Assert.assertThat( sqlFields.getConstantFields(), Matchers.empty() );
    Assert.assertThat( sqlFields.getIifFunctionFields(), Matchers.empty() );
  }

  @Test
  public void testParseStar() throws KettleSQLException {
    SQLFields sqlFields = new SQLFields( TABLE, serviceFields, "*" );

    Assert.assertThat( sqlFields.getFields(), validSqlFields() );
  }

  @Test
  public void testDistinct() throws Exception {
    for ( SQLFields sqlFields : ImmutableList.of(
      new SQLFields( TABLE, serviceFields, "distinct " + SELECT_CLAUSE ),
      new SQLFields( TABLE, serviceFields, "Distinct " + SELECT_CLAUSE ),
      new SQLFields( TABLE, serviceFields, "DISTINCT " + SELECT_CLAUSE )
    ) ) {
      Assert.assertThat( sqlFields.getFieldsClause(), CoreMatchers.equalTo( SELECT_CLAUSE ) );
      Assert.assertThat( sqlFields.getFields(), validSqlFields() );
      Assert.assertThat( sqlFields.isDistinct(), Matchers.is( true ) );
    }
  }


  public Matcher<Iterable<? extends SQLField>> validSqlFields() {
    return Matchers.contains( sqlField( NO_SPACE ), sqlField( WITH_SPACES ) );
  }

  public Matcher<? super SQLField> sqlField( String name ) {
    return CoreMatchers.allOf(
      Matchers.instanceOf( SQLField.class ),
      Matchers.hasProperty( "name", CoreMatchers.equalTo( name ) )
    );
  }
}
