---
name: iteration-delivery
description: >
  当用户提出一个新的迭代需求、新功能、或要求推进一个 step 时使用本技能。
  按四阶段推进：Requirements（需求与验收标准）→ Design（基于当前项目结构的方案设计）
  → Development（逐步开发顺序与做法）→ Testing（本地测试方案），
  每个迭代任务在 docs/ 下一个独立文件夹产出四份文档。触发词：新需求、新迭代、新功能、推进 step、开发 XX。
---

# 迭代交付方法（iteration-delivery）

## 目标
把一个新的迭代需求拆解为可执行、可验收的四份文档，逐步推进直到本地测试通过。

## 流程总览
Requirements → Design → Development → Testing → 代码实现 → 本地测试

- 四阶段依次执行，上一份文档产出并确认后再进入下一阶段，禁止跳步。
- 每份文档基于「当前项目结构」与「上一份文档的结论」来写。
- 每份文档末尾必须有「完成定义（DoD）」和「进入下一阶段的门禁」。

## 文档存放与命名
docs/<迭代名>/requirements.md
docs/<迭代名>/design.md
docs/<迭代名>/development.md
docs/<迭代名>/testing.md
<迭代名> 使用短横线小写命名（如 initial-mvp、week2-knowledge-comment）。
每个迭代任务一个独立文件夹，一个文件夹内固定包含 requirements/design/development/testing 四个文件。

## 阶段一：Requirements（需求与验收标准）
- 输入：用户原始需求。
- 使用模板：templates/requirements.md
- DoD：范围边界清晰，每条需求都有可测试的验收标准。

## 阶段二：Design（方案设计）
- 输入：Requirements + 当前项目结构（先浏览 src/、pom.xml）。
- 使用模板：templates/design.md
- DoD：方案细化到包/类/接口/表结构/API 级别，标注集成点与风险。

## 阶段三：Development（逐步开发）
- 输入：Design。
- 使用模板：templates/development.md
- DoD：拆成有序 step，每个 step 有「涉及文件 + 做法 + 验证点」。

## 阶段四：Testing（本地测试）
- 输入：Development + 已实现代码。
- 使用模板：templates/testing.md
- DoD：覆盖单元/切片/端到端/手工验收，明确通过标准与失败处理。

## 门禁规则
- Requirements 未确认不写 Design；Design 未确认不写 Development；以此类推。
- 每阶段产出后，如涉及代码变更先运行 .\mvnw.cmd test 再进入下一阶段。
- 本地测试通过是「step 完成」的唯一标志。
