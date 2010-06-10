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
import org.olap4j.CellSet;


public class OlapUtil {

    public static CellDataSet cellSet2Matrix(final CellSet cellSet) {
        if (cellSet == null) {
            return null;
        }
        final CellSetFormatter pcsf = new CellSetFormatter();

        final Matrix matrix = pcsf.format(cellSet);
        final CellDataSet cds = new CellDataSet(matrix.getMatrixWidth(), matrix.getMatrixHeight());

        int z = 0;
        final AbstractBaseCell[][] bodyvalues = new AbstractBaseCell[matrix.getMatrixHeight() - matrix.getOffset()][matrix
                .getMatrixWidth()];
        for (int y = matrix.getOffset(); y < matrix.getMatrixHeight(); y++) {

            for (int x = 0; x < matrix.getMatrixWidth(); x++) {
                bodyvalues[z][x] = matrix.get(x, y);
            }
            z++;
        }

        cds.setCellSetBody(bodyvalues);

        final AbstractBaseCell[][] headervalues = new AbstractBaseCell[matrix.getOffset()][matrix.getMatrixWidth()];
        for (int y = 0; y < matrix.getOffset(); y++) {
            for (int x = 0; x < matrix.getMatrixWidth(); x++) {
                headervalues[y][x] = matrix.get(x, y);
            }
        }
        cds.setCellSetHeaders(headervalues);
        cds.setOffset(matrix.getOffset());
        return cds;

    }
}
