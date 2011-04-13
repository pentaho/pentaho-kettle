package org.pentaho.di.imp;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.ImportRulePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.imp.rule.ImportValidationFeedback;
import org.pentaho.di.imp.rule.ImportRuleInterface;
import org.pentaho.di.imp.rules.BaseImportRule;
import org.w3c.dom.Node;

public class ImportRules implements Cloneable {

  public static final String            XML_TAG = "rules";

  protected List<ImportRuleInterface> rules;

  public ImportRules() {
    rules = new ArrayList<ImportRuleInterface>();
  }
  
  /**
   * Perform a deep clone
   * @return a deep copy of the all the import rules.
   */
  @Override
  public ImportRules clone() {

    ImportRules importRules = new ImportRules();
    
    for (ImportRuleInterface rule : rules) {
      importRules.getRules().add( rule.clone() );
    }
    
    return importRules;
  }
  
  public List<ImportValidationFeedback> verifyRules(Object subject) {
    List<ImportValidationFeedback> feedback = new ArrayList<ImportValidationFeedback>();
    
    for (ImportRuleInterface rule : rules) {
      feedback.addAll(rule.verifyRule(subject));
    }
    
    return feedback;
    
  }
  
  public void loadXML(Node rulesNode) throws KettleException {
    List<Node> ruleNodes = XMLHandler.getNodes(rulesNode, BaseImportRule.XML_TAG);
    for (Node ruleNode : ruleNodes) {
      String id = XMLHandler.getTagValue(ruleNode, "id");

      PluginRegistry registry = PluginRegistry.getInstance();

      PluginInterface plugin = registry.findPluginWithId(ImportRulePluginType.class, id);
      if (plugin==null) {
        throw new KettleException("The import rule of type '"+id+"' could not be found in the plugin registry.");
      }
      ImportRuleInterface rule = (ImportRuleInterface) registry.loadClass(plugin);
      
      rule.loadXML(ruleNode);

      getRules().add(rule);
    }
  }

  public String getXML() {
    StringBuilder xml = new StringBuilder();
    
    xml.append( XMLHandler.openTag(XML_TAG) ).append(Const.CR).append(Const.CR);
    
    for (ImportRuleInterface rule : getRules()) {
      
      PluginInterface plugin = PluginRegistry.getInstance().getPlugin(ImportRulePluginType.class, rule.getId());
      xml.append("<!-- ").append(plugin.getName()).append(" : ").append(plugin.getDescription()).append(Const.CR).append(" -->").append(Const.CR);
      
      xml.append(rule.getXML());
      xml.append(Const.CR).append(Const.CR);
    }
    
    xml.append( XMLHandler.closeTag(XML_TAG) ) ;

    return xml.toString();
  }

  public List<ImportRuleInterface> getRules() {
    return rules;
  }

  public void setRules(List<ImportRuleInterface> rules) {
    this.rules = rules;
  }

}
