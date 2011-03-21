package org.pentaho.di.core.util;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMeta;

public class MetaGenerator {
  
  public static final TypeFieldDefinition[] FIELDS  = new TypeFieldDefinition[] {
      new TypeFieldDefinition( ValueMeta.TYPE_BOOLEAN, "UsingLines" ),
      new TypeFieldDefinition( ValueMeta.TYPE_INTEGER, "MaxNumberOfSuggestions" ),

      new TypeFieldDefinition( ValueMeta.TYPE_STRING, "NameField" ),
      new TypeFieldDefinition( ValueMeta.TYPE_STRING, "OrganizationField" ),
      new TypeFieldDefinition( ValueMeta.TYPE_STRING, "DepartmentField" ),
      new TypeFieldDefinition( ValueMeta.TYPE_STRING, "PostBoxField" ),
      new TypeFieldDefinition( ValueMeta.TYPE_STRING, "SubPremiseField" ),
      new TypeFieldDefinition( ValueMeta.TYPE_STRING, "PremiseField" ),
      new TypeFieldDefinition( ValueMeta.TYPE_STRING, "HouseNumberField" ),
      new TypeFieldDefinition( ValueMeta.TYPE_STRING, "HouseNumberAdditionField" ),
      new TypeFieldDefinition( ValueMeta.TYPE_STRING, "DependentThoroughfareField" ),
      new TypeFieldDefinition( ValueMeta.TYPE_STRING, "ThoroughfareField" ),
      new TypeFieldDefinition( ValueMeta.TYPE_STRING, "DependentLocalityField" ),
      new TypeFieldDefinition( ValueMeta.TYPE_STRING, "LocalityField" ),
      new TypeFieldDefinition( ValueMeta.TYPE_STRING, "PostTownField" ),
      new TypeFieldDefinition( ValueMeta.TYPE_STRING, "DeliveryServiceQualifierField" ),
      new TypeFieldDefinition( ValueMeta.TYPE_STRING, "PostalCodeField" ),
      new TypeFieldDefinition( ValueMeta.TYPE_STRING, "SubAdministrativeAreaField" ),
      new TypeFieldDefinition( ValueMeta.TYPE_STRING, "AdministrativeAreaField" ),
      new TypeFieldDefinition( ValueMeta.TYPE_STRING, "CountryNameField" ),
      new TypeFieldDefinition( ValueMeta.TYPE_STRING, "CountryCodeField" ),
  };
  

  private TypeFieldDefinition[] fields;
  
  public MetaGenerator(TypeFieldDefinition[] fields) {
    this.fields = fields;
  }
  
