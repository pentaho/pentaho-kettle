package org.pentaho.di.trans.steps.metainject;

public class TargetStepAttribute {

  private String stepname;
  private String attributeKey;
  private boolean detail;

  /**
   * @param stepname
   * @param attributeKey
   */
  public TargetStepAttribute(String stepname, String attributeKey, boolean detail) {
    this.stepname = stepname;
    this.attributeKey = attributeKey;
    this.detail = detail;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TargetStepAttribute)) return false;
    if (obj==this) return true;
    
    TargetStepAttribute target = (TargetStepAttribute) obj;
    return stepname.equalsIgnoreCase(target.getStepname()) && attributeKey.equals(target.getAttributeKey());
  }
  
  @Override
  public int hashCode() {
    return stepname.hashCode() ^ attributeKey.hashCode();
  }
  
  /**
   * @return the stepname
   */
  public String getStepname() {
    return stepname;
  }

  /**
   * @param stepname
   *          the stepname to set
   */
  public void setStepname(String stepname) {
    this.stepname = stepname;
  }

  /**
   * @return the attributeKey
   */
  public String getAttributeKey() {
    return attributeKey;
  }

  /**
   * @param attributeKey
   *          the attributeKey to set
   */
  public void setAttributeKey(String attributeKey) {
    this.attributeKey = attributeKey;
  }

  public void setDetail(boolean detail) {
    this.detail = detail;
  }

  public boolean isDetail() {
    return detail;
  }

}
