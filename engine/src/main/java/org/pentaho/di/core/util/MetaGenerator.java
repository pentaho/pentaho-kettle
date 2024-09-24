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

package org.pentaho.di.core.util;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMetaInterface;

public class MetaGenerator {

  public static final TypeFieldDefinition[] FIELDS = new TypeFieldDefinition[] {
    new TypeFieldDefinition( ValueMetaInterface.TYPE_BOOLEAN, "UsingLines" ),
    new TypeFieldDefinition( ValueMetaInterface.TYPE_INTEGER, "MaxNumberOfSuggestions" ),

    new TypeFieldDefinition( ValueMetaInterface.TYPE_STRING, "NameField" ),
    new TypeFieldDefinition( ValueMetaInterface.TYPE_STRING, "OrganizationField" ),
    new TypeFieldDefinition( ValueMetaInterface.TYPE_STRING, "DepartmentField" ),
    new TypeFieldDefinition( ValueMetaInterface.TYPE_STRING, "PostBoxField" ),
    new TypeFieldDefinition( ValueMetaInterface.TYPE_STRING, "SubPremiseField" ),
    new TypeFieldDefinition( ValueMetaInterface.TYPE_STRING, "PremiseField" ),
    new TypeFieldDefinition( ValueMetaInterface.TYPE_STRING, "HouseNumberField" ),
    new TypeFieldDefinition( ValueMetaInterface.TYPE_STRING, "HouseNumberAdditionField" ),
    new TypeFieldDefinition( ValueMetaInterface.TYPE_STRING, "DependentThoroughfareField" ),
    new TypeFieldDefinition( ValueMetaInterface.TYPE_STRING, "ThoroughfareField" ),
    new TypeFieldDefinition( ValueMetaInterface.TYPE_STRING, "DependentLocalityField" ),
    new TypeFieldDefinition( ValueMetaInterface.TYPE_STRING, "LocalityField" ),
    new TypeFieldDefinition( ValueMetaInterface.TYPE_STRING, "PostTownField" ),
    new TypeFieldDefinition( ValueMetaInterface.TYPE_STRING, "DeliveryServiceQualifierField" ),
    new TypeFieldDefinition( ValueMetaInterface.TYPE_STRING, "PostalCodeField" ),
    new TypeFieldDefinition( ValueMetaInterface.TYPE_STRING, "SubAdministrativeAreaField" ),
    new TypeFieldDefinition( ValueMetaInterface.TYPE_STRING, "AdministrativeAreaField" ),
    new TypeFieldDefinition( ValueMetaInterface.TYPE_STRING, "CountryNameField" ),
    new TypeFieldDefinition( ValueMetaInterface.TYPE_STRING, "CountryCodeField" ), };

  private TypeFieldDefinition[] fields;

  public MetaGenerator( TypeFieldDefinition[] fields ) {
    this.fields = fields;
  }

