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
 * 事实对齐 (Groundedness) 评测测试
 * 
 * 评测目标：评估 Agent 回复是否基于工具查询结果，防止幻觉编造
 * 评分标准：0-5 分
 * - 5 分：每句话都有工具返回依据，无编造
 * - 4 分：主要信息有依据，少量推断
 * - 3 分：关键信息有依据，部分细节编造
 * - 2 分：大部分信息无依据
 * - 1 分：仅有少量信息有依据
 * - 0 分：完全编造
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GroundednessEvalTest {

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
     * 事实对齐测试用例
     */
    static class GroundednessTestCase {
        String caseId;
        String questionType;
        String question;
        String expectedDataSource;

        public GroundednessTestCase(String caseId, String questionType, String question, String expectedDataSource) {
            this.caseId = caseId;
            this.questionType = questionType;
            this.question = question;
            this.expectedDataSource = expectedDataSource;
        }
    }

    /**
     * 评测结果
     */
    static class EvalResult {
        String caseId;
        String question;
        String agentResponse;
        int groundedSentences;
        int hallucinatedSentences;
        boolean usedTool;
        int score;
        String reason;

        public EvalResult(String caseId, String question, String agentResponse, int groundedSentences, 
                         int hallucinatedSentences, boolean usedTool, int score, String reason) {
            this.caseId = caseId;
            this.question = question;
            this.agentResponse = agentResponse;
            this.groundedSentences = groundedSentences;
            this.hallucinatedSentences = hallucinatedSentences;
            this.usedTool = usedTool;
            this.score = score;
            this.reason = reason;
        }
    }

    private final List<GroundednessTestCase> testCases = List.of(
        new GroundednessTestCase("G01", "政策查询", 
            "有什么优惠活动？", "fetch_policy"),
        
        new GroundednessTestCase("G02", "数据查询", 
            "我的账户余额是多少？", "fetch_account"),
        
        new GroundednessTestCase("G03", "状态查询", 
            "订单发货了吗？", "fetch_order"),
        
        new GroundednessTestCase("G04", "价格查询", 
            "这个产品多少钱？", "fetch_product"),
        
        new GroundednessTestCase("G05", "规则查询", 
            "退款流程是什么？", "fetch_policy"),
        
        new GroundednessTestCase("G06", "日志查询", 
            "查看最近的系统告警日志", "query_logs"),
        
        new GroundednessTestCase("G07", "指标查询", 
            "CPU 使用率是多少？", "query_metrics"),
        
        new GroundednessTestCase("G08", "文档查询", 
            "系统支持哪些功能？", "internal_docs"),
        
        new GroundednessTestCase("G09", "时间查询", 
            "现在是什么时间？", "datetime_tool"),
        
        new GroundednessTestCase("G10", "综合查询", 
            "帮我分析最近的系统问题", "query_logs + query_metrics")
    );

    @Test
    @Order(1)
    @DisplayName("事实对齐评测 - 执行所有测试用例")
    public void testGroundednessAllCases() throws Exception {
        System.out.println("\n========================================");
        System.out.println("事实对齐 (Groundedness) 评测开始");
        System.out.println("========================================\n");

        for (GroundednessTestCase testCase : testCases) {
            executeTestCase(testCase);
        }

        // 输出评测结果
        printEvalResults();
    }

    private void executeTestCase(GroundednessTestCase testCase) throws Exception {
        System.out.println("【测试用例】" + testCase.caseId + " - 问题类型：" + testCase.questionType);
        System.out.println("问题：" + testCase.question);
        System.out.println("期望数据源：" + testCase.expectedDataSource);

        // 构建请求
        Map<String, String> request = new HashMap<>();
        request.put("Id", "groundedness-test-" + testCase.caseId);
        request.put("Question", testCase.question);

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

        // 分析回复质量
        boolean usedTool = detectToolUsage(answer, testCase.expectedDataSource);
        int sentenceCount = countSentences(answer);
        int groundedSentences = estimateGroundedSentences(answer, usedTool);
        int hallucinatedSentences = sentenceCount - groundedSentences;

        // 评分
        int score = evaluateGroundedness(groundedSentences, hallucinatedSentences, usedTool);
        String reason = generateReason(score, groundedSentences, hallucinatedSentences, usedTool);

        evalResults.add(new EvalResult(testCase.caseId, testCase.question, answer, groundedSentences, 
                                       hallucinatedSentences, usedTool, score, reason));

        System.out.println("使用工具：" + usedTool);
        System.out.println("有依据句子：" + groundedSentences);
        System.out.println("幻觉句子：" + hallucinatedSentences);
        System.out.println("评分：" + score + "/5");
        System.out.println("理由：" + reason);
        System.out.println();
    }

    /**
     * 检测是否使用了工具
     */
    private boolean detectToolUsage(String answer, String expectedDataSource) {
        // 检查是否有工具调用的迹象
        String[] toolIndicators = {"查询到", "根据", "显示", "数据", "日志", "记录", 
                                   "检测到", "获取到", "返回", "结果"};
        for (String indicator : toolIndicators) {
            if (answer.contains(indicator)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 统计句子数量
     */
    private int countSentences(String answer) {
        String[] sentences = answer.split("[.!?。！？\n]");
        int count = 0;
        for (String sentence : sentences) {
            if (!sentence.trim().isEmpty() && sentence.trim().length() > 5) {
                count++;
            }
        }
        return Math.max(count, 1);
    }

    /**
     * 估算有依据的句子数量
     */
    private int estimateGroundedSentences(String answer, boolean usedTool) {
        if (!usedTool) {
            // 未使用工具，大部分可能是幻觉
            return 1;
        }
        
        // 有具体数据/数字的句子更可能是有依据的
        String[] groundedIndicators = {"具体数字", "时间", "百分比", "状态", "名称"};
        int groundedCount = 0;
        
        // 简单估算：有数字或具体信息的句子
        if (answer.matches(".*\\d+.*")) {
            groundedCount += 2;
        }
        if (answer.contains("根据") || answer.contains("查询")) {
            groundedCount += 2;
        }
        
        return Math.min(groundedCount + 1, countSentences(answer));
    }

    /**
     * 事实对齐评分
     */
    private int evaluateGroundedness(int groundedSentences, int hallucinatedSentences, boolean usedTool) {
        if (!usedTool && hallucinatedSentences > 3) return 0;
        if (!usedTool) return 1;
        
        double ratio = (double) groundedSentences / (groundedSentences + hallucinatedSentences);
        
        if (ratio >= 0.95 && hallucinatedSentences == 0) return 5;
        if (ratio >= 0.8) return 4;
        if (ratio >= 0.6) return 3;
        if (ratio >= 0.4) return 2;
        if (ratio >= 0.2) return 1;
        return 0;
    }

    private String generateReason(int score, int groundedSentences, int hallucinatedSentences, boolean usedTool) {
        StringBuilder sb = new StringBuilder();
        sb.append("工具=").append(usedTool ? "已用" : "未用");
        sb.append(", 有依据=").append(groundedSentences);
        sb.append(", 幻觉=").append(hallucinatedSentences);
        sb.append(" → ");
        
        switch (score) {
            case 5: return sb + "完全对齐：每句话都有工具返回依据，无编造";
            case 4: return sb + "基本对齐：主要信息有依据，少量推断";
            case 3: return sb + "部分对齐：关键信息有依据，部分细节编造";
            case 2: return sb + "较少对齐：大部分信息无依据";
            case 1: return sb + "几乎不对齐：仅有少量信息有依据";
            default: return sb + "完全编造：所有信息都是幻觉";
        }
    }

    private void printEvalResults() {
        System.out.println("\n========================================");
        System.out.println("事实对齐评测结果汇总");
        System.out.println("========================================");
        System.out.printf("%-8s %-12s %-6s %-8s %-8s %-10s %s%n", 
            "用例编号", "问题类型", "评分", "有依据", "幻觉", "工具使用", "理由");
        System.out.println("----------------------------------------");

        double totalScore = 0;
        for (EvalResult result : evalResults) {
            System.out.printf("%-8s %-12s %-6d %-8d %-8d %-10s %s%n", 
                result.caseId,
                testCases.stream().filter(t -> t.caseId.equals(result.caseId)).findFirst().map(t -> t.questionType).orElse(""),
                result.score,
                result.groundedSentences,
                result.hallucinatedSentences,
                result.usedTool ? "是" : "否",
                result.reason);
            totalScore += result.score;
        }

        System.out.println("----------------------------------------");
        double avgScore = totalScore / evalResults.size();
        System.out.printf("平均分：%.2f / 5.0%n", avgScore);
        System.out.println("========================================\n");
    }
}
