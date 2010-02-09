/*
 * $Header: PluginPropertiesFactory.java
 * $Revision:
 * $Date: 13.05.2009 18:14:00
 *
 * ==========================================================
 * COPYRIGHTS
 * ==========================================================
 */
package org.pentaho.di.core.util;


/**
 * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
 * 
 */
public class PluginPropertyFactory {

    private final KeyValueSet properties;

    /**
     * Constructor.
     * 
     * @param properties
     *            properties to set.
     * @throws IllegalArgumentException
     *             if properties is null.
     */
    public PluginPropertyFactory(final KeyValueSet properties) throws IllegalArgumentException {
        Assert.assertNotNull(properties, "Properties cannot be null");
        this.properties = properties;
    }

    /**
     * @return the properties
     */
    public KeyValueSet getProperties() {
        return this.properties;
    }

    /**
     * @param key
     *            key to set.
     * @return new string property.
     * @throws IllegalArgumentException
     *             if key is invalid.
     */
    public StringPluginProperty createString(final String key) throws IllegalArgumentException {
        final StringPluginProperty property = new StringPluginProperty(key);
        this.properties.add(property);
        return property;
    }

    /**
     * @param key
     *            key to set.
     * @return new integer property.
     * @throws IllegalArgumentException
     *             if key is invalid.
     */
    public IntegerPluginProperty createInteger(final String key) throws IllegalArgumentException {
        final IntegerPluginProperty property = new IntegerPluginProperty(key);
        this.properties.add(property);
        return property;
    }

    /**
     * @param key
     *            key to set.
     * @return new boolean property.
     * @throws IllegalArgumentException
     *             if key is invalid.
     */
    public BooleanPluginProperty createBoolean(final String key) throws IllegalArgumentException {
        final BooleanPluginProperty property = new BooleanPluginProperty(key);
        this.properties.add(property);
        return property;
    }

    /**
     * @param key
     *            key.
     * @return new string list.
     * @throws IllegalArgumentException
     *             if key is invalid.
     */
    public StringListPluginProperty createStringList(final String key) throws IllegalArgumentException {
        final StringListPluginProperty property = new StringListPluginProperty(key);
        this.properties.add(property);
        return property;
    }
}
