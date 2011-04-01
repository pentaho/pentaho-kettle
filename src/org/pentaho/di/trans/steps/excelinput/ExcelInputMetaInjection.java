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

    // Add the fields...
    //
    {
      StepInjectionMetaEntry fieldsEntry = new StepInjectionMetaEntry(Entry.FIELDS.name(), Entry.FIELDS.getValueType(), Entry.FIELDS.getDescription());
      all.add(fieldsEntry);
  
      StepInjectionMetaEntry fieldEntry = new StepInjectionMetaEntry(Entry.FIELD.name(), Entry.FIELD.getValueType(), Entry.FIELD.getDescription());
      fieldsEntry.getDetails().add(fieldEntry);
  
      
      for (Entry entry : Entry.values()) {
        if (entry.getParent()==Entry.FIELD){ 
          StepInjectionMetaEntry metaEntry = new StepInjectionMetaEntry(entry.name(), entry.getValueType(), entry.getDescription());
          fieldEntry.getDetails().add(metaEntry);
        }
      }
    }

    // And the sheets
    //
    {
      StepInjectionMetaEntry sheetsEntry = new StepInjectionMetaEntry(Entry.SHEETS.name(), Entry.SHEETS.getValueType(), Entry.SHEETS.getDescription());
      all.add(sheetsEntry);
  
      StepInjectionMetaEntry sheetEntry = new StepInjectionMetaEntry(Entry.SHEET.name(), Entry.SHEET.getValueType(), Entry.SHEET.getDescription());
      sheetsEntry.getDetails().add(sheetEntry);
  
      
      for (Entry entry : Entry.values()) {
        if (entry.getParent()==Entry.SHEET){ 
          StepInjectionMetaEntry metaEntry = new StepInjectionMetaEntry(entry.name(), entry.getValueType(), entry.getDescription());
          sheetEntry.getDetails().add(metaEntry);
        }
      }
    }

    return all;
  }

  @Override
  public void injectStepMetadataEntries(List<StepInjectionMetaEntry> all) throws KettleException {
    
    List<ExcelInputField> excelInputFields = new ArrayList<ExcelInputField>();
    List<ExcelInputSheet> sheets = new ArrayList<ExcelInputMetaInjection.ExcelInputSheet>();
    
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
        if (fieldsEntry== Entry.SHEETS) {
          for (StepInjectionMetaEntry lookField : lookFields.getDetails()) {
            Entry fieldEntry = Entry.findEntry(lookField.getKey());
            if (fieldEntry!=null) {
              if (fieldEntry == Entry.SHEET) {
                
                String sheetName = null;
                int startCol = 0;
                int startRow = 0;
                
                List<StepInjectionMetaEntry> entries = lookField.getDetails();
                for (StepInjectionMetaEntry entry : entries) {
                  Entry metaEntry = Entry.findEntry(entry.getKey());
                  if (metaEntry!=null) {
                    String value = (String)entry.getValue();
                    switch(metaEntry) {
                    case SHEET_NAME: sheetName = value; break;
                    case SHEET_START_ROW:  startRow = Const.toInt(value, 0); break;
                    case SHEET_START_COL:  startCol = Const.toInt(value, 0); break;
                    }
                  }
                }
                
                sheets.add(new ExcelInputSheet(sheetName, startCol, startRow));
              }
            }
          }
        }
      }
    }

    // Pass the grid to the step metadata
    //
    meta.setField(excelInputFields.toArray(new ExcelInputField[excelInputFields.size()]));
    
    // Set the sheet names too..
    //
    String[] sheetNames = new String[sheets.size()];
    int[] startCols = new int[sheets.size()];
    int[] startRows = new int[sheets.size()];
    
    for (int i=0;i<sheets.size();i++) {
      sheetNames[i] = sheets.get(i).sheetName;
      startCols[i] = sheets.get(i).startCol;
      startRows[i] = sheets.get(i).startRow;
    }
    meta.setSheetName(sheetNames);
    meta.setStartColumn(startCols);
    meta.setStartRow(startRows);
  }

  public ExcelInputMeta getMeta() {
    return meta;
  }


  private enum Entry {

    FIELDS(ValueMetaInterface.TYPE_NONE, "All the fields on the spreadsheets"),
    FIELD(ValueMetaInterface.TYPE_NONE, "One field"),

    NAME(FIELD, ValueMetaInterface.TYPE_STRING, "Field name"),
    TYPE(FIELD, ValueMetaInterface.TYPE_STRING, "Field data type"),
    LENGTH(FIELD, ValueMetaInterface.TYPE_STRING, "Field length"),
    PRECISION(FIELD, ValueMetaInterface.TYPE_STRING, "Field precision"),
    TRIM_TYPE(FIELD, ValueMetaInterface.TYPE_STRING, "Field trim type (none, left, right, both)"),
    FORMAT(FIELD, ValueMetaInterface.TYPE_STRING, "Field conversion format"),
    CURRENCY(FIELD, ValueMetaInterface.TYPE_STRING, "Field currency symbol"),
    DECIMAL(FIELD, ValueMetaInterface.TYPE_STRING, "Field decimal symbol"),
    GROUP(FIELD, ValueMetaInterface.TYPE_STRING, "Field group symbol"),
    REPEAT(FIELD, ValueMetaInterface.TYPE_STRING, "Field repeat (Y/N)"),

    SHEETS(ValueMetaInterface.TYPE_NONE, "All the sheets in the spreadsheets"),
    SHEET(ValueMetaInterface.TYPE_NONE, "One sheet in the spreadsheet"),

    SHEET_NAME(SHEET, ValueMetaInterface.TYPE_STRING, "Sheet name"),
    SHEET_START_ROW(SHEET, ValueMetaInterface.TYPE_STRING, "Sheet start row"),
    SHEET_START_COL(SHEET, ValueMetaInterface.TYPE_STRING, "Sheet start col"),
    ;

    private int valueType;
    private String description;
    private Entry parent;

    private Entry(int valueType, String description) {
      this.valueType = valueType;
      this.description = description;
    }

    private Entry(Entry parent, int valueType, String description) {
      this.parent = parent;
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
    
    public Entry getParent() {
      return parent;
    }
  }

  
  public class ExcelInputSheet {
    public String sheetName;
    public int startCol;
    public int startRow;
    /**
     * @param sheetName
     * @param startCol
     * @param startRow
     */
    private ExcelInputSheet(String sheetName, int startCol, int startRow) {
      this.sheetName = sheetName;
      this.startCol = startCol;
      this.startRow = startRow;
    }
  }

}
