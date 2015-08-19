package org.pentaho.di.core.gui;

public interface NewFileInputFieldInterface extends Comparable<NewFileInputFieldInterface> {
  public int getPosition();

  public int getLength();

  public String getName();

  public void setLength( int i );

  public NewFileInputFieldInterface createNewInstance( String newFieldname, int x, int newlength );
}
