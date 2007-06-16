package org.pentaho.di.shared;

public interface SharedObjectInterface
{
    public void setShared(boolean shared);
    public boolean isShared();
    
    public String getName();
    public String getXML();
}
