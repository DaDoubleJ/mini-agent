# mini-coding-agent

Java 8 + Maven 的最小命令行 Agent 学习项目。

当前进度：

- 第 1 天：CLI + DeepSeek Chat Completion 调用
- 第 2 天：手动工具系统，包括 `list_files`、`read_file`、`search_code`

## 项目结构

```text
mini-coding-agent/
├── pom.xml
├── README.md
└── src/
    └── main/
        └── java/
            └── com/
                └── example/
                    └── minicodingagent/
                        ├── ChatClient.java
                        ├── ConsoleApp.java
                        ├── Main.java
                        ├── Message.java
                        └── tool/
                            ├── Tool.java
                            ├── ToolRegistry.java
                            ├── ToolResult.java
                            ├── WorkspacePath.java
                            └── impl/
                                ├── ListFilesTool.java
                                ├── ReadFileTool.java
                                └── SearchCodeTool.java
```

## 环境变量

聊天模式必填：

```powershell
$env:DEEPSEEK_API_KEY="your-api-key"
```

可选：

```powershell
$env:DEEPSEEK_BASE_URL="https://api.deepseek.com"
$env:DEEPSEEK_MODEL="deepseek-v4-flash"
```

`DEEPSEEK_BASE_URL` 默认值是 `https://api.deepseek.com`。

`DEEPSEEK_MODEL` 默认值是 `deepseek-v4-flash`。如果该模型不可用，到 DeepSeek 官方 API 文档查看当前可用模型，然后替换环境变量即可。

## 构建

```powershell
mvn package
```

## 运行

```powershell
java -jar target/mini-coding-agent-1.0-SNAPSHOT.jar
```

默认启动聊天模式。工具系统是 Agent 的内部能力，不要求用户在正常聊天时手动选择工具。

## 聊天模式

```text
System prompt:
You are a concise Java assistant.
User prompt:
Explain Java 8 Optional in one paragraph.
```

## 手动测试工具

Day 2 还没有实现 Agent Loop，所以工具只提供开发测试入口：

```powershell
java -jar target/mini-coding-agent-1.0-SNAPSHOT.jar --tool
```

测试 `list_files`：

```text
Tool name:
list_files
Parameters JSON, or blank for {}:
{"path":"src/main/java/com/example/minicodingagent"}
```

测试 `read_file`：

```text
Tool name:
read_file
Parameters JSON, or blank for {}:
{"path":"src/main/java/com/example/minicodingagent/tool/Tool.java"}
```

测试 `search_code`：

```text
Tool name:
search_code
Parameters JSON, or blank for {}:
{"keyword":"ToolResult","path":"src/main/java"}
```

路径越界会被拒绝：

```text
{"path":"../mini-coding-agent-7day-plan.md"}
```

会返回：

```text
Path is outside workspace: ../mini-coding-agent-7day-plan.md
```

## 工具约束

- 只能读取当前 workspace 内的路径。
- 不实现文件写入。
- 不实现代码修改。
- 不实现 Agent Loop。
