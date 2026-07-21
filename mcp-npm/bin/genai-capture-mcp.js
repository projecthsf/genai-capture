#!/usr/bin/env node
'use strict';

/*
 * Thin launcher for the GenAI Capture MCP server.
 *
 * GenAI Capture is a locally-installed desktop app that exposes an MCP stdio
 * server via `--mcp`. This package doesn't contain the server — it locates the
 * installed app and execs it, passing stdio straight through so the MCP client
 * talks to the app directly. If the app isn't installed, it prints where to get
 * it and exits non-zero.
 *
 * Configure in a client, e.g. Claude Code:
 *   claude mcp add genai-capture -- npx -y genai-capture-mcp
 */

const { spawn } = require('child_process');
const fs = require('fs');
const os = require('os');
const path = require('path');

const DOWNLOAD_URL = 'https://github.com/projecthsf/genai-capture/releases/latest';

function candidates() {
  const list = [];
  if (process.env.GENAI_CAPTURE_BIN) list.push(process.env.GENAI_CAPTURE_BIN);

  if (process.platform === 'darwin') {
    const rel = 'GenAI Capture.app/Contents/MacOS/GenAI Capture';
    list.push(path.join('/Applications', rel));
    list.push(path.join(os.homedir(), 'Applications', rel));
  } else if (process.platform === 'win32') {
    const bases = [
      process.env['LOCALAPPDATA'],
      process.env['ProgramFiles'],
      process.env['ProgramFiles(x86)'],
    ].filter(Boolean);
    for (const b of bases) list.push(path.join(b, 'GenAI Capture', 'GenAI Capture.exe'));
  } else {
    // Linux isn't distributed yet; honor a PATH install or the env override.
    list.push('/opt/genai-capture/bin/GenAI Capture');
  }
  return list;
}

function findBinary() {
  for (const c of candidates()) {
    try { if (c && fs.existsSync(c)) return c; } catch (_) { /* ignore */ }
  }
  return null;
}

const bin = findBinary();
if (!bin) {
  process.stderr.write(
    'GenAI Capture is not installed.\n' +
    'Download it from ' + DOWNLOAD_URL + '\n' +
    'then re-run. (Or set GENAI_CAPTURE_BIN to the app binary path.)\n');
  process.exit(1);
}

const child = spawn(bin, ['--mcp', ...process.argv.slice(2)], { stdio: 'inherit' });
child.on('error', (err) => {
  process.stderr.write('Failed to launch GenAI Capture: ' + err.message + '\n');
  process.exit(1);
});
child.on('exit', (code, signal) => {
  if (signal) process.kill(process.pid, signal);
  else process.exit(code == null ? 0 : code);
});
