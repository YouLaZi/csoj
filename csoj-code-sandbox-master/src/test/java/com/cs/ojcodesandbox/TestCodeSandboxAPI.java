import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TestCodeSandboxAPI {
    private static final String SANDBOX_URL = "http://localhost:8090/executeCodeSync";
    private static final String AUTH_HEADER = "secretKey";
    
    public static void main(String[] args) {
        String javaCode = """
            import java.util.*;
            
            class Solution {
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
            """;
        
        List<String> testInputs = Arrays.asList(
            "[[1,3],[2,6],[8,10],[15,18]]",
            "[[1,4],[4,5]]"
        );
        
        System.out.println("=== Test API ===");
        
        for (int i = 0; i < testInputs.size(); i++) {
            String input = testInputs.get(i);
            System.out.println("\ntest_usage " + (i + 1) + ": " + input);
            
            try {
                String response = callCodeSandbox(javaCode, Arrays.asList(input));
                System.out.println("res: " + response);
            } catch (Exception e) {
                System.err.println("fail: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private static String callCodeSandbox(String code, List<String> inputList) throws Exception {
        URL url = new URL(SANDBOX_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("auth", AUTH_HEADER);
        conn.setDoOutput(true);
        
        String requestBody = String.format(
            "{\"code\":\"%s\",\"language\":\"java\",\"inputList\":%s}",
            code.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n"),
            inputListToJson(inputList)
        );
        
        System.out.println("request_to_sandbox...");
        System.out.println("request_length: " + requestBody.length());
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = conn.getResponseCode();
        System.out.println("code: " + responseCode);
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                    responseCode >= 200 && responseCode < 300 ? 
                    conn.getInputStream() : conn.getErrorStream(), 
                    StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        
        return response.toString();
    }
    
    private static String inputListToJson(List<String> inputList) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < inputList.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(inputList.get(i).replace("\"", "\\\"")).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }
}