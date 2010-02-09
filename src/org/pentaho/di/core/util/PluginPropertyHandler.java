/*
 * $Header: PluginPropertyHandler.java
 * $Revision:
 * $Date: 13.05.2009 12:55:59
 *
 * ==========================================================
 * COPYRIGHTS
 * ==========================================================
 */
package org.pentaho.di.core.util;

import java.util.prefs.Preferences;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.FunctorException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
 * 
 */
public final class PluginPropertyHandler {

    /**
     * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
     * 
     */
    public abstract static class AbstractHandler implements Closure {
        /**
         * {@inheritDoc}
         * 
         * @see org.apache.commons.collections.Closure#execute(java.lang.Object)
         * @throws IllegalArgumentException
         *             if property is null.
         * @throws FunctorException
         *             if KettleException in handle thrown.
         */
        public final void execute(final Object property) throws IllegalArgumentException, FunctorException {
            Assert.assertNotNull(property, "Plugin property cannot be null");
            try {
                this.handle((PluginProperty) property);
            } catch (KettleException e) {
                throw new FunctorException("EXCEPTION: " + this, e);
            }
        }

        /**
         * Handle property.
         * 
         * @param property
         *            property.
         * @throws KettleException
         *             ...
         */
        protected abstract void handle(final PluginProperty property) throws KettleException;
    }

    /**
     * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
     * 
     *         Fail/throws KettleException.
     */
    public static class Fail extends AbstractHandler {

        /**
         * The message.
         */
        public static final String MESSAGE = "Forced exception";

        /**
         * The instance.
         */
        public static final Fail INSTANCE = new Fail();

        /**
         * {@inheritDoc}
         * 
         * @see at.aschauer.commons.pentaho.plugin.PluginPropertyHandler.AbstractHandler#handle(at.aschauer.commons.pentaho.plugin.PluginProperty)
         */
        @Override
        protected void handle(final PluginProperty property) throws KettleException {
            throw new KettleException(MESSAGE);
        }

    }

    /**
     * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
     * 
     */
    public static class AppendXml extends AbstractHandler {

        private final StringBuilder builder = new StringBuilder();

        /**
         * {@inheritDoc}
         * 
         * @see at.aschauer.commons.pentaho.plugin.PluginPropertyHandler.AbstractHandler#handle(at.aschauer.commons.pentaho.plugin.PluginProperty)
         */
        @Override
        protected void handle(final PluginProperty property) {
            property.appendXml(this.builder);
        }

        /**
         * @return XML string.
         */
        public String getXml() {
            return this.builder.toString();
        }

    }

    /**
     * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
     * 
     */
    public static class LoadXml extends AbstractHandler {

        private final Node node;

        /**
         * Constructor.
         * 
         * @param node
         *            node to set.
         * @throws IllegalArgumentException
         *             if node is null.
         */
        public LoadXml(final Node node) throws IllegalArgumentException {
            super();
            Assert.assertNotNull(node, "Node cannot be null");
            this.node = node;
        }

        /**
         * {@inheritDoc}
         * 
         * @see at.aschauer.commons.pentaho.plugin.PluginPropertyHandler.AbstractHandler#handle(at.aschauer.commons.pentaho.plugin.PluginProperty)
         */
        @Override
        protected void handle(final PluginProperty property) {
            property.loadXml(this.node);
        }

    }

    /**
     * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
     * 
     */
    public static class SaveToRepository extends AbstractHandler {

        private final Repository repository;

        private final ObjectId transformationId;

        private final ObjectId stepId;

        /**
         * Constructor.
         * 
         * @param repository
         *            repository to use.
         * @param transformationId
         *            transformation ID to set.
         * @param stepId
         *            step ID to set.
         * @throws IllegalArgumentException
         *             if repository is null.
         */
        public SaveToRepository(final Repository repository, final ObjectId transformationId, final ObjectId stepId)
                throws IllegalArgumentException {
            super();
            Assert.assertNotNull(repository, "Repository cannot be null");
            this.repository = repository;
            this.transformationId = transformationId;
            this.stepId = stepId;
        }

