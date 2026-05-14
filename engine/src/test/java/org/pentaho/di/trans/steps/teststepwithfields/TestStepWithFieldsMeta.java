package org.pentaho.di.trans.steps.teststepwithfields;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Test step meta that adds configurable fields to the output.
 * Used for testing TransMeta.getThisStepFields() functionality.
 */
public class TestStepWithFieldsMeta extends BaseStepMeta implements StepMetaInterface {

  private List<TestFieldDefinition> fields;

  public TestStepWithFieldsMeta() {
    super();
    this.fields = new ArrayList<>();
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    // Not implemented for this test class
  }

  @Override
  public Object clone() {
    TestStepWithFieldsMeta retval = (TestStepWithFieldsMeta) super.clone();
    if ( this.fields != null ) {
      retval.fields = new ArrayList<>();
      for ( TestFieldDefinition field : this.fields ) {
        retval.fields.add( field.clone() );
      }
    }
    return retval;
  }

  @Override
  public void setDefault() {
    fields = new ArrayList<>();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId stepId, List<DatabaseMeta> databases )
    throws KettleException {
    // Not implemented for this test class
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId transformationId, ObjectId stepId )
    throws KettleException {
    // Not implemented for this test class
  }

  // This API signature is the one being tested. It exists for backwards compatibility with existing published plugins, 
  // and is called from TransMeta.compatibleGetStepFields. 
  // This test exists to verify that this signature continues to be called. DO NOT REMOVE or change this method signature.
  @Override
  @Deprecated // but explicitly here for the test
  public void getFields( RowMetaInterface rowMeta, String origin, RowMetaInterface[] info,
                         StepMeta nextStep, VariableSpace space )
    throws KettleStepException {
    // Add all configured fields to the row metadata
    if ( fields != null ) {
      for ( TestFieldDefinition field : fields ) {
        try {
          ValueMetaInterface vm = ValueMetaFactory.createValueMeta( field.getName(), field.getType() );
          vm.setOrigin( origin );
          vm.setLength( field.getLength() );
          vm.setPrecision( field.getPrecision() );
          rowMeta.addValueMeta( vm );
        } catch ( Exception e ) {
          throw new KettleStepException( "Error creating value meta for field: " + field.getName(), e );
        }
      }
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
                     RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info,
                     VariableSpace space, Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    if ( fields == null || fields.isEmpty() ) {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING,
        "No fields have been configured", stepMeta );
      remarks.add( cr );
    } else {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK,
        fields.size() + " field(s) configured", stepMeta );
      remarks.add( cr );
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
                                 TransMeta transMeta, Trans trans ) {
    return new TestStepWithFields( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new TestStepWithFieldsData();
  }

  /**
   * Get the list of field definitions
   */
  public List<TestFieldDefinition> getTestFields() {
    return fields;
  }

  /**
   * Set the list of field definitions
   */
  public void setTestFields( List<TestFieldDefinition> fields ) {
    this.fields = fields;
  }

  /**
   * Add a field definition
   */
  public void addTestField( TestFieldDefinition field ) {
    if ( this.fields == null ) {
      this.fields = new ArrayList<>();
    }
    this.fields.add( field );
  }

  /**
   * Class representing a field definition
   */
  public static class TestFieldDefinition implements Cloneable {
    private String name;
    private int type;
    private int length;
    private int precision;

    public TestFieldDefinition() {
      this.type = ValueMetaInterface.TYPE_STRING;
      this.length = -1;
      this.precision = -1;
    }

    public TestFieldDefinition( String name, int type ) {
      this();
      this.name = name;
      this.type = type;
    }

    public TestFieldDefinition( String name, int type, int length ) {
      this( name, type );
      this.length = length;
    }

    public TestFieldDefinition( String name, int type, int length, int precision ) {
      this( name, type, length );
      this.precision = precision;
    }

    @Override
    public TestFieldDefinition clone() {
      try {
        return (TestFieldDefinition) super.clone();
      } catch ( CloneNotSupportedException e ) {
        return new TestFieldDefinition( name, type, length, precision );
      }
    }

    public String getName() {
      return name;
    }

    public void setName( String name ) {
      this.name = name;
    }

    public int getType() {
      return type;
    }

    public void setType( int type ) {
      this.type = type;
    }

    public int getLength() {
      return length;
    }

    public void setLength( int length ) {
      this.length = length;
    }

    public int getPrecision() {
      return precision;
    }

    public void setPrecision( int precision ) {
      this.precision = precision;
    }
  }
}
