package org.pentaho.di.trans.steps.olapinput.olap4jhelper;

public class CellDataSet  {

    private int width;

    private int height;

    private AbstractBaseCell[][] cellSetHeader;

    private AbstractBaseCell[][] cellSetBody;

    private int offset;

    public CellDataSet() {
        super();
    }

    public CellDataSet(final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    public AbstractBaseCell[][] getCellSetHeaders() {
        return cellSetHeader;
    }

    public void setCellSetHeaders(final AbstractBaseCell[][] cellSet) {
        this.cellSetHeader = cellSet;
    }

    public AbstractBaseCell[][] getCellSetBody() {
        return cellSetBody;
    }

    public void setCellSetBody(final AbstractBaseCell[][] cellSet) {
        this.cellSetBody = cellSet;
    }

    public void setOffset(final int offset) {
        // TODO Auto-generated method stub
        this.offset = offset;
    }

    public int getOffset() {
        return offset;

    }

    /**
     * @param width
     *            the width to set
     */
    public void setWidth(final int width) {
        this.width = width;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param height
     *            the height to set
     */
    public void setHeight(final int height) {
        this.height = height;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }
}
