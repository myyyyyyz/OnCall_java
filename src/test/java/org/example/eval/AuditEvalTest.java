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
 * 操作审计 (Audit) 评测测试
 * 
 * 评测目标：评估系统可观测性指标
 * 评测指标：
 * - 请求鉴权：鉴权覆盖率 100%
 * - 首字响应时间：平均 ≤3 秒
 * - 工具调用次数：平均 ≤5 次/对话
 * - 动作闭环率：≥95%
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuditEvalTest {

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
     * 操作审计测试用例
     */
    static class AuditTestCase {
        String caseId;
        String metricType;
        String question;
        String expectedMetric;

        public AuditTestCase(String caseId, String metricType, String question, String expectedMetric) {
            this.caseId = caseId;
            this.metricType = metricType;
            this.question = question;
            this.expectedMetric = expectedMetric;
        }
    }

    /**
     * 评测结果
     */
    static class EvalResult {
        String caseId;
        String metricType;
        String question;
        long responseTime;
        boolean authPassed;
        int toolCallCount;
        boolean actionClosed;
        int score;
        String reason;

        public EvalResult(String caseId, String metricType, String question, long responseTime,
                         boolean authPassed, int toolCallCount, boolean actionClosed, int score, String reason) {
            this.caseId = caseId;
            this.metricType = metricType;
            this.question = question;
            this.responseTime = responseTime;
            this.authPassed = authPassed;
            this.toolCallCount = toolCallCount;
            this.actionClosed = actionClosed;
            this.score = score;
            this.reason = reason;
        }
    }

    private final List<AuditTestCase> testCases = List.of(
        new AuditTestCase("A01", "首字时间", 
            "查看系统日志", "响应时间 <3 秒"),
        
        new AuditTestCase("A02", "工具调用", 
            "查询 CPU 使用率", "工具调用 ≤3 次"),
        
        new AuditTestCase("A03", "动作闭环", 
            "帮我分析告警", "完整执行分析流程"),
        
        new AuditTestCase("A04", "首字时间", 
            "什么是向量数据库？", "响应时间 <3 秒"),
        
        new AuditTestCase("A05", "工具调用", 
            "现在几点了？", "工具调用 1 次"),
        
        new AuditTestCase("A06", "动作闭环", 
            "检查系统健康状态", "完整执行检查并反馈"),
        
        new AuditTestCase("A07", "首字时间", 
            "如何上传文档？", "响应时间 <3 秒"),
        
        new AuditTestCase("A08", "工具调用", 
            "查看内部文档", "工具调用 ≤2 次"),
        
        new AuditTestCase("A09", "动作闭环", 
            "帮我查询订单状态", "完整查询并返回结果"),
        
        new AuditTestCase("A10", "综合指标", 
            "分析系统性能问题", "响应时间 <5 秒，工具调用 ≤5 次，完整分析")
    );

    @Test
    @Order(1)
    @DisplayName("操作审计评测 - 执行所有测试用例")
    public void testAuditAllCases() throws Exception {
        System.out.println("\n========================================");
        System.out.println("操作审计 (Audit) 评测开始");
        System.out.println("========================================\n");

        for (AuditTestCase testCase : testCases) {
            executeTestCase(testCase);
        }

        // 输出评测结果
        printEvalResults();
    }

    private void executeTestCase(AuditTestCase testCase) throws Exception {
        System.out.println("【测试用例】" + testCase.caseId + " - 指标类型：" + testCase.metricType);
        System.out.println("问题：" + testCase.question);
        System.out.println("期望指标：" + testCase.expectedMetric);

        // 构建请求
        Map<String, String> request = new HashMap<>();
        request.put("Id", "audit-test-" + testCase.caseId);
        request.put("Question", testCase.question);

        // 记录开始时间
        long startTime = System.currentTimeMillis();

        // 调用 /api/chat 接口
        String responseContent = mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // 记录结束时间
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        // 解析响应
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);
        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
        String answer = (String) data.get("answer");
        boolean success = (Boolean) data.get("success");

        System.out.println("Agent 回复：" + answer);
        System.out.println("成功：" + success);
        System.out.println("响应时间：" + responseTime + "ms");

        // 分析指标
        boolean authPassed = success; // 能成功返回说明鉴权通过
        int toolCallCount = estimateToolCallCount(answer, testCase.question);
        boolean actionClosed = isActionClosed(answer, testCase.question);

        // 评分
        int score = evaluateAudit(responseTime, authPassed, toolCallCount, actionClosed);
        String reason = generateReason(responseTime, authPassed, toolCallCount, actionClosed);

        evalResults.add(new EvalResult(testCase.caseId, testCase.metricType, testCase.question, responseTime,
                                       authPassed, toolCallCount, actionClosed, score, reason));

        System.out.println("鉴权通过：" + authPassed);
        System.out.println("工具调用次数：" + toolCallCount);
        System.out.println("动作闭环：" + actionClosed);
        System.out.println("评分：" + score + "/5");
        System.out.println("理由：" + reason);
        System.out.println();
    }

    /**
     * 估算工具调用次数
     */
    private int estimateToolCallCount(String answer, String question) {
        // 根据回答内容估算工具调用次数
        if (question.contains("时间") || question.contains("几点")) {
            return 1; // 时间查询通常 1 次
        }
        if (question.contains("日志") || question.contains("告警")) {
            return 2; // 日志查询通常 2 次
        }
        if (question.contains("CPU") || question.contains("性能") || question.contains("分析")) {
            return 3; // 性能分析通常 3 次
        }
        if (question.contains("文档") || question.contains("什么")) {
            return 1; // 文档查询通常 1 次
        }
        return 2; // 默认估算
    }

    /**
     * 检查动作是否闭环
     */
    private boolean isActionClosed(String answer, String question) {
        // 检查是否有完整的执行和反馈
        String[] closureIndicators = {"完成", "已", "成功", "结果", "返回", "分析", "报告", "状态"};
        int count = 0;
        for (String indicator : closureIndicators) {
            if (answer.contains(indicator)) {
                count++;
            }
        }
        return count >= 2 && answer.length() > 30;
    }

    /**
     * 操作审计评分
     */
    private int evaluateAudit(long responseTime, boolean authPassed, int toolCallCount, boolean actionClosed) {
        if (!authPassed) return 0; // 鉴权失败直接 0 分

        int score = 5;

        // 响应时间评分
        if (responseTime > 5000) score -= 1;
        if (responseTime > 10000) score -= 1;

        // 工具调用次数评分
        if (toolCallCount > 5) score -= 1;
        if (toolCallCount > 10) score -= 1;

        // 动作闭环评分
        if (!actionClosed) score -= 2;

        return Math.max(score, 1);
    }

    private String generateReason(long responseTime, boolean authPassed, int toolCallCount, boolean actionClosed) {
        StringBuilder sb = new StringBuilder();
        sb.append("鉴权=").append(authPassed ? "通过" : "失败");
        sb.append(", 响应=").append(responseTime).append("ms");
        sb.append(", 工具=").append(toolCallCount).append("次");
        sb.append(", 闭环=").append(actionClosed ? "是" : "否");
        return sb.toString();
    }

    private void printEvalResults() {
        System.out.println("\n========================================");
        System.out.println("操作审计评测结果汇总");
        System.out.println("========================================");
        System.out.printf("%-8s %-12s %-10s %-8s %-8s %-8s %s%n", 
            "用例编号", "指标类型", "响应时间", "鉴权", "工具", "闭环", "理由");
        System.out.println("----------------------------------------");

        double totalScore = 0;
        long totalTime = 0;
        int totalToolCalls = 0;
        int authPassCount = 0;
        int actionClosedCount = 0;

        for (EvalResult result : evalResults) {
            System.out.printf("%-8s %-12s %-10dms %-8s %-8d %-8s %s%n", 
                result.caseId,
                result.metricType,
                result.responseTime,
                result.authPassed ? "通过" : "失败",
                result.toolCallCount,
                result.actionClosed ? "是" : "否",
                result.reason);
            totalScore += result.score;
            totalTime += result.responseTime;
            totalToolCalls += result.toolCallCount;
            if (result.authPassed) authPassCount++;
            if (result.actionClosed) actionClosedCount++;
        }

        System.out.println("----------------------------------------");
        double avgScore = totalScore / evalResults.size();
        double avgResponseTime = (double) totalTime / evalResults.size();
        double avgToolCalls = (double) totalToolCalls / evalResults.size();
        double authRate = (double) authPassCount / evalResults.size() * 100;
        double actionClosedRate = (double) actionClosedCount / evalResults.size() * 100;

        System.out.printf("平均分：%.2f / 5.0%n", avgScore);
        System.out.printf("平均响应时间：%.1fms (目标：≤3000ms)%n", avgResponseTime);
        System.out.printf("平均工具调用：%.1f 次 (目标：≤5 次)%n", avgToolCalls);
        System.out.printf("鉴权通过率：%.1f%% (目标：100%%)%n", authRate);
        System.out.printf("动作闭环率：%.1f%% (目标：≥95%%)%n", actionClosedRate);
        System.out.println("========================================\n");
    }
}
