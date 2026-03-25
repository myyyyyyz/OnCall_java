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
 * 清晰度 (Clarity) 评测测试
 * 
 * 评测目标：评估 Agent 的回复是否通俗易懂，避免术语堆砌
 * 评分标准：0-5 分
 * - 5 分：用比喻/例子，无术语，结构清晰
 * - 4 分：有少量术语但解释，结构较好
 * - 3 分：有术语，但能理解主要内容
 * - 2 分：术语较多，需要专业知识
 * - 1 分：大量术语堆砌，难以理解
 * - 0 分：逻辑混乱，无法理解
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClarityEvalTest {

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
     * 清晰度测试用例
     */
    static class ClarityTestCase {
        String caseId;
        String questionType;
        String question;
        List<String> expectedBehaviors;

        public ClarityTestCase(String caseId, String questionType, String question, List<String> expectedBehaviors) {
            this.caseId = caseId;
            this.questionType = questionType;
            this.question = question;
            this.expectedBehaviors = expectedBehaviors;
        }
    }

    /**
     * 评测结果
     */
    static class EvalResult {
        String caseId;
        String question;
        String agentResponse;
        int termCount;
        boolean hasMetaphor;
        boolean hasStructure;
        int score;
        String reason;

        public EvalResult(String caseId, String question, String agentResponse, int termCount, 
                         boolean hasMetaphor, boolean hasStructure, int score, String reason) {
            this.caseId = caseId;
            this.question = question;
            this.agentResponse = agentResponse;
            this.termCount = termCount;
            this.hasMetaphor = hasMetaphor;
            this.hasStructure = hasStructure;
            this.score = score;
            this.reason = reason;
        }
    }

    private final List<ClarityTestCase> testCases = List.of(
        new ClarityTestCase("C01", "技术概念", 
            "什么是向量数据库？", 
            List.of("比喻解释", "避免术语", "举例说明")),
        
        new ClarityTestCase("C02", "操作流程", 
            "怎么上传文档到系统？", 
            List.of("分步骤说明", "清晰指引", "截图提示")),
        
        new ClarityTestCase("C03", "故障排查", 
            "为什么我的查询一直超时？", 
            List.of("先说结论", "再解释原因", "提供解决方案")),
        
        new ClarityTestCase("C04", "业务政策", 
            "你们有什么优惠政策？", 
            List.of("表格呈现", "列表说明", "条件清晰")),
        
        new ClarityTestCase("C05", "复杂问题", 
            "RAG 检索和模型微调有什么区别？", 
            List.of("对比说明", "避免术语", "举例")),
        
        new ClarityTestCase("C06", "技术概念", 
            "什么是 Agent 智能体？", 
            List.of("比喻解释", "场景举例", "简单易懂")),
        
        new ClarityTestCase("C07", "操作流程", 
            "如何查看系统日志？", 
            List.of("分步骤", "命令清晰", "注意事项")),
        
        new ClarityTestCase("C08", "故障排查", 
            "服务启动失败怎么办？", 
            List.of("常见原因", "排查步骤", "解决方案")),
        
        new ClarityTestCase("C09", "业务政策", 
            "数据保留策略是什么？", 
            List.of("时间清晰", "分类说明", "易懂")),
        
        new ClarityTestCase("C10", "复杂问题", 
            "向量检索和关键词检索有什么区别？", 
            List.of("对比说明", "举例", "场景说明"))
    );

    @Test
    @Order(1)
    @DisplayName("清晰度评测 - 执行所有测试用例")
    public void testClarityAllCases() throws Exception {
        System.out.println("\n========================================");
        System.out.println("清晰度 (Clarity) 评测开始");
        System.out.println("========================================\n");

        for (ClarityTestCase testCase : testCases) {
            executeTestCase(testCase);
        }

        // 输出评测结果
        printEvalResults();
    }

    private void executeTestCase(ClarityTestCase testCase) throws Exception {
        System.out.println("【测试用例】" + testCase.caseId + " - 问题类型：" + testCase.questionType);
        System.out.println("问题：" + testCase.question);
        System.out.println("期望表现：" + testCase.expectedBehaviors);

        // 构建请求
        Map<String, String> request = new HashMap<>();
        request.put("Id", "clarity-test-" + testCase.caseId);
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
        int termCount = countTechnicalTerms(answer);
        boolean hasMetaphor = hasMetaphorOrExample(answer);
        boolean hasStructure = hasStructure(answer);

        // 评分
        int score = evaluateClarity(answer, termCount, hasMetaphor, hasStructure);
        String reason = generateReason(score, termCount, hasMetaphor, hasStructure);

        evalResults.add(new EvalResult(testCase.caseId, testCase.question, answer, termCount, 
                                       hasMetaphor, hasStructure, score, reason));

        System.out.println("术语数量：" + termCount);
        System.out.println("有比喻/例子：" + hasMetaphor);
        System.out.println("有结构：" + hasStructure);
        System.out.println("评分：" + score + "/5");
        System.out.println("理由：" + reason);
        System.out.println();
    }

    /**
     * 统计技术术语数量
     */
    private int countTechnicalTerms(String answer) {
        String[] terms = {"API", "HTTP", "JSON", "SDK", "数据库", "向量", "Embedding", 
                         "Token", "REST", "Endpoint", "微服务", "容器", "Docker", "K8s",
                         "MQ", "Redis", "MySQL", "接口", "参数", "响应", "请求"};
        int count = 0;
        String upperAnswer = answer.toUpperCase();
        for (String term : terms) {
            if (upperAnswer.contains(term.toUpperCase())) {
                count++;
            }
        }
        return count;
    }

    /**
     * 检查是否有比喻或例子
     */
    private boolean hasMetaphorOrExample(String answer) {
        String[] indicators = {"比如", "例如", "就像", "好比", "举例", "场景", "假设", "像"};
        for (String indicator : indicators) {
            if (answer.contains(indicator)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否有结构（分点、分步骤）
     */
    private boolean hasStructure(String answer) {
        String[] indicators = {"1.", "2.", "3.", "第一", "第二", "第三", "步骤", 
                              "首先", "其次", "然后", "最后", "-", "•", "\n"};
        for (String indicator : indicators) {
            if (answer.contains(indicator)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 清晰度评分
     */
    private int evaluateClarity(String answer, int termCount, boolean hasMetaphor, boolean hasStructure) {
        // 5 分：用比喻/例子，无术语 (<2)，结构清晰
        if (hasMetaphor && termCount < 2 && hasStructure) return 5;
        // 4 分：有少量术语但解释，结构较好
        if (termCount < 4 && hasStructure) return 4;
        // 3 分：有术语，但能理解主要内容
        if (termCount < 6) return 3;
        // 2 分：术语较多
        if (termCount < 8) return 2;
        // 1 分：大量术语堆砌
        return 1;
    }

    private String generateReason(int score, int termCount, boolean hasMetaphor, boolean hasStructure) {
        StringBuilder sb = new StringBuilder();
        sb.append("术语数=").append(termCount);
        sb.append(", 比喻=").append(hasMetaphor ? "有" : "无");
        sb.append(", 结构=").append(hasStructure ? "清晰" : "一般");
        sb.append(" → ");
        
        switch (score) {
            case 5: return sb + "非常清晰：用比喻/例子，无术语，结构清晰";
            case 4: return sb + "清晰：有少量术语但解释，结构较好";
            case 3: return sb + "基本清晰：有术语，但能理解主要内容";
            case 2: return sb + "较模糊：术语较多，需要专业知识";
            default: return sb + "模糊：大量术语堆砌，难以理解";
        }
    }

    private void printEvalResults() {
        System.out.println("\n========================================");
        System.out.println("清晰度评测结果汇总");
        System.out.println("========================================");
        System.out.printf("%-8s %-12s %-8s %-6s %-6s %-6s %s%n", 
            "用例编号", "问题类型", "评分", "术语", "比喻", "结构", "理由");
        System.out.println("----------------------------------------");

        double totalScore = 0;
        for (EvalResult result : evalResults) {
            System.out.printf("%-8s %-12s %-8d %-6d %-6s %-6s %s%n", 
                result.caseId,
                testCases.stream().filter(t -> t.caseId.equals(result.caseId)).findFirst().map(t -> t.questionType).orElse(""),
                result.score,
                result.termCount,
                result.hasMetaphor ? "有" : "无",
                result.hasStructure ? "是" : "否",
                result.reason);
            totalScore += result.score;
        }

        System.out.println("----------------------------------------");
        double avgScore = totalScore / evalResults.size();
        System.out.printf("平均分：%.2f / 5.0%n", avgScore);
        System.out.println("========================================\n");
    }
}
