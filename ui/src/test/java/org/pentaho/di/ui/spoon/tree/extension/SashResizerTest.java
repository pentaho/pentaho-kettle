package org.pentaho.di.ui.spoon.tree.extension;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.stream.IntStream;

import org.junit.Test;

public class SashResizerTest {

  @Test
  public void test() {
    SashResizer resizer =
        new SashResizer( new int[] { 200, 200, 200 } );
    int[] res = resizer.weights();
    assertEquals( 100, IntStream.of( res ).sum() );
    // should be roughly 1/3, last may be bigger
    assertArrayEquals( new int[] { 33, 33, 34 }, res );

    res = resizer.disable( 1, res );
    assertEquals( 100, IntStream.of( res ).sum() );
    // should be roughly 50/50, last may be bigger
    assertArrayEquals( new int[] { 49, 0, 51 }, res );

    res = resizer.disable( 2, res );
    assertEquals( 100, IntStream.of( res ).sum() );
    assertArrayEquals( new int[] {100, 0, 0}, res );

    res = resizer.enable( 1, res );
    assertEquals( 100, IntStream.of( res ).sum() );
    assertArrayEquals( new int[] { 67, 33, 0 }, res );

    res = resizer.enable( 2, res );
    assertEquals( 100, IntStream.of( res ).sum() );
    assertArrayEquals( new int[] { 32, 16, 52 }, res );
  }

  @Test
  public void testDual() {
    SashResizer resizer = new SashResizer( new int[] { 200, 200 } );
    int[] res = resizer.weights();
    assertArrayEquals( new int[] { 50, 50 }, res );
    res = resizer.disable(0, res);
    assertArrayEquals( new int[] { 0, 100 }, res );
    res = resizer.enable(0, res);
    assertArrayEquals( new int[] { 50, 50 }, res );
  }

  @Test
  public void test4() {
    SashResizer resizer = new SashResizer( new int[] { 0, 500, 500, 500 } );
    int[] res = resizer.weights();
    assertArrayEquals( new int[] { 0, 33, 33, 34 }, res );
    res = resizer.disable(2, res);
    assertArrayEquals( new int[] { 0, 49, 0, 51 }, res );
    res = resizer.enable(2, res);
    assertArrayEquals( new int[] { 0, 32, 33, 35 }, res );
  }
}
