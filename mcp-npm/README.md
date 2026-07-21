# genai-capture-mcp

MCP server for **[GenAI Capture](https://github.com/projecthsf/genai-capture)** — let AI
agents (Claude Code, Claude Desktop, Cursor, …) capture your screen, **see the
image**, annotate it (arrows, boxes, text, numbered steps, highlight, blur/redact),
and save the result. Great for AI-authored visual bug reports and before/after proof.

This package is a thin launcher: it finds the locally-installed GenAI Capture app and
runs its built-in MCP server (`--mcp`). It does not contain the server itself.

## Prerequisites

Install the GenAI Capture desktop app first (it carries the MCP server and the capture
engine): **https://github.com/projecthsf/genai-capture/releases/latest**

## Use

Claude Code:

```bash
claude mcp add genai-capture -- npx -y genai-capture-mcp
```

Claude Desktop / Cursor (`mcpServers` config):

```json
{
  "mcpServers": {
    "genai-capture": { "command": "npx", "args": ["-y", "genai-capture-mcp"] }
  }
}
```

If the app is installed somewhere non-standard, set `GENAI_CAPTURE_BIN` to the app
binary path.

## Tools

`capture_fullscreen`, `capture_region`, `annotate`, `get_image`, `save_image`.

Everything runs locally — images only go to the MCP client you connected. Each capture
plays a short sound as an audible cue that the screen was read.

## License

Apache-2.0
