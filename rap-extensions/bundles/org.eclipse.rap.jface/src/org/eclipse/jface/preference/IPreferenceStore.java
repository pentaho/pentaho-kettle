/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.preference;

import java.io.Serializable;

import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * The <code>IPreferenceStore</code> interface represents a table mapping
 * named preferences to values. If there is no value for a given name, 
 * then that preferences's default value is returned; and if there is no
 * default value for that preference, then a default-default value is returned.
 * The default-default values for the primitive types are as follows:
 * <ul>
 * 	<li><code>boolean</code> = <code>false</code></li>
 * 	<li><code>double</code> = <code>0.0</code></li>
 * 	<li><code>float</code> = <code>0.0f</code></li>
 * 	<li><code>int</code> = <code>0</code></li>
 *  <li><code>long</code> = <code>0</code></li>
 * 	<li><code>String</code> = <code>""</code> (the empty string)</li>
 * </ul>
 * <p>
 * Thus a preference store maintains two values for each of a set of
 * names: a current value and a default value.
 * The typical usage is to establish the defaults for all known preferences
 * and then restore previously stored values for preferences whose values 
 * were different from their defaults. After the current values of
 * the preferences have been modified, it is a simple matter to write
 * out only those preferences whose values are different from their defaults.
 * This two-tiered approach to saving and restoring preference setting
 * minimized the number of preferences that need to be persisted; indeed,
 * the normal starting state does not require storing any preferences
 * at all.
 * </p>
 * <p>
 * A property change event is reported whenever a preferences current
 * value actually changes (whether through <code>setValue</code>,
 * <code>setToDefault</code>, or other unspecified means). Note, however,
 * that manipulating default values (with <code>setDefault</code>)
 * does not cause such events to be reported.
 * </p>
 * <p>
 * Clients who need a preference store may implement this interface or 
 * instantiate the standard implementation <code>PreferenceStore</code>.
 * </p>
 *
 * @see PreferenceStore
 */
public interface IPreferenceStore extends Serializable {

    /**
     * The default-default value for boolean preferences (<code>false</code>).
     */
    public static final boolean BOOLEAN_DEFAULT_DEFAULT = false;

    /**
     * The default-default value for double preferences (<code>0.0</code>).
     */
    public static final double DOUBLE_DEFAULT_DEFAULT = 0.0;

    /**
     * The default-default value for float preferences (<code>0.0f</code>).
     */
    public static final float FLOAT_DEFAULT_DEFAULT = 0.0f;

    /**
     * The default-default value for int preferences (<code>0</code>).
     */
    public static final int INT_DEFAULT_DEFAULT = 0;

    /**
     * The default-default value for long preferences (<code>0L</code>).
     */
    public static final long LONG_DEFAULT_DEFAULT = 0L;

    /**
     * The default-default value for String preferences (<code>""</code>).
     */
    public static final String STRING_DEFAULT_DEFAULT = ""; //$NON-NLS-1$

    /**
     * The string representation used for <code>true</code> (<code>"true"</code>).
     */
    public static final String TRUE = "true"; //$NON-NLS-1$

    /**
     * The string representation used for <code>false</code> (<code>"false"</code>).
     */
    public static final String FALSE = "false"; //$NON-NLS-1$

    /**
     * <p>
     * Adds a property change listener to this preference store.
     * </p>
     * <p>
     * <b>Note</b> The types of the oldValue and newValue of the
     * generated PropertyChangeEvent are determined by whether
     * or not the typed API in IPreferenceStore was called.
     * If values are changed via setValue(name,type) the 
     * values in the PropertyChangedEvent will be of that type.
     * If they are set using a non typed API (i.e. #setToDefault
     * or using the OSGI Preferences) the values will be unconverted
     * Strings.
     * </p>
     * <p>
     * A listener will be called in the same Thread
     * that it is invoked in. Any Thread dependant listeners (such as 
     * those who update an SWT widget) will need to update in the
     * correct Thread. In the case of an SWT update you can update
     * using Display#syncExec(Runnable) or Display#asyncExec(Runnable).
     * </p>
     * <p>  
     * Likewise any application that updates an IPreferenceStore 
     * from a Thread other than the UI Thread should be aware of
     * any listeners that require an update in the UI Thread. 
     * </p>
     *
     * @param listener a property change listener
     * @see org.eclipse.jface.util.PropertyChangeEvent
     * @see #setToDefault(String)
     * @see #setValue(String, boolean)
     * @see #setValue(String, double)
     * @see #setValue(String, float)
     * @see #setValue(String, int)
     * @see #setValue(String, long)
     * @see #setValue(String, String)
     */
    public void addPropertyChangeListener(IPropertyChangeListener listener);

