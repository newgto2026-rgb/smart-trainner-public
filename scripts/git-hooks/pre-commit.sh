#!/usr/bin/env sh

set -eu

branch="$(git symbolic-ref --quiet --short HEAD 2>/dev/null || true)"
if [ "$branch" = "main" ] || [ "$branch" = "master" ]; then
  echo "[Policy] main/master에서 직접 commit할 수 없습니다. PR 브랜치를 사용하세요." >&2
  exit 1
fi
