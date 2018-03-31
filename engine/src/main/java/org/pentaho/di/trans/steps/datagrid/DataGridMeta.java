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

package org.pentaho.di.trans.steps.datagrid;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

public class DataGridMeta extends BaseStepMeta implements StepMetaInterface {
  private String[] currency;
  private String[] decimal;
  private String[] group;

  private String[] fieldName;
  private String[] fieldType;
  private String[] fieldFormat;

  private int[] fieldLength;
  private int[] fieldPrecision;
  /** Flag : set empty string **/
  private boolean[] setEmptyString;

  private List<List<String>> dataLines;

  public DataGridMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return the setEmptyString
   */
  public boolean[] isSetEmptyString() {
    return setEmptyString;
  }

  /**
   * @param setEmptyString
   *          the setEmptyString to set
   */
  public void setEmptyString( boolean[] setEmptyString ) {
    this.setEmptyString = setEmptyString;
  }

  /**
   * @return Returns the currency.
   */
  public String[] getCurrency() {
    return currency;
  }

  /**
   * @param currency
   *          The currency to set.
   */
  public void setCurrency( String[] currency ) {
    this.currency = currency;
  }

  /**
   * @return Returns the decimal.
   */
  public String[] getDecimal() {
    return decimal;
  }

  /**
   * @param decimal
   *          The decimal to set.
   */
  public void setDecimal( String[] decimal ) {
    this.decimal = decimal;
  }

  /**
   * @return Returns the fieldFormat.
   */
  public String[] getFieldFormat() {
    return fieldFormat;
  }

  /**
   * @param fieldFormat
   *          The fieldFormat to set.
   */
  public void setFieldFormat( String[] fieldFormat ) {
    this.fieldFormat = fieldFormat;
  }

  /**
   * @return Returns the fieldLength.
   */
  public int[] getFieldLength() {
    return fieldLength;
  }

  /**
   * @param fieldLength
   *          The fieldLength to set.
   */
  public void setFieldLength( int[] fieldLength ) {
    this.fieldLength = fieldLength;
  }

  /**
   * @return Returns the fieldName.
   */
  public String[] getFieldName() {
    return fieldName;
  }

  /**
   * @param fieldName
   *          The fieldName to set.
   */
  public void setFieldName( String[] fieldName ) {
    this.fieldName = fieldName;
  }

  /**
   * @return Returns the fieldPrecision.
   */
  public int[] getFieldPrecision() {
    return fieldPrecision;
  }

  /**
   * @param fieldPrecision
   *          The fieldPrecision to set.
   */
  public void setFieldPrecision( int[] fieldPrecision ) {
    this.fieldPrecision = fieldPrecision;
  }

  /**
   * @return Returns the fieldType.
   */
  public String[] getFieldType() {
    return fieldType;
  }

  /**
   * @param fieldType
   *          The fieldType to set.
   */
  public void setFieldType( String[] fieldType ) {
    this.fieldType = fieldType;
  }

  /**
   * @return Returns the group.
   */
  public String[] getGroup() {
    return group;
  }

  /**
   * @param group
   *          The group to set.
   */
  public void setGroup( String[] group ) {
    this.group = group;
  }

  public List<List<String>> getDataLines() {
    return dataLines;
  }

