package org.pentaho.di.trans.steps.teststepwithfields;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.teststepwithfields.TestStepWithFieldsMeta.TestFieldDefinition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit test for TestStepWithFieldsMeta and TransMeta integration testing the getThisStepFields() method.
 * This test verifies that fields configured in TestStepWithFieldsMeta are properly added to the
 * transformation's metadata using the actual implementations (not mocks).
 */
public class TestStepWithFieldsMetaGetThisStepFieldsTest {

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  private TransMeta transMeta;
  private TestStepWithFieldsMeta testStepMeta;
  private StepMeta testStep;

  @Before
  public void setUp() {
    // Create a new TransMeta instance (actual implementation, not mock)
    transMeta = new TransMeta();
    transMeta.setName( "Test Transformation" );

    // Create a new TestStepWithFieldsMeta instance (actual implementation, not mock)
    testStepMeta = new TestStepWithFieldsMeta();

    // Configure the step with additional fields
    testStepMeta.addTestField( new TestFieldDefinition( "field1", ValueMetaInterface.TYPE_STRING, 50 ) );
    testStepMeta.addTestField( new TestFieldDefinition( "field2", ValueMetaInterface.TYPE_INTEGER ) );
    testStepMeta.addTestField( new TestFieldDefinition( "field3", ValueMetaInterface.TYPE_NUMBER, 10, 2 ) );
    testStepMeta.addTestField( new TestFieldDefinition( "field4", ValueMetaInterface.TYPE_STRING, 100 ) );

    // Create a StepMeta for the test step
    testStep = new StepMeta( "TestStep", testStepMeta );

    // Add the step to the transformation
    transMeta.addStep( testStep );
  }

  @Test
  public void testGetThisStepFields_AddsConfiguredFields() throws KettleException {
    // Given: An empty input row
    RowMetaInterface inputRow = new RowMeta();

    // When: We call getThisStepFields on the test step
    RowMetaInterface outputFields = transMeta.getThisStepFields( testStep, null, inputRow );

    // Then: The output should contain all configured fields
    assertNotNull( "Output fields should not be null", outputFields );
    assertEquals( "Should have 4 fields", 4, outputFields.size() );

    // Verify field 1: field1 (String)
    ValueMetaInterface field1 = outputFields.getValueMeta( 0 );
    assertNotNull( "field1 should exist", field1 );
    assertEquals( "Field name should be field1", "field1", field1.getName() );
    assertEquals( "Field type should be String", ValueMetaInterface.TYPE_STRING, field1.getType() );
    assertEquals( "Field length should be 50", 50, field1.getLength() );
    assertEquals( "Field origin should be TestStep", "TestStep", field1.getOrigin() );

    // Verify field 2: field2 (Integer)
    ValueMetaInterface field2 = outputFields.getValueMeta( 1 );
    assertNotNull( "field2 should exist", field2 );
    assertEquals( "Field name should be field2", "field2", field2.getName() );
    assertEquals( "Field type should be Integer", ValueMetaInterface.TYPE_INTEGER, field2.getType() );

    // Verify field 3: field3 (Number with precision)
    ValueMetaInterface field3 = outputFields.getValueMeta( 2 );
    assertNotNull( "field3 should exist", field3 );
    assertEquals( "Field name should be field3", "field3", field3.getName() );
    assertEquals( "Field type should be Number", ValueMetaInterface.TYPE_NUMBER, field3.getType() );
    assertEquals( "Field length should be 10", 10, field3.getLength() );
    assertEquals( "Field precision should be 2", 2, field3.getPrecision() );

    // Verify field 4: field4 (String)
    ValueMetaInterface field4 = outputFields.getValueMeta( 3 );
    assertNotNull( "field4 should exist", field4 );
    assertEquals( "Field name should be field4", "field4", field4.getName() );
    assertEquals( "Field type should be String", ValueMetaInterface.TYPE_STRING, field4.getType() );
    assertEquals( "Field length should be 100", 100, field4.getLength() );
  }

  @Test
  public void testGetThisStepFields_WithNoFieldsConfigured() throws KettleException {
    // Given: A step with no fields configured
    testStepMeta.setTestFields( null );

    RowMetaInterface inputRow = new RowMeta();

    // When: We call getThisStepFields
    RowMetaInterface outputFields = transMeta.getThisStepFields( testStep, null, inputRow );

    // Then: The output should be empty
    assertNotNull( "Output fields should not be null", outputFields );
    assertEquals( "Should have 0 fields", 0, outputFields.size() );
  }

  @Test
  public void testGetThisStepFields_PreservesInputFields() throws KettleException {
    // Given: Input row with existing fields (simulating a previous step)
    RowMeta inputRow = new RowMeta();
    inputRow.addValueMeta( new ValueMetaString( "existing_field_1" ) );
    inputRow.addValueMeta( new ValueMetaInteger( "existing_field_2" ) );

    // When: We call getThisStepFields
    RowMetaInterface outputFields = transMeta.getThisStepFields( testStep, null, inputRow );

    // Then: The output should contain both existing and new fields
    assertNotNull( "Output fields should not be null", outputFields );
    assertEquals( "Should have 6 fields (2 existing + 4 new)", 6, outputFields.size() );

    // Verify existing fields are still present
    assertEquals( "First field should be existing_field_1", "existing_field_1",
      outputFields.getValueMeta( 0 ).getName() );
    assertEquals( "Second field should be existing_field_2", "existing_field_2",
      outputFields.getValueMeta( 1 ).getName() );

    // Verify new fields were added
    assertEquals( "Third field should be field1", "field1",
      outputFields.getValueMeta( 2 ).getName() );
    assertEquals( "Fourth field should be field2", "field2",
      outputFields.getValueMeta( 3 ).getName() );
    assertEquals( "Fifth field should be field3", "field3",
      outputFields.getValueMeta( 4 ).getName() );
    assertEquals( "Sixth field should be field4", "field4",
      outputFields.getValueMeta( 5 ).getName() );
  }

