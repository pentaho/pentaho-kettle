/*
 * $Header: PentahoMessages.java
 * $Revision:
 * $Date: 11.05.2009 16:45:09
 *
 * ==========================================================
 * COPYRIGHTS
 * ==========================================================
 */
package org.pentaho.di.core.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.pentaho.di.i18n.BaseMessages;

/**
 * Simple utility for messages. Usage: <code>
 * [...]
 * private static final PluginMessages MESSAGES = PluginMessages.getMessages([class]).
 * [...]
 * 
 * MESSAGES.getString([key])
 * </code>
 * 
 * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
 * 
 */
public final class PluginMessages {

    private static final Map<String, PluginMessages> MESSAGES_MAP = new HashMap<String, PluginMessages>();

    /**
     * Factory method.
     * 
     * @param packageName
     *            package name.
     * @return messages.
     * @throws IllegalArgumentException
     *             if package name is blank.
     */
    public static PluginMessages getMessages(final String packageName) throws IllegalArgumentException {
        Assert.assertNotBlank(packageName, "Package name cannot be blank");
        synchronized (PluginMessages.class) {
            if (MESSAGES_MAP.containsKey(packageName)) {
                return MESSAGES_MAP.get(packageName);
            }
            MESSAGES_MAP.put(packageName, new PluginMessages(packageName));
        }
        return MESSAGES_MAP.get(packageName);
    }

    /**
     * Factory method.
     * 
     * @param someClassInPackage
     *            some class in package.
     * @return messages.
     * @throws IllegalArgumentException
     *             if class is null
     */
    @SuppressWarnings("unchecked")
    public static PluginMessages getMessages(final Class someClassInPackage) throws IllegalArgumentException {
        Assert.assertNotNull(someClassInPackage, "Class cannot be null");
        return getMessages(someClassInPackage.getPackage().getName());
    }

    private final String packageName;

    /**
     * @param packageName
     *            package name to set.
     */
    private PluginMessages(final String packageName) {
        this.packageName = packageName;
    }

    /**
     * @return the packageName
     */
    public String getPackageName() {
        return this.packageName;
    }

    /**
     * 
     * @param key
     *            the key.
     * @return the message.
     */
    public String getString(final String key) {
        return BaseMessages.getString(this.packageName, key);
    }

    /**
     * 
     * @param key
     *            the key.
     * @param param1
     *            the param1.
     * @return the message.
     */
    public String getString(final String key, final String param1) {
        return BaseMessages.getString(this.packageName, key, param1);
    }

    /**
     * 
     * @param key
     *            the key.
     * @param param1
     *            the param1.
     * @param param2
     *            the param2.
     * @return the message.
     */
    public String getString(final String key, final String param1, final String param2) {
        return BaseMessages.getString(this.packageName, key, param1, param2);
    }

    /**
     * @param key
     *            the key.
     * @param param1
     *            the param1.
     * @param param2
     *            the param2.
     * @param param3
     *            the param3.
     * @return the message.
     */
    public String getString(final String key, final String param1, final String param2, final String param3) {
        return BaseMessages.getString(this.packageName, key, param1, param2, param3);
    }

    /**
     * 
     * @param key
     *            the key.
     * @param param1
     *            the param1.
     * @param param2
     *            the param2.
     * @param param3
     *            the param3.
     * @param param4
     *            the param4.
     * @return the message.
     */
    public String getString(final String key, final String param1, final String param2, final String param3,
            final String param4) {
        return BaseMessages.getString(this.packageName, key, param1, param2, param3, param4);
    }

    /**
     * 
     * @param key
     *            the key.
     * @param param1
     *            the param1.
     * @param param2
     *            the param2.
     * @param param3
     *            the param3.
     * @param param4
     *            the param4.
     * @param param5
     *            the param5.
     * @return the message.
     */
    public String getString(final String key, final String param1, final String param2, final String param3,
            final String param4, final String param5) {
        return BaseMessages.getString(this.packageName, key, param1, param2, param3, param4, param5);
    }

    /**
     * 
     * @param key
     *            the key.
     * @param param1
     *            the param1.
     * @param param2
     *            the param2.
     * @param param3
     *            the param3.
     * @param param4
     *            the param4.
     * @param param5
     *            the param5.
     * @param param6
     *            the param6.
     * @return the message.
     */
    public String getString(final String key, final String param1, final String param2, final String param3,
            final String param4, final String param5, final String param6) {
        return BaseMessages.getString(this.packageName, key, param1, param2, param3, param4, param5, param6);
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append("packageName", this.packageName);
        return builder.toString();
    }

}
