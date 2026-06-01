#!/usr/bin/env bash
# 释放 8081~8084（Linux）
set -euo pipefail
for p in 8081 8082 8083 8084 8085; do
  if command -v fuser >/dev/null 2>&1; then
    fuser -k "${p}/tcp" 2>/dev/null || true
  elif command -v lsof >/dev/null 2>&1; then
    pid=$(lsof -ti ":$p" 2>/dev/null || true)
    [[ -n "${pid:-}" ]] && kill -9 $pid 2>/dev/null || true
  fi
done
echo "完成。"
