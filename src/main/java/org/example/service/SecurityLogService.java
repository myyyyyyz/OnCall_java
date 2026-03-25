package org.example.service;

import org.example.entity.SecurityLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 安全日志服务
 * 异步记录所有鉴权相关操作
 */
@Service
public class SecurityLogService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityLogService.class);

    // 使用内存队列异步存储日志（生产环境应写入数据库或 CLS）
    private final LinkedBlockingQueue<Runnable> logQueue = new LinkedBlockingQueue<>(1000);
    private final ThreadPoolExecutor executor;

    // 内存存储日志（用于查询）
    private final List<SecurityLog> logStore = new ArrayList<>();
    private final Object lock = new Object();

    public SecurityLogService() {
        // 启动日志写入线程
        executor = new ThreadPoolExecutor(
                1, 1,
                0L, TimeUnit.MILLISECONDS,
                logQueue,
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );
        executor.submit(this::processLogs);
    }

    /**
     * 记录日志
     *
     * @param log 安全日志
     */
    public void log(SecurityLog log) {
        try {
            executor.submit(() -> {
                synchronized (lock) {
                    logStore.add(log);
                    // 保留最近 1000 条日志
                    if (logStore.size() > 1000) {
                        logStore.remove(0);
                    }
                }
                logger.info("安全日志：{}", log);
            });
        } catch (Exception e) {
            logger.error("日志写入失败", e);
        }
    }

    /**
     * 处理日志队列（保留方法但不再使用）
     */
    private void processLogs() {
        // 此方法已不再使用，日志直接在 log() 方法中处理
    }

    /**
     * 查询所有日志
     *
     * @return 日志列表
     */
    public List<SecurityLog> getAllLogs() {
        synchronized (lock) {
            return new ArrayList<>(logStore);
        }
    }

    /**
     * 根据用户名查询日志
     *
     * @param username 用户名
     * @return 日志列表
     */
    public List<SecurityLog> getLogsByUsername(String username) {
        synchronized (lock) {
            List<SecurityLog> result = new ArrayList<>();
            for (SecurityLog log : logStore) {
                if (username.equals(log.getUsername())) {
                    result.add(log);
                }
            }
            return result;
        }
    }

    /**
     * 根据操作类型查询日志
     *
     * @param action 操作类型
     * @return 日志列表
     */
    public List<SecurityLog> getLogsByAction(String action) {
        synchronized (lock) {
            List<SecurityLog> result = new ArrayList<>();
            for (SecurityLog log : logStore) {
                if (action.equals(log.getAction())) {
                    result.add(log);
                }
            }
            return result;
        }
    }

    /**
     * 创建登录日志
     */
    public SecurityLog createLoginLog(String username, String ipAddress, String result, String errorMessage) {
        SecurityLog log = new SecurityLog();
        log.setUsername(username);
        log.setAction("LOGIN");
        log.setIpAddress(ipAddress);
        log.setResult(result);
        log.setErrorMessage(errorMessage);
        return log;
    }

    /**
     * 创建登出日志
     */
    public SecurityLog createLogoutLog(String username, String ipAddress, String result) {
        SecurityLog log = new SecurityLog();
        log.setUsername(username);
        log.setAction("LOGOUT");
        log.setIpAddress(ipAddress);
        log.setResult(result);
        return log;
    }

    /**
     * 创建操作日志
     */
    public SecurityLog createOperationLog(Long userId, String username, String action, String resource,
                                          org.example.constant.RiskLevel riskLevel, String result, String errorMessage) {
        SecurityLog log = new SecurityLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setAction(action);
        log.setResource(resource);
        log.setRiskLevel(riskLevel);
        log.setResult(result);
        log.setErrorMessage(errorMessage);
        return log;
    }
}
