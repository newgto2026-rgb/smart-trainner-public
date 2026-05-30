#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CACHE_DIR="$ROOT/tmp/swift-module-cache"
BIN="$ROOT/tmp/audit_exercise_images"

mkdir -p "$CACHE_DIR"
CLANG_MODULE_CACHE_PATH="$CACHE_DIR" swiftc -O -framework AppKit "$ROOT/scripts/audit_exercise_images.swift" -o "$BIN"
"$BIN" --repo-root "$ROOT" "$@"
