package org.pentaho.di.trans.steps.rules;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.ObjectFilter;
import org.drools.runtime.StatefulKnowledgeSession;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * This Transformation Step allows a user to execute a rule set against
 * an individual rule or a collection of rules.
 * 
 * Additional columns can be added to the output from the rules and these
 * (of course) can be used for routing if desired.
 * 
 * @author cboyden
 *
 */

public class RulesData  extends BaseStepData implements StepDataInterface {
	private static Class<?> PKG = Rules.class; // for i18n purposes
	
	private RowMetaInterface outputRowMeta;
	
	private KnowledgeBuilder kbuilder;
	private KnowledgeBase kbase;
	
	private Rules.Column[] columnList;
	private Map<String, Rules.Column> resultMap = new HashMap<String, Rules.Column>();
	
	private String ruleString;
	public String getRuleString() {
		return ruleString;
	}

	public void setRuleString(String ruleString) {
		this.ruleString = ruleString;
	}

	public String getRuleFilePath() {
		return ruleFilePath;
	}

	public void setRuleFilePath(String ruleFilePath) {
		this.ruleFilePath = ruleFilePath;
	}

	private String ruleFilePath;
	
	public void setOutputRowMeta(RowMetaInterface outputRowMeta) {
		this.outputRowMeta = outputRowMeta;
	}

	public RowMetaInterface getOutputRowMeta() {
		return outputRowMeta;
	}
	
	public void initializeRules() {
		Resource ruleSet = null;
		if(ruleString != null) {
			ruleSet = ResourceFactory.newReaderResource(new StringReader(ruleString));
		} else {
			ruleSet = ResourceFactory.newFileResource(ruleFilePath);
		}
		kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		kbuilder.add(ruleSet, ResourceType.DRL);
		
		if(kbuilder.hasErrors()) {
			System.out.println(kbuilder.getErrors().toString());
			throw new RuntimeException("Failed to compile drl");
		}
		
		Collection<KnowledgePackage> pkgs = kbuilder.getKnowledgePackages();
		
		kbase = KnowledgeBaseFactory.newKnowledgeBase();
		// Cache the knowledge base as its creation is intensive
		kbase.addKnowledgePackages(pkgs);
	}
	
	public void initializeColumns(RowMetaInterface inputRowMeta) {
		if(inputRowMeta == null) {
			BaseMessages.getString(PKG, "RulesData.InitializeColumns.InputRowMetaIsNull");
			return;
		}
		
		// Create objects for insertion into the rules engine
        List<ValueMetaInterface> columns = inputRowMeta.getValueMetaList();
        
        // This array must 1-1 match the row[] feteched by getRow()
        columnList = new Rules.Column[columns.size()];
        
        for(int i = 0; i < columns.size(); i++) {
        	ValueMetaInterface column = columns.get(i);
        	
        	Rules.Column c = new Rules.Column(true);
        	c.setName(column.getName());
        	c.setType(column.getTypeDesc());
        	c.setPayload(null);
        	
        	columnList[i] = c;
        }
	}
	
	public void loadRow(Object[] r) {
		for(int i = 0; i < columnList.length; i++) {
			columnList[i].setPayload(r[i]);
		}
	}
	
	public void execute() {
		StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
		for(int i = 0; i < columnList.length; i++) {
			session.insert(columnList[i]);
		}
		
		session.fireAllRules();
		
		Collection<Object> oList = session.getObjects(new ObjectFilter() {
			public boolean accept(Object o) {
				if(o instanceof Rules.Column && !((Rules.Column)o).isExternalSource()) {
					return true;
				}
				return false;
			}
		});
		
		for(Object o : oList) {
			resultMap.put(((Rules.Column)o).getName(), (Rules.Column)o);
		}
		
		session.dispose();
	}
	
	/**
	 * 
	 * @param columnName Column.payload associated with the result, or null if not found
	 * @return
	 */
	public Object fetchResult(String columnName) {
		return resultMap.get(columnName);
	}
	
	public void shutdown() {
	}
}
