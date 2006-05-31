package be.ibridge.kettle.trans;

public class StepPluginMeta {

    protected Class className;
    protected String id;
    protected String longDesc;
    protected String tooltipDesc;
    protected String imageFileName;
    protected String category;
    
    
    public StepPluginMeta(Class className, String id, String longDesc, String tooltipDesc, String imageFileName, String category) {
        this.className = className;
        this.id = id;
        this.longDesc = longDesc;
        this.tooltipDesc = tooltipDesc;
        this.imageFileName = imageFileName;
        this.category = category;
    }
    
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public Class getClassName() {
        return className;
    }
    public void setClassName(Class className) {
        this.className = className;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageFileName() {
        return imageFileName;
    }
    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }
    public String getLongDesc() {
        return longDesc;
    }
    public void setLongDesc(String longDesc) {
        this.longDesc = longDesc;
    }
    public String getTooltipDesc() {
        return tooltipDesc;
    }
    public void setTooltipDesc(String tooltipDesc) {
        this.tooltipDesc = tooltipDesc;
    }
    
    
}
