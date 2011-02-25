package org.pentaho.di.trans.steps.excelinput;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;

/**
 * To keep it simple, this metadata injection interface only supports the fields in the spreadsheet for the time being.
 * 
 * @author Matt
 */
public class ExcelInputMetaInjection implements StepMetaInjectionInterface {
  
  private ExcelInputMeta meta;

  public ExcelInputMetaInjection(ExcelInputMeta meta) {
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
    
    List<ExcelInputField> excelInputFields = new ArrayList<ExcelInputField>();
    
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
                
                ExcelInputField inputField = new ExcelInputField();
                
                List<StepInjectionMetaEntry> entries = lookField.getDetails();
                for (StepInjectionMetaEntry entry : entries) {
                  Entry metaEntry = Entry.findEntry(entry.getKey());
                  if (metaEntry!=null) {
                    String value = (String)entry.getValue();
                    switch(metaEntry) {
                    case NAME:      inputField.setName(value); break;
                    case TYPE:      inputField.setType( ValueMeta.getType(value) ); break;
                    case LENGTH:    inputField.setLength(Const.toInt(value, -1)); break;
                    case PRECISION: inputField.setPrecision(Const.toInt(value, -1)); break;
                    case CURRENCY:  inputField.setCurrencySymbol(value); break;
                    case GROUP:     inputField.setGroupSymbol(value); break;
                    case DECIMAL:   inputField.setDecimalSymbol(value); break;
                    case FORMAT:    inputField.setFormat(value); break;
                    case TRIM_TYPE: inputField.setTrimType(ValueMeta.getTrimTypeByCode(value)); break;
                    case REPEAT:    inputField.setRepeated(ValueMeta.convertStringToBoolean(value)); break;
                    }
                  }
                }
                
                excelInputFields.add(inputField);
              }
            }
          }
        }
      }
    }

    // Pass the grid to the step metadata
    //
    meta.setField(excelInputFields.toArray(new ExcelInputField[excelInputFields.size()]));

  }

  public ExcelInputMeta getMeta() {
    return meta;
  }


  private enum Entry {

    FIELDS(ValueMetaInterface.TYPE_NONE, "All the fields"),
    FIELD(ValueMetaInterface.TYPE_NONE, "One field"),

    NAME(ValueMetaInterface.TYPE_STRING, "Field name"),
    TYPE(ValueMetaInterface.TYPE_STRING, "Field data type"),
    LENGTH(ValueMetaInterface.TYPE_STRING, "Field length"),
    PRECISION(ValueMetaInterface.TYPE_STRING, "Field precision"),
    TRIM_TYPE(ValueMetaInterface.TYPE_STRING, "Field trim type (none, left, right, both)"),
    FORMAT(ValueMetaInterface.TYPE_STRING, "Field conversion format"),
    CURRENCY(ValueMetaInterface.TYPE_STRING, "Field currency symbol"),
    DECIMAL(ValueMetaInterface.TYPE_STRING, "Field decimal symbol"),
    GROUP(ValueMetaInterface.TYPE_STRING, "Field group symbol"),
    REPEAT(ValueMetaInterface.TYPE_STRING, "Field repeat (Y/N)"),
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
