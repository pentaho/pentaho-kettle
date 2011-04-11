package org.pentaho.di.imp.rules;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.ImportRulePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.imp.rule.ImportValidationFeedback;
import org.pentaho.di.imp.rule.ImporterRuleInterface;
import org.w3c.dom.Node;

public abstract class BaseImportRule implements ImporterRuleInterface {

  public static String XML_TAG = "rule";
  
  private String            id;
  private boolean           enabled;

  public BaseImportRule() {
    this.enabled=false;
  }

  public abstract List<ImportValidationFeedback> verifyRule(Object subject);

  public String getXML() {
    StringBuilder xml = new StringBuilder();
    
    xml.append(XMLHandler.addTagValue("id", isEnabled()));
    xml.append(XMLHandler.addTagValue("enabled", isEnabled()));
    
    return xml.toString();
  }

  public void loadXML(Node ruleNode) throws KettleException {
    id = XMLHandler.getTagValue(ruleNode, "id");
    enabled = ValueMeta.convertStringToBoolean( XMLHandler.getTagValue(ruleNode, "enabled") );
  }

  @Override
  public String toString() {
    // The rule name with an indication of whether or not this rule is enabled should do for now. 
    //
    String pluginId = PluginRegistry.getInstance().getPluginId(this);
    PluginInterface plugin = PluginRegistry.getInstance().findPluginWithId(ImportRulePluginType.class, pluginId);
    return plugin.getName()+" ("+(enabled?"enabled":"disabled")+").";
  }
  
  /**
   * @return the enabled
   */
  public boolean isEnabled() {
    return enabled;
  }
  
  /**
   * @param enabled
   *          the enabled to set
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

}
