package org.example.eval;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 共情能力 (Empathy) 评测测试
 * 
 * 评测目标：评估 Agent 在面对用户负面情绪时，是否能体现关怀而非机械回复
 * 评分标准：0-5 分
 * - 5 分：主动表达理解和歉意，语气温暖，提供明确解决方案
 * - 4 分：有道歉和共情表达，但略显公式化
 * - 3 分：有基本共情词汇，但较简短
 * - 2 分：共情表达非常弱
 * - 1 分：几乎无共情，机械回复
 * - 0 分：完全无共情，冷漠
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EmpathyEvalTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final List<EvalResult> evalResults = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * 共情能力测试用例
     * 来源：评测体系方案 - Empathy-Test 数据集
     */
    static class EmpathyTestCase {
        String caseId;
        String userEmotion;
        String userInput;
        List<String> expectedBehaviors;

        public EmpathyTestCase(String caseId, String userEmotion, String userInput, List<String> expectedBehaviors) {
            this.caseId = caseId;
            this.userEmotion = userEmotion;
            this.userInput = userInput;
            this.expectedBehaviors = expectedBehaviors;
        }
    }

    /**
     * 评测结果
     */
    static class EvalResult {
        String caseId;
        String userInput;
        String agentResponse;
        int score;
        String reason;

        public EvalResult(String caseId, String userInput, String agentResponse, int score, String reason) {
            this.caseId = caseId;
            this.userInput = userInput;
            this.agentResponse = agentResponse;
            this.score = score;
            this.reason = reason;
        }
    }

    private final List<EmpathyTestCase> testCases = List.of(
        new EmpathyTestCase("E01", "angry", 
            "你们系统怎么回事？又出故障了！我要投诉！", 
            List.of("道歉", "共情", "解决方案")),
        
        new EmpathyTestCase("E02", "anxious", 
            "我的数据会不会丢失？我很担心！", 
            List.of("安抚", "明确保证", "解释")),
        
        new EmpathyTestCase("E03", "confused", 
            "我完全搞不懂这个功能怎么用，太复杂了", 
            List.of("耐心", "简化解释", "步骤指导")),
        
        new EmpathyTestCase("E04", "disappointed", 
            "这已经是我第三次反馈了，怎么还没解决", 
            List.of("道歉", "升级处理", "安抚")),
        
        new EmpathyTestCase("E05", "impatient", 
            "快点给我解决！我没时间等！", 
            List.of("高效", "安抚", "明确时间")),
        
        new EmpathyTestCase("E06", "frustrated", 
            "为什么每次都要我重复说问题？", 
            List.of("道歉", "理解", "解释流程")),
        
        new EmpathyTestCase("E07", "worried", 
            "这个操作安全吗？会不会影响我的数据？", 
            List.of("安抚", "安全保证", "详细说明")),
        
        new EmpathyTestCase("E08", "angry", 
            "你们的客服太差了！问题一直没人管！", 
            List.of("道歉", "共情", "跟进承诺")),
        
        new EmpathyTestCase("E09", "anxious", 
            "我刚才的操作是不是错了？会不会有问题？", 
            List.of("安抚", "确认状态", "提供解决方案")),
        
        new EmpathyTestCase("E10", "confused", 
            "这个提示是什么意思？我看不懂", 
            List.of("耐心解释", "简化说明", "举例"))
    );

    @Test
    @Order(1)
    @DisplayName("共情能力评测 - 执行所有测试用例")
    public void testEmpathyAllCases() throws Exception {
        System.out.println("\n========================================");
        System.out.println("共情能力 (Empathy) 评测开始");
        System.out.println("========================================\n");

        for (EmpathyTestCase testCase : testCases) {
            executeTestCase(testCase);
        }

        // 输出评测结果
        printEvalResults();
    }

    private void executeTestCase(EmpathyTestCase testCase) throws Exception {
        System.out.println("【测试用例】" + testCase.caseId + " - 用户情绪：" + testCase.userEmotion);
        System.out.println("用户输入：" + testCase.userInput);
        System.out.println("期望表现：" + testCase.expectedBehaviors);

        // 构建请求
        Map<String, String> request = new HashMap<>();
        request.put("Id", "empathy-test-" + testCase.caseId);
        request.put("Question", testCase.userInput);

        // 调用 /api/chat 接口
        String responseContent = mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // 解析响应
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);
        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
        String answer = (String) data.get("answer");
        boolean success = (Boolean) data.get("success");

        System.out.println("Agent 回复：" + answer);
        System.out.println("成功：" + success);

        // 评分（这里使用简化评分，实际应由人工或 LLM 评测）
        int score = evaluateEmpathy(answer, testCase.expectedBehaviors);
        String reason = generateReason(score, testCase.expectedBehaviors);

        evalResults.add(new EvalResult(testCase.caseId, testCase.userInput, answer, score, reason));

        System.out.println("评分：" + score + "/5");
        System.out.println("理由：" + reason);
        System.out.println();
    }

    /**
     * 简化共情评分逻辑
     * 实际应由人工或 LLM 评测
     */
    private int evaluateEmpathy(String answer, List<String> expectedBehaviors) {
        String lowerAnswer = answer.toLowerCase();
        int matchedBehaviors = 0;

        // 检查共情关键词
        String[] empathyKeywords = {"理解", "抱歉", "对不起", "歉意", "着急", "担心", "耐心", "马上", "尽快", "帮您"};
        for (String keyword : empathyKeywords) {
            if (lowerAnswer.contains(keyword.toLowerCase())) {
                matchedBehaviors++;
                break;
            }
        }

        // 检查期望行为
        for (String behavior : expectedBehaviors) {
            if (behavior.equals("道歉") && (lowerAnswer.contains("抱歉") || lowerAnswer.contains("对不起") || lowerAnswer.contains("歉意"))) {
                matchedBehaviors++;
            } else if (behavior.equals("共情") && (lowerAnswer.contains("理解") || lowerAnswer.contains("感受") || lowerAnswer.contains("心情"))) {
                matchedBehaviors++;
            } else if (behavior.equals("解决方案") && (lowerAnswer.contains("解决") || lowerAnswer.contains("处理") || lowerAnswer.contains("方案"))) {
                matchedBehaviors++;
            } else if (behavior.equals("安抚") && (lowerAnswer.contains("放心") || lowerAnswer.contains("安全") || lowerAnswer.contains("保证"))) {
                matchedBehaviors++;
            } else if (behavior.equals("耐心") && (lowerAnswer.contains("详细") || lowerAnswer.contains("步骤") || lowerAnswer.contains("解释"))) {
                matchedBehaviors++;
            }
        }

        // 根据匹配的行为数评分
        if (matchedBehaviors >= 3) return 5;
        if (matchedBehaviors >= 2) return 4;
        if (matchedBehaviors >= 1) return 3;
        return 2;
    }

    private String generateReason(int score, List<String> expectedBehaviors) {
        switch (score) {
            case 5: return "优秀：主动表达理解和歉意，语气温暖，提供明确解决方案";
            case 4: return "良好：有道歉和共情表达，但略显公式化";
            case 3: return "中等：有基本共情词汇，但较简短";
            case 2: return "较差：共情表达非常弱";
            default: return "需要改进";
        }
    }

    private void printEvalResults() {
        System.out.println("\n========================================");
        System.out.println("共情能力评测结果汇总");
        System.out.println("========================================");
        System.out.printf("%-8s %-12s %-10s %s%n", "用例编号", "用户情绪", "评分", "理由");
        System.out.println("----------------------------------------");

        double totalScore = 0;
        for (EvalResult result : evalResults) {
            System.out.printf("%-8s %-12s %-10d %s%n", 
                result.caseId, 
                testCases.stream().filter(t -> t.caseId.equals(result.caseId)).findFirst().map(t -> t.userEmotion).orElse(""),
                result.score, 
                result.reason);
            totalScore += result.score;
        }

        System.out.println("----------------------------------------");
        double avgScore = totalScore / evalResults.size();
        System.out.printf("平均分：%.2f / 5.0%n", avgScore);
        System.out.println("========================================\n");
    }
}
