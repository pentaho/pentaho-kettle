package org.pentaho.di.www;

public class SlaveSequenceValueRange {
  private long value;
  private long increment;
  
  public SlaveSequenceValueRange() {
  }
  
  /**
   * @param value
   * @param increment
   */
  public SlaveSequenceValueRange(long value, long increment) {
    this.value = value;
    this.increment = increment;
  }
  
  public long getMaximum() {
    return value+increment;
  }
  
  /**
   * @return the value
   */
  public long getValue() {
    return value;
  }
  /**
   * @param value the value to set
   */
  public void setValue(long value) {
    this.value = value;
  }
  /**
   * @return the increment
   */
  public long getIncrement() {
    return increment;
  }
  /**
   * @param increment the increment to set
   */
  public void setIncrement(long increment) {
    this.increment = increment;
  }
  
  
}
