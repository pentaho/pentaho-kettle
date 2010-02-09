/*
 * $Header: AbstractStepMeta.java
 * $Revision:
 * $Date: 10.05.2009 19:51:46
 *
 * ==========================================================
 * COPYRIGHTS
 * ==========================================================
 */
package org.pentaho.di.core.util;

import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.PluginPropertyHandler.LoadXml;
import org.pentaho.di.core.util.PluginPropertyHandler.ReadFromPreferences;
import org.pentaho.di.core.util.PluginPropertyHandler.ReadFromRepository;
import org.pentaho.di.core.util.PluginPropertyHandler.SaveToPreferences;
import org.pentaho.di.core.util.PluginPropertyHandler.SaveToRepository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
 * 
 */
/**
 * @author <a href="mailto:michael.gugerell@aschauer-edv.at">Michael Gugerell(asc145)</a>
 * 
 */
public abstract class AbstractStepMeta extends BaseStepMeta implements StepMetaInterface {

    private static final String CONNECTION_NAME = "connection";

    private final PluginPropertyFactory propertyFactory = new PluginPropertyFactory(new KeyValueSet());

    private DatabaseMeta dbMeta;

    private StringPluginProperty connectionName;

    /**
     * Default constructor.
     */
    public AbstractStepMeta() {
        super();
        this.connectionName = this.propertyFactory.createString(CONNECTION_NAME);
    }

    /**
     * @return the propertyFactory
     */
    public PluginPropertyFactory getPropertyFactory() {
        return this.propertyFactory;
    }

    /**
     * @return the properties
     */
    public KeyValueSet getProperties() {
        return this.propertyFactory.getProperties();
    }

    /**
     * Saves properties to preferences.
     * 
     * @throws BackingStoreException
     *             ...
     */
    public void saveAsPreferences() throws BackingStoreException {
        final Preferences node = Preferences.userNodeForPackage(this.getClass());
        this.getProperties().walk(new SaveToPreferences(node));
        node.flush();
    }

    /**
     * Read properties from preferences.
     */
    public void readFromPreferences() {
        final Preferences node = Preferences.userNodeForPackage(this.getClass());
        this.getProperties().walk(new ReadFromPreferences(node));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.pentaho.di.trans.step.StepMetaInterface#loadXML(org.w3c.dom.Node, java.util.List, java.util.Map)
     */
    public void loadXML(final Node node, final List<DatabaseMeta> databaseMeta, final Map<String, Counter> counters)
            throws KettleXMLException {
        this.getProperties().walk(new LoadXml(node));
        initDbMeta(databaseMeta);
    }

    /**
     * @param databaseList
     *            A list of available DatabaseMeta in this transformation.
     */
    private void initDbMeta(final List<DatabaseMeta> databaseList) {
        if (!StringUtils.isEmpty(this.connectionName.getValue())) {
            this.dbMeta = DatabaseMeta.findDatabase(databaseList, this.connectionName.getValue());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.pentaho.di.trans.step.BaseStepMeta#getXML()
     */
    @Override
    public String getXML() throws KettleException {
        return PluginPropertyHandler.toXml(this.getProperties());
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.pentaho.di.trans.step.StepMetaInterface#readRep(org.pentaho.di.repository.Repository, long,
     *      java.util.List, java.util.Map)
     */
    public void readRep(final Repository repo, final ObjectId stepId, final List<DatabaseMeta> databaseList,
            final Map<String, Counter> counters) throws KettleException {
    	PluginPropertyHandler.walk(this.getProperties(), new ReadFromRepository(repo, stepId));
        initDbMeta(databaseList);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.pentaho.di.trans.step.StepMetaInterface#saveRep(org.pentaho.di.repository.Repository, long, long)
     */
    public void saveRep(final Repository repo, final ObjectId transformationId, final ObjectId stepId) throws KettleException {
        final SaveToRepository handler = new SaveToRepository(repo, transformationId, stepId);
        PluginPropertyHandler.walk(this.getProperties(), handler);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.pentaho.di.trans.step.StepMetaInterface#getStepData()
     */
    public StepDataInterface getStepData() {
        // you may be override this.
        return new GenericStepData();
    }

    /**
     * @return the connectionName
     */
    public StringPluginProperty getConnectionName() {
        return this.connectionName;
    }

    /**
     * @param connectionName
     *            the connectionName to set
     */
    public void setConnectionName(final StringPluginProperty connectionName) {
        this.connectionName = connectionName;
    }

    /**
     * @return the dbMeta
     */
    public DatabaseMeta getDbMeta() {
        return this.dbMeta;
    }

    /**
     * @param dbMeta
     *            the dbMeta to set
     */
    public void setDbMeta(final DatabaseMeta dbMeta) {
        this.dbMeta = dbMeta;
    }
}
