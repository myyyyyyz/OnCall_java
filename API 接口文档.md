# SuperBizAgent API 接口文档

## 接口概览

| 基础 URL | `http://localhost:9900` |
|---------|------------------------|
| API 前缀 | `/api` |
| 静态页面 | `/` |

---

## API 接口列表

### 1. 对话相关接口

#### 1.1 普通对话接口
- **路径**: `POST /api/chat`
- **描述**: 发送普通对话请求（支持工具调用，非流式）
- **请求体**:
  ```json
  {
    "Id": "session-123",
    "Question": "你好，有什么可以帮助我的？"
  }
  ```
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "success": true,
      "answer": "你好！我是 SuperBizAgent...",
      "errorMessage": null
    }
  }
  ```
- **前端调用**: `app.js:608` - `sendQuickMessage()`

#### 1.2 流式对话接口
- **路径**: `POST /api/chat_stream`
- **描述**: 发送流式对话请求（SSE 流式输出，支持多轮对话和工具调用）
- **请求体**:
  ```json
  {
    "Id": "session-123",
    "Question": "你好，有什么可以帮助我的？"
  }
  ```
- **响应**: SSE 流式响应
  ```
  event: message
  data: {"type":"content","data":"你好！"}
  
  event: message
  data: {"type":"content","data":"我是 SuperBizAgent..."}
  
  event: message
  data: {"type":"done","data":null}
  ```
- **前端调用**: `app.js:664` - `sendStreamMessage()`

#### 1.3 清空会话历史
- **路径**: `POST /api/chat/clear`
- **描述**: 清空指定会话的历史记录
- **请求体**:
  ```json
  {
    "Id": "session-123"
  }
  ```
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": "会话历史已清空"
  }
  ```
- **前端调用**: 侧边栏"清空对话"按钮

#### 1.4 获取会话信息
- **路径**: `GET /api/chat/session/{sessionId}`
- **描述**: 获取指定会话的信息（消息对数、创建时间等）
- **路径参数**:
  - `sessionId`: 会话 ID
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "sessionId": "session-123",
      "messagePairCount": 5,
      "createTime": 1711353600000
    }
  }
  ```

---

### 2. 文件上传接口

#### 2.1 上传文件到知识库
- **路径**: `POST /api/upload`
- **描述**: 上传文档文件到知识库，并自动创建向量索引
- **请求类型**: `multipart/form-data`
- **请求参数**:
  - `file`: 文件对象（支持 .md, .txt, .pdf, .docx 等格式）
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "filename": "document.md",
      "filePath": "uploads/document.md",
      "fileSize": 10240
    }
  }
  ```
- **前端调用**: `app.js:1060` - `uploadFile()`
- **前端触发**: 拖拽文件上传 或 点击上传按钮

---

### 3. 智能运维接口

#### 3.1 AI 智能运维分析
- **路径**: `POST /api/ai_ops`
- **描述**: 启动多 Agent 协作流程，自动分析告警并生成运维报告（SSE 流式输出）
- **请求体**: 无（自动读取系统告警和指标）
- **响应**: SSE 流式响应
  ```
  event: message
  data: {"type":"content","data":"正在读取告警并拆解任务...\n"}
  
  event: message
  data: {"type":"content","data":"📋 **告警分析报告**\n\n"}
  
  event: message
  data: {"type":"done","data":null}
  ```
- **前端调用**: `app.js:1105` - `sendAIOpsRequest()`
- **前端触发**: 点击"AI 运维分析"按钮

---

### 4. Milvus 向量数据库接口

#### 4.1 健康检查
- **路径**: `GET /milvus/health`
- **描述**: 检查 Milvus 向量数据库连接状态
- **响应**:
  ```json
  {
    "message": "ok",
    "collections": ["biz", "knowledge_base"]
  }
  ```
- **用途**: 系统健康检查、运维监控

---

## 前端 API 调用汇总

| 功能 | API 路径 | HTTP 方法 | 前端函数 | 代码位置 |
|------|---------|----------|---------|---------|
| 普通对话 | `/api/chat` | POST | `sendQuickMessage()` | `app.js:608` |
| 流式对话 | `/api/chat_stream` | POST | `sendStreamMessage()` | `app.js:664` |
| 清空会话 | `/api/chat/clear` | POST | - | - |
| 获取会话 | `/api/chat/session/{id}` | GET | - | - |
| 文件上传 | `/api/upload` | POST | `uploadFile()` | `app.js:1060` |
| AI 运维 | `/api/ai_ops` | POST | `sendAIOpsRequest()` | `app.js:1105` |
| Milvus 健康 | `/milvus/health` | GET | - | - |

---

## 前端配置

### API 基础 URL
```javascript
// app.js:4
this.apiBaseUrl = 'http://localhost:9900/api';
```

### 会话管理
```javascript
// app.js:6-7
this.sessionId = this.generateSessionId();
this.chatHistories = this.loadChatHistories();
```

---

## 响应格式规范

### 成功响应
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 错误响应
```json
{
  "code": 500,
  "message": "错误描述",
  "data": null
}
```

---

## SSE 流式响应格式

### 内容块
```
event: message
data: {"type":"content","data":"增量内容..."}
```

### 错误
```
event: message
data: {"type":"error","data":"错误信息"}
```

### 完成
```
event: message
data: {"type":"done","data":null}
```

---

**文档版本**: v1.0.0  
**最后更新**: 2026 年 3 月 25 日
