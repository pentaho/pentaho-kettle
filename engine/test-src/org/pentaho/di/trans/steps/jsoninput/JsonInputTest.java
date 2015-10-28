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

package org.pentaho.di.trans.steps.jsoninput;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.ComparisonFailure;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

public class JsonInputTest {

  private StepMockHelper<JsonInputMeta, JsonInputData> helper;

  private static final String getBasicTestJson() {
    return "{ \"store\": {\n"
        + "    \"book\": [ \n"
        + "      { \"category\": \"reference\",\n"
        + "        \"author\": \"Nigel Rees\",\n"
        + "        \"title\": \"Sayings of the Century\",\n"
        + "        \"price\": 8.95\n"
        + "      },\n"
        + "      { \"category\": \"fiction\",\n"
        + "        \"author\": \"Evelyn Waugh\",\n"
        + "        \"title\": \"Sword of Honour\",\n"
        + "        \"price\": 12.99\n"
        + "      },\n"
        + "      { \"category\": \"fiction\",\n"
        + "        \"author\": \"Herman Melville\",\n"
        + "        \"title\": \"Moby Dick\",\n"
        + "        \"isbn\": \"0-553-21311-3\",\n"
        + "        \"price\": 8.99\n"
        + "      },\n"
        + "      { \"category\": \"fiction\",\n"
        + "        \"author\": \"J. R. R. Tolkien\",\n"
        + "        \"title\": \"The Lord of the Rings\",\n"
        + "        \"isbn\": \"0-395-19395-8\",\n"
        + "        \"price\": 22.99\n"
        + "      }\n"
        + "    ],\n"
        + "    \"bicycle\": {\n"
        + "      \"color\": \"red\",\n"
        + "      \"price\": 19.95\n"
        + "    }\n"
        + "  }\n"
        + "}";
  }

  @BeforeClass
  public static void init() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() {
    helper =
        new StepMockHelper<JsonInputMeta, JsonInputData>( "json input test", JsonInputMeta.class, JsonInputData.class );
    when( helper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        helper.logChannelInterface );
    when( helper.trans.isRunning() ).thenReturn( true );
  }

  @After
  public void tearDown() {
    helper.cleanUp();
  }

  @Test
  public void testAttrFilter() throws Exception {
    final String jsonInputField = getBasicTestJson();
    testSimpleJsonPath( "$..book[?(@.isbn)].author", new ValueMetaString( "author w/ isbn" ),
        new Object[][] { new Object[] { jsonInputField } },
        new Object[][] { new Object[] { jsonInputField, "Herman Melville" },
                         new Object[] { jsonInputField, "J. R. R. Tolkien" } } );
  }

  @Test
  public void testChildDot() throws Exception {
    final String jsonInputField = getBasicTestJson();
    testSimpleJsonPath( "$.store.bicycle.color", new ValueMetaString( "bcol" ),
        new Object[][] { new Object[] { jsonInputField } },
        new Object[][] { new Object[] { jsonInputField, "red" } } );
    testSimpleJsonPath( "$.store.bicycle.price", new ValueMetaNumber( "p" ),
        new Object[][] { new Object[] { jsonInputField } },
        new Object[][] { new Object[] { jsonInputField, 19.95 } } );
  }

  @Test
  public void testChildBrackets() throws Exception {
    final String jsonInputField = getBasicTestJson();
    testSimpleJsonPath( "$..['store']['bicycle']['color']", new ValueMetaString( "bcol" ),
        new Object[][] { new Object[] { jsonInputField } },
        new Object[][] { new Object[] { jsonInputField, "red" } } );
  }

  @Test
  public void testIndex() throws Exception {
    final String jsonInputField = getBasicTestJson();
    testSimpleJsonPath( "$..book[2].title", new ValueMetaString( "title" ),
        new Object[][] { new Object[] { jsonInputField } },
        new Object[][] { new Object[] { jsonInputField, "Moby Dick" } } );
  }

  @Test
  public void testIndexFirst() throws Exception {
    final String jsonInputField = getBasicTestJson();
    testSimpleJsonPath( "$..book[:2].category", new ValueMetaString( "category" ),
        new Object[][] { new Object[] { jsonInputField } },
        new Object[][] { new Object[] { jsonInputField, "reference" },
                         new Object[] { jsonInputField, "fiction" } } );
  }

