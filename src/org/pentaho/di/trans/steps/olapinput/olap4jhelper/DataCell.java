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

public class DataCell extends AbstractBaseCell  {


    private Number rawNumber = null;

    private MemberCell parentColMember = null;


    /**
     * 
     * Blank constructor for serialization purposes, don't use it.
     * 
     */
    public DataCell() {
        super();
    }

    /**
     * Construct a Data Cell containing olap data.
     * 
     * @param b
     * @param c
     */
    public DataCell(final boolean right, final boolean sameAsPrev) {
        super();
        this.right = right;
        this.sameAsPrev = sameAsPrev;
    }
    public MemberCell getParentColMember() {
        return parentColMember;
    }

    public void setParentColMember(final MemberCell parentColMember) {
        this.parentColMember = parentColMember;
    }

    public MemberCell getParentRowMember() {
        return parentRowMember;
    }

    public void setParentRowMember(final MemberCell parentRowMember) {
        this.parentRowMember = parentRowMember;
    }

    private MemberCell parentRowMember = null;

    public Number getRawNumber() {
        return rawNumber;
    }

    public void setRawNumber(final Number rawNumber) {
        this.rawNumber = rawNumber;
    }


}
