/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.trans.steps.textfileinput;

/**
 * Processor of Filters. Kind of inversion principle, and to make
 * unit testing easier.
 * 
 * @author Sven Boden
 */
public class TextFileFilterProcessor {

    /** The filters to process */
    private TextFileFilter filters[];
    private boolean stopProcessing;

    /**
     * @param filters        The filters to process
     */
    public TextFileFilterProcessor(TextFileFilter filters[]) {
        this.filters = filters;
        this.stopProcessing = false;

        if (filters.length == 0) {
            // This makes processing faster in case there are no filters.
            filters = null;
        }
    }

    public boolean doFilters(String line) {
        if (filters == null) {
            return true;
        }

        boolean filterOK = true;  // if false: skip this row
        boolean positiveMode = false;
        boolean positiveMatchFound = false;

        // If we have at least one positive filter, we enter positiveMode
        // Negative filters will always take precendence, meaning that the line
        // is skipped if one of them is found
        
        for (int f = 0; f < filters.length && filterOK; f++) {
            TextFileFilter filter = filters[f];
            if (filter.isFilterPositive())
                positiveMode = true;

            if (filter.getFilterString() != null && filter.getFilterString().length() > 0) {
                int from = filter.getFilterPosition();
                if (from >= 0) {
                    int to = from + filter.getFilterString().length();
                    if (line.length() >= from && line.length() >= to) {
                        String sub = line.substring(filter.getFilterPosition(), to);
                        if (sub.equalsIgnoreCase(filter.getFilterString())) {
                            if (filter.isFilterPositive()) {
                                positiveMatchFound = true;
                            } else {
                                filterOK = false; // skip this one!
                            }
                        }
                    }
                } else // anywhere on the line
                {
                    int idx = line.indexOf(filter.getFilterString());
                    if (idx >= 0) {
                        if (filter.isFilterPositive()) {
                            positiveMatchFound = true;
                        } else {
                            filterOK = false; // skip this one!
                        }
                    }
                }

                if (!filterOK) {
                    boolean isFilterLastLine = filter.isFilterLastLine();
                    if (isFilterLastLine) {
                        stopProcessing = true;
                    }
                }
            }
        }

        // Positive mode and no match found? Discard the line
        if (filterOK && positiveMode && !positiveMatchFound)
            filterOK = false;

        return filterOK;
    }

    /**
     * Was processing requested to be stopped. Can only be true when doFilters was
     * false.
     * 
     * @return == true: processing should stop, == false: processing should continue.
     */
    public boolean isStopProcessing() {
        return stopProcessing;
    }
}