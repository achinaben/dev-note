# AGENTS.md — 仓库根目录

本仓库的可运行产品为 **scm-platform**（供应链 ERP/OMS/WMS/TMS 微服务演示）。其余目录为业务/架构笔记，无需构建。

## Cursor Cloud

开发与验证请进入 **scm-platform** 子目录，并阅读其中的 **AGENTS.md** 中「Cursor Cloud specific instructions」小节（JDK 17、起服端口、测试与 E2E 命令）。

快速命令：

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
cd scm-platform && mvn test
cd scm-platform/scripts && bash start-all.sh
```
