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

package org.pentaho.di.trans.steps.transexecutor;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * The job to be executed in the transformation can receive parameters. These are either coming from an input row (the
 * first row in a group of rows) or from a static variable or value.
 *
 * @author matt
 * @version 4.3
 * @since 2011-AUG-29
 *
 */
public class TransExecutorParameters implements Cloneable {

  public static final String XML_TAG = "parameters";

  private static final String XML_VARIABLES_TAG = "variablemapping";

  /** The name of the variable to set in the job */
  private String[] variable;

  private String[] field;

  /** This is a simple String with optionally variables in them **/
  private String[] input;

  /** This flag causes the job to inherit all variables from the parent transformation */
  private boolean inheritingAllVariables;

  public TransExecutorParameters() {
    super();

    variable = new String[] {};
    field = new String[] {};
    input = new String[] {};

    inheritingAllVariables = true;
  }

  public void allocate( int nrVariables ) {
    variable = new String[nrVariables];
    field = new String[nrVariables];
    input = new String[nrVariables];
  }

  @Override
  public Object clone() {
    try {
      TransExecutorParameters retval = (TransExecutorParameters) super.clone();
      int nrVariables = variable.length;
      retval.allocate( nrVariables );
      System.arraycopy( variable, 0, retval.variable, 0, nrVariables );
      System.arraycopy( field, 0, retval.field, 0, nrVariables );
      System.arraycopy( input, 0, retval.input, 0, nrVariables );
      return retval;
    } catch ( CloneNotSupportedException e ) {
      throw new RuntimeException( e ); // Nope, we don't want that in our code.
    }
  }

  public TransExecutorParameters( Node paramNode ) {

    int nrVariables = XMLHandler.countNodes( paramNode, XML_VARIABLES_TAG );
    allocate( nrVariables );

    for ( int i = 0; i < variable.length; i++ ) {
      Node variableMappingNode = XMLHandler.getSubNodeByNr( paramNode, XML_VARIABLES_TAG, i );

      variable[i] = XMLHandler.getTagValue( variableMappingNode, "variable" );
      field[i] = XMLHandler.getTagValue( variableMappingNode, "field" );
      input[i] = XMLHandler.getTagValue( variableMappingNode, "input" );
    }

    inheritingAllVariables = "Y".equalsIgnoreCase( XMLHandler.getTagValue( paramNode, "inherit_all_vars" ) );
  }

  public String getXML() {
    StringBuilder xml = new StringBuilder( 200 );

    xml.append( "    " ).append( XMLHandler.openTag( XML_TAG ) );

    for ( int i = 0; i < variable.length; i++ ) {
      xml.append( "       " ).append( XMLHandler.openTag( XML_VARIABLES_TAG ) );
      xml.append( XMLHandler.addTagValue( "variable", variable[i], false ) );
      xml.append( XMLHandler.addTagValue( "field", field[i], false ) );
      xml.append( XMLHandler.addTagValue( "input", input[i], false ) );
      xml.append( XMLHandler.closeTag( XML_VARIABLES_TAG ) ).append( Const.CR );
    }
    xml.append( "    " ).append( XMLHandler.addTagValue( "inherit_all_vars", inheritingAllVariables ) );
    xml.append( "    " ).append( XMLHandler.closeTag( XML_TAG ) );

    return xml.toString();
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    for ( int i = 0; i < variable.length; i++ ) {
      rep.saveStepAttribute( id_transformation, id_step, i, "parameter_variable", variable[i] );
      rep.saveStepAttribute( id_transformation, id_step, i, "parameter_field", field[i] );
      rep.saveStepAttribute( id_transformation, id_step, i, "parameter_input", input[i] );
    }
    rep.saveStepAttribute( id_transformation, id_step, "parameter_inherit_all_vars", inheritingAllVariables );
  }

  public TransExecutorParameters( Repository rep, ObjectId id_step ) throws KettleException {
    int nrVariables = rep.countNrStepAttributes( id_step, "parameter_variable" );

    allocate( nrVariables );
    for ( int i = 0; i < nrVariables; i++ ) {
      variable[i] = rep.getStepAttributeString( id_step, i, "parameter_variable" );
      field[i] = rep.getStepAttributeString( id_step, i, "parameter_field" );
      input[i] = rep.getStepAttributeString( id_step, i, "parameter_input" );
    }
    inheritingAllVariables = rep.getStepAttributeBoolean( id_step, "parameter_inherit_all_vars" );
  }

  /**
   * @return the field name to use
   */
  public String[] getField() {
    return field;
  }

  /**
   * @param field
   *          the input field name to set
   */
  public void setField( String[] field ) {
    this.field = field;
  }

  /**
   * @return the variable
   */
  public String[] getVariable() {
    return variable;
  }

  /**
   * @param variable
   *          the variable to set
   */
  public void setVariable( String[] variable ) {
    this.variable = variable;
  }

  /**
   * @return the inheritingAllVariables
   */
  public boolean isInheritingAllVariables() {
    return inheritingAllVariables;
  }

  /**
   * @param inheritingAllVariables
   *          the inheritingAllVariables to set
   */
  public void setInheritingAllVariables( boolean inheritingAllVariables ) {
    this.inheritingAllVariables = inheritingAllVariables;
  }

  /**
   * @return the input
   */
  public String[] getInput() {
    return input;
  }

  /**
   * @param input
   *          the input to set
   */
  public void setInput( String[] input ) {
    this.input = input;
  }
}
