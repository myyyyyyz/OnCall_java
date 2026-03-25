package org.example.entity;

import org.example.constant.RiskLevel;

import java.time.LocalDateTime;

/**
 * 安全日志实体
 */
public class SecurityLog {

    /**
     * 日志 ID
     */
    private Long id;

    /**
     * 操作用户 ID
     */
    private Long userId;

    /**
     * 操作用户名
     */
    private String username;

    /**
     * 操作类型 (LOGIN/LOGOUT/UPLOAD/DELETE 等)
     */
    private String action;

    /**
     * 操作资源 (URL/路径)
     */
    private String resource;

    /**
     * 风险等级
     */
    private RiskLevel riskLevel;

    /**
     * 操作结果 (SUCCESS/FAILURE)
     */
    private String result;

    /**
     * 请求 IP
     */
    private String ipAddress;

    /**
     * 客户端信息
     */
    private String userAgent;

    /**
     * 请求参数 (JSON)
     */
    private String requestParams;

    /**
     * 错误信息 (如有)
     */
    private String errorMessage;

    /**
     * 确认码 (如有)
     */
    private String confirmationCode;

    /**
     * 操作时间
     */
    private LocalDateTime timestamp;

    public SecurityLog() {
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "SecurityLog{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", action='" + action + '\'' +
                ", result='" + result + '\'' +
                ", riskLevel=" + riskLevel +
                ", timestamp=" + timestamp +
                '}';
    }
}