  public String generateCode() {

    StringBuilder code = new StringBuilder( 5000 );

    // Generate the declarations...
    //
    for ( TypeFieldDefinition field : fields ) {
      code.append( "  private " + field.getTypeDescription() + " " + field.getMemberName() + ";" ).append(
        Const.CR );
    }
    code.append( Const.CR );

    // Generate getXML()...
    //
    code.append( "  public String getXML() throws KettleException {" ).append( Const.CR );
    code.append( "    StringBuilder xml = new StringBuilder(100);" ).append( Const.CR );
    for ( TypeFieldDefinition field : fields ) {
      code.append(
        "    xml.append(XMLHandler.addTagValue(\""
          + field.getFieldName() + "\", " + field.getMemberName() + "));" ).append( Const.CR );
    }
    code.append( "    return xml.toString();" ).append( Const.CR );
    code.append( "  }" ).append( Const.CR );
    code.append( Const.CR );

    // Generate loadXML()...
    //
    code.append( "  public void loadXML(Node stepnode, List<DatabaseMeta> databases, "
      + "IMetaStore metaStore) throws KettleXMLException {" ).append( Const.CR );
    for ( TypeFieldDefinition field : fields ) {
      switch ( field.getType() ) {
        case ValueMetaInterface.TYPE_STRING:
          code.append(
            "    "
              + field.getMemberName() + " = XMLHandler.getTagValue(stepnode, \"" + field.getFieldName()
              + "\");" ).append( Const.CR );
          break;
        case ValueMetaInterface.TYPE_BOOLEAN:
          code.append(
            "    "
              + field.getMemberName() + " = \"Y\".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, \""
              + field.getFieldName() + "\"));" ).append( Const.CR );
          break;
        case ValueMetaInterface.TYPE_INTEGER:
          code.append(
            "    "
              + field.getMemberName() + " = Const.toInt(XMLHandler.getTagValue(stepnode, \""
              + field.getFieldName() + "\"), -1);" ).append( Const.CR );
          break;
        default:
          break;
      }
    }
    code.append( "  }" ).append( Const.CR );
    code.append( Const.CR );

    // Save to repository
    //
    code
      .append(
        "  public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {" )
      .append( Const.CR );
    for ( TypeFieldDefinition field : fields ) {
      code.append(
        "    rep.saveStepAttribute(id_transformation, id_step, \""
          + field.getFieldName() + "\", " + field.getMemberName() + ");" ).append( Const.CR );
    }
    code.append( "  }" ).append( Const.CR );
    code.append( Const.CR );

    // Load from repository
    //
    code.append( "  public void readRep(Repository rep, ObjectId id_step, "
      + "List<DatabaseMeta> databases) throws KettleException {" ).append( Const.CR );
    for ( TypeFieldDefinition field : fields ) {
      switch ( field.getType() ) {
        case ValueMetaInterface.TYPE_STRING:
          code.append(
            "    "
              + field.getMemberName() + " = rep.getStepAttributeString(id_step, \"" + field.getFieldName()
              + "\");" ).append( Const.CR );
          break;
        case ValueMetaInterface.TYPE_BOOLEAN:
          code.append(
            "    "
              + field.getMemberName() + " = rep.getStepAttributeBoolean(id_step, \"" + field.getFieldName()
              + "\");" ).append( Const.CR );
          break;
        case ValueMetaInterface.TYPE_INTEGER:
          code.append(
            "    "
              + field.getMemberName() + " = (int) rep.getStepAttributeInteger(id_step, \""
              + field.getFieldName() + "\");" ).append( Const.CR );
          break;
        default:
          break;
      }
    }
    code.append( "  }" ).append( Const.CR );
    code.append( Const.CR );

    // Getters & Setters
    //
    for ( TypeFieldDefinition field : fields ) {

      String getPrefix;
      String setPrefix;
      switch ( field.getType() ) {
        case ValueMetaInterface.TYPE_BOOLEAN:
          getPrefix = "is";
          setPrefix = "set";
          break;
        default:
          getPrefix = "get";
          setPrefix = "set";
          break;
      }

      code
        .append( "  public " + field.getTypeDescription() + " " + getPrefix + field.getFieldName() + "() {" )
        .append( Const.CR );
      code.append( "    return " + field.getMemberName() + ";" ).append( Const.CR );
      code.append( "  }" ).append( Const.CR );
      code.append( Const.CR );

      code.append(
        "  public void "
          + setPrefix + field.getFieldName() + "(" + field.getTypeDescription() + " " + field.getMemberName()
          + ") {" ).append( Const.CR );
      code.append( "    this." + field.getMemberName() + " = " + field.getMemberName() + ";" ).append( Const.CR );
      code.append( "  }" ).append( Const.CR );
      code.append( Const.CR );
    }

    /*
     * nameFieldItem = new TableItem(wInputFields.table, SWT.NONE); nameFieldItem.setText(1, BaseMessages.getString(PKG,
     * "PIQAddressDialog.NameField.Description")); nameFieldItem.setText(2, Const.NVL(input.getNameField(), ""));
     */

    for ( TypeFieldDefinition field : fields ) {
      code.append( "  " + field.getMemberName() + "Item = new TableItem(wInputFields.table, SWT.NONE);" ).append(
        Const.CR );
      code.append(
        "  "
          + field.getMemberName() + "Item.setText(1, BaseMessages.getString(PKG, \"PIQAddressDialog."
          + field.getFieldName() + ".Description\"));" ).append( Const.CR );
      code.append(
        "  "
          + field.getMemberName() + "Item.setText(2, Const.NVL(input.get" + field.getFieldName()
          + "(), \"\"));" ).append( Const.CR );
    }

    for ( TypeFieldDefinition field : fields ) {
      code.append( "PIQAddressDialog." + field.getFieldName() + ".Description = " + field.getFieldName() ).append(
        " DESCRIPTION TODO" ).append( Const.CR );
    }

    return code.toString();
  }

  public static void main( String[] args ) {
    MetaGenerator generator = new MetaGenerator( FIELDS );
    System.out.println( generator.generateCode() );
  }

}
