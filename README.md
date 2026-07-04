# mini-coding-agent

Java 8 + Maven command-line project for building a small Claude Code style coding agent.

Current progress:

- Day 1: CLI + DeepSeek Chat Completion call
- Day 2: read-only tool system with `list_files`, `read_file`, and `search_code`
- Day 3: Agent Loop that lets the model choose tools and return a final answer
- Day 4: write tools with `create_file` and `replace_in_file`

## Project Structure

```text
mini-coding-agent/
|-- pom.xml
|-- README.md
|-- config.properties.example
`-- src/main/java/com/example/minicodingagent/
    |-- ChatClient.java
    |-- ConsoleApp.java
    |-- Main.java
    |-- Message.java
    |-- agent/
    |   |-- Agent.java
    |   |-- AgentDecision.java
    |   `-- AgentPrompts.java
    `-- tool/
        |-- Tool.java
        |-- ToolRegistry.java
        |-- ToolResult.java
        |-- WorkspacePath.java
        `-- impl/
            |-- ListFilesTool.java
            |-- CreateFileTool.java
            |-- ReadFileTool.java
            |-- ReplaceInFileTool.java
            `-- SearchCodeTool.java
```

## Configuration

You can configure the API key in either of two ways.

Option 1: environment variables:

```powershell
$env:DEEPSEEK_API_KEY="your-api-key"
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

## Agent Mode

Normal startup enters agent mode directly:

```powershell
java -jar target/mini-coding-agent-1.0-SNAPSHOT.jar
```

The app has a built-in system prompt. Users only enter their own request:

```text
> Read ToolRegistry and summarize what it does
```

The model must return JSON internally:

```json
{"type":"tool_call","tool":"read_file","arguments":{"path":"src/main/java/com/example/minicodingagent/tool/ToolRegistry.java"}}
```

or:

```json
{"type":"final","answer":"ToolRegistry stores tools by name and lets the agent register, find, and list them."}
```

The Agent Loop prints each model decision and a short tool result summary.
Write tools also print the target path and change summary before writing.

## Tool Test Mode

The tool system is an internal agent capability. You can still use a development test entry:

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

Test `create_file`:

```text
Tool name:
create_file
Parameters JSON, or blank for {}:
{"path":"tmp/day4-demo.txt","content":"hello day4"}
```

Expected result:

```text
[write] create_file path: tmp\day4-demo.txt
[write] summary: create new file, bytes=10
Tool success:
Created file: tmp\day4-demo.txt
```

Test `replace_in_file`:

```text
Tool name:
replace_in_file
Parameters JSON, or blank for {}:
{"path":"tmp/day4-demo.txt","oldText":"hello","newText":"hi"}
```

Expected result:

```text
[write] replace_in_file path: tmp\day4-demo.txt
[write] summary: occurrences=1, oldTextLength=5, newTextLength=2
Tool success:
Replaced 1 occurrence(s) in file: tmp\day4-demo.txt
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
- Write tools can only create files or replace exact text inside the current workspace.
- Write tools refuse to modify the `.git` directory.
- `create_file` fails if the target file already exists.
- `replace_in_file` fails if `oldText` is not found.
- It does not implement function calling.
- It does not stream output.
