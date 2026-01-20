import java.util.*;

public class TestMergeIntervals {
  public static void main(String[] args) {
    // Test case 1: [[1,3],[2,6],[8,10],[15,18]]
    String input1 = "[[1,3],[2,6],[8,10],[15,18]]";
    System.out.println("Input1: " + input1);
    System.out.println("Output1: " + mergeIntervals(input1));

    // Test case 2: [[1,4],[4,5]]
    String input2 = "[[1,4],[4,5]]";
    System.out.println("Input2: " + input2);
    System.out.println("Output2: " + mergeIntervals(input2));
  }

  public static String mergeIntervals(String input) {
    // Parse input 2D array
    input = input.trim().substring(1, input.length() - 1); // Remove outer []
    List<int[]> intervals = new ArrayList<>();

    if (!input.isEmpty()) {
      String[] parts = input.split("\\],\\[");
      for (String part : parts) {
        part = part.replace("[", "").replace("]", "");
        String[] nums = part.split(",");
        intervals.add(
            new int[] {Integer.parseInt(nums[0].trim()), Integer.parseInt(nums[1].trim())});
      }
    }

    // Sort intervals
    intervals.sort(Comparator.comparingInt(a -> a[0]));

    List<int[]> result = new ArrayList<>();
    for (int[] interval : intervals) {
      if (result.isEmpty() || result.get(result.size() - 1)[1] < interval[0]) {
        result.add(interval);
      } else {
        result.get(result.size() - 1)[1] = Math.max(result.get(result.size() - 1)[1], interval[1]);
      }
    }

    // Generate output
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < result.size(); i++) {
      sb.append("[").append(result.get(i)[0]).append(",").append(result.get(i)[1]).append("]");
      if (i < result.size() - 1) {
        sb.append(",");
      }
    }
    sb.append("]");
    return sb.toString();
  }
}