  @Test
  public void testIndexLastObj() throws Exception {
    final String jsonInputField = getBasicTestJson();
    JsonInput jsonInput =
        createBasicTestJsonInput( "$..book[-1:]", new ValueMetaString( "last book" ), "json",
            new Object[] { jsonInputField } );
    RowComparatorListener rowComparator = new RowComparatorListener(
        new Object[] { jsonInputField,
                       "{ \"category\": \"fiction\",\n"
                     + "  \"author\": \"J. R. R. Tolkien\",\n"
                     + "  \"title\": \"The Lord of the Rings\",\n"
                     + "  \"isbn\": \"0-395-19395-8\",\n"
                     + "  \"price\": 22.99\n"
                     + "}\n" } );
    rowComparator.setComparator( 1, new RowComparatorListener.Comparison<Object>() {
      @Override
      public boolean equals( Object one, Object two ) throws Exception {
        return jsonEquals( (String) one, (String) two );
      }
    } );
    jsonInput.addRowListener( rowComparator );
    processRows( jsonInput, 2 );
    Assert.assertEquals(1, jsonInput.getLinesWritten() );
  }

  @Test
  public void testIndexList() throws Exception {
    final String jsonInputField = getBasicTestJson();
    testSimpleJsonPath( "$..book[1,3].price", new ValueMetaNumber( "price" ),
        new Object[][] { new Object[] { jsonInputField } },
        new Object[][] { new Object[] { jsonInputField, 12.99 },
                         new Object[] { jsonInputField, 22.99 } } );
  }

  @Test
  public void testDualExp() throws Exception {
    JsonInputField isbn = new JsonInputField( "isbn" );
    isbn.setPath( "$..book[?(@.isbn)].isbn" );
    isbn.setType( ValueMetaInterface.TYPE_STRING );
    JsonInputField price = new JsonInputField( "price" );
    price.setPath( "$..book[?(@.isbn)].price" );
    price.setType( ValueMetaInterface.TYPE_NUMBER );

    JsonInputMeta meta = createSimpleMeta( "json", isbn, price );
    JsonInput jsonInput = createJsonInput( "json", meta, new Object[] { getBasicTestJson() } );
    RowComparatorListener rowComparator = new RowComparatorListener(
        new Object[] { null, "0-553-21311-3", 8.99 },
        new Object[] { null, "0-395-19395-8", 22.99 } );
    rowComparator.setComparator( 0, null );
    processRows( jsonInput, 3 );
    Assert.assertEquals("error", 0, jsonInput.getErrors() );
    Assert.assertEquals("lines written", 2, jsonInput.getLinesWritten() );
  }

  @Test
  public void testDualExpMismatchError() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    helper.redirectLog( out, LogLevel.ERROR );
    JsonInputField isbn = new JsonInputField( "isbn" );
    isbn.setPath( "$..book[?(@.isbn)].isbn" );
    isbn.setType( ValueMetaInterface.TYPE_STRING );
    JsonInputField price = new JsonInputField( "price" );
    price.setPath( "$..book[*].price" );
    price.setType( ValueMetaInterface.TYPE_NUMBER );

