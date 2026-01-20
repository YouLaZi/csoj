import java.util.*;

public class TestUserSubmission {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        
        // Parse input like [[1,3],[2,6],[8,10],[15,18]]
        input = input.trim();
        if (input.startsWith("[") && input.endsWith("]")) {
            input = input.substring(1, input.length() - 1);
        }
        
        String[] intervalStrs = input.split("\\],\\[");
        int[][] intervals = new int[intervalStrs.length][2];
        
        for (int i = 0; i < intervalStrs.length; i++) {
            String intervalStr = intervalStrs[i].replaceAll("[\\[\\]]", "");
            String[] parts = intervalStr.split(",");
            intervals[i][0] = Integer.parseInt(parts[0].trim());
            intervals[i][1] = Integer.parseInt(parts[1].trim());
        }
        
        // Execute user's merge logic
        int[][] result = merge(intervals);
        
        // Print result in expected format
        System.out.print("[");
        for (int i = 0; i < result.length; i++) {
            System.out.print("[" + result[i][0] + "," + result[i][1] + "]");
            if (i < result.length - 1) {
                System.out.print(",");
            }
        }
        System.out.println("]");
    }
    
    // User's merge method
    public static int[][] merge(int[][] intervals) {
        Arrays.sort(intervals, (v1, v2) -> v1[0] - v2[0]);
        int[][] res = new int[intervals.length][2];
        int idx = -1;
        for (int[] interval: intervals) {
            if (idx == -1 || interval[0] > res[idx][1]) {
                res[++idx] = interval;
            } else {
                res[idx][1] = Math.max(res[idx][1], interval[1]);
            }
        }
        return Arrays.copyOf(res, idx + 1);
    }
}