import java.util.*;

public class TestUserCode {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        
        System.out.println("[DEBUG] 接收到的输入: " + input);
        
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
                    intervals.add(new int[]{Integer.parseInt(nums[0].trim()), Integer.parseInt(nums[1].trim())});
                }
            }
        }
        
        System.out.println("[DEBUG] 解析后的区间数量: " + intervals.size());
        for (int i = 0; i < intervals.size(); i++) {
            System.out.println("[DEBUG] 区间" + i + ": [" + intervals.get(i)[0] + "," + intervals.get(i)[1] + "]");
        }
        
        MergeIntervalsSolution solution = new MergeIntervalsSolution();
        int[][] result = solution.merge(intervals.toArray(new int[0][]));
        
        System.out.println("[DEBUG] 合并后的区间数量: " + result.length);
        for (int i = 0; i < result.length; i++) {
            System.out.println("[DEBUG] 结果区间" + i + ": [" + result[i][0] + "," + result[i][1] + "]");
        }
        
        System.out.println("[OUTPUT] Arrays.deepToString格式: " + Arrays.deepToString(result));
        
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < result.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("[").append(result[i][0]).append(",").append(result[i][1]).append("]");
        }
        sb.append("]");
        System.out.println("[OUTPUT] 紧凑格式: " + sb.toString());
        
        System.out.println(sb.toString());
    }
}

class MergeIntervalsSolution {
    public int[][] merge(int[][] intervals) {
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