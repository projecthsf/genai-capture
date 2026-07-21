# Publishing genai-capture-mcp to npm + the MCP Registry

The registry only stores metadata; it verifies against the **npm** package, so npm
must be published first. Two steps below need your credentials (npm login, GitHub
OAuth) — everything else is prepared in this folder.

## 1. Publish the npm package

```bash
cd mcp-npm
npm publish --access public        # runs `npm adduser` first if not logged in
```

Verify: <https://www.npmjs.com/package/genai-capture-mcp>

The registry checks that the published package's `package.json` has
`"mcpName": "io.github.projecthsf/genai-capture"` (it does) and that
`server.json`'s package `version` matches the npm `version` (both `1.0.0`).

## 2. Install the publisher CLI

```bash
brew install mcp-publisher
# or: curl -L "https://github.com/modelcontextprotocol/registry/releases/latest/download/mcp-publisher_$(uname -s | tr '[:upper:]' '[:lower:]')_$(uname -m | sed 's/x86_64/amd64/;s/aarch64/arm64/').tar.gz" | tar xz mcp-publisher && sudo mv mcp-publisher /usr/local/bin/
```

## 3. Authenticate and publish

```bash
cd mcp-npm
mcp-publisher login github          # browser OAuth; must be run as a projecthsf member (linhnnmt = admin ✓)
mcp-publisher publish               # reads ./server.json
```

The `io.github.projecthsf/*` namespace is authorized because linhnnmt is a projecthsf
org admin.

Verify:

```bash
curl "https://registry.modelcontextprotocol.io/v0.1/servers?search=io.github.projecthsf/genai-capture"
```

## Releasing a new version later

The shim rarely changes (it just launches the installed app), so its version is
independent of the app version. To ship a shim update: bump `version` in both
`package.json` and `server.json` (keep them equal), `npm publish`, then
`mcp-publisher publish`.

## Directory submissions (no artifact required)

- **Glama** — <https://glama.ai/mcp/servers> (auto-indexes the registry; usually no action needed once registry-published)
- **mcp.so** — submit at <https://mcp.so/submit>
- **PulseMCP** — <https://www.pulsemcp.com> (crawls the registry)
- **awesome-mcp-servers** — open a PR adding an entry to
  <https://github.com/punkpeye/awesome-mcp-servers> under a Screenshots/Images
  section, e.g.:
  `- [GenAI Capture](https://github.com/projecthsf/genai-capture) 🍎 - Capture and annotate the screen (arrows, boxes, blur/redact) for AI agents.`
