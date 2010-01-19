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

import java.math.BigDecimal;
import java.util.Date;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

public class FieldHelper
{
    private int index = -1;
    private ValueMetaInterface meta;

    public FieldHelper(RowMetaInterface rowMeta, String fieldName)
    {
        this.meta = rowMeta.searchValueMeta(fieldName);
        this.index = rowMeta.indexOfValue(fieldName);
        if (this.index == -1)
        {
            throw new IllegalArgumentException(String.format("FieldHelper could not be initialized. The field named '%s' not found.", fieldName));
        }
    }
    
    public Object getObject(Object[] dataRow)
    {
        return dataRow[index];
    }

    public BigDecimal getBigNumber(Object[] dataRow) throws KettleValueException
    {
        return meta.getBigNumber(dataRow[index]);
    }

    public byte[] getBinary(Object[] dataRow) throws KettleValueException
    {
        return meta.getBinary(dataRow[index]);
    }

    public Boolean getBoolean(Object[] dataRow) throws KettleValueException
    {
        return meta.getBoolean(dataRow[index]);
    }

    public Date getDate(Object[] dataRow) throws KettleValueException
    {
        return meta.getDate(dataRow[index]);
    }

    public Long getInteger(Object[] dataRow) throws KettleValueException
    {
        return meta.getInteger(dataRow[index]);
    }

    public Double getNumber(Object[] dataRow) throws KettleValueException
    {
        return meta.getNumber(dataRow[index]);
    }

    public String getString(Object[] dataRow) throws KettleValueException
    {
        return meta.getString(dataRow[index]);
    }

    public ValueMetaInterface getValueMeta()
    {
        return meta;
    }

    public int indexOfValue()
    {
        return index;
    }
    
    public void setValue(Object[] dataRow, Object value)
    {
        dataRow[index] = value;
    }
}
