# SimpleDifficulty Temperature HUD

Minecraft 1.12.2 的 SimpleDifficulty 客户端温度 HUD 附属模组。作者：**KingJJ**。

[下载正式版](https://github.com/King1309773/SDTemperatureHUD/releases/latest) · [1.6.1 更新说明](CHANGELOG.md) · [反馈问题](https://github.com/King1309773/SDTemperatureHUD/issues)

## 功能

- 紧凑显示当前体温、目标温度及变化趋势，数字随温度配色。
- 温度分析面板：基础环境、装备、季节、附近热源等影响；默认按住 **Tab** 查看，可在配置中调整模式。
- 两级温度预警：接近危险时慢速脉冲，危险时快速闪烁；提示音默认关闭，可单独开启。
- 游戏内布局编辑、独立缩放、颜色主题、显示项目和外观即时预览。
- 中文配置，常用 / 外观 / 提示 / 高级四页。
- 移动影响变化提示独立限频：至少间隔 30 秒游戏活动时间，冷却中不刷新、不补发；热源、装备等其他来源不受这项限制。
- 可选 Xaero 联动：小地图原生“群系基础温度”信息项，以及大地图顶部群系名称后的彩色温度。

地图温度是 SimpleDifficulty 客户端规则下的**群系基础参考值（0–25）**，不是摄氏度、当前体温或目标温度，不包括季节、天气、海拔、装备和附近热源等修正。多人服务器的实际规则可能与客户端不同。

## 安装

需要 Minecraft **1.12.2**、Forge 和 **SimpleDifficulty 0.3.9**。发行构建基于 Forge **14.23.5.2860** / Java 8。

1. 从 Releases 下载 `sdtemperaturehud-1.6.1.jar`。
2. 退出游戏，将 JAR 放入对应实例的 `mods` 文件夹。
3. 升级时移出旧版或实验版 JAR，只保留一个版本；可以保留原配置，建议先备份。

**本模组只需客户端安装，服务器不需要安装。** SimpleDifficulty 是必需依赖；Xaero 地图是可选依赖，不装地图也能使用温度 HUD。依赖模组须自行取得，仓库与发行包不附带第三方模组。

## Xaero 联动

当前联动版本白名单（Minecraft 1.12.2）：

| 组件 | 版本 |
| --- | --- |
| Xaero Minimap | 26.5.1、26.6.0 |
| Xaero World Map | 1.46.0、1.47.0 |
| XaeroLib | 1.7.3 |

- 小地图入口：**小地图设置 → 信息设置 → 信息显示管理器 → 群系基础温度**。
- 开关、拖动排序、前缀颜色、背景和对齐由 Xaero 管理。数字自动匹配冷热颜色，近似到原生 16 色。
- 大地图温度接在顶部原生群系名称之后，可在本模组“地图联动”设置中开关。
- 地图区域标签暂不启用；未知或不可用数据留白，不主动生成或加载区块。
- 旧 `minimapEnabled` 配置值只作为信息项首次默认值，之后以 Xaero 原生偏好为准。
- 非白名单版本会保守停用相应联动；白名单不表示全部版本组合和边界场景均已逐项实测。

Tab 也可能被原版玩家列表使用，可按自己的整合包设置调整按键。

## 从源码构建

使用 **JDK 8**。项目附带 Gradle 4.10.3 wrapper，使用 ForgeGradle 3.0.197 / stable_39 映射。

从各模组的官方发布渠道取得以下依赖，按文件名放入项目 `libs/` 目录（不要提交到 Git）：

```text
SimpleDifficulty-1.12.2-0.3.9.jar
xaerolib-forge-1.12.2-1.7.3.jar
xaerominimap-forge-1.12.2-26.6.0.jar
xaeroworldmap-forge-1.12.2-1.47.0.jar
```

Windows：

```powershell
.\gradlew.bat build
```

Linux / macOS：

```sh
sh gradlew build
```

首次构建需要联网下载 Gradle、Forge 和测试依赖；缓存完整后可以添加 `--offline`。输出为 `build/libs/sdtemperaturehud-1.6.1.jar`。Xaero 构建依赖不会打包进发行 JAR。

`reference/sdtemperaturehud-1.2.1.jar` 是本模组自身的历史回归测试样本，仅供测试，不应安装或与新版同时放入 mods。测试也包含原生 Xaero 信息管理器的无界面回归，但不代替游戏内渲染验收。

## 1.6.1 文件校验

SHA-256：

```text
9EEEA225DDD363EAD943268160489FFBA73CFC226E1BF28AD20919199ED3E615  sdtemperaturehud-1.6.1.jar
```

1.6.1 发布前重新构建，75 个测试套件、284 项测试全部通过，失败、错误、跳过均为 0；构建包与此前提供的 1.6.1 移动提示修复测试包逐字节相同。自动测试与源码审查不等于已完成真实游戏画面验收；1.6.1 的游戏内验证仍待进行。

## 反馈

请在 Issues 提供 Minecraft / Forge / SimpleDifficulty / Xaero 版本、复现步骤及截图或相关日志。提交前请移除日志中的账号、服务器地址和其他私人信息。

本仓库暂未指定开源许可证。
