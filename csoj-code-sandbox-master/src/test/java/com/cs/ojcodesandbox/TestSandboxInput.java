import java.util.*;

public class TestSandboxInput {
    public static void main(String[] args) {
        // Simulate sandbox input method
        Scanner scanner = new Scanner(System.in);
        
        // Read input
        String input = scanner.nextLine();
        System.out.println("Received input: " + input);
        
        // Parse 2D array input
        input = input.trim().substring(1, input.length() - 1); // Remove outer []
        List<int[]> intervals = new ArrayList<>();
        
        if (!input.isEmpty()) {
            String[] parts = input.split("\\],\\[");
            for (String part : parts) {
                part = part.replace("[", "").replace("]", "");
                String[] nums = part.split(",");
                intervals.add(new int[]{Integer.parseInt(nums[0].trim()), Integer.parseInt(nums[1].trim())});
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
        
        // Output result
        System.out.print("[");
        for (int i = 0; i < result.size(); i++) {
            System.out.print("[" + result.get(i)[0] + "," + result.get(i)[1] + "]");
            if (i < result.size() - 1) {
                System.out.print(",");
            }
        }
        System.out.println("]");
        
        scanner.close();
    }
}