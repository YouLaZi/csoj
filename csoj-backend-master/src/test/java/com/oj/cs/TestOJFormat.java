import java.util.*;

public class TestOJFormat {
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    String input = scanner.nextLine();

    System.out.println("输入: " + input);

    input = input.trim();
    if (input.startsWith("[") && input.endsWith("]")) {
      input = input.substring(1, input.length() - 1);
    }

    List<int[]> intervals = new ArrayList<>();
    if (!input.trim().isEmpty()) {
      String[] parts = input.split("],\\[");
      for (String part : parts) {
        part = part.replace("[", "").replace("]", "");
        String[] nums = part.split(",");
        if (nums.length == 2) {
          intervals.add(
              new int[] {Integer.parseInt(nums[0].trim()), Integer.parseInt(nums[1].trim())});
        }
      }
    }

    int[][] result = merge(intervals.toArray(new int[0][]));

    System.out.println("格式1 (Arrays.deepToString): " + Arrays.deepToString(result));

    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < result.length; i++) {
      if (i > 0) sb.append(",");
      sb.append("[").append(result[i][0]).append(",").append(result[i][1]).append("]");
    }
    sb.append("]");
    System.out.println("格式2 (手动构建): " + sb.toString());

    System.out.println("格式3 (期望格式): [[1,6],[8,10],[15,18]]");
    System.out.println("格式4 (期望格式): [[1,5]]");
  }

  public static int[][] merge(int[][] intervals) {
    if (intervals.length <= 1) {
      return intervals;
    }

    Arrays.sort(intervals, (v1, v2) -> v1[0] - v2[0]);
    int[][] res = new int[intervals.length][2];
    int idx = -1;
    for (int[] interval : intervals) {
      if (idx == -1 || interval[0] > res[idx][1]) {
        res[++idx] = interval;
      } else {
        res[idx][1] = Math.max(res[idx][1], interval[1]);
      }
    }
    return Arrays.copyOf(res, idx + 1);
  }
}
