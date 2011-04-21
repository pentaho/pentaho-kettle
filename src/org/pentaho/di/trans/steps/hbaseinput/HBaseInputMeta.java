/* Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */

package org.pentaho.di.trans.steps.hbaseinput;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.hbase.mapping.HBaseValueMeta;
import org.pentaho.hbase.mapping.Mapping;
import org.pentaho.hbase.mapping.MappingAdmin;
import org.w3c.dom.Node;

@Step(id = "HBaseInput", image = "HB.png", name = "HBase Input", description="Reads data from a HBase table according to a mapping", categoryDescription="Hadoop")
public class HBaseInputMeta extends BaseStepMeta implements StepMetaInterface {
  
  protected String m_zookeeperHosts;
  
  protected String m_coreConfigURL;
  
  protected String m_defaultConfigURL;
  
  protected String m_sourceTableName;
  
  protected String m_sourceMappingName;
  
  /** Whether to output the key as a field */
//  protected boolean m_outputKeyAsField = true;
  
  /** Start key value for range scans */
  protected String m_keyStart;
  
  /** Stop key value for range scans */
  protected String m_keyStop;
  
  /** Scanner caching */
  protected String m_scannerCacheSize;
  
  protected transient Mapping m_cachedMapping;
  
  /** The selected fields to output. If null, then all fields from the mapping are output */
  protected List<HBaseValueMeta> m_outputFields;

  /** The configured column filters. If null, then no filters are applied to the result set */
  protected List<ColumnFilter> m_filters;
  
  /** 
   * If true, then any matching filter will cause the row to be output,
   * otherwise all filters have to return true before the row is output
   */
  protected boolean m_matchAnyFilter;
  
  public void setZookeeperHosts(String z) {
    m_zookeeperHosts = z;
  }
  
  public String getZookeeperHosts() {
    return m_zookeeperHosts;
  }
  
  public void setCoreConfigURL(String coreConfig) {
    m_coreConfigURL = coreConfig;
    m_cachedMapping = null;
  }
  
  public String getCoreConfigURL() {
    return m_coreConfigURL;
  }
  
  public void setDefaulConfigURL(String defaultConfig) {
    m_defaultConfigURL = defaultConfig;
    m_cachedMapping = null;
  }
  
  public String getDefaultConfigURL() {
    return m_defaultConfigURL;
  }
  
  public void setSourceTableName(String sourceTable) {
    m_sourceTableName = sourceTable;
    m_cachedMapping = null;
  }
  
  public String getSourceTableName() {
    return m_sourceTableName;
  }
  
  public void setSourceMappingName(String sourceMapping) {
    m_sourceMappingName = sourceMapping;
    m_cachedMapping = null;
  }
  
  public String getSourceMappingName() {
    return m_sourceMappingName;
  }
  
  public void setMatchAnyFilter(boolean a) {
    m_matchAnyFilter = a;
  }
  
  public boolean getMatchAnyFilter() {
    return m_matchAnyFilter;
  }
  
/*  public void setOutputKeyAsField(boolean k) {
    m_outputKeyAsField = k;
  }
  
  public boolean getOutputKeyAsField() {
    return m_outputKeyAsField;
  } */
  
  public void setKeyStartValue(String start) {
    m_keyStart = start;
  }
  
  public String getKeyStartValue() {
    return m_keyStart;
  }
  
  public void setKeyStopValue(String stop) {
    m_keyStop = stop;
  }
  
  public String getKeyStopValue() {
    return m_keyStop;
  }
  
  public void setScannerCacheSize(String s) {
    m_scannerCacheSize = s;
  }
  
  public String getScannerCacheSize() {
    return m_scannerCacheSize;
  }
  
  public void setOutputFields(List<HBaseValueMeta> fields) {
    m_outputFields = fields;
  }
  
  public List<HBaseValueMeta> getOutputFields() {
    return m_outputFields;
  }
  
