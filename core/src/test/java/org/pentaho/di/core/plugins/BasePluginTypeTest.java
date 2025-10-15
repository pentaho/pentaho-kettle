/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.core.plugins;

import org.junit.jupiter.api.Test;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.KettleLogStore;

import java.io.*;
import java.util.*;
import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BasePluginTypeTest {

  static class DummyPluginType extends BasePluginType {
    DummyPluginType() { super(DummyAnnotation.class, "dummyId", "dummyName"); }
    protected void registerXmlPlugins() {}
    protected String extractID(Annotation a) { return "id"; }
    protected String extractName(Annotation a) { return "name"; }
    protected String extractDesc(Annotation a) { return "desc"; }
    protected String extractCategory(Annotation a) { return "cat"; }
    protected String extractImageFile(Annotation a) { return "img"; }
    protected boolean extractSeparateClassLoader(Annotation a) { return false; }
    protected String extractI18nPackageName(Annotation a) { return null; }
    protected String extractDocumentationUrl(Annotation a) { return null; }
    protected String extractSuggestion(Annotation a) { return null; }
    protected String extractCasesUrl(Annotation a) { return null; }
    protected String extractForumUrl(Annotation a) { return null; }
    protected void addExtraClasses(Map<Class<?>, String> classMap, Class<?> clazz, Annotation annotation) {}
    protected String processClasspath(String baseFolder, String classpath) {
      Properties props = loadPluginProperties(baseFolder);
      String result = classpath;
      for (String name : props.stringPropertyNames()) {
        result = result.replace("${" + name + "}", props.getProperty(name));
      }
      return result;
    }

    protected List<String> getVariables(String classpath) {
      List<String> vars = new ArrayList<>();
      int idx = 0;
      while ((idx = classpath.indexOf("${", idx)) != -1) {
        int end = classpath.indexOf("}", idx);
        if (end != -1) {
          vars.add(classpath.substring(idx + 2, end));
          idx = end + 1;
        } else {
          break;
        }
      }
      return vars;
    }
  }
  @interface DummyAnnotation {}

  @Test
  void testProcessClasspathAndGetVariables() throws Exception {
    var bpt = spy(new DummyPluginType());
    var classpath = "${VAR1}:${VAR2}";
    var props = new Properties();
    props.setProperty("VAR1", "lib1");
    props.setProperty("VAR2", "lib2");
    doReturn(props).when(bpt).loadPluginProperties(anyString());
    var result = bpt.getClass().getDeclaredMethod("processClasspath", String.class, String.class)
      .invoke(bpt, "/tmp", classpath).toString();
    assertEquals("lib1:lib2", result);

    var vars = (List<String>) bpt.getClass().getDeclaredMethod("getVariables", String.class)
      .invoke(bpt, classpath);
    assertEquals(List.of("VAR1", "VAR2"), vars);
  }

  @Test
  void testLoadPluginProperties() throws Exception {
    var bpt = spy(new DummyPluginType());
    var tempDir = new File(System.getProperty("java.io.tmpdir"), "pluginTest");
    tempDir.mkdir();
    var propFile = new File(tempDir, "plugin.properties");
    try (var fos = new FileOutputStream(propFile)) {
      var p = new Properties();
      p.setProperty("foo", "bar");
      p.store(fos, null);
    }
    var loaded = (Properties) BasePluginType.class.getDeclaredMethod("loadPluginProperties", String.class)
      .invoke(bpt, tempDir.getAbsolutePath());
    assertEquals("bar", loaded.getProperty("foo"));
    propFile.delete();
    tempDir.delete();
  }

  @Test
  void testGetCodedTranslation() {
    assertNull(BasePluginType.getCodedTranslation(null));
    assertEquals("!SomeKey!", BasePluginType.getCodedTranslation("i18n:org.pentaho.di.core.plugins:SomeKey"));
    assertEquals("plain", BasePluginType.getCodedTranslation("plain"));
  }

  @Test
  void testGetTranslation() {
    assertNull(BasePluginType.getTranslation(null, null, null, DummyPluginType.class));
    assertEquals("plain", BasePluginType.getTranslation("plain", null, null, DummyPluginType.class));
    assertEquals("!SomeKey!", BasePluginType.getTranslation("i18n:org.pentaho.di.core.plugins:SomeKey", "org.pentaho.di.core.plugins", null, DummyPluginType.class));
  }

  @Test
  void testGetAlternativeTranslation() throws Exception {
    var bpt = spy(new DummyPluginType());
    var map = Map.of("en_us", "alt");
    var result = (String) BasePluginType.class.getDeclaredMethod("getAlternativeTranslation", String.class, Map.class)
      .invoke(bpt, "plain", map);
    assertEquals("alt", result);
    var result2 = (String) BasePluginType.class.getDeclaredMethod("getAlternativeTranslation", String.class, Map.class)
      .invoke(bpt, "", map);
    assertNull(result2);
  }

  @Test
  void testFindPluginXmlFilesAndFindPluginFiles() {
    var bpt = spy(new DummyPluginType());
    var files = bpt.findPluginXmlFiles("ram:/basePluginTypeTest/");
    assertNotNull(files);
    var files2 = bpt.findPluginFiles("ram:/basePluginTypeTest/", ".*plugin\\.xml$");
    assertNotNull(files2);
  }

  @Test
  void testSettersAndGetters() {
    var bpt = spy(new DummyPluginType());
    bpt.setId("testId");
    assertEquals("testId", bpt.getId());
    bpt.setName("testName");
    assertEquals("testName", bpt.getName());
    var folders = new ArrayList<PluginFolderInterface>();
    bpt.setPluginFolders(folders);
    assertEquals(folders, bpt.getPluginFolders());
  }

  @Test
  void testToString() {
    var bpt = new DummyPluginType();
    assertEquals("dummyName(dummyId)", bpt.toString());
  }

  @Test
  void testAddObjectTypeAndGetAdditionalRuntimeObjectTypes() {
    var bpt = new DummyPluginType();
    bpt.addObjectType(String.class, "xmlNode");
    assertEquals(Map.of(String.class, "xmlNode"), bpt.getAdditionalRuntimeObjectTypes());
  }

  // Java
  @Test
  void testRegisterNativesThrowsExceptionIfFileNotFound() throws Exception {
    DummyPluginType bpt = new DummyPluginType() {
      @Override
      protected InputStream getResAsStreamExternal(String s) { return null; }
      @Override
      protected InputStream getFileInputStreamExternal(String s) { return null; }
      @Override
      protected boolean isReturn() { return false; }
    };
    var ex = assertThrows(KettlePluginException.class, bpt::registerNatives);
    assertTrue(ex.getMessage().contains("Unable to find native plugins definition file"));
  }

  @Test
  void testRegisterNativesReturnIfFileNotFound() {
    DummyPluginType bpt = new DummyPluginType() {
      @Override
      protected InputStream getResAsStreamExternal(String s) { return null; }
      @Override
      protected InputStream getFileInputStreamExternal(String s) { return null; }
      @Override
      protected boolean isReturn() { return true; }
    };
    assertDoesNotThrow(bpt::registerNatives);
  }

  @Test
  void testRegisterNativesWithAlternativeFile() throws Exception {
    var bpt = spy(new DummyPluginType());
    var nameField = BasePluginType.class.getDeclaredField("name");
    nameField.setAccessible(true);
    nameField.set(bpt, "dummyName");
    doReturn("alt.xml").when(bpt).getAlternativePluginFile();
    doReturn("alt.xml").when(bpt).getPropertyExternal(anyString(), any());
    doReturn(null).when(bpt).getResAsStreamExternal(anyString());
    doReturn(null).when(bpt).getFileInputStreamExternal(anyString());
    doReturn(false).when(bpt).isReturn();
    var ex = assertThrows(KettlePluginException.class, bpt::registerNatives);
    assertTrue(ex.getMessage().contains("Unable to find native plugins definition file"));
  }

  @Test
  void testRegisterPluginsThrowsKettleXMLException() throws Exception {
    DummyPluginType bpt = new DummyPluginType() {
      @Override
      protected InputStream getResAsStreamExternal(String s) {
        return new ByteArrayInputStream("<bad>".getBytes());
      }
      @Override
      protected boolean isReturn() { return false; }
      @Override
      protected void registerPlugins(InputStream is) throws KettleXMLException {
        throw new KettleXMLException("bad xml");
      }
    };
    var ex = assertThrows(KettlePluginException.class, bpt::registerNatives);
    assertTrue(ex.getMessage().contains("Unable to read the kettle XML config file"));
  }

  @Test
  void testRegisterPluginFromXmlResourceThrowsException() throws Exception {
    DummyPluginType bpt = new DummyPluginType();
    var node = mock(org.w3c.dom.Node.class);
    var ex = assertThrows(KettlePluginException.class, () -> {
      // Force XMLHandler.getTagAttribute to throw
      bpt.registerPluginFromXmlResource(node, null, DummyPluginType.class, true, null);
    });
    assertTrue(ex.getMessage().contains("Unable to read plugin xml"));
  }

  @Test
  void testFindPluginFilesWithInvalidFolder() {
    DummyPluginType bpt = new DummyPluginType();
    var files = bpt.findPluginFiles("/nonexistent/folder", ".*plugin\\.xml$");
    assertTrue(files.isEmpty());
    var xmlFiles = bpt.findPluginXmlFiles("/nonexistent/folder");
    assertTrue(xmlFiles.isEmpty());
  }

  @Test
  void testGetCodedTranslationMalformed() {
    assertEquals("i18n:badformat", BasePluginType.getCodedTranslation("i18n:badformat"));
  }

  @Test
  void testGetTranslationMalformed() {
    assertEquals("i18n:badformat", BasePluginType.getTranslation("i18n:badformat", null, null, DummyPluginType.class));
  }

  @Test
  void testGetAlternativeTranslationNoMatch() throws Exception {
    DummyPluginType bpt = new DummyPluginType();
    Map<String, String> map = Map.of("fr_fr", "alt");
    String result = (String) BasePluginType.class.getDeclaredMethod("getAlternativeTranslation", String.class, Map.class)
      .invoke(bpt, "plain", map);
    assertEquals("plain", result);
  }

  @Test
  void testProcessClasspathWithMissingVariables() throws Exception {
    DummyPluginType bpt = spy(new DummyPluginType());
    Properties props = new Properties();
    doReturn(props).when(bpt).loadPluginProperties(anyString());
    var method = BasePluginType.class.getDeclaredMethod("processClasspath", String.class, String.class);
    method.setAccessible(true); // <-- Add this line
    String result = (String) method.invoke(bpt, "/tmp", "${MISSING}");
    assertEquals("", result);
  }

  @Test
  void testLoadPluginPropertiesMissingFile() throws Exception {
    KettleLogStore.init(); // Ensure logging is initialized
    DummyPluginType bpt = new DummyPluginType();
    try {
      Properties props = (Properties) BasePluginType.class.getDeclaredMethod("loadPluginProperties", String.class)
        .invoke(bpt, "/nonexistent/folder");
      assertNull(props);
    } catch (Exception e) {
      // Acceptable if an exception is thrown due to missing file
      assertTrue(e.getCause() instanceof FileNotFoundException
        || e.getCause() instanceof IOException
        || e.getCause() instanceof RuntimeException);
    }
  }

  @Test
  void testAddObjectTypeMultiple() {
    DummyPluginType bpt = new DummyPluginType();
    bpt.addObjectType(String.class, "xmlNode1");
    bpt.addObjectType(Integer.class, "xmlNode2");
    Map<Class<?>, String> expected = new HashMap<>();
    expected.put(String.class, "xmlNode1");
    expected.put(Integer.class, "xmlNode2");
    assertEquals(expected, bpt.getAdditionalRuntimeObjectTypes());
  }

  @Test
  void testSetTransverseLibDirs() throws Exception {
    DummyPluginType bpt = new DummyPluginType();
    bpt.setTransverseLibDirs(true);
    var field = BasePluginType.class.getDeclaredField("searchLibDir");
    field.setAccessible(true);
    assertTrue(field.getBoolean(bpt));
  }

  @Test
  void testToStringWithNulls() throws Exception {
    DummyPluginType bpt = new DummyPluginType();
    var nameField = BasePluginType.class.getDeclaredField("name");
    var idField = BasePluginType.class.getDeclaredField("id");
    nameField.setAccessible(true);
    idField.setAccessible(true);
    nameField.set(bpt, null);
    idField.set(bpt, null);
    assertEquals("null(null)", bpt.toString());
  }
  // Java
  @Test
  void testFindAnnotatedClassFilesWithInvalidFolder() throws Exception {
    DummyPluginType bpt = new DummyPluginType();
    var method = BasePluginType.class.getDeclaredMethod("findAnnotatedClassFiles", String.class, Class.class);
    method.setAccessible(true);
    // Pass a non-existent folder and a dummy annotation
    List<?> result = (List<?>) method.invoke(bpt, "/nonexistent/folder", DummyAnnotation.class);
    assertTrue(result.isEmpty());
  }

  @Test
  void testCreateUrlClassLoaderWithInvalidFiles() throws Exception {
    DummyPluginType bpt = new DummyPluginType();
    var method = BasePluginType.class.getDeclaredMethod("createUrlClassLoader", List.class, ClassLoader.class);
    method.setAccessible(true);
    // Pass an empty list and null parent
    ClassLoader loader = (ClassLoader) method.invoke(bpt, Collections.emptyList(), null);
    assertNotNull(loader);
  }
}
