/*
 * $Header: AddClosureArrayList.java
 * $Revision:
 * $Date: 12.05.2009 22:24:09
 *
 * ==========================================================
 * COPYRIGHTS
 * ==========================================================
 */
package org.pentaho.di.core.util;

import java.util.ArrayList;

import org.apache.commons.collections.Closure;

/**
 * @param <T>
 *            type.
 * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
 * 
 */
public class AddClosureArrayList<T> extends ArrayList<T> implements Closure {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 2395583665248110276L;

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.commons.collections.Closure#execute(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void execute(final Object input) {
        this.add((T) input);
    }

}