  public void setDataLines( List<List<String>> dataLines ) {
    this.dataLines = dataLines;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public void allocate( int nrfields ) {
    fieldName = new String[nrfields];
    fieldType = new String[nrfields];
    fieldFormat = new String[nrfields];
    fieldLength = new int[nrfields];
    fieldPrecision = new int[nrfields];
    currency = new String[nrfields];
    decimal = new String[nrfields];
    group = new String[nrfields];
    setEmptyString = new boolean[nrfields];
  }

  @Override
  public Object clone() {
    DataGridMeta retval = (DataGridMeta) super.clone();

    int nrfields = fieldName.length;

    retval.allocate( nrfields );

    System.arraycopy( fieldName, 0, retval.fieldName, 0, nrfields );
    System.arraycopy( fieldType, 0, retval.fieldType, 0, nrfields );
    System.arraycopy( fieldFormat, 0, retval.fieldFormat, 0, nrfields );
    System.arraycopy( currency, 0, retval.currency, 0, nrfields );
    System.arraycopy( decimal, 0, retval.decimal, 0, nrfields );
    System.arraycopy( group, 0, retval.group, 0, nrfields );
    System.arraycopy( fieldLength, 0, retval.fieldLength, 0, nrfields );
    System.arraycopy( fieldPrecision, 0, retval.fieldPrecision, 0, nrfields );
    System.arraycopy( setEmptyString, 0, retval.setEmptyString, 0, nrfields );

    if ( dataLines != null ) {
      retval.setDataLines( new ArrayList<List<String>>() );
      for ( List<String> line : dataLines ) {
        List<String> newLine = new ArrayList<String>();
        newLine.addAll( line );
        retval.getDataLines().add( newLine );
      }
    }
    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int nrfields = XMLHandler.countNodes( fields, "field" );

      allocate( nrfields );

      String slength, sprecision;

      for ( int i = 0; i < nrfields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        fieldName[i] = XMLHandler.getTagValue( fnode, "name" );
        fieldType[i] = XMLHandler.getTagValue( fnode, "type" );
        fieldFormat[i] = XMLHandler.getTagValue( fnode, "format" );
        currency[i] = XMLHandler.getTagValue( fnode, "currency" );
        decimal[i] = XMLHandler.getTagValue( fnode, "decimal" );
        group[i] = XMLHandler.getTagValue( fnode, "group" );
        slength = XMLHandler.getTagValue( fnode, "length" );
        sprecision = XMLHandler.getTagValue( fnode, "precision" );

        fieldLength[i] = Const.toInt( slength, -1 );
        fieldPrecision[i] = Const.toInt( sprecision, -1 );
        String emptyString = XMLHandler.getTagValue( fnode, "set_empty_string" );
        setEmptyString[i] = !Utils.isEmpty( emptyString ) && "Y".equalsIgnoreCase( emptyString );
      }

      Node datanode = XMLHandler.getSubNode( stepnode, "data" );
      // NodeList childNodes = datanode.getChildNodes();
      dataLines = new ArrayList<List<String>>();

      Node lineNode = datanode.getFirstChild();
      while ( lineNode != null ) {
        if ( "line".equals( lineNode.getNodeName() ) ) {
          List<String> line = new ArrayList<String>();
          Node itemNode = lineNode.getFirstChild();
          while ( itemNode != null ) {
            if ( "item".equals( itemNode.getNodeName() ) ) {
              String itemNodeValue = XMLHandler.getNodeValue( itemNode );
              line.add( itemNodeValue );
            }
            itemNode = itemNode.getNextSibling();
          }
          /*
           * for (int f=0;f<nrfields;f++) { Node itemNode = XMLHandler.getSubNodeByNr(lineNode, "item", f); String item
           * = XMLHandler.getNodeValue(itemNode); line.add(item); }
           */
          dataLines.add( line );

        }

        lineNode = lineNode.getNextSibling();
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  @Override
  public void setDefault() {
    int i, nrfields = 0;

    allocate( nrfields );

    DecimalFormat decimalFormat = new DecimalFormat();

    for ( i = 0; i < nrfields; i++ ) {
      fieldName[i] = "field" + i;
      fieldType[i] = "Number";
      fieldFormat[i] = "\u00A40,000,000.00;\u00A4-0,000,000.00";
      fieldLength[i] = 9;
      fieldPrecision[i] = 2;
      currency[i] = decimalFormat.getDecimalFormatSymbols().getCurrencySymbol();
      decimal[i] = new String( new char[] { decimalFormat.getDecimalFormatSymbols().getDecimalSeparator() } );
      group[i] = new String( new char[] { decimalFormat.getDecimalFormatSymbols().getGroupingSeparator() } );
      setEmptyString[i] = false;
    }

    dataLines = new ArrayList<List<String>>();
  }

  @Override
  public void getFields( RowMetaInterface rowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    for ( int i = 0; i < fieldName.length; i++ ) {
      try {
        if ( !Utils.isEmpty( fieldName[i] ) ) {
          int type = ValueMetaFactory.getIdForValueMeta( fieldType[i] );
          if ( type == ValueMetaInterface.TYPE_NONE ) {
            type = ValueMetaInterface.TYPE_STRING;
          }
          ValueMetaInterface v = ValueMetaFactory.createValueMeta( fieldName[i], type );
          v.setLength( fieldLength[i] );
          v.setPrecision( fieldPrecision[i] );
          v.setOrigin( name );
          v.setConversionMask( fieldFormat[i] );
          v.setCurrencySymbol( currency[i] );
          v.setGroupingSymbol( group[i] );
          v.setDecimalSymbol( decimal[i] );

          rowMeta.addValueMeta( v );
        }
      } catch ( Exception e ) {
        throw new KettleStepException( "Unable to create value of type " + fieldType[i], e );
      }
    }
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( "    <fields>" ).append( Const.CR );
    for ( int i = 0; i < fieldName.length; i++ ) {
      if ( fieldName[i] != null && fieldName[i].length() != 0 ) {
        retval.append( "      <field>" ).append( Const.CR );
        retval.append( "        " ).append( XMLHandler.addTagValue( "name", fieldName[i] ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "type", fieldType[i] ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "format", fieldFormat[i] ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "currency", currency[i] ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "decimal", decimal[i] ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "group", group[i] ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "length", fieldLength[i] ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "precision", fieldPrecision[i] ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "set_empty_string", setEmptyString[i] ) );
        retval.append( "      </field>" ).append( Const.CR );
      }
    }
    retval.append( "    </fields>" ).append( Const.CR );

    retval.append( "    <data>" ).append( Const.CR );
    for ( List<String> line : dataLines ) {
      retval.append( "      <line> " );
      for ( String item : line ) {
        retval.append( XMLHandler.addTagValue( "item", item, false ) );
      }
      retval.append( " </line>" ).append( Const.CR );
    }
    retval.append( "    </data>" ).append( Const.CR );

    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId idStep, List<DatabaseMeta> databases ) throws KettleException {

    try {
      int nrfields = rep.countNrStepAttributes( idStep, "field_name" );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        fieldName[i] = rep.getStepAttributeString( idStep, i, "field_name" );
        fieldType[i] = rep.getStepAttributeString( idStep, i, "field_type" );

        fieldFormat[i] = rep.getStepAttributeString( idStep, i, "field_format" );
        currency[i] = rep.getStepAttributeString( idStep, i, "field_currency" );
        decimal[i] = rep.getStepAttributeString( idStep, i, "field_decimal" );
        group[i] = rep.getStepAttributeString( idStep, i, "field_group" );
        fieldLength[i] = (int) rep.getStepAttributeInteger( idStep, i, "field_length" );
        fieldPrecision[i] = (int) rep.getStepAttributeInteger( idStep, i, "field_precision" );
        setEmptyString[i] = rep.getStepAttributeBoolean( idStep, i, "set_empty_string", false );
      }

      int nrLines = (int) rep.getStepAttributeInteger( idStep, "nr_lines" );
      dataLines = new ArrayList<List<String>>();
      for ( int i = 0; i < nrLines; i++ ) {
        List<String> line = new ArrayList<String>();

        for ( int f = 0; f < nrfields; f++ ) {
          String item = rep.getStepAttributeString( idStep, i, "item_" + f );
          line.add( item );
        }

        dataLines.add( line );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId idTransformation, ObjectId idStep ) throws KettleException {
    try {
      for ( int i = 0; i < fieldName.length; i++ ) {
        if ( fieldName[i] != null && fieldName[i].length() != 0 ) {
          rep.saveStepAttribute( idTransformation, idStep, i, "field_name", fieldName[i] );
          rep.saveStepAttribute( idTransformation, idStep, i, "field_type", fieldType[i] );
          rep.saveStepAttribute( idTransformation, idStep, i, "field_format", fieldFormat[i] );
          rep.saveStepAttribute( idTransformation, idStep, i, "field_currency", currency[i] );
          rep.saveStepAttribute( idTransformation, idStep, i, "field_decimal", decimal[i] );
          rep.saveStepAttribute( idTransformation, idStep, i, "field_group", group[i] );
          rep.saveStepAttribute( idTransformation, idStep, i, "field_length", fieldLength[i] );
          rep.saveStepAttribute( idTransformation, idStep, i, "field_precision", fieldPrecision[i] );
          rep.saveStepAttribute( idTransformation, idStep, i, "set_empty_string", setEmptyString[i] );
        }
      }

      rep.saveStepAttribute( idTransformation, idStep, "nr_lines", dataLines.size() );

      for ( int i = 0; i < dataLines.size(); i++ ) {
        List<String> line = dataLines.get( i );
        for ( int f = 0; f < line.size(); f++ ) {
          String item = line.get( f );
          rep.saveStepAttribute( idTransformation, idStep, i, "item_" + f, item );
        }
      }

    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + idStep, e );
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new DataGrid( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new DataGridData();
  }

  @Override
  public StepMetaInjectionInterface getStepMetaInjectionInterface() {
    return new DataGridMetaInjection( this );
  }

}