  public void setColumnFilters(List<ColumnFilter> list) {
    m_filters = list;
  }
  
  public List<ColumnFilter> getColumnFilters() {
    return m_filters;
  }

  public void setDefault() {
    m_coreConfigURL = null;
    m_defaultConfigURL = null;
    m_cachedMapping = null;
    m_sourceTableName = null;
    m_sourceMappingName = null;
    m_keyStart = null;
    m_keyStop = null;
  }
  
  private String getIndexValues(HBaseValueMeta vm) {
    Object[] labels = vm.getIndex();
    StringBuffer vals = new StringBuffer();
    vals.append("{");
    
    for (int i = 0; i < labels.length; i++) {
      if (i != labels.length - 1) {
        vals.append(labels[i].toString().trim()).append(",");
      } else {
        vals.append(labels[i].toString().trim()).append("}");
      }
    }
    return vals.toString();
  }
  
  public String getXML() {
    StringBuffer retval = new StringBuffer();
    
    if (!Const.isEmpty(m_zookeeperHosts)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("zookeeper_hosts", 
          m_zookeeperHosts));
    }
    if (!Const.isEmpty(m_coreConfigURL)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("core_config_url", 
          m_coreConfigURL));
    }
    if (!Const.isEmpty(m_defaultConfigURL)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("default_config_url", 
          m_defaultConfigURL));
    }
    if (!Const.isEmpty(m_sourceTableName)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("source_table_name", 
          m_sourceTableName));
    }
    if (!Const.isEmpty(m_sourceMappingName)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("source_mapping_name", 
          m_sourceMappingName));
    }
    if (!Const.isEmpty(m_keyStart)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("key_start", 
          m_keyStart));
    }
    if (!Const.isEmpty(m_keyStop)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("key_stop", 
          m_keyStop));
    }
    if (!Const.isEmpty(m_scannerCacheSize)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("scanner_cache_size", 
          m_scannerCacheSize));
    }    
    
    if (m_outputFields != null && m_outputFields.size() > 0) {
      retval.append("\n    ").append(XMLHandler.openTag("output_fields"));
      
      for (HBaseValueMeta vm : m_outputFields) {
        retval.append("\n        ").append(XMLHandler.openTag("field"));
        retval.append("\n            ").append(XMLHandler.addTagValue("table_name", vm.getTableName()));
        retval.append("\n            ").append(XMLHandler.addTagValue("mapping_name", vm.getMappingName()));
        retval.append("\n            ").append(XMLHandler.addTagValue("alias", vm.getAlias()));
        retval.append("\n            ").append(XMLHandler.addTagValue("family", vm.getColumnFamily()));
        retval.append("\n            ").append(XMLHandler.addTagValue("column", vm.getColumnName()));
        retval.append("\n            ").append(XMLHandler.addTagValue("key", vm.isKey()));
        retval.append("\n            ").append(XMLHandler.addTagValue("type", 
            ValueMeta.getTypeDesc(vm.getType())));
        String format = vm.getConversionMask();
        retval.append("\n            ").append(XMLHandler.addTagValue("format", format));
        if (vm.getStorageType() == ValueMetaInterface.STORAGE_TYPE_INDEXED) {
          retval.append("\n            ").append(XMLHandler.addTagValue("index_values",
              getIndexValues(vm)));          
        }
        retval.append("\n        ").append(XMLHandler.closeTag("field"));
      }
      
      retval.append("\n    ").append(XMLHandler.closeTag("output_fields"));
    }
    
    if (m_filters != null && m_filters.size() > 0) {
      retval.append("\n    ").append(XMLHandler.openTag("column_filters"));
      
      for (ColumnFilter f : m_filters) {
        f.appendXML(retval);
      }
      retval.append("\n    ").append(XMLHandler.closeTag("column_filters"));
    }
    
    retval.append("\n    ").append(XMLHandler.addTagValue("match_any_filter", 
        m_matchAnyFilter));
    
    return retval.toString();
  }  

  public void loadXML(Node stepnode, List<DatabaseMeta> databases,
      Map<String, Counter> counters) throws KettleXMLException {
    
    m_zookeeperHosts = XMLHandler.getTagValue(stepnode, "zookeeper_hosts");
    m_coreConfigURL = XMLHandler.getTagValue(stepnode, "core_config_url");
    m_defaultConfigURL = XMLHandler.getTagValue(stepnode, "default_config_url"); 
    m_sourceTableName = XMLHandler.getTagValue(stepnode, "source_table_name");
    m_sourceMappingName = XMLHandler.getTagValue(stepnode, "source_mapping_name");
    m_keyStart = XMLHandler.getTagValue(stepnode, "key_start");
    m_keyStop = XMLHandler.getTagValue(stepnode, "key_stop");
    m_scannerCacheSize = XMLHandler.getTagValue(stepnode, "scanner_cache_size");
    String m = XMLHandler.getTagValue(stepnode, 
      "match_any_filter");
    if (!Const.isEmpty(m)) {
      m_matchAnyFilter = m.equalsIgnoreCase("Y");
    }
    
    Node fields = XMLHandler.getSubNode(stepnode, "output_fields");
    if (fields != null && XMLHandler.countNodes(fields, "field") > 0) {
      int nrfields = XMLHandler.countNodes(fields, "field");
      m_outputFields = new ArrayList<HBaseValueMeta>();
      
      for (int i = 0; i < nrfields; i++) {
        Node fieldNode = XMLHandler.getSubNodeByNr(fields, "field", i);
        
        String isKey = XMLHandler.getTagValue(fieldNode, "key").trim();
        String alias = XMLHandler.getTagValue(fieldNode, "alias").trim();
        String colFamily = "";
        String colName = alias;
        if (!isKey.equalsIgnoreCase("Y")) {
          colFamily = XMLHandler.getTagValue(fieldNode, "family").trim();
          colName = XMLHandler.getTagValue(fieldNode, "column").trim();
        }        

        String typeS = XMLHandler.getTagValue(fieldNode, "type").trim();
        HBaseValueMeta vm = new HBaseValueMeta(colFamily + HBaseValueMeta.SEPARATOR 
            + colName + HBaseValueMeta.SEPARATOR + alias, 
            ValueMeta.getType(typeS), -1, -1);
        vm.setTableName(XMLHandler.getTagValue(fieldNode, "table_name"));
        vm.setTableName(XMLHandler.getTagValue(fieldNode, "mapping_name"));
        vm.setKey(isKey.equalsIgnoreCase("Y"));
        
        String format = XMLHandler.getTagValue(fieldNode, "format");
        if (!Const.isEmpty(format)) {
          vm.setConversionMask(format);
        }
        
        String indexValues = XMLHandler.getTagValue(fieldNode, "index_values");
        if (!Const.isEmpty(indexValues)) {
          String[] labels = indexValues.replace("{", "").replace("}", "").split(",");
          if (labels.length < 1) {
            throw new KettleXMLException("Indexed/nominal type must have at least one " +
                          "label declared");
          }
          for (int j = 0; j < labels.length; j++) {
            labels[j] = labels[j].trim();
          }
          vm.setIndex(labels);
          vm.setStorageType(ValueMetaInterface.STORAGE_TYPE_INDEXED);
        }
        
        m_outputFields.add(vm);
      }      
    }
    
    Node filters = XMLHandler.getSubNode(stepnode, "column_filters");
    if (filters != null && XMLHandler.countNodes(filters, "filter") > 0) {
      int nrFilters = XMLHandler.countNodes(filters, "filter");
      m_filters = new ArrayList<ColumnFilter>();
      
      for (int i = 0; i < nrFilters; i++) {
        Node filterNode = XMLHandler.getSubNodeByNr(filters, "filter", i);
        m_filters.add(ColumnFilter.getFilter(filterNode));
      }
    }
  }

  public void saveRep(Repository rep, ObjectId id_transformation,
      ObjectId id_step) throws KettleException {

    if (!Const.isEmpty(m_zookeeperHosts)) {
      rep.saveStepAttribute(id_transformation, id_step, "zookeeper_hosts", 
          m_zookeeperHosts);
    }
    if (!Const.isEmpty(m_coreConfigURL)) {
      rep.saveStepAttribute(id_transformation, id_step, "core_config_url", 
          m_coreConfigURL);
    }
    if (!Const.isEmpty(m_defaultConfigURL)) {
      rep.saveStepAttribute(id_transformation, id_step, "default_config_url", 
          m_defaultConfigURL);
    }
    if (!Const.isEmpty(m_sourceTableName)) {
      rep.saveStepAttribute(id_transformation, id_step,"source_table_name", 
          m_sourceTableName);
    }
    if (!Const.isEmpty(m_sourceMappingName)) {
      rep.saveStepAttribute(id_transformation, id_step,"source_mapping_name", 
          m_sourceMappingName);
    }
    if (!Const.isEmpty(m_keyStart)) {
      rep.saveStepAttribute(id_transformation, id_step,"key_start", 
          m_keyStart);
    }
    if (!Const.isEmpty(m_keyStop)) {
      rep.saveStepAttribute(id_transformation, id_step,"key_stop", 
          m_keyStop);
    }
    if (!Const.isEmpty(m_scannerCacheSize)) {
      rep.saveStepAttribute(id_transformation, id_step,"scanner_cache_size", 
          m_scannerCacheSize);
    }
    
    if (m_outputFields != null && m_outputFields.size() > 0) {
            
      for (int i = 0; i < m_outputFields.size(); i++) {
        HBaseValueMeta vm = m_outputFields.get(i);
        
        rep.saveStepAttribute(id_transformation, id_step, i, "table_name", vm.getTableName());
        rep.saveStepAttribute(id_transformation, id_step, i, "mapping_name", vm.getMappingName());
        rep.saveStepAttribute(id_transformation, id_step, i, "alias", vm.getAlias());
        rep.saveStepAttribute(id_transformation, id_step, i, "family", vm.getColumnFamily());
        rep.saveStepAttribute(id_transformation, id_step, i, "column", vm.getColumnName());
        rep.saveStepAttribute(id_transformation, id_step, i, "key", vm.isKey());
        rep.saveStepAttribute(id_transformation, id_step, i, "type", 
            ValueMeta.getTypeDesc(vm.getType()));
        String format = vm.getConversionMask();
        rep.saveStepAttribute(id_transformation, id_step, i, "format", format);
        if (vm.getStorageType() == ValueMetaInterface.STORAGE_TYPE_INDEXED) {
          rep.saveStepAttribute(id_transformation, id_step, i, "index_values",
              getIndexValues(vm));          
        }
      }      
    }
    
    if (m_filters != null && m_filters.size() > 0) {
      for (int i = 0; i < m_filters.size(); i++) {
        ColumnFilter f = m_filters.get(i);
        f.saveRep(rep, id_transformation, id_step, i);
      }
    }
    
    rep.saveStepAttribute(id_transformation, id_step, "match_any_filter", 
        m_matchAnyFilter);
  }

  public void readRep(Repository rep, ObjectId id_step,
      List<DatabaseMeta> databases, Map<String, Counter> counters)
      throws KettleException {
    
    m_zookeeperHosts = rep.getStepAttributeString(id_step, 0, "zookeeper_hosts");
    m_coreConfigURL = rep.getStepAttributeString(id_step, 0, "core_config_url");
    m_defaultConfigURL = rep.getStepAttributeString(id_step, 0, "default_config_url");
    m_sourceTableName = rep.getStepAttributeString(id_step, 0, "source_table_name");
    m_sourceMappingName = rep.getStepAttributeString(id_step, 0, "source_mapping_name");
    m_keyStart = rep.getStepAttributeString(id_step, 0, "key_start");
    m_keyStop = rep.getStepAttributeString(id_step, 0, "key_stop");
    m_matchAnyFilter = rep.getStepAttributeBoolean(id_step, 0, "match_any_filter");
    m_scannerCacheSize = rep.getStepAttributeString(id_step, 0, "scanner_cache_size");
    
    int nrfields = rep.countNrStepAttributes(id_step, "field");
    
    if (nrfields > 0) {
      m_outputFields = new ArrayList<HBaseValueMeta>();

      for (int i = 0; i < nrfields; i++) {

        String colFamily = rep.getStepAttributeString(id_step, i, "family");
        if (!Const.isEmpty(colFamily)) {
          colFamily = colFamily.trim();
        }
        String colName = rep.getStepAttributeString(id_step, i, "column");
        if (!Const.isEmpty(colName)) {
          colName = colName.trim();
        }
        String alias = rep.getStepAttributeString(id_step, i, "alias").trim();
        String typeS = rep.getStepAttributeString(id_step, i, "type").trim();
        boolean isKey = rep.getStepAttributeBoolean(id_step, i , "key");
        HBaseValueMeta vm = new HBaseValueMeta(colFamily + HBaseValueMeta.SEPARATOR 
            + colName + HBaseValueMeta.SEPARATOR + alias, ValueMeta.getType(typeS), -1, -1);
        vm.setTableName(rep.getStepAttributeString(id_step, i, "table_name"));
        vm.setTableName(rep.getStepAttributeString(id_step, i, "mapping_name"));
        vm.setKey(isKey);

        String format = rep.getStepAttributeString(id_step, i, "format");
        if (!Const.isEmpty(format)) {
          vm.setConversionMask(format);
        }

        String indexValues = rep.getStepAttributeString(id_step, i, "index_values");
        if (!Const.isEmpty(indexValues)) {
          String[] labels = indexValues.replace("{", "").replace("}", "").split(",");
          if (labels.length < 1) {
            throw new KettleXMLException("Indexed/nominal type must have at least one " +
            "label declared");
          }
          for (int j = 0; j < labels.length; j++) {
            labels[j] = labels[j].trim();
          }
          vm.setIndex(labels);
          vm.setStorageType(ValueMetaInterface.STORAGE_TYPE_INDEXED);          
        }
        m_outputFields.add(vm);
      }
    }
    
    int nrFilters = rep.countNrStepAttributes(id_step, "filter");
    if (nrFilters > 0) {
      m_filters = new ArrayList<ColumnFilter>();
      
      for (int i = 0; i < nrFilters; i++) {
        m_filters.add(ColumnFilter.getFilter(rep, i, id_step));
      }
    }
  }

  public void check(List<CheckResultInterface> remarks, TransMeta transMeta,
      StepMeta stepMeta, RowMetaInterface prev, String[] input,
      String[] output, RowMetaInterface info) {
    
    RowMeta r = new RowMeta();
    try {
      getFields(r, "testName", null, null, null);
      
      CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_OK, 
          "Step can connect to HBase. Named mapping exists", stepMeta);
      remarks.add(cr);
    } catch (Exception ex) {
      CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, 
          ex.getMessage(), stepMeta);
      remarks.add(cr);
    }
  }

  public StepInterface getStep(StepMeta stepMeta,
      StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans) {

    return new HBaseInput(stepMeta, stepDataInterface, 
        copyNr, transMeta, trans);
  }

  public StepDataInterface getStepData() {

    return new HBaseInputData();
  }
  
  private void setupCachedMapping(VariableSpace space) throws KettleStepException {
    if (Const.isEmpty(m_coreConfigURL) && Const.isEmpty(m_zookeeperHosts)) {
      throw new KettleStepException("No output fields available (missing " +
      "connection details)!");
    }

    if (Const.isEmpty(m_sourceTableName) || Const.isEmpty(m_sourceMappingName)) {
      throw new KettleStepException("No output fields available (missing table " +
      "mapping details)!");
    }

    if (m_cachedMapping == null) {
      // cache the mapping information
      Configuration conf = null;
      URL coreConf = null;
      URL defaultConf = null;
      String zookeeperHosts = null;
      
      try {
        if (!Const.isEmpty(m_coreConfigURL)) {
          coreConf = HBaseInputData.stringToURL(space.environmentSubstitute(m_coreConfigURL));
        }
        if (!Const.isEmpty((m_defaultConfigURL))) {
          defaultConf = HBaseInputData.stringToURL(space.environmentSubstitute(m_defaultConfigURL));
        }
        if (!Const.isEmpty(m_zookeeperHosts)) {
          zookeeperHosts = space.environmentSubstitute(m_zookeeperHosts);
        }
        conf = HBaseInputData.getHBaseConnection(zookeeperHosts, coreConf, defaultConf);            
      } catch (IOException ex) {
        throw new KettleStepException(ex.getMessage());
      }
      
      MappingAdmin mappingAdmin = null;
      try {
        mappingAdmin = new MappingAdmin(conf);
      } catch (Exception ex) {
        throw new KettleStepException(ex.getMessage());
      }

      try {
        m_cachedMapping = 
          mappingAdmin.getMapping(m_sourceTableName, m_sourceMappingName);
      } catch (IOException ex) {
        throw new KettleStepException(ex.getMessage());
      }
    }
  }
  
  public void getFields(RowMetaInterface rowMeta, String origin, 
      RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) 
    throws KettleStepException {
    
    rowMeta.clear(); // start afresh - eats the input
    
    if (m_outputFields != null) {
      // we have some stored field information - use this
      for (HBaseValueMeta vm : m_outputFields) {

        vm.setOrigin(origin);
        rowMeta.addValueMeta(vm);
      }
    } else {
      // want all fields from the mapping - connect and get the details
      setupCachedMapping(space);

//      if (getOutputKeyAsField()) {
      int kettleType;
      if (m_cachedMapping.getKeyType() == Mapping.KeyType.DATE || 
          m_cachedMapping.getKeyType() == Mapping.KeyType.UNSIGNED_DATE) {
        kettleType = ValueMetaInterface.TYPE_DATE;
      } else if (m_cachedMapping.getKeyType() == Mapping.KeyType.STRING) {
        kettleType = ValueMetaInterface.TYPE_STRING;
      } else {
        kettleType = ValueMetaInterface.TYPE_INTEGER;
      }

      ValueMetaInterface keyMeta = new ValueMeta(m_cachedMapping.getKeyName(), kettleType);

      keyMeta.setOrigin(origin);
      rowMeta.addValueMeta(keyMeta);
  //    }


      // Add the rest of the fields in the mapping
      Map<String, HBaseValueMeta> mappedColumnsByAlias = 
        m_cachedMapping.getMappedColumns();
      Set<String> aliasSet = mappedColumnsByAlias.keySet();
      for (String alias : aliasSet) {
        HBaseValueMeta columnMeta = mappedColumnsByAlias.get(alias);
        columnMeta.setOrigin(origin);
        rowMeta.addValueMeta(columnMeta);
      }
    }
  }
  
  /**
   * Get the UI for this step.
   *
   * @param shell a <code>Shell</code> value
   * @param meta a <code>StepMetaInterface</code> value
   * @param transMeta a <code>TransMeta</code> value
   * @param name a <code>String</code> value
   * @return a <code>StepDialogInterface</code> value
   */
  public StepDialogInterface getDialog(Shell shell, 
                                       StepMetaInterface meta,
                                       TransMeta transMeta, 
                                       String name) {

    return new HBaseInputDialog(shell, meta, transMeta, name);
  }
  
}