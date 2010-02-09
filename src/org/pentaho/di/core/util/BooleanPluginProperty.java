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

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
 * 
 */
public class BooleanPluginProperty extends KeyValue<Boolean> implements PluginProperty {

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
    public BooleanPluginProperty(final String key) throws IllegalArgumentException {
        super(key, DEFAULT_BOOLEAN_VALUE);
    }

    /**
     * {@inheritDoc}
     * 
     * @see at.aschauer.commons.pentaho.plugin.PluginProperty#evaluate()
     */
    public boolean evaluate() {
        return Boolean.TRUE.equals(this.getValue());
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
        final String stringValue = XMLHandler.getTagValue(node, this.getKey());
        this.setValue(BOOLEAN_STRING_TRUE.equalsIgnoreCase(stringValue));
    }

    /**
     * {@inheritDoc}
     * 
     * @see at.aschauer.commons.pentaho.plugin.PluginProperty#readFromRepositoryStep(org.pentaho.di.repository.Repository,
     *      long)
     */
    public void readFromRepositoryStep(final Repository repository, final ObjectId stepId) throws KettleException {
        final boolean value = repository.getStepAttributeBoolean(stepId, this.getKey());
        this.setValue(value);
    }

    /**
     * {@inheritDoc}
     * 
     * @see at.aschauer.commons.pentaho.plugin.PluginProperty#saveToPreferences(java.util.prefs.Preferences)
     */
    public void saveToPreferences(final Preferences node) {
        node.putBoolean(this.getKey(), this.getValue());
    }

    /**
     * {@inheritDoc}
     * 
     * @see at.aschauer.commons.pentaho.plugin.PluginProperty#readFromPreferences(java.util.prefs.Preferences)
     */
    public void readFromPreferences(final Preferences node) {
        this.setValue(node.getBoolean(this.getKey(), this.getValue()));
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
