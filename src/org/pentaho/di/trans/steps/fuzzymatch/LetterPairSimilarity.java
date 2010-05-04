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

package org.pentaho.di.trans.steps.fuzzymatch;

import java.util.ArrayList;

import org.pentaho.di.core.Const;

public class LetterPairSimilarity
{

	/** @return an array of adjacent letter pairs contained in the input string */

	   private static String[] letterPairs(String str) {
	       int numPairs = str.length()-1;
	       if(str.length()==0) numPairs=0;
	       String[] pairs = new String[numPairs];
	       for (int i=0; i<numPairs; i++) {
	           pairs[i] = str.substring(i,i+2);
	       }
	       return pairs;
	   }

	   
	/** @return an ArrayList of 2-character Strings. */

	   private static ArrayList<String> wordLetterPairs(String str) {
	       ArrayList<String> allPairs = new ArrayList<String>();
	       // Tokenize the string and put the tokens/words into an array 
	       String[] words = str.split("\\s");
	       // For each word
	       for (int w=0; w < words.length; w++) {
	           // Find the pairs of characters
	           String[] pairsInWord = letterPairs(words[w]);
	           for (int p=0; p < pairsInWord.length; p++) {
	               allPairs.add(pairsInWord[p]);
	           }
	       }
	       return allPairs;
	   }


	/** @return lexical similarity value in the range [0,1] */
	
	public static double getSimiliarity(String str1, String str2) {
		if(Const.isEmpty(str1) && Const.isEmpty(str2)) return new Double(1);
	    ArrayList<String> pairs1 = wordLetterPairs(str1.toUpperCase());
	    ArrayList<String> pairs2 = wordLetterPairs(str2.toUpperCase());
	    int intersection = 0;
	    int union = pairs1.size() + pairs2.size();
	    
	    for (int i=0; i<pairs1.size(); i++) {
	        Object pair1=pairs1.get(i);
	        for(int j=0; j<pairs2.size(); j++) {
	            Object pair2=pairs2.get(j);
	            if (pair1.equals(pair2)) {
	                intersection++;
	                pairs2.remove(j);
	                break;
	            }
	        }
	    }
	    return (2.0*intersection)/union;
	}
}