    try ( LocaleChange enUS = new LocaleChange( Locale.US ) ) {
      JsonInputMeta meta = createSimpleMeta( "json", isbn, price );
      JsonInput jsonInput = createJsonInput( "json", meta, new Object[] { getBasicTestJson() } );

      processRows( jsonInput, 3 );

      Assert.assertEquals( "error", 1, jsonInput.getErrors() );
      Assert.assertEquals( "rows written", 0, jsonInput.getLinesWritten() );
      String errors = IOUtils.toString( new ByteArrayInputStream( out.toByteArray() ), StandardCharsets.UTF_8.name() );
      String expectedError =
          "The data structure is not the same inside the resource!"
          + " We found 4 values for json path [$..book[*].price],"
          + " which is different that the number returned for path [$..book[?(@.isbn)].isbn] (2 values)."
          + " We MUST have the same number of values for all paths.";
      Assert.assertTrue( errors.contains( expectedError ) );
    }
  }

  @Test
  public void testBadExp() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    helper.redirectLog( out, LogLevel.ERROR );

    try ( LocaleChange enUS = new LocaleChange( Locale.US ) ) {
      JsonInput jsonInput = createBasicTestJsonInput( "$..fail", new ValueMetaString( "result" ), "json",
          new Object[] { getBasicTestJson() } );
      processRows( jsonInput, 2 );
      Assert.assertEquals( "error", 1, jsonInput.getErrors() );
      Assert.assertEquals( "rows written", 0, jsonInput.getLinesWritten() );

      String expectedError = "We can not find any data with path [$..fail]";
      String errors = IOUtils.toString( new ByteArrayInputStream( out.toByteArray() ), StandardCharsets.UTF_8.name() );
      Assert.assertTrue( errors.contains( expectedError ) );
    }
  }

  private void testSimpleJsonPath( String jsonPath,
      ValueMetaInterface outputMeta,
      Object[][] inputRows, Object[][] outputRows ) throws Exception {
    final String inCol = "in";

    JsonInput jsonInput = createBasicTestJsonInput( jsonPath, outputMeta, inCol, inputRows );

    RowComparatorListener rowComparator = new RowComparatorListener( outputRows );

    jsonInput.addRowListener( rowComparator );
    processRows( jsonInput, outputRows.length + 1 );
    Assert.assertEquals( "rows written", outputRows.length, jsonInput.getLinesWritten() );
    Assert.assertEquals( "errors", 0, jsonInput.getErrors() );
  }

  private void processRows( StepInterface step, final int maxCalls ) throws Exception {
    for ( int outRowIdx = 0; outRowIdx < maxCalls; outRowIdx++ ) {
      if ( !step.processRow( helper.processRowsStepMetaInterface, helper.processRowsStepDataInterface ) ) {
        break;
      }
    }
  }

  private JsonInput createBasicTestJsonInput( String jsonPath, ValueMetaInterface outputMeta, final String inCol,
      Object[]... inputRows ) {
    JsonInputField jpath = new JsonInputField( outputMeta.getName() );
    jpath.setPath( jsonPath );
    jpath.setType( outputMeta.getType() );

    JsonInputMeta meta = createSimpleMeta( inCol, jpath );
    return createJsonInput( inCol, meta, inputRows );
  }

  private JsonInput createJsonInput( final String inCol, JsonInputMeta meta, Object[]... inputRows ) {
    JsonInputData data = new JsonInputData();

    JsonInput jsonInput = new JsonInput( helper.stepMeta, helper.stepDataInterface, 0, helper.transMeta, helper.trans );

    RowSet input = helper.getMockInputRowSet( inputRows );
    RowMetaInterface rowMeta = createRowMeta( new ValueMetaString( inCol ) );
    input.setRowMeta( rowMeta );
    jsonInput.getInputRowSets().add( input );
    jsonInput.setInputRowMeta( rowMeta );
    jsonInput.init( meta, data );
    return jsonInput;
  }

  private JsonInputMeta createSimpleMeta( String inputColumn, JsonInputField ... jsonPathFields ) {
    JsonInputMeta jsonInputMeta = new JsonInputMeta();
    jsonInputMeta.setDefault();
    jsonInputMeta.setInFields( true );
    jsonInputMeta.setFieldValue( inputColumn );
    jsonInputMeta.setInputFields( jsonPathFields );
    return jsonInputMeta;
  }

  private static class RowComparatorListener extends RowAdapter {

    Object[][] data;
    int rowNbr = 0;
    private Map<Integer, Comparison<Object>> comparators = new HashMap<>();

    public RowComparatorListener( Object[]... data ) {
      this.data = data;
    }

    /**
     * @param colIdx
     * @param comparator
     */
    public void setComparator( int colIdx, Comparison<Object> comparator ) {
      comparators.put( colIdx, comparator );
    }

    @Override
    public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
      if ( rowNbr >= data.length ) {
        throw new ComparisonFailure( "too many output rows", "" + data.length, "" + rowNbr );
      } else {
        for ( int i = 0; i < data[rowNbr].length; i++ ) {
          try {
            boolean eq = true;
            if ( comparators.containsKey( i ) ) {
              Comparison<Object> comp = comparators.get( i );
              if ( comp != null ) {
                eq = comp.equals( data[rowNbr][i], row[i] );
              }
            } else {
              ValueMetaInterface valueMeta = rowMeta.getValueMeta( i );
              eq = valueMeta.compare( data[rowNbr][i], row[i] ) == 0;
            }
            if ( !eq ) {
              throw new ComparisonFailure( String.format( "Mismatch row %d, column %d", rowNbr, i ), rowMeta
                  .getString( data[rowNbr] ), rowMeta.getString( row ) );
            }
          } catch ( Exception e ) {
            throw new AssertionError( String.format( "Value type at row %d, column %d", rowNbr, i ), e );
          }
        }
        rowNbr++;
      }
    }

    protected static interface Comparison<T> {
      public boolean equals( T one, T two ) throws Exception;
    }
  }

  private static class LocaleChange implements AutoCloseable {

    private Locale original;

    public LocaleChange( Locale newLocale ) {
      original = LanguageChoice.getInstance().getDefaultLocale();
      LanguageChoice.getInstance().setDefaultLocale( newLocale );
    }

    @Override
    public void close() throws Exception {
      LanguageChoice.getInstance().setDefaultLocale( original );
    }
  }

  /**
   * compare json (deep equals ignoring order)
   */
  private static final boolean jsonEquals( String json1, String json2 ) throws Exception {
    ObjectMapper om = new ObjectMapper();
    JsonNode parsedJson1 = om.readTree( json1 );
    JsonNode parsedJson2 = om.readTree( json2 );
    return parsedJson1.equals( parsedJson2 );
  }

  private static RowMetaInterface createRowMeta( ValueMetaInterface ... valueMetas ) {
    RowMeta rowMeta = new RowMeta();
    rowMeta.setValueMetaList( Arrays.asList( valueMetas ) );
    return rowMeta;
  }

}
