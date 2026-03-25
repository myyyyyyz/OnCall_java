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
 * 业务终态 (Resolution) 评测测试
 * 
 * 评测目标：评估用户问题是否真正得到解决，而非仅给出回复
 * 判定标准：已解决/部分解决/未解决/无法解决
 * 评测指标：解决率 ≥85%，一次解决率 ≥70%
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResolutionEvalTest {

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
     * 业务终态测试用例
     */
    static class ResolutionTestCase {
        String caseId;
        String problemType;
        String problem;
        String expectedResolution;

        public ResolutionTestCase(String caseId, String problemType, String problem, String expectedResolution) {
            this.caseId = caseId;
            this.problemType = problemType;
            this.problem = problem;
            this.expectedResolution = expectedResolution;
        }
    }

    /**
     * 评测结果
     */
    static class EvalResult {
        String caseId;
        String problem;
        String agentResponse;
        int conversationTurns;
        String resolutionStatus;
        boolean hasSolution;
        boolean hasFollowUp;
        int score;
        String reason;

        public EvalResult(String caseId, String problem, String agentResponse, int conversationTurns,
                         String resolutionStatus, boolean hasSolution, boolean hasFollowUp, int score, String reason) {
            this.caseId = caseId;
            this.problem = problem;
            this.agentResponse = agentResponse;
            this.conversationTurns = conversationTurns;
            this.resolutionStatus = resolutionStatus;
            this.hasSolution = hasSolution;
            this.hasFollowUp = hasFollowUp;
            this.score = score;
            this.reason = reason;
        }
    }

    private final List<ResolutionTestCase> testCases = List.of(
        new ResolutionTestCase("R01", "信息查询", 
            "我的订单状态是什么？", "返回订单状态信息"),
        
        new ResolutionTestCase("R02", "操作指导", 
            "怎么修改密码？", "提供修改密码步骤"),
        
        new ResolutionTestCase("R03", "故障排查", 
            "登录失败怎么办？", "提供排查方案并解决"),
        
        new ResolutionTestCase("R04", "政策咨询", 
            "退款政策是什么？", "清晰说明退款政策"),
        
        new ResolutionTestCase("R05", "复杂问题", 
            "我要退款并重新下单", "完成退款流程指导"),
        
        new ResolutionTestCase("R06", "信息查询", 
            "账户余额还有多少？", "返回余额信息"),
        
        new ResolutionTestCase("R07", "故障排查", 
            "系统报错 503 怎么办？", "提供 503 错误排查方案"),
        
        new ResolutionTestCase("R08", "操作指导", 
            "如何导出系统日志？", "提供导出步骤"),
        
        new ResolutionTestCase("R09", "政策咨询", 
            "新用户有什么优惠？", "说明优惠政策"),
        
        new ResolutionTestCase("R10", "复杂问题", 
            "系统性能下降，帮我分析原因", "提供性能分析和建议"),
        
        new ResolutionTestCase("R11", "信息查询", 
            "最近有什么系统告警？", "返回告警列表"),
        
        new ResolutionTestCase("R12", "故障排查", 
            "CPU 使用率过高怎么办？", "提供 CPU 高使用率排查方案"),
        
        new ResolutionTestCase("R13", "操作指导", 
            "如何添加新用户？", "提供添加用户步骤"),
        
        new ResolutionTestCase("R14", "政策咨询", 
            "数据保留策略是什么？", "说明数据保留政策"),
        
        new ResolutionTestCase("R15", "复杂问题", 
            "帮我检查系统健康状况", "提供健康检查报告")
    );

    @Test
    @Order(1)
    @DisplayName("业务终态评测 - 执行所有测试用例")
    public void testResolutionAllCases() throws Exception {
        System.out.println("\n========================================");
        System.out.println("业务终态 (Resolution) 评测开始");
        System.out.println("========================================\n");

        for (ResolutionTestCase testCase : testCases) {
            executeTestCase(testCase);
        }

        // 输出评测结果
        printEvalResults();
    }

    private void executeTestCase(ResolutionTestCase testCase) throws Exception {
        System.out.println("【测试用例】" + testCase.caseId + " - 问题类型：" + testCase.problemType);
        System.out.println("问题：" + testCase.problem);
        System.out.println("期望解决：" + testCase.expectedResolution);

        // 构建请求
        Map<String, String> request = new HashMap<>();
        request.put("Id", "resolution-test-" + testCase.caseId);
        request.put("Question", testCase.problem);

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
        int conversationTurns = 1; // 单次对话
        boolean hasSolution = hasSolution(answer);
        boolean hasFollowUp = hasFollowUpSuggestions(answer);
        String resolutionStatus = determineResolutionStatus(answer, hasSolution, hasFollowUp);
        int score = calculateScore(resolutionStatus);
        String reason = generateReason(resolutionStatus, hasSolution, hasFollowUp);

        evalResults.add(new EvalResult(testCase.caseId, testCase.problem, answer, conversationTurns,
                                       resolutionStatus, hasSolution, hasFollowUp, score, reason));

        System.out.println("对话轮次：" + conversationTurns);
        System.out.println("有解决方案：" + hasSolution);
        System.out.println("有后续建议：" + hasFollowUp);
        System.out.println("终态判定：" + resolutionStatus);
        System.out.println("评分：" + score);
        System.out.println("理由：" + reason);
        System.out.println();
    }

    /**
     * 检查是否有解决方案
     */
    private boolean hasSolution(String answer) {
        String[] solutionIndicators = {"解决", "方案", "步骤", "方法", "操作", "处理", 
                                       "建议", "可以", "需要", "请"};
        for (String indicator : solutionIndicators) {
            if (answer.contains(indicator)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否有后续建议
     */
    private boolean hasFollowUpSuggestions(String answer) {
        String[] followUpIndicators = {"如果", "如有", "随时", "联系", "进一步", "其他", 
                                       "帮助", "问题", "需要"};
        int count = 0;
        for (String indicator : followUpIndicators) {
            if (answer.contains(indicator)) {
                count++;
            }
        }
        return count >= 2;
    }

    /**
     * 判定终态
     */
    private String determineResolutionStatus(String answer, boolean hasSolution, boolean hasFollowUp) {
        if (hasSolution && answer.length() > 50) {
            return "已解决";
        } else if (hasSolution) {
            return "部分解决";
        } else if (answer.length() > 20) {
            return "未解决";
        } else {
            return "无法解决";
        }
    }

    /**
     * 计算分数
     */
    private int calculateScore(String resolutionStatus) {
        switch (resolutionStatus) {
            case "已解决": return 5;
            case "部分解决": return 3;
            case "未解决": return 1;
            case "无法解决": return 2;
            default: return 0;
        }
    }

    private String generateReason(String resolutionStatus, boolean hasSolution, boolean hasFollowUp) {
        return "终态=" + resolutionStatus + 
               ", 方案=" + (hasSolution ? "有" : "无") + 
               ", 建议=" + (hasFollowUp ? "有" : "无");
    }

    private void printEvalResults() {
        System.out.println("\n========================================");
        System.out.println("业务终态评测结果汇总");
        System.out.println("========================================");
        System.out.printf("%-8s %-12s %-10s %-8s %-8s %-8s %s%n", 
            "用例编号", "问题类型", "终态判定", "轮次", "方案", "建议", "理由");
        System.out.println("----------------------------------------");

        double totalScore = 0;
        int resolvedCount = 0;
        for (EvalResult result : evalResults) {
            System.out.printf("%-8s %-12s %-10s %-8d %-8s %-8s %s%n", 
                result.caseId,
                testCases.stream().filter(t -> t.caseId.equals(result.caseId)).findFirst().map(t -> t.problemType).orElse(""),
                result.resolutionStatus,
                result.conversationTurns,
                result.hasSolution ? "有" : "无",
                result.hasFollowUp ? "有" : "无",
                result.reason);
            totalScore += result.score;
            if (result.resolutionStatus.equals("已解决")) {
                resolvedCount++;
            }
        }

        System.out.println("----------------------------------------");
        double avgScore = totalScore / evalResults.size();
        double resolutionRate = (double) resolvedCount / evalResults.size() * 100;
        System.out.printf("平均分：%.2f / 5.0%n", avgScore);
        System.out.printf("解决率：%.1f%% (%d/%d)%n", resolutionRate, resolvedCount, evalResults.size());
        System.out.println("目标：解决率 ≥85%");
        System.out.println("========================================\n");
    }
}