  public String generateCode() {
    
    StringBuilder code = new StringBuilder(5000);
    
    // Generate the declarations...
    //
    for (TypeFieldDefinition field : fields) {
      code.append("  private "+field.getTypeDescription()+" "+field.getMemberName()+";").append(Const.CR);
    }
    code.append(Const.CR);

    // Generate getXML()...
    //
    code.append("  public String getXML() throws KettleException {").append(Const.CR);
    code.append("    StringBuilder xml = new StringBuilder(100);").append(Const.CR);
    for (TypeFieldDefinition field : fields) {
      code.append("    xml.append(XMLHandler.addTagValue(\""+field.getFieldName()+"\", "+field.getMemberName()+"));").append(Const.CR);
    }
    code.append("    return xml.toString();").append(Const.CR);
    code.append("  }").append(Const.CR);
    code.append(Const.CR);
    
    // Generate loadXML()...
    //
    code.append("  public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {").append(Const.CR);
    for (TypeFieldDefinition field : fields) {
      switch(field.getType()) {
      case ValueMeta.TYPE_STRING: 
        code.append("    "+field.getMemberName()+" = XMLHandler.getTagValue(stepnode, \""+field.getFieldName()+"\");").append(Const.CR); 
        break;
      case ValueMeta.TYPE_BOOLEAN:
        code.append("    "+field.getMemberName()+" = \"Y\".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, \""+field.getFieldName()+"\"));").append(Const.CR); 
        break;
      case ValueMeta.TYPE_INTEGER:
        code.append("    "+field.getMemberName()+" = Const.toInt(XMLHandler.getTagValue(stepnode, \""+field.getFieldName()+"\"), -1);").append(Const.CR); 
        break;
      }
    }
    code.append("  }").append(Const.CR);
    code.append(Const.CR);

    // Save to repository
    //
    code.append("  public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {").append(Const.CR);
    for (TypeFieldDefinition field : fields) {
      code.append("    rep.saveStepAttribute(id_transformation, id_step, \""+field.getFieldName()+"\", "+field.getMemberName()+");").append(Const.CR);
    }
    code.append("  }").append(Const.CR);
    code.append(Const.CR);

    // Load from repository
    //
    code.append("  public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {").append(Const.CR);
    for (TypeFieldDefinition field : fields) {
      switch(field.getType()) {
      case ValueMeta.TYPE_STRING: 
        code.append("    "+field.getMemberName()+" = rep.getStepAttributeString(id_step, \""+field.getFieldName()+"\");").append(Const.CR);
        break;
      case ValueMeta.TYPE_BOOLEAN: 
        code.append("    "+field.getMemberName()+" = rep.getStepAttributeBoolean(id_step, \""+field.getFieldName()+"\");").append(Const.CR);
        break;
      case ValueMeta.TYPE_INTEGER: 
        code.append("    "+field.getMemberName()+" = (int) rep.getStepAttributeInteger(id_step, \""+field.getFieldName()+"\");").append(Const.CR);
        break;
      }
    }
    code.append("  }").append(Const.CR);
    code.append(Const.CR);

    
    // Getters & Setters
    //
    for (TypeFieldDefinition field : fields) {

      String getPrefix;
      String setPrefix;
      switch(field.getType()) {
      case ValueMeta.TYPE_BOOLEAN: getPrefix = "is"; setPrefix="set"; break;
      default: getPrefix = "get"; setPrefix="set"; break;
      }
      
      code.append("  public "+field.getTypeDescription()+" "+getPrefix+field.getFieldName()+"() {").append(Const.CR);
      code.append("    return "+field.getMemberName()+";").append(Const.CR);
      code.append("  }").append(Const.CR);
      code.append(Const.CR);

      code.append("  public void "+setPrefix+field.getFieldName()+"("+field.getTypeDescription()+" "+field.getMemberName()+") {").append(Const.CR);
      code.append("    this."+field.getMemberName()+" = "+field.getMemberName()+";").append(Const.CR);
      code.append("  }").append(Const.CR);
      code.append(Const.CR);
    }
   
    
    /*
          nameFieldItem = new TableItem(wInputFields.table, SWT.NONE);
          nameFieldItem.setText(1, BaseMessages.getString(PKG, "PIQAddressDialog.NameField.Description"));
          nameFieldItem.setText(2, Const.NVL(input.getNameField(), ""));
     */

    for (TypeFieldDefinition field : fields) {
       code.append("  "+field.getMemberName()+"Item = new TableItem(wInputFields.table, SWT.NONE);").append(Const.CR);
       code.append("  "+field.getMemberName()+"Item.setText(1, BaseMessages.getString(PKG, \"PIQAddressDialog."+field.getFieldName()+".Description\"));").append(Const.CR);
       code.append("  "+field.getMemberName()+"Item.setText(2, Const.NVL(input.get"+field.getFieldName()+"(), \"\"));").append(Const.CR);       
    }

    for (TypeFieldDefinition field : fields) {
      code.append("PIQAddressDialog."+field.getFieldName()+".Description = "+field.getFieldName()).append(" DESCRIPTION TODO").append(Const.CR);
   }

    return code.toString();
  }
  
  public static void main(String[] args) {
    MetaGenerator generator = new MetaGenerator(FIELDS);
    System.out.println(generator.generateCode());
  }
  
}
