#!/usr/bin/env bash
# JaCoCo 行覆盖率门禁（需先 mvn -Pcoverage verify）
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MIN="${COVERAGE_MIN:-0.50}"
cd "$ROOT"
shopt -s globstar nullglob
files=(**/target/site/jacoco/jacoco.csv)
if [[ ${#files[@]} -eq 0 ]]; then
  echo "未找到 jacoco.csv，请先运行: mvn -Pcoverage verify" >&2
  exit 1
fi
below=()
for csv in "${files[@]}"; do
  read -r missed covered < <(awk -F',' 'NR>1 {m+=$8; c+=$9} END {print m+0, c+0}' "$csv")
  total=$((missed + covered))
  [[ $total -eq 0 ]] && continue
  ratio=$(awk "BEGIN {printf \"%.4f\", $covered/$total}")
  echo "$csv line coverage $ratio"
  awk "BEGIN {exit !($ratio < $MIN)}" && below+=("$csv ($ratio)")
done
if [[ ${#below[@]} -gt 0 ]]; then
  echo "低于阈值 $MIN: ${below[*]}" >&2
  exit 1
fi
echo "全部模块行覆盖率 >= $MIN"
