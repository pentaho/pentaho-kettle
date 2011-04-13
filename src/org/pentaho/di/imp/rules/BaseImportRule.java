package org.pentaho.di.imp.rules;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.ImportRulePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.imp.rule.ImportRuleInterface;
import org.pentaho.di.imp.rule.ImportValidationFeedback;
import org.w3c.dom.Node;

public abstract class BaseImportRule implements ImportRuleInterface {

  public static String XML_TAG = "rule";
  
  private String            id;
  private boolean           enabled;

  public BaseImportRule() {
    this.enabled=false;
  }
  
  public ImportRuleInterface clone() {
    try {
      return (ImportRuleInterface)super.clone();
    } catch(CloneNotSupportedException e) {
      throw new RuntimeException("Unable to clone import rule", e);
    }
  }
  
  public boolean isUnique() {
    return true;
  }

  public abstract List<ImportValidationFeedback> verifyRule(Object subject);

  public String getXML() {
    StringBuilder xml = new StringBuilder();
    
    xml.append(XMLHandler.addTagValue("id", id));
    xml.append(XMLHandler.addTagValue("enabled", enabled));
    
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

  /**
   * This returns the expected name for the composite that allows a base import
   * rule to be edited.
   * 
   * The expected name is in the org.pentaho.di.ui tree and has a class name
   * that is the name of the job entry with 'Composite' added to the end.
   * 
   * e.g. if the import rule class name is:
   * "org.pentaho.di.imp.rules.DatabaseConfigurationImportRule" the composite
   * then the composite class name would be:
   * "org.pentaho.di.ui.imp.rules.DatabaseConfigurationImportRuleComposite"
   * 
   * If the composite class for a job entry does not match this pattern it
   * should override this method and return the appropriate class name
   * 
   * @return full class name of the composite class
   */
  @Override
  public String getCompositeClassName() {
    String className = getClass().getCanonicalName();
    className = className.replaceFirst("\\.di\\.", ".di.ui.");
    className += "Composite";
    return className;
  }

}
