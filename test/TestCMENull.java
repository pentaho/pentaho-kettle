import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class TestCMENull {
  public static void main(String[] args) {
    List<Object> values = Collections.synchronizedList(new LinkedList<Object>());
    values.add("abcd");
    values.add(Boolean.valueOf(true));
    values.add(null);
    for (Object value : values) {
      System.out.println("value: "+value);
    }
  }
}
