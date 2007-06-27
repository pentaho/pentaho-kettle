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
public class CheckResult
{
    public static final int TYPE_RESULT_NONE = 0;

    public static final int TYPE_RESULT_OK = 1;

    public static final int TYPE_RESULT_COMMENT = 2;

    public static final int TYPE_RESULT_WARNING = 3;

    public static final int TYPE_RESULT_ERROR = 4;

    public static final String typeDesc[] = {
                                             "",
                                             Messages.getString("CheckResult.OK"),
                                             Messages.getString("CheckResult.Remark"),
                                             Messages.getString("CheckResult.Warning"),
                                             Messages.getString("CheckResult.Error")
                                             };

    private int type;

    private String text;

    // MB - Support both JobEntry and Step Checking
    // 6/26/07
    private CheckResultSourceInterface sourceMeta;
    
    private String errorCode;

    public CheckResult()
    {
        this(TYPE_RESULT_NONE, "", null);
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
        sb.append(typeDesc[type]).append(": ").append(text);

        if (sourceMeta != null)
            sb.append(" (").append(sourceMeta.getName()).append(")");

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
}