        /**
         * {@inheritDoc}
         * 
         * @see at.aschauer.commons.pentaho.plugin.PluginPropertyHandler.AbstractHandler#handle(at.aschauer.commons.pentaho.plugin.PluginProperty)
         */
        @Override
        protected void handle(final PluginProperty property) throws KettleException {
            property.saveToRepositoryStep(this.repository, this.transformationId, this.stepId);
        }

    }

    /**
     * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
     * 
     */
    public static class ReadFromRepository extends AbstractHandler {

        private final Repository repository;

        private final ObjectId stepId;

        /**
         * Constructor.
         * 
         * @param repository
         *            the repository.
         * @param stepId
         *            the step ID.
         * @throws IllegalArgumentException
         *             if repository is null.
         */
        public ReadFromRepository(final Repository repository, final ObjectId stepId) throws IllegalArgumentException {
            super();
            Assert.assertNotNull(repository, "Repository cannot be null");
            this.repository = repository;
            this.stepId = stepId;
        }

        /**
         * {@inheritDoc}
         * 
         * @see at.aschauer.commons.pentaho.plugin.PluginPropertyHandler.AbstractHandler#handle(at.aschauer.commons.pentaho.plugin.PluginProperty)
         */
        @Override
        protected void handle(final PluginProperty property) throws KettleException {
            property.readFromRepositoryStep(this.repository, this.stepId);
        }

    }

    /**
     * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
     * 
     */
    public static class SaveToPreferences extends AbstractHandler {

        private final Preferences node;

        /**
         * Constructor.
         * 
         * @param node
         *            node to set.
         * @throws IllegalArgumentException
         *             if node is null.
         */
        public SaveToPreferences(final Preferences node) throws IllegalArgumentException {
            super();
            Assert.assertNotNull(node, "Node cannot be null");
            this.node = node;
        }

        /**
         * {@inheritDoc}
         * 
         * @see at.aschauer.commons.pentaho.plugin.PluginPropertyHandler.AbstractHandler#handle(at.aschauer.commons.pentaho.plugin.PluginProperty)
         */
        @Override
        protected void handle(final PluginProperty property) {
            property.saveToPreferences(this.node);
        }

    }

    /**
     * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
     * 
     */
    public static class ReadFromPreferences extends AbstractHandler {

        private final Preferences node;

        /**
         * Constructor.
         * 
         * @param node
         *            node to set.
         * @throws IllegalArgumentException
         *             if node is null.
         */
        public ReadFromPreferences(final Preferences node) throws IllegalArgumentException {
            super();
            Assert.assertNotNull(node, "Node cannot be null");
            this.node = node;
        }

        /**
         * {@inheritDoc}
         * 
         * @see at.aschauer.commons.pentaho.plugin.PluginPropertyHandler.AbstractHandler#handle(at.aschauer.commons.pentaho.plugin.PluginProperty)
         */
        @Override
        protected void handle(final PluginProperty property) {
            property.readFromPreferences(this.node);
        }

    }

    /**
     * @param properties
     *            properties to test.
     * @throws IllegalArgumentException
     *             if properties is null.
     */
    public static void assertProperties(final KeyValueSet properties) throws IllegalArgumentException {
        Assert.assertNotNull(properties, "Properties cannot be null");
    }

    /**
     * @param properties
     *            properties
     * @return XML String
     * @throws IllegalArgumentException
     *             if properties is null
     */
    public static String toXml(final KeyValueSet properties) throws IllegalArgumentException {
        assertProperties(properties);
        final AppendXml handler = new AppendXml();
        properties.walk(handler);
        return handler.getXml();
    }

    /**
     * @param properties
     *            properties.
     * @param handler
     *            handler.
     * @throws KettleException
     *             ...
     * @throws IllegalArgumentException
     *             if properties is null.
     */
    public static void walk(final KeyValueSet properties, final Closure handler) throws KettleException,
            IllegalArgumentException {
        assertProperties(properties);
        try {
            properties.walk(handler);
        } catch (FunctorException e) {
            throw (KettleException) e.getCause();
        }
    }

    /**
     * Avoid instance creation.
     */
    private PluginPropertyHandler() {
        super();
    }
}
