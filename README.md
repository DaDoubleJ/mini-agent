# mini-coding-agent

Java 8 + Maven command-line project for building a small Claude Code style coding agent.

Current progress:

- Day 1: CLI + DeepSeek Chat Completion call
- Day 2: read-only tool system with `list_files`, `read_file`, and `search_code`

## Project Structure

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

## Environment Variables

You can configure the API key in either of two ways.

Option 1: environment variables:

```powershell
$env:DEEPSEEK_API_KEY="your-api-key"
```

Optional:

```powershell
$env:DEEPSEEK_BASE_URL="https://api.deepseek.com"
$env:DEEPSEEK_MODEL="deepseek-v4-flash"
```

Option 2: local config file:

```powershell
copy config.properties.example config.properties
```

Then edit `config.properties`:

```properties
deepseek.apiKey=your-api-key
deepseek.baseUrl=https://api.deepseek.com
deepseek.model=deepseek-v4-flash
```

`config.properties` is ignored by Git, so your real API key should not be committed.

`DEEPSEEK_BASE_URL` defaults to `https://api.deepseek.com`.

`DEEPSEEK_MODEL` defaults to `deepseek-v4-flash`. If the model is unavailable, check the current DeepSeek API docs and set `DEEPSEEK_MODEL` to an available model.

## Build

```powershell
mvn package
```

## Chat Mode

Normal startup enters chat mode directly:

```powershell
java -jar target/mini-coding-agent-1.0-SNAPSHOT.jar
```

The app has a built-in system prompt. Users only enter their own request:

```text
>
用一句话解释 Java Stream
```

## Tool Test Mode

The tool system is an internal agent capability. Day 2 does not implement the Agent Loop yet, so tools are exposed only through a development test entry:

```powershell
java -jar target/mini-coding-agent-1.0-SNAPSHOT.jar --tool
```

Test `list_files`:

```text
Tool name:
list_files
Parameters JSON, or blank for {}:
{"path":"src/main/java/com/example/minicodingagent"}
```

Test `read_file`:

```text
Tool name:
read_file
Parameters JSON, or blank for {}:
{"path":"src/main/java/com/example/minicodingagent/tool/Tool.java"}
```

Test `search_code`:

```text
Tool name:
search_code
Parameters JSON, or blank for {}:
{"keyword":"ToolResult","path":"src/main/java"}
```

Path traversal is rejected:

```text
{"path":"../mini-coding-agent-7day-plan.md"}
```

Expected result:

```text
Path is outside workspace: ../mini-coding-agent-7day-plan.md
```

## Current Constraints

- The app can only read paths inside the current workspace.
- It does not write files.
- It does not modify code.
- It does not implement the Agent Loop yet.