  @Test
  public void testGetThisStepFields_FieldOriginIsSetCorrectly() throws KettleException {
    // Given: An empty input row
    RowMetaInterface inputRow = new RowMeta();

    // When: We call getThisStepFields
    RowMetaInterface outputFields = transMeta.getThisStepFields( testStep, null, inputRow );

    // Then: All fields should have the correct origin
    for ( int i = 0; i < outputFields.size(); i++ ) {
      ValueMetaInterface field = outputFields.getValueMeta( i );
      assertEquals( "Field origin should be TestStep", "TestStep", field.getOrigin() );
    }
  }

  @Test
  public void testGetThisStepFields_FieldTypesAreCorrect() throws KettleException {
    // Given: An empty input row
    RowMetaInterface inputRow = new RowMeta();

    // When: We call getThisStepFields
    RowMetaInterface outputFields = transMeta.getThisStepFields( testStep, null, inputRow );

    // Then: Field types should match configuration
    assertEquals( "field1 should be String type",
      ValueMetaInterface.TYPE_STRING,
      outputFields.getValueMeta( 0 ).getType() );
    assertEquals( "field2 should be Integer type",
      ValueMetaInterface.TYPE_INTEGER,
      outputFields.getValueMeta( 1 ).getType() );
    assertEquals( "field3 should be Number type",
      ValueMetaInterface.TYPE_NUMBER,
      outputFields.getValueMeta( 2 ).getType() );
    assertEquals( "field4 should be String type",
      ValueMetaInterface.TYPE_STRING,
      outputFields.getValueMeta( 3 ).getType() );
  }

  @Test
  public void testGetThisStepFields_WithDifferentFieldTypes() throws KettleException {
    // Given: A step with various field types
    TestStepWithFieldsMeta meta = new TestStepWithFieldsMeta();
    meta.addTestField( new TestFieldDefinition( "string_field", ValueMetaInterface.TYPE_STRING, 255 ) );
    meta.addTestField( new TestFieldDefinition( "int_field", ValueMetaInterface.TYPE_INTEGER ) );
    meta.addTestField( new TestFieldDefinition( "number_field", ValueMetaInterface.TYPE_NUMBER, 15, 5 ) );
    meta.addTestField( new TestFieldDefinition( "boolean_field", ValueMetaInterface.TYPE_BOOLEAN ) );
    meta.addTestField( new TestFieldDefinition( "date_field", ValueMetaInterface.TYPE_DATE ) );
    StepMeta step = new StepMeta( "MultiTypeStep", meta );
    transMeta.addStep( step );

    RowMetaInterface inputRow = new RowMeta();

    // When: We call getThisStepFields
    RowMetaInterface outputFields = transMeta.getThisStepFields( step, null, inputRow );

    // Then: All field types should be correctly represented
    assertNotNull( "Output fields should not be null", outputFields );
    assertEquals( "Should have 5 fields", 5, outputFields.size() );

    assertEquals( "string_field", outputFields.getValueMeta( 0 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, outputFields.getValueMeta( 0 ).getType() );

    assertEquals( "int_field", outputFields.getValueMeta( 1 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, outputFields.getValueMeta( 1 ).getType() );

    assertEquals( "number_field", outputFields.getValueMeta( 2 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_NUMBER, outputFields.getValueMeta( 2 ).getType() );

    assertEquals( "boolean_field", outputFields.getValueMeta( 3 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_BOOLEAN, outputFields.getValueMeta( 3 ).getType() );

    assertEquals( "date_field", outputFields.getValueMeta( 4 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_DATE, outputFields.getValueMeta( 4 ).getType() );
  }

  @Test
  public void testGetThisStepFields_FieldLengthAndPrecisionPreserved() throws KettleException {
    // Given: Fields with specific length and precision
    TestStepWithFieldsMeta meta = new TestStepWithFieldsMeta();
    meta.addTestField( new TestFieldDefinition( "varchar_field", ValueMetaInterface.TYPE_STRING, 200 ) );
    meta.addTestField( new TestFieldDefinition( "decimal_field", ValueMetaInterface.TYPE_NUMBER, 18, 4 ) );

    StepMeta step = new StepMeta( "PrecisionStep", meta );
    transMeta.addStep( step );

    RowMetaInterface inputRow = new RowMeta();

    // When: We call getThisStepFields
    RowMetaInterface outputFields = transMeta.getThisStepFields( step, null, inputRow );

    // Then: Length and precision should be preserved
    ValueMetaInterface varcharField = outputFields.getValueMeta( 0 );
    assertEquals( "varchar_field length should be 200", 200, varcharField.getLength() );

    ValueMetaInterface decimalField = outputFields.getValueMeta( 1 );
    assertEquals( "decimal_field length should be 18", 18, decimalField.getLength() );
    assertEquals( "decimal_field precision should be 4", 4, decimalField.getPrecision() );
  }
}
