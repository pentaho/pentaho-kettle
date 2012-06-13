package org.pentaho.di.core.plugins;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.scannotation.AnnotationDB;

public class JarFileCache {

  private static JarFileCache cache;
  
  Map<FileObject, AnnotationDB> annotationMap; 
  
  private JarFileCache() {
    annotationMap = new HashMap<FileObject, AnnotationDB>();
  }
  
  public static JarFileCache getInstance() {
    if (cache==null) {
      cache = new JarFileCache();
    }
    return cache;
  }
  
  public void addEntry(FileObject fileObject, AnnotationDB annotationDb) {
    annotationMap.put(fileObject, annotationDb);
  }
  
  public AnnotationDB getEntry(FileObject fileObject) {
    return annotationMap.get(fileObject);
  }
  
  public void clear() {
    annotationMap.clear();
  }
}