    /**
     * Returns whether the named preference is known to this preference
     * store.
     *
     * @param name the name of the preference
     * @return <code>true</code> if either a current value or a default
     *  value is known for the named preference, and <code>false</code> otherwise
     */
    public boolean contains(String name);

    /**
     * Fires a property change event corresponding to a change to the
     * current value of the preference with the given name.
     * <p>
     * This method is provided on this interface to simplify the implementation 
     * of decorators. There is normally no need to call this method since
     * <code>setValue</code> and <code>setToDefault</code> report such
     * events in due course. Implementations should funnel all preference
     * changes through this method.
     * </p>
     *
     * @param name the name of the preference, to be used as the property
     *  in the event object
     * @param oldValue the old value
     * @param newValue the new value
     */
    public void firePropertyChangeEvent(String name, Object oldValue,
            Object newValue);

    /**
     * Returns the current value of the boolean-valued preference with the
     * given name.
     * Returns the default-default value (<code>false</code>) if there
     * is no preference with the given name, or if the current value 
     * cannot be treated as a boolean.
     *
     * @param name the name of the preference
     * @return the boolean-valued preference
     */
    public boolean getBoolean(String name);

    /**
     * Returns the default value for the boolean-valued preference
     * with the given name.
     * Returns the default-default value (<code>false</code>) if there
     * is no default preference with the given name, or if the default 
     * value cannot be treated as a boolean.
     *
     * @param name the name of the preference
     * @return the default value of the named preference
     */
    public boolean getDefaultBoolean(String name);

    /**
     * Returns the default value for the double-valued preference
     * with the given name.
     * Returns the default-default value (<code>0.0</code>) if there
     * is no default preference with the given name, or if the default 
     * value cannot be treated as a double.
     *
     * @param name the name of the preference
     * @return the default value of the named preference
     */
    public double getDefaultDouble(String name);

    /**
     * Returns the default value for the float-valued preference
     * with the given name.
     * Returns the default-default value (<code>0.0f</code>) if there
     * is no default preference with the given name, or if the default 
     * value cannot be treated as a float.
     *
     * @param name the name of the preference
     * @return the default value of the named preference
     */
    public float getDefaultFloat(String name);

    /**
     * Returns the default value for the integer-valued preference
     * with the given name.
     * Returns the default-default value (<code>0</code>) if there
     * is no default preference with the given name, or if the default 
     * value cannot be treated as an integer.
     *
     * @param name the name of the preference
     * @return the default value of the named preference
     */
    public int getDefaultInt(String name);

    /**
     * Returns the default value for the long-valued preference
     * with the given name.
     * Returns the default-default value (<code>0L</code>) if there
     * is no default preference with the given name, or if the default 
     * value cannot be treated as a long.
     *
     * @param name the name of the preference
     * @return the default value of the named preference
     */
    public long getDefaultLong(String name);

    /**
     * Returns the default value for the string-valued preference
     * with the given name.
     * Returns the default-default value (the empty string <code>""</code>) 
     * is no default preference with the given name, or if the default 
     * value cannot be treated as a string.
     *
     * @param name the name of the preference
     * @return the default value of the named preference
     */
    public String getDefaultString(String name);

    /**
     * Returns the current value of the double-valued preference with the
     * given name.
     * Returns the default-default value (<code>0.0</code>) if there
     * is no preference with the given name, or if the current value 
     * cannot be treated as a double.
     *
     * @param name the name of the preference
     * @return the double-valued preference
     */
    public double getDouble(String name);

    /**
     * Returns the current value of the float-valued preference with the
     * given name.
     * Returns the default-default value (<code>0.0f</code>) if there
     * is no preference with the given name, or if the current value 
     * cannot be treated as a float.
     *
     * @param name the name of the preference
     * @return the float-valued preference
     */
    public float getFloat(String name);

