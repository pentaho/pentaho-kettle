package org.pentaho.di.trans.steps.olapinput.olap4jhelper;

public class DataCell extends AbstractBaseCell  {


    private Number rawNumber = null;

    private MemberCell parentColMember = null;


    /**
     * 
     * Blank constructor for serialization purposes, don't use it.
     * 
     */
    public DataCell() {
        super();
    }

    /**
     * Construct a Data Cell containing olap data.
     * 
     * @param b
     * @param c
     */
    public DataCell(final boolean right, final boolean sameAsPrev) {
        super();
        this.right = right;
        this.sameAsPrev = sameAsPrev;
    }
    public MemberCell getParentColMember() {
        return parentColMember;
    }

    public void setParentColMember(final MemberCell parentColMember) {
        this.parentColMember = parentColMember;
    }

    public MemberCell getParentRowMember() {
        return parentRowMember;
    }

    public void setParentRowMember(final MemberCell parentRowMember) {
        this.parentRowMember = parentRowMember;
    }

    private MemberCell parentRowMember = null;

    public Number getRawNumber() {
        return rawNumber;
    }

    public void setRawNumber(final Number rawNumber) {
        this.rawNumber = rawNumber;
    }


}
