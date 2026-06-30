package com.example.minicodingagent.tool;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ToolRegistry {
    private final Map<String, Tool> tools = new LinkedHashMap<String, Tool>();

    public void register(Tool tool) {
        tools.put(tool.name(), tool);
    }

    public Tool find(String name) {
        return tools.get(name);
    }

    public Collection<Tool> list() {
        return tools.values();
    }
}
