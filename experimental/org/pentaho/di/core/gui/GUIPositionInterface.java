package org.pentaho.di.core.gui;


public interface GUIPositionInterface
{
    public Point getLocation();
    public void setLocation(Point p);
    public void setLocation(int x, int y);
    
    public boolean isSelected();
    public void setSelected(boolean selected);
}
