/*
 * $Header: PluginProperty.java
 * $Revision:
 * $Date: 13.05.2009 11:39:09
 *
 * ==========================================================
 * COPYRIGHTS
 * ==========================================================
 */
package org.pentaho.di.core.util;

import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
 * 
 */
public class StringPluginProperty extends KeyValue<String> implements PluginProperty {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -2990345692552430357L;

    /**
     * Constructor. Value is null.
     * 
     * @param key
     *            key to set.
     * @throws IllegalArgumentException
     *             if key is invalid.
     */
    public StringPluginProperty(final String key) throws IllegalArgumentException {
        super(key, DEFAULT_STRING_VALUE);
    }
    
    

    /**
     * {@inheritDoc}
     *
     * @see at.aschauer.commons.pentaho.plugin.PluginProperty#evaluate()
     */
    public boolean evaluate() {
        return StringUtils.isNotBlank(this.getValue());
    }



    /**
     * {@inheritDoc}
     * 
     * @see at.aschauer.commons.pentaho.plugin.PluginProperty#appendXml(java.lang.StringBuilder)
     */
    public void appendXml(final StringBuilder builder) {
        builder.append(XMLHandler.addTagValue(this.getKey(), this.getValue()));
    }

    /**
     * {@inheritDoc}
     * 
     * @see at.aschauer.commons.pentaho.plugin.PluginProperty#loadXml(org.w3c.dom.Node)
     */
    public void loadXml(final Node node) {
        final String value = XMLHandler.getTagValue(node, this.getKey());
        this.setValue(value);
    }

    /**
     * {@inheritDoc}
     * 
     * @see at.aschauer.commons.pentaho.plugin.PluginProperty#readFromRepositoryStep(org.pentaho.di.repository.Repository,
     *      long)
     */
    public void readFromRepositoryStep(final Repository repository, final ObjectId stepId) throws KettleException {
        final String value = repository.getStepAttributeString(stepId, this.getKey());
        this.setValue(value);
    }

    /**
     * {@inheritDoc}
     * 
     * @see at.aschauer.commons.pentaho.plugin.PluginProperty#saveToPreferences(java.util.prefs.Preferences)
     */
    public void saveToPreferences(final Preferences node) {
        node.put(this.getKey(), this.getValue());
    }

    /**
     * {@inheritDoc}
     * 
     * @see at.aschauer.commons.pentaho.plugin.PluginProperty#readFromPreferences(java.util.prefs.Preferences)
     */
    public void readFromPreferences(final Preferences node) {
        this.setValue(node.get(this.getKey(), this.getValue()));
    }

    /**
     * {@inheritDoc}
     * 
     * @see at.aschauer.commons.pentaho.plugin.PluginProperty#saveToRepositoryStep(org.pentaho.di.repository.Repository,
     *      long, long)
     */
    public void saveToRepositoryStep(final Repository repository, final ObjectId transformationId, final ObjectId stepId)
            throws KettleException {
        repository.saveStepAttribute(transformationId, stepId, this.getKey(), this.getValue());
    }

}
