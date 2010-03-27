/***** BEGIN LICENSE BLOCK *****
The contents of this package are subject to the GNU Lesser Public License
 (the "License"); you may not use this file except in compliance with
the License. You may obtain a copy of the License at
http://www.gnu.org/licenses/lgpl-2.1.txt

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the
License.

The Original Code is Kettle User Defined Java Class Step

The Initial Developer of the Original Code is
Daniel Einspanjer deinspanjer@mozilla.com
Portions created by the Initial Developer are Copyright (C) 2009
the Initial Developer. All Rights Reserved.

Contributor(s):
Matt Casters mcaster@pentaho.com

***** END LICENSE BLOCK *****/

package org.pentaho.di.trans.steps.userdefinedjavaclass;

import org.pentaho.di.core.exception.KettleStepException;

public class UserDefinedJavaClassDef
{
    public enum ClassType
    {
        NORMAL_CLASS, TRANSFORM_CLASS
    }

    private ClassType classType;
    private boolean   classActive;
    private String    className;
    private String    source;

    public UserDefinedJavaClassDef(ClassType classType, String className, String source)
    {
        super();
        this.classType = classType;
        this.className = className;
        this.source = source;
        classActive = true;
    }

    public ClassType getClassType()
    {
        return classType;
    }

    public void setClassType(ClassType iScriptType)
    {
        this.classType = iScriptType;
    }

    public String getSource()
    {
        return this.source;
    }

    public String getTransformedSource() throws KettleStepException
    {
        StringBuilder sb = new StringBuilder(getSource());
        appendConstructor(sb);
        return sb.toString();
    }
    public void setSource(String source)
    {
        this.source = source;
    }

    private static final String CONSTRUCTOR = "\n\npublic %s(UserDefinedJavaClass parent, UserDefinedJavaClassMeta meta, UserDefinedJavaClassData data) throws KettleStepException { super(parent,meta,data);}";
    private void appendConstructor(StringBuilder sb)
    {
        sb.append(String.format(CONSTRUCTOR, className));
    }
    
    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public boolean isTransformClass()
    {
        return (this.classActive && this.classType == ClassType.TRANSFORM_CLASS);
    }

    public void setActive(boolean classActive)
    {
        this.classActive = classActive;
    }

    public boolean isActive()
    {
        return classActive;
    }
}
