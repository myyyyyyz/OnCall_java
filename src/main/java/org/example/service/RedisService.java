package org.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis 工具服务
 */
@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置字符串值
     *
     * @param key   键
     * @param value 值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置字符串值（带过期时间）
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取字符串值
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取字符串值（指定类型）
     *
     * @param key   键
     * @param clazz 目标类型
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null && clazz.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * 删除键
     *
     * @param key 键
     * @return 是否删除成功
     */
    public boolean delete(String key) {
        Boolean result = redisTemplate.delete(key);
        return result != null && result;
    }

    /**
     * 检查键是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    public boolean hasKey(String key) {
        Boolean result = redisTemplate.hasKey(key);
        return result != null && result;
    }

    /**
     * 设置过期时间
     *
     * @param key     键
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 是否设置成功
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        Boolean result = redisTemplate.expire(key, timeout, unit);
        return result != null && result;
    }

    /**
     * 获取剩余过期时间
     *
     * @param key  键
     * @param unit 时间单位
     * @return 剩余时间
     */
    public Long getExpire(String key, TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }

    /**
     * 自增（用于计数器）
     *
     * @param key  键
     * @param delta 增量
     * @return 自增后的值
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 自减
     *
     * @param key  键
     * @param delta 减量
     * @return 自减后的值
     */
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    // ==================== Hash 操作 ====================

    /**
     * Hash 设置字段值
     *
     * @param key   键
     * @param field 字段
     * @param value 值
     */
    public void hashSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * Hash 获取字段值
     *
     * @param key   键
     * @param field 字段
     * @return 值
     */
    public Object hashGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * Hash 删除字段
     *
     * @param key    键
     * @param fields 字段数组
     * @return 删除的字段数量
     */
    public Long hashDelete(String key, Object... fields) {
        return redisTemplate.opsForHash().delete(key, fields);
    }

    // ==================== List 操作 ====================

    /**
     * List 左侧推入
     *
     * @param key   键
     * @param value 值
     */
    public void leftPush(String key, Object value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * List 右侧推入
     *
     * @param key   键
     * @param value 值
     */
    public void rightPush(String key, Object value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * List 获取指定范围的元素
     *
     * @param key   键
     * @param start 起始索引
     * @param end   结束索引
     * @return 元素列表
     */
    public java.util.List<Object> listRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    // ==================== Set 操作 ====================

    /**
     * Set 添加元素
     *
     * @param key    键
     * @param values 元素数组
     */
    public void setAdd(String key, Object... values) {
        redisTemplate.opsForSet().add(key, values);
    }

    /**
     * Set 获取所有元素
     *
     * @param key 键
     * @return 元素集合
     */
    public java.util.Set<Object> setMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * Set 移除元素
     *
     * @param key    键
     * @param values 元素数组
     */
    public void setRemove(String key, Object... values) {
        redisTemplate.opsForSet().remove(key, values);
    }
}
