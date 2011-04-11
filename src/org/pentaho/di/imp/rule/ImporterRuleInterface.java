package org.pentaho.di.imp.rule;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.w3c.dom.Node;

public interface ImporterRuleInterface {
  
  public List<ImportValidationFeedback> verifyRule(Object subject);
  
  public boolean isEnabled();
  public void setEnabled(boolean enabled);
  
  public void loadXML(Node ruleNode) throws KettleException;
  public String getXML();
}

