/**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

package org.pentaho.di.core;


/**
 * This class is used to store results of transformation and step verifications.
 * 
 * @author Matt
 * @since 11-01-04
 * 
 */
public class CheckResult implements CheckResultInterface
{

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
        return CheckResultInterface.typeDesc[type];
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
        sb.append(CheckResultInterface.typeDesc[type]).append(": ").append(text); //$NON-NLS-1$

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
