package org.pentaho.di.core.util;

import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Utility class to hold the result of a set of string evaluations: a valid conversion metadata object (with data type, trim options, etc) and the minimum and maximum value encountered. 

 * @author matt
 *
 */
public class StringEvaluationResult {
	
	private ValueMetaInterface conversionMeta;
	private Object min;
	private Object max;
	private int nrNull;
	private int nrSuccesses;
	private int nrFailures;

	public StringEvaluationResult(ValueMetaInterface conversionMeta) {
		this.conversionMeta = conversionMeta;
		this.nrNull=0;
	}
	
	@Override
	public String toString() {
		return conversionMeta.toStringMeta();
	}

	/**
	 * @return the conversionMeta
	 */
	public ValueMetaInterface getConversionMeta() {
		return conversionMeta;
	}

	/**
	 * @param conversionMeta the conversionMeta to set
	 */
	public void setConversionMeta(ValueMetaInterface conversionMeta) {
		this.conversionMeta = conversionMeta;
	}

	/**
	 * @return the min
	 */
	public Object getMin() {
		return min;
	}

	/**
	 * @param min the min to set
	 */
	public void setMin(Object min) {
		this.min = min;
	}

	/**
	 * @return the max
	 */
	public Object getMax() {
		return max;
	}

	/**
	 * @param max the max to set
	 */
	public void setMax(Object max) {
		this.max = max;
	}

	/**
	 * @return The number of null values encountered 
	 */
	public int getNrNull() {
		return nrNull;
	}
	
	/**
	 * @param nrNull Set the number of null values to set
	 */
	public void setNrNull(int nrNull) {
		this.nrNull = nrNull;
	}
	
	/**
	 * Increment the number of null values encountered.
	 */
	public void incrementNrNull() {
		nrNull++;
	}
	
	/**
	 * Increment the number of successes by one.
	 */
	public void incrementSuccesses() {
		nrSuccesses++;
	}
	
	/**
	 * Increment the number of failures by one.
	 */
	public void incrementFailures() {
		nrFailures++;
	}

	/**
	 * @return the nrSuccesses
	 */
	public int getNrSuccesses() {
		return nrSuccesses;
	}

	/**
	 * @param nrSuccesses the nrSuccesses to set
	 */
	public void setNrSuccesses(int nrSuccesses) {
		this.nrSuccesses = nrSuccesses;
	}

	/**
	 * @return the nrFailures
	 */
	public int getNrFailures() {
		return nrFailures;
	}

	/**
	 * @param nrFailures the nrFailures to set
	 */
	public void setNrFailures(int nrFailures) {
		this.nrFailures = nrFailures;
	}
	
}