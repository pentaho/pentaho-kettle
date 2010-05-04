/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.trans.steps.olapinput.olap4jhelper;

public abstract class AbstractBaseCell  {


    /** The formatted value. */
    private String formattedValue;

    /** The raw value. */
    private String rawValue;

    public boolean right = false;

    public boolean sameAsPrev = false;

    private String parentDimension = null;

    /**
     * Blank Constructor for serialization dont use.
     */
    public AbstractBaseCell() {
    }

    /**
     * BaseCell Constructor, every cell type should inherit basecell.
     * @param right
     * @param sameAsPrev
     */
    public AbstractBaseCell(final boolean right, final boolean sameAsPrev) {
        this.right = right;
        this.sameAsPrev = sameAsPrev;
    }

    /**
     * Gets the formatted value.
     * @return the formatted value
     */
    public String getFormattedValue() {
        return formattedValue;
    }

    /**
     * Gets the raw value.
     * @return the raw value
     */
    public String getRawValue() {
        return rawValue;
    }

    /**
     * Sets the formatted value.
     * 
     * @param formattedValue
     *            the new formatted value
     */
    public void setFormattedValue(final String formattedValue) {
        this.formattedValue = formattedValue;
    }

    /**
     * Sets the raw value.
     * 
     * @param rawValue
     *            the new raw value
     */
    public void setRawValue(final String rawValue) {
        this.rawValue = rawValue;
    }

    /**
     * 
     * @param set
     */
    public void setRight(final boolean set) {
        this.right = set;
    }

    /**
     * Set true if value is same as the previous one in the row.
     * @param same
     */
    public void setSameAsPrev(final boolean same) {
        this.sameAsPrev = same;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return formattedValue;
    }

    public void setParentDimension(final String pdim) {
        parentDimension = pdim;
    }

    public String getParentDimension() {
        return parentDimension;
    }

}
