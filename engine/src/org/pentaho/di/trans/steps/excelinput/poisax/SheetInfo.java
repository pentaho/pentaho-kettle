package org.pentaho.di.trans.steps.excelinput.poisax;

public class SheetInfo {
  private String name;
  private int    nrRows;

  /**
   * @param name
   * @param nrRows
   */
  private SheetInfo(String name, int nrRows) {
    this.name = name;
    this.nrRows = nrRows;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the nrRows
   */
  public int getNrRows() {
    return nrRows;
  }

  /**
   * @param nrRows
   *          the nrRows to set
   */
  public void setNrRows(int nrRows) {
    this.nrRows = nrRows;
  }

}