    /**
     * Returns the current value of the integer-valued preference with the
     * given name.
     * Returns the default-default value (<code>0</code>) if there
     * is no preference with the given name, or if the current value 
     * cannot be treated as an integter.
     *
     * @param name the name of the preference
     * @return the int-valued preference
     */
    public int getInt(String name);

    /**
     * Returns the current value of the long-valued preference with the
     * given name.
     * Returns the default-default value (<code>0L</code>) if there
     * is no preference with the given name, or if the current value 
     * cannot be treated as a long.
     *
     * @param name the name of the preference
     * @return the long-valued preference
     */
    public long getLong(String name);

    /**
     * Returns the current value of the string-valued preference with the
     * given name.
     * Returns the default-default value (the empty string <code>""</code>)
     * if there is no preference with the given name, or if the current value 
     * cannot be treated as a string.
     *
     * @param name the name of the preference
     * @return the string-valued preference
     */
    public String getString(String name);

    /**
     * Returns whether the current value of the preference with the given name
     * has the default value.
     *
     * @param name the name of the preference
     * @return <code>true</code> if the preference has a known default value
     * and its current value is the same, and <code>false</code> otherwise
     * (including the case where the preference is unknown to this store)
     */
    public boolean isDefault(String name);

    /**
     * Returns whether the current values in this property store
     * require saving.
     *
     * @return <code>true</code> if at least one of values of 
     *  the preferences known to this store has changed and 
     *  requires saving, and <code>false</code> otherwise.
     */
    public boolean needsSaving();

    /**
     * Sets the current value of the preference with the given name to
     * the given string value without sending a property change.
     * <p>
     * This method does not fire a property change event and 
     * should only be used for setting internal preferences 
     * that are not meant to be processed by listeners.
     * Normal clients should instead call #setValue.
     * </p>
     *
     * @param name the name of the preference
     * @param value the new current value of the preference
     */
    public void putValue(String name, String value);

    /**
	 * Removes the given listener from this preference store. Has no effect if the listener is not
	 * registered.
	 * 
	 * @param listener a property change listener, must not be <code>null</code>
	 */
    public void removePropertyChangeListener(IPropertyChangeListener listener);

    /**
     * Sets the default value for the double-valued preference with the
     * given name. 
     * <p>
     * Note that the current value of the preference is affected if
     * the preference's current value was its old default value, in which
     * case it changes to the new default value. If the preference's current
     * is different from its old default value, its current value is
     * unaffected. No property change events are reported by changing default
     * values.
     * </p>
     *
     * @param name the name of the preference
     * @param value the new default value for the preference
     */
    public void setDefault(String name, double value);

    /**
     * Sets the default value for the float-valued preference with the
     * given name. 
     * <p>
     * Note that the current value of the preference is affected if
     * the preference's current value was its old default value, in which
     * case it changes to the new default value. If the preference's current
     * is different from its old default value, its current value is
     * unaffected. No property change events are reported by changing default
     * values.
     * </p>
     *
     * @param name the name of the preference
     * @param value the new default value for the preference
     */
    public void setDefault(String name, float value);

    /**
     * Sets the default value for the integer-valued preference with the
     * given name. 
     * <p>
     * Note that the current value of the preference is affected if
     * the preference's current value was its old default value, in which
     * case it changes to the new default value. If the preference's current
     * is different from its old default value, its current value is
     * unaffected. No property change events are reported by changing default
     * values.
     * </p>
     *
     * @param name the name of the preference
     * @param value the new default value for the preference
     */
    public void setDefault(String name, int value);

    /**
     * Sets the default value for the long-valued preference with the
     * given name. 
     * <p>
     * Note that the current value of the preference is affected if
     * the preference's current value was its old default value, in which
     * case it changes to the new default value. If the preference's current
     * is different from its old default value, its current value is
     * unaffected. No property change events are reported by changing default
     * values.
     * </p>
     *
     * @param name the name of the preference
     * @param value the new default value for the preference
     */
    public void setDefault(String name, long value);

    /**
     * Sets the default value for the string-valued preference with the
     * given name. 
     * <p>
     * Note that the current value of the preference is affected if
     * the preference's current value was its old default value, in which
     * case it changes to the new default value. If the preference's current
     * is different from its old default value, its current value is
     * unaffected. No property change events are reported by changing default
     * values.
     * </p>
     *
     * @param name the name of the preference
     * @param defaultObject the new default value for the preference
     */
    public void setDefault(String name, String defaultObject);

