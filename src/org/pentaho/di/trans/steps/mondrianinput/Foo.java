package org.pentaho.di.trans.steps.mondrianinput;
/*
// $Id: $
// This software is subject to the terms of the Common Public License
// Agreement, available at the following URL:
// http://www.opensource.org/licenses/cpl.html.
// Copyright (C) 2007-2007 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/

import mondrian.olap.*;

import java.util.List;
import java.util.ArrayList;
import java.io.PrintWriter;

/**
 * <code>Foo</code> ...
 *
 * @author jhyde
 * @version $Id: $
 * @since Mar 12, 2007
 */
public class Foo {
    public static void main(String[] args) {
        final String connectString = "Provider=mondrian;" +
            "Jdbc=jdbc:odbc:MondrianFoodMart;" +
            "Catalog=file:demo/FoodMart.xml;" +
            "JdbcDrivers=sun.jdbc.odbc.JdbcOdbcDriver";
        Connection connection = DriverManager.getConnection(connectString, null, true);
        Query query = connection.parseQuery(
            "select\n" +
                " {([Gender].[F], [Measures].[Unit Sales]),\n" +
                "  ([Gender].[M], [Measures].[Store Sales]),\n" +
                "  ([Gender].[F], [Measures].[Unit Sales])} on columns,\n" +
                " CrossJoin([Marital Status].Members,\n" +
                "           [Product].Children) on rows\n" +
                "from [Sales]");
        Result result = connection.execute(query);
        final PrintWriter pw = new PrintWriter(System.out);

        pw.println("Output in flattened format (one row for each cell)");
        outputFlattened(result, pw);

        pw.println("Output in row format (one row for each cell)");
        outputRectangular(result, pw);
        pw.flush();
    }

    /**
     * Outputs one row per tuple on the rows axis.
     * @param result Result set to serialize
     * @param pw Print writer to print result to
     */
    private static void outputRectangular(Result result, PrintWriter pw) {
        final Axis[] axes = result.getAxes();
        if (axes.length != 2) {
            throw new IllegalArgumentException("tabular output only supported for 2-dimensional results");
        }
        List<String> headings = new ArrayList<String>();
        List<List<Object>> rows = new ArrayList<List<Object>>();

        final Axis rowsAxis = axes[1];
        final Axis columnsAxis = axes[0];

        int rowOrdinal = -1;
        int[] coords = {0, 0};
        for (Position rowPos : rowsAxis.getPositions()) {
            ++rowOrdinal;
            coords[1] = rowOrdinal;
            if (rowOrdinal == 0) {
                // Generate headings on the first row. Note that if there are
                // zero rows, we don't have enough metadata to generate
                // headings.

                // First headings are for the members on the rows axis.
                for (Member rowMember : rowPos) {
                    headings.add(rowMember.getHierarchy().getUniqueName());
                }

                // Rest of the headings are for the members on the columns axis.
                // If there are more than one member at each postition,
                // concatenate the unique names.
                for (Position columnPos : columnsAxis.getPositions()) {
                    String heading = "";
                    for (Member columnMember : columnPos) {
                        if (!heading.equals("")) {
                            heading += ", ";
                        }
                        heading += columnMember.getUniqueName();
                    }
                    headings.add(heading);
                }
            }

            List<Object> rowValues = new ArrayList<Object>();

            // The first row values describe the members on the rows axis.
            for (Member rowMember : rowPos) {
                rowValues.add(rowMember.getUniqueName());
            }

            // Rest of the row values are the raw cell values.
            // NOTE: Could also/instead output formatted cell values here.
            // NOTE: Could also output all properties of each cell.
            for (int columnOrdinal = 0;
                columnOrdinal < columnsAxis.getPositions().size();
                ++columnOrdinal) {
                coords[0] = columnOrdinal;
                final Cell cell = result.getCell(coords);
                rowValues.add(cell.getValue());
            }

            rows.add(rowValues);
        }

        // Print the headings and rows.
        pw.println("Headings:");
        int columnOrdinal = -1;
        for (String heading : headings) {
            pw.println("\theading #" + (++columnOrdinal) + ": " + heading);
        }
        pw.println();

        pw.println("Rows:");
        rowOrdinal = -1;
        for (List<Object> rowValues : rows) {
            pw.println("\trow #" + (++rowOrdinal) + ":");
            columnOrdinal = -1;
            for (Object rowValue : rowValues) {
                pw.println("\t\tvalue #" + (++columnOrdinal) + ": " + rowValue);
            }
        }
    }

    private static void outputFlattened(Result result, PrintWriter pw) {
        final Axis[] axes = result.getAxes();
        List<List<Object>> rows = new ArrayList<List<Object>>();
        List<String> headings = new ArrayList<String>();

        // Compute headings. Each heading is a hierarchy name. If there are say
        // 2 members on the columns, and 3 members on the rows axis, then there
        // will be 5 headings.
        for (Axis axis : axes) {
            final List<Position> positions = axis.getPositions();
            if (positions.isEmpty()) {
                // Result set is empty. There is no data to print, and we cannot
                // even deduce column headings.
                return;
            }
            for (Member member : positions.get(0)) {
                headings.add(member.getHierarchy().getUniqueName());
            }
        }

        int[] coords = new int[axes.length];
        outputFlattenedRecurse(result, rows, new ArrayList<Object>(), coords, 0);

        // Print the headings and rows.
        pw.println("Headings:");
        int columnOrdinal = -1;
        for (String heading : headings) {
            pw.println("\theading #" + (++columnOrdinal) + ": " + heading);
        }
        pw.println();

        int rowOrdinal = -1;
        for (List<Object> rowValues : rows) {
            pw.println("Row #" + (++rowOrdinal) + ":");
            columnOrdinal = -1;
            for (Object rowValue : rowValues) {
                pw.println("\tvalue #" + (++columnOrdinal) + ": " + rowValue);
            }
        }
    }

    private static void outputFlattenedRecurse(
        Result result,
        List<List<Object>> rows,
        List<Object> rowValues,
        int[] coords,
        int axisOrdinal)
    {
        final Axis[] axes = result.getAxes();
        if (axisOrdinal == axes.length) {
            final Cell cell = result.getCell(coords);
            // Output the raw (unformatted) value of the cell.
            // NOTE: We could output other properties of the cell here, such as its
            // formatted value, too.
            rowValues.add(cell.getValue());

            // Add a copy of the completed row to the list of rows.
            rows.add(new ArrayList<Object>(rowValues));
        } else {
            final Axis axis = axes[axisOrdinal];
            int k = -1;
            int saveLength = rowValues.size();
            for (Position position : axis.getPositions()) {
                coords[axisOrdinal] = ++k;
                for (Member member : position) {
                    rowValues.add(member.getUniqueName());
                }
                outputFlattenedRecurse(
                    result, rows, rowValues, coords, axisOrdinal + 1);
                while (rowValues.size() > saveLength) {
                    rowValues.remove(rowValues.size() - 1);
                }
            }
        }
    }
}

// End Foo.java
