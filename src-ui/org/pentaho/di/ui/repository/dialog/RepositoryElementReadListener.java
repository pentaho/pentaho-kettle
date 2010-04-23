package org.pentaho.di.ui.repository.dialog;

import org.xml.sax.SAXParseException;

public interface RepositoryElementReadListener {
  public boolean transformationElementRead(String xml);
  public boolean jobElementRead(String xml);
  public void fatalXmlErrorEncountered(SAXParseException e);
}
