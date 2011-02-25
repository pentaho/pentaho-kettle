package org.pentaho.di.trans.steps.normaliser;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;

/**
 * To keep it simple, this metadata injection interface only supports the fields to denormalize for the time being.
 * 
 * @author Matt
 */
public class NormaliserMetaInjection implements StepMetaInjectionInterface {
  
  private NormaliserMeta meta;

  public NormaliserMetaInjection(NormaliserMeta meta) {
    this.meta = meta;
  }

  @Override
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> all = new ArrayList<StepInjectionMetaEntry>();
    
    StepInjectionMetaEntry fieldsEntry = new StepInjectionMetaEntry("FIELDS", ValueMetaInterface.TYPE_NONE, "All the fields on the spreadsheets");
    all.add(fieldsEntry);

    StepInjectionMetaEntry fieldEntry = new StepInjectionMetaEntry("FIELD", ValueMetaInterface.TYPE_NONE, "All the fields on the spreadsheets");
    fieldsEntry.getDetails().add(fieldEntry);

    
    for (Entry entry : Entry.values()) {
      if (entry.getValueType()!=ValueMetaInterface.TYPE_NONE){ 
        StepInjectionMetaEntry metaEntry = new StepInjectionMetaEntry(entry.name(), entry.getValueType(), entry.getDescription());
        fieldEntry.getDetails().add(metaEntry);
      }
    }
    
    return all;
  }

  @Override
  public void injectStepMetadataEntries(List<StepInjectionMetaEntry> all) throws KettleException {
    
    List<NormaliserField> normaliserFields = new ArrayList<NormaliserField>();
    
    // Parse the fields, inject into the meta class..
    //
    for (StepInjectionMetaEntry lookFields : all) {
      Entry fieldsEntry = Entry.findEntry(lookFields.getKey());
      if (fieldsEntry!=null) {
        if (fieldsEntry== Entry.FIELDS) {
          for (StepInjectionMetaEntry lookField : lookFields.getDetails()) {
            Entry fieldEntry = Entry.findEntry(lookField.getKey());
            if (fieldEntry!=null) {
              if (fieldEntry == Entry.FIELD) {
                
                NormaliserField normaliserField = new NormaliserField();
                
                List<StepInjectionMetaEntry> entries = lookField.getDetails();
                for (StepInjectionMetaEntry entry : entries) {
                  Entry metaEntry = Entry.findEntry(entry.getKey());
                  if (metaEntry!=null) {
                    String value = (String)entry.getValue();
                    switch(metaEntry) {
                    case NAME:               normaliserField.setName(value); break;
                    case VALUE:              normaliserField.setValue(value); break;
                    case NORMALISED:         normaliserField.setNorm(value); break;
                    }
                  }
                }
                
                normaliserFields.add(normaliserField);
              }
            }
          }
        }
      }
    }

    // Pass the grid to the step metadata
    //
    meta.allocate(normaliserFields.size());
    for (int i=0;i<normaliserFields.size();i++) {
      meta.getFieldName()[i] = normaliserFields.get(i).getName();
      meta.getFieldValue()[i] = normaliserFields.get(i).getValue();
      meta.getFieldNorm()[i] = normaliserFields.get(i).getNorm();
    }

  }

  public NormaliserMeta getMeta() {
    return meta;
  }

  private class NormaliserField {
    private String name;
    private String value;
    private String norm;
    
    /**
     * @param name
     * @param value
     * @param norm
     */
    private NormaliserField() {
    }
    
    /**
     * @return the name
     */
    public String getName() {
      return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
      this.name = name;
    }
    /**
     * @return the value
     */
    public String getValue() {
      return value;
    }
    /**
     * @param value the value to set
     */
    public void setValue(String value) {
      this.value = value;
    }
    /**
     * @return the norm
     */
    public String getNorm() {
      return norm;
    }
    /**
     * @param norm the norm to set
     */
    public void setNorm(String norm) {
      this.norm = norm;
    }
    
  }
  
  private enum Entry {

    FIELDS(ValueMetaInterface.TYPE_NONE, "All the fields"),
    FIELD(ValueMetaInterface.TYPE_NONE, "One field"),

    NAME(ValueMetaInterface.TYPE_STRING, "Input field name"),
    VALUE(ValueMetaInterface.TYPE_STRING, "Type field value"),
    NORMALISED(ValueMetaInterface.TYPE_STRING, "Normalised field name"),
    ;
    
    private int valueType;
    private String description;

    private Entry(int valueType, String description) {
      this.valueType = valueType;
      this.description = description;
    }

    /**
     * @return the valueType
     */
    public int getValueType() {
      return valueType;
    }

    /**
     * @return the description
     */
    public String getDescription() {
      return description;
    }
    
    public static Entry findEntry(String key) {
      return Entry.valueOf(key);
    }
  }
  

}
