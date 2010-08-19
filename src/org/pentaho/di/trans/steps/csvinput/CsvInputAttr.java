package org.pentaho.di.trans.steps.csvinput;

import org.pentaho.di.core.KettleAttributeInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * TODO: move this to XML somewhere...
 * 
 * @author matt
 *
 */
public enum CsvInputAttr implements KettleAttributeInterface {
  
  FILENAME("filename", "CsvInputDialog.Filename.Label", null, ValueMetaInterface.TYPE_STRING, null),
  FILENAME_FIELD("filename_field", "CsvInputDialog.FilenameField.Label", null, ValueMetaInterface.TYPE_STRING, null),
  INCLUDE_FILENAME("include_filename", "CsvInputDialog.IncludeFilenameField.Label", null, ValueMetaInterface.TYPE_BOOLEAN, null),
  ROW_NUM_FIELD("rownum_field", "CsvInputDialog.RowNumField.Label", null, ValueMetaInterface.TYPE_STRING, null),
  HEADER_PRESENT("header", "CsvInputDialog.HeaderPresent.Label", null, ValueMetaInterface.TYPE_BOOLEAN, null),
  DELIMITER("separator", "CsvInputDialog.Delimiter.Label", null, ValueMetaInterface.TYPE_STRING, null),
  ENCLOSURE("enclosure", "CsvInputDialog.Enclosure.Label", null, ValueMetaInterface.TYPE_STRING, null),
  BUFFERSIZE("buffer_size", "CsvInputDialog.BufferSize.Label", null, ValueMetaInterface.TYPE_STRING, null),
  LAZY_CONVERSION("lazy_conversion", "CsvInputDialog.LazyConversion.Label", null, ValueMetaInterface.TYPE_BOOLEAN, null),
  PARALLEL("parallel", "CsvInputDialog.RunningInParallel.Label", null, ValueMetaInterface.TYPE_BOOLEAN, null),
  ADD_FILENAME_RESULT("add_filename_result", "CsvInputDialog.AddResult.Label", "CsvInputDialog.AddResult.Tooltip", ValueMetaInterface.TYPE_BOOLEAN, null),
  ENCODING("encoding", "CsvInputDialog.Encoding.Label", null, ValueMetaInterface.TYPE_STRING, null),

  FIELDS("fields", "CsvInputDialog.Fields.Label", null, ValueMetaInterface.TYPE_NONE, null),
  FIELD("field", "CsvInputDialog.Field.Label", null, ValueMetaInterface.TYPE_NONE, FIELDS),

  FIELD_NAME("name", "field_name", "CsvInputDialog.NameColumn.Column", null, ValueMetaInterface.TYPE_STRING, FIELD),
  FIELD_TYPE("type", "field_type", "CsvInputDialog.TypeColumn.Column", null, ValueMetaInterface.TYPE_STRING, FIELD),
  FIELD_FORMAT("format", "field_format", "CsvInputDialog.FormatColumn.Column", null, ValueMetaInterface.TYPE_STRING, FIELD),
  FIELD_LENGTH("length", "field_length", "CsvInputDialog.LengthColumn.Column", null, ValueMetaInterface.TYPE_STRING, FIELD),
  FIELD_PRECISION("precision", "field_precision", "CsvInputDialog.PrecisionColumn.Column", null, ValueMetaInterface.TYPE_STRING, FIELD),
  FIELD_CURRENCY("currency", "field_currency", "CsvInputDialog.CurrencyColumn.Column", null, ValueMetaInterface.TYPE_STRING, FIELD),
  FIELD_DECIMAL("decimal", "field_decimal", "CsvInputDialog.DecimalColumn.Column", null, ValueMetaInterface.TYPE_STRING, FIELD),
  FIELD_GROUP("group", "field_group", "CsvInputDialog.GroupColumn.Column", null, ValueMetaInterface.TYPE_STRING, FIELD),
  FIELD_TRIM_TYPE("trim_type", "field_trim_type", "CsvInputDialog.TrimTypeColumn.Column", null, ValueMetaInterface.TYPE_STRING, FIELD),
  ;
  
  private String xmlCode;
  private String repCode;
  private String description;
  private String tooltip;
  private int    type;
  private CsvInputAttr parent;
  
  /**
   * @param xmlCode
   * @param repCode
   * @param description
   * @param tooltip
   * @param type
   */
  private CsvInputAttr(String xmlCode, String repCode, String description, String tooltip, int type, CsvInputAttr parent) {
    this.xmlCode = xmlCode;
    this.repCode = repCode;
    this.description = description;
    this.tooltip = tooltip;
    this.type = type;
    this.parent = parent;
  }
  
  /**
   * @param code
   * @param description
   * @param tooltip
   * @param type
   */
  private CsvInputAttr(String code, String description, String tooltip, int type, CsvInputAttr parent) {
    this(code, code, description, tooltip, type, parent);
  }
  
  public String getKey() {
    return repCode;
  }
  
  /**
   * @param xmlCode The XML Code to search for
   * @return the attribute that corresponds to the XML code or null if nothing was found.
   */
  public static CsvInputAttr findByKey(String key) {
    for (CsvInputAttr attr : values()) {
      if (attr.getKey().equals(key)) return attr;
    }
    return null;
  }
  
  /**
   * @return the xmlCode
   */
  public String getXmlCode() {
    return xmlCode;
  }

  /**
   * @return the repCode
   */
  public String getRepCode() {
    return repCode;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @return the tooltip
   */
  public String getTooltip() {
    return tooltip;
  }

  /**
   * @return the type
   */
  public int getType() {
    return type;
  }

  public CsvInputAttr getParent() {
    return parent;
  }

}
