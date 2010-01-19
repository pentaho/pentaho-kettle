/***** BEGIN LICENSE BLOCK *****
Version: MPL 1.1/GPL 2.0/LGPL 2.1

The contents of this project are subject to the Mozilla Public License Version
1.1 (the "License"); you may not use this file except in compliance with
the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the
License.

The Original Code is Mozilla Corporation Metrics ETL for AMO

The Initial Developer of the Original Code is
Daniel Einspanjer deinspanjer@mozilla.com
Portions created by the Initial Developer are Copyright (C) 2008
the Initial Developer. All Rights Reserved.

Contributor(s):

Alternatively, the contents of this file may be used under the terms of
either the GNU General Public License Version 2 or later (the "GPL"), or
the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
in which case the provisions of the GPL or the LGPL are applicable instead
of those above. If you wish to allow use of your version of this file only
under the terms of either the GPL or the LGPL, and not to allow others to
use your version of this file under the terms of the MPL, indicate your
decision by deleting the provisions above and replace them with the notice
and other provisions required by the LGPL or the GPL. If you do not delete
the provisions above, a recipient may use your version of this file under
the terms of any one of the MPL, the GPL or the LGPL.

***** END LICENSE BLOCK *****/

package org.pentaho.di.trans.steps.userdefinedjavaclass;

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

    public String getTransformedSource()
    {
        String retval = appendConstructor();
        return retval;
    }
    public void setSource(String source)
    {
        this.source = source;
    }

    private static final String CONSTRUCTOR = "\n\npublic ~CLASSNAME~(UserDefinedJavaClass parent, UserDefinedJavaClassMeta meta, UserDefinedJavaClassData data) throws KettleStepException { super(parent,meta,data);}";
    private String appendConstructor()
    {
        return source + CONSTRUCTOR.replace("~CLASSNAME~", className);
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
