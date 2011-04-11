package org.pentaho.di.imp;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.ImportRulePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.imp.rule.ImportValidationFeedback;
import org.pentaho.di.imp.rule.ImporterRuleInterface;
import org.pentaho.di.imp.rules.BaseImportRule;
import org.w3c.dom.Node;

public class ImportRules {

  public static final String            XML_TAG = "rules";

  protected List<ImporterRuleInterface> rules;

  public ImportRules() {
    rules = new ArrayList<ImporterRuleInterface>();
  }
  
  public List<ImportValidationFeedback> verifyRules(Object subject) {
    List<ImportValidationFeedback> feedback = new ArrayList<ImportValidationFeedback>();
    
    for (ImporterRuleInterface rule : rules) {
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
      ImporterRuleInterface rule = (ImporterRuleInterface) registry.loadClass(plugin);

      rule.loadXML(ruleNode);

      getRules().add(rule);
    }
  }

  public String getXML() {
    StringBuilder xml = new StringBuilder();
    
    xml.append( XMLHandler.openTag(XML_TAG) ) ;
    
    for (ImporterRuleInterface rule : getRules()) {
      xml.append(rule.getXML());
    }
    
    xml.append( XMLHandler.closeTag(XML_TAG) ) ;

    return xml.toString();
  }

  public List<ImporterRuleInterface> getRules() {
    return rules;
  }

  public void setRules(List<ImporterRuleInterface> rules) {
    this.rules = rules;
  }

}
