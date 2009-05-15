/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.core;

import org.pentaho.di.i18n.BaseMessages;


/**
 * This class is used to store results of transformation and step verifications.
 * 
 * @author Matt
 * @since 11-01-04
 * 
 */
public class CheckResult implements CheckResultInterface
{
	private static Class<?> PKG = Const.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	  public static final String typeDesc[] = {
          "", //$NON-NLS-1$
          BaseMessages.getString(PKG, "CheckResult.OK"), //$NON-NLS-1$
          BaseMessages.getString(PKG, "CheckResult.Remark"), //$NON-NLS-1$
          BaseMessages.getString(PKG, "CheckResult.Warning"), //$NON-NLS-1$
          BaseMessages.getString(PKG, "CheckResult.Error") //$NON-NLS-1$
          };
	  
    private int type;

    private String text;

    // MB - Support both JobEntry and Step Checking
    // 6/26/07
    private CheckResultSourceInterface sourceMeta;
    
    private String errorCode;

    public CheckResult()
    {
        this(CheckResultInterface.TYPE_RESULT_NONE, "", null); //$NON-NLS-1$
    }

    public CheckResult(int t, String s, CheckResultSourceInterface sourceMeta)
    {
        type = t;
        text = s;
        this.sourceMeta = sourceMeta;
    }

    public CheckResult(int t, String errorCode, String s, CheckResultSourceInterface sourceMeta)
    {
        this(t, s, sourceMeta);
        this.errorCode = errorCode;
    }


    public int getType()
    {
        return type;
    }

    public String getTypeDesc()
    {
        return typeDesc[type];
    }

    public String getText()
    {
        return text;
    }

    public CheckResultSourceInterface getSourceInfo()
    {
        return sourceMeta;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(typeDesc[type]).append(": ").append(text); //$NON-NLS-1$

        if (sourceMeta != null)
            sb.append(" (").append(sourceMeta.getName()).append(")");  //$NON-NLS-1$ //$NON-NLS-2$

        return sb.toString();
    }

    /**
     * @return the errorCode
     */
    public String getErrorCode()
    {
        return errorCode;
    }

    /**
     * @param errorCode the errorCode to set
     */
    public void setErrorCode(String errorCode)
    {
        this.errorCode = errorCode;
    }
    public void setText(String value) {
      this.text = value;
    }

    public void setType(int value) {
      this.type = value;
    }


}