    /**
     * Sets the default value for the boolean-valued preference with the
     * given name. 
     * <p>
     * Note that the current value of the preference is affected if
     * the preference's current value was its old default value, in which
     * case it changes to the new default value. If the preference's current
     * is different from its old default value, its current value is
     * unaffected. No property change events are reported by changing default
     * values.
     * </p>
     *
     * @param name the name of the preference
     * @param value the new default value for the preference
     */
    public void setDefault(String name, boolean value);

    /**
     * Sets the current value of the preference with the given name back
     * to its default value.
     * <p>
     * Note that the preferred way of re-initializing a preference to the
     * appropriate default value is to call <code>setToDefault</code>.
     * This is implemented by removing the named value from the store, 
     * thereby exposing the default value.
     * </p>
     *
     * @param name the name of the preference
     */
    public void setToDefault(String name);

    /**
     * Sets the current value of the double-valued preference with the
     * given name.
     * <p>
     * A property change event is reported if the current value of the 
     * preference actually changes from its previous value. In the event
     * object, the property name is the name of the preference, and the
     * old and new values are wrapped as objects.
     * </p>
     * <p>
     * Note that the preferred way of re-initializing a preference to its
     * default value is to call <code>setToDefault</code>.
     * </p>
     *
     * @param name the name of the preference
     * @param value the new current value of the preference
     */
    public void setValue(String name, double value);

    /**
     * Sets the current value of the float-valued preference with the
     * given name.
     * <p>
     * A property change event is reported if the current value of the 
     * preference actually changes from its previous value. In the event
     * object, the property name is the name of the preference, and the
     * old and new values are wrapped as objects.
     * </p>
     * <p>
     * Note that the preferred way of re-initializing a preference to its
     * default value is to call <code>setToDefault</code>.
     * </p>
     *
     * @param name the name of the preference
     * @param value the new current value of the preference
     */
    public void setValue(String name, float value);

    /**
     * Sets the current value of the integer-valued preference with the
     * given name.
     * <p>
     * A property change event is reported if the current value of the 
     * preference actually changes from its previous value. In the event
     * object, the property name is the name of the preference, and the
     * old and new values are wrapped as objects.
     * </p>
     * <p>
     * Note that the preferred way of re-initializing a preference to its
     * default value is to call <code>setToDefault</code>.
     * </p>
     *
     * @param name the name of the preference
     * @param value the new current value of the preference
     */
    public void setValue(String name, int value);

    /**
     * Sets the current value of the long-valued preference with the
     * given name.
     * <p>
     * A property change event is reported if the current value of the 
     * preference actually changes from its previous value. In the event
     * object, the property name is the name of the preference, and the
     * old and new values are wrapped as objects.
     * </p>
     * <p>
     * Note that the preferred way of re-initializing a preference to its
     * default value is to call <code>setToDefault</code>.
     * </p>
     *
     * @param name the name of the preference
     * @param value the new current value of the preference
     */
    public void setValue(String name, long value);

    /**
     * Sets the current value of the string-valued preference with the
     * given name.
     * <p>
     * A property change event is reported if the current value of the 
     * preference actually changes from its previous value. In the event
     * object, the property name is the name of the preference, and the
     * old and new values are wrapped as objects.
     * </p>
     * <p>
     * Note that the preferred way of re-initializing a preference to its
     * default value is to call <code>setToDefault</code>.
     * </p>
     *
     * @param name the name of the preference
     * @param value the new current value of the preference
     */
    public void setValue(String name, String value);

    /**
     * Sets the current value of the boolean-valued preference with the
     * given name.
     * <p>
     * A property change event is reported if the current value of the 
     * preference actually changes from its previous value. In the event
     * object, the property name is the name of the preference, and the
     * old and new values are wrapped as objects.
     * </p>
     * <p>
     * Note that the preferred way of re-initializing a preference to its
     * default value is to call <code>setToDefault</code>.
     * </p>
     *
     * @param name the name of the preference
     * @param value the new current value of the preference
     */
    public void setValue(String name, boolean value);
}
