# 质量门禁（Sonar + 覆盖率）

## 行覆盖率

CI job `coverage-gate` 在 `mvn -Pcoverage verify` 后执行 `scripts/check-coverage.sh`，默认阈值 **50%**（`COVERAGE_MIN=0.50`）。

本地：

```powershell
cd scm-platform
mvn -Pcoverage verify
.\scripts\check-coverage.ps1
```

## SonarCloud（可选）

1. 在 SonarCloud 创建项目 `scm-platform`，生成 `SONAR_TOKEN`。
2. 在 GitHub 仓库 Settings → Secrets 添加 `SONAR_TOKEN`。
3. CI job `sonar` 在 `coverage` 成功后自动 `mvn -Pcoverage,sonar sonar:sonar`（无 token 时跳过）。

`sonar-project.properties` 已配置 JaCoCo XML 与 Surefire 报告路径。

### 建议质量门

| 指标 | 建议 |
|------|------|
| 覆盖率 | ≥ 50%（与 JaCoCo 门禁一致） |
| 重复率 | ≤ 3% |
| 阻断问题 | 0 |
| 安全热点 | 已审查 |

未配置 `SONAR_TOKEN` 时不影响合并；配置后可在 Sonar 控制台启用 Quality Gate 并关联 GitHub 分支保护。
