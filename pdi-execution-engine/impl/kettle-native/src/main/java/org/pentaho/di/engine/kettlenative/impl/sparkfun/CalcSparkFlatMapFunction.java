package org.pentaho.di.engine.kettlenative.impl.sparkfun;

import org.apache.spark.api.java.function.FlatMapFunction;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNone;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.engine.api.IData;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.calculator.Calculator;
import org.pentaho.di.trans.steps.calculator.CalculatorData;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.pentaho.di.engine.kettlenative.impl.KettleNativeUtil.createTrans;

/**
 * Wrap Kettle's Calculator step, allowing it to be run as a spark FlatMapFunction.
 */
public class CalcSparkFlatMapFunction implements Serializable, FlatMapFunction<IData, IData> {
  private  transient TransMeta transMeta;
  String stepName;

  public CalcSparkFlatMapFunction( TransMeta transMeta, String stepName ) {
    this.stepName = stepName;
    this.transMeta = transMeta;
  }

  private Calculator createCalculator( TransMeta transMeta, String stepName, List<Object[]> input, List<Object[]> output ) {
    return new Calculator( transMeta.findStep( stepName ), null, 0, transMeta, createTrans() ) {
      @Override
      public void putRow( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
        output.add(row );
      }
      @Override
      public Object[] getRow() throws KettleException {
        return input.size() == 0 ? null : input.remove(0);
      }
    };
  }

  @Override public Iterator<IData> call( IData data ) throws Exception {
    try {
      if (data == null || data.getData() == null || data.getData()[0] == null) {
        return Collections.<IData>emptyList().iterator();
      }
      List<Object[]> input = new ArrayList<>();
      List<Object[]> output = new ArrayList<>();
      Calculator calc = createCalculator( transMeta, stepName, input, output );
      RowMetaInterface rowMetaInterface = inferRowMeta( data.getData() );
      CalculatorData calcData = new CalculatorData();
      calcData.setCalcRowMeta( rowMetaInterface );
      input.add( data.getData() );
      calc.setInputRowMeta( rowMetaInterface );
      calc.processRow( transMeta.findStep( stepName ).getStepMetaInterface(), calcData );

      return output.stream()
        .map( objects -> (IData) () -> objects )
        .collect( Collectors.toList() )
        .iterator();
    } catch ( KettleException e ) {
      throw new RuntimeException( e );
    }

  }

  private RowMetaInterface inferRowMeta( Object[] input ) {
    RowMetaInterface rowMeta;
    try {
      rowMeta = transMeta.getStepFields( transMeta.findStep( stepName ) );
    } catch ( KettleStepException e ) {
      throw new RuntimeException( e );
    }
    int i = 1;
    for ( Object o : input ) {
      if ( o == null ) {
        continue;
      }
      switch( o.getClass().getTypeName() ) {
        case "java.lang.String": rowMeta.addValueMeta( new ValueMetaString( rowMeta.getFieldNames()[ i++ ] ) );
          break;
        case "java.lang.Long": rowMeta.addValueMeta( new ValueMetaInteger( rowMeta.getFieldNames()[ i++ ] ) );
          break;
        default: rowMeta.addValueMeta( new ValueMetaNone( rowMeta.getFieldNames()[ i++ ] ) );
      }
    }
    return rowMeta;
  }


  /**
   * transMeta is not serializable.  Leverage the custom serialization.
   */
  private void writeObject(ObjectOutputStream oos)
    throws IOException {
    oos.defaultWriteObject();
    try {
      oos.writeObject( transMeta.getXML() );
    } catch ( KettleException e ) {
      e.printStackTrace();
    }

  }

  private void readObject(ObjectInputStream ois)
    throws ClassNotFoundException, IOException {
    ois.defaultReadObject();
    try {
      String xml = ois.readObject( ).toString();
      Document doc = XMLHandler.loadXMLString( xml );
      Node stepNode = XMLHandler.getSubNode( doc, "transformation" );
      transMeta =  new TransMeta( stepNode, null );

    } catch ( KettleException e ) {
      e.printStackTrace();
    }
  }

}
