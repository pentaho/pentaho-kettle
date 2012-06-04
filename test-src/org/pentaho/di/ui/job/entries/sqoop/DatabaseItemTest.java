package org.pentaho.di.ui.job.entries.sqoop;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DatabaseItemTest {

  @Test
  public void instantiate_name() {
    String name = "name";
    DatabaseItem item = new DatabaseItem(name);

    assertEquals(name, item.getName());
    assertEquals(name, item.getDisplayName());
  }

  @Test
  public void instantiate_displayName() {
    String name = "name";
    String displayName = "display name";
    DatabaseItem item = new DatabaseItem(name, displayName);

    assertEquals(name, item.getName());
    assertEquals(displayName, item.getDisplayName());
  }

  @Test
  public void equals() {
    DatabaseItem item1 = new DatabaseItem("test");
    DatabaseItem item2 = new DatabaseItem("test");
    DatabaseItem item3 = new DatabaseItem("testing");

    assertFalse(item1.equals(null));
    assertFalse(item1.equals(item3));

    assertTrue(item1.equals(item1));
    assertTrue(item1.equals(item2));
  }

  @Test
  public void testHashCode() {
    String name = "test";
    DatabaseItem item1 = new DatabaseItem(name);

    assertEquals(name.hashCode(), item1.hashCode());
  }

  @Test
  public void testToString() {
    String name = "test";
    DatabaseItem item = new DatabaseItem(name);

    assertEquals(name, item.toString());
  }
}
