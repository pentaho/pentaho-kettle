package org.pentaho.di.core.hash;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleValueException;

/**
 * 
 * Test class for the basic functionality of LongHashIndex.
 * 
 */
public class LongHashIndexTest {

  @Test( timeout = 2000 )
  public void testNoEndlessLoop() throws KettleValueException {
    long[] inputData = {
      3034, 2085, 1912, 9367, 8442, 783, 2839, 8610, 5152, 7388, 7511, 1251, 3043, 3889, 9543, 9353, 2241, 5416,
      2127, 3513, 2171, 8633, 5594, 7228, 2225, 581, 6524, 7171, 5928, 5710, 804, 9535, 3334, 3383, 3113, 248,
      4801, 8927, 1815, 2382, 7043, 2962, 8362, 8353, 5883, 4489, 4554, 218, 50, 200, 9519, 6615, 8264, 5797, 3586,
      3185, 299, 2334, 1944, 4953, 9829, 5852, 8424, 1046, 918, 3110, 3145, 7828, 2672, 3890, 8661, 8405, 2653,
      1514, 856, 8381, 523, 8343, 6566, 9127, 9679, 8667, 3952, 8310, 164, 8947, 822, 778, 7415, 6463, 6576, 849,
      4308, 7044, 7549, 6609, 2494, 1594, 4664, 7937, 2788, 6395, 6875, 6480, 7568, 6789, 2864, 5432, 4163, 3164,
      9755, 4898, 5639, 5425, 3291, 6811, 149, 8243, 1423, 3869, 1158, 590, 6376, 5665, 629, 7425, 4414, 9882,
      6844, 5941, 4815, 7917, 7673, 4153, 4266, 6673, 9082, 8574, 6424, 8245, 8460, 9875, 6946, 9506, 7479, 2138,
      2091, 4336, 5657, 77, 2269, 3682, 7421, 2671, 1413, 4430, 2762, 1174, 9153, 4963, 1959, 3165, 7703, 7539,
      9833, 5661, 1189, 839, 1310, 382, 9538, 5953, 2448, 2368, 5385, 1847, 5919, 6954, 9685, 7568, 6915, 4009,
      3990, 4742, 5196, 126, 693, 5448, 2405, 1853, 8109, 8198, 6761, 350, 4825, 3288, 9778, 7801, 7950, 2754,
      4137, 5682, 6611, 858, 9058, 9616, 5261, 5946, 7339, 5296, 1818, 2591, 2949, 147, 4511, 2431, 7376, 8260,
      5719, 264, 5649, 1671, 6014, 9714, 5349, 9824, 8063, 9646, 1203, 5800, 9024, 6730, 1645, 7332, 9524, 4672,
      2984, 5491, 8, 8492, 6134, 8895, 9105, 6947, 4917, 9552, 3332, 8117, 3949, 9464, 6730, 7707, 5456, 288, 4462,
      6444, 9706, 4575, 8890, 7367, 676, 2974, 2721, 2209, 9692, 6968, 9708, 6959, 2382, 7975, 9866, 2260, 7707,
      8916, 1811, 5375, 2490, 7478, 1915, 5538, 4217, 1125, 14, 4033, 8474, 8433, 4315, 4426, 9201, 3205 };

    LongHashIndex index = new LongHashIndex();
    for ( long currentElement : inputData ) {
      index.put( currentElement, currentElement );
    }
  }

  @Test
  public void testPutElements() throws KettleValueException {
    LongHashIndex index = new LongHashIndex();
    index.put( 1L, 1L );
    index.put( 2L, 2L );
    assertThat( "Some elements were not added.", index.getSize(), equalTo( 2 ) );
  }

  @Test
  public void testPutElements_with_same_index_point() throws KettleValueException {
    LongHashIndex index = new LongHashIndex();
    long firstVariable = 3513L;
    long secondVariableWithSameIndexPoint = 8633L;
    index.put( firstVariable, 1L );
    index.put( secondVariableWithSameIndexPoint, 2L );
    assertThat( "Element has uncorrect value.", index.get( firstVariable ), equalTo( 1L ) );
    assertThat( "Element has uncorrect value.", index.get( secondVariableWithSameIndexPoint ), equalTo( 2L ) );
  }

  @Test
  public void testPutElements_when_initial_size_is_less_than_elements_count() throws KettleValueException {
    LongHashIndex index = new LongHashIndex( 0 );
    index.put( 1L, 1L );
    index.put( 2L, 2L );
    assertThat( "Some elements were not added.", index.getSize(), equalTo( 2 ) );
  }

  @Test
  public void testGet() throws KettleValueException {
    LongHashIndex index = new LongHashIndex();
    index.put( 1L, 1L );
    assertThat( "Element has uncorrect value.", index.get( 1L ), equalTo( 1L ) );
  }

  @Test
  public void testSize() throws KettleValueException {
    LongHashIndex index = new LongHashIndex();
    assertThat( "Incorrect size of empty index.", index.getSize(), equalTo( 0 ) );
    index.put( 1L, 1L );
    assertThat( "Incorrect size of index.", index.getSize(), equalTo( 1 ) );
  }

  @Test
  public void testIsEmpty() throws KettleValueException {
    LongHashIndex index = new LongHashIndex();
    assertThat( "Empty index should return true.", index.isEmpty(), is( true ) );
    index.put( 1L, 1L );
    assertThat( "Not empty index should return false.", index.isEmpty(), is( false ) );
  }

}
