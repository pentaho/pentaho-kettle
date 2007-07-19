package org.pentaho.di.core.gui;

public interface TextFileInputFieldInterface extends Comparable<TextFileInputFieldInterface>
{
    public int getPosition();
    public int getLength();
    public String getName();
    public void setLength(int i);
    
    public TextFileInputFieldInterface createNewInstance(String newFieldname, int x, int newlength);
}
