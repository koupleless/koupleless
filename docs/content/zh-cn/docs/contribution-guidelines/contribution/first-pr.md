---
title: 完成第一次 PR 提交
date: 2023-09-21T10:28:35+08:00
weight: 200
---

## 认领或提交 Issue
不论您是修复 bug、新增功能或者改进现有功能，在您提交代码之前，请在 [SOFAServerless](https://github.com/sofastack/sofa-serverless) 或 [SOFAArk](https://github.com/sofastack/sofa-ark) GitHub 上认领一个 Issue 并将 Assignee 指定为自己（新人建议认领 <b>good-first-issue</b> 标签的新手任务）。或者提交一个新的 Issue，描述您要修复的问题或者要增加、改进的功能。这样做的好处是能避免与其他人的**工作重复**。

## 获取源码
要修改或新增功能，在提 Issue 或者领取现有 Issue 后，点击左上角的`fork`按钮，复制一份 SOFAServerless 或 SOFAArk 主干代码到您的代码仓库。


## 拉分支
SOFAServerless 和 SOFAArk 所有修改都在个人分支上进行，修改完后提交 `pull request`，当前在跑通 PR 流水线之后，会由相应组件的 PMC 或 Maintainer 负责 Review 与合并代码到主干（master）。因此，在 fork 源码后，您需要：

-  下载代码到本地，这一步您可以选择 git/https 方式：
```
git clone https://github.com/您的账号名/sofa-serverless.git
```
```
git clone https://github.com/您的账号名/sofa-ark.git
```

-  拉分支准备修改代码：
```
git branch add_xxx_feature
```
<br />执行完上述命令后，您的代码仓库就切换到相应分支了。执行如下命令可以看到您当前分支：
```
  git branch -a
```
如果您想切换回主干，执行下面命令：
```
  git checkout -b master
```
如果您想切换回分支，执行下面命令：
```
  git checkout -b "branchName"
```


## 修改代码提交到本地
拉完分支后，就可以修改代码了。

### 修改代码注意事项

- 代码风格保持一致。SOFAServerless arklet 和 sofa-ark 通过 Maven 插件来保持代码格式一致，在提交代码前，务必先本地执行：
```
mvn clean compile
```
module-controller 和 arkctl Golang 代码的格式化能力还在建设中。

-  补充单元测试代码。
-  确保新修改通过所有单元测试。
-  如果是 bug 修复，应该提供新的单元测试来证明以前的代码存在 bug，而新的代码已经解决了这些 bug。对于 arklet 和 sofa-ark 您可以用如下命令运行所有测试：
```
mvn clean test
```
对于 module-controller 和 arkctl，您可以用如下命令运行所有测试：
```
make test
```
也可以通过 IDE 来辅助运行。

### 其它注意事项

- 请保持您编辑的代码使用原有风格，尤其是空格换行等。
- 对于无用的注释，请直接删除。注释必须使用英文。
- 对逻辑和功能不容易被理解的地方添加注释。
- 务必第一时间更新 docs/content/zh-cn/ 目录中的 “docs”、“contribution-guidelines” 目录中的相关文档。

修改完代码后，执行如下命令提交所有修改到本地：
```
git commit -am '添加xx功能'
```


## 提交代码到远程仓库
在代码提交到本地后，就是与远程仓库同步代码了。执行如下命令提交本地修改到 github 上：
```
git push origin "branchname"
```
如果前面您是通过 fork 来做的，那么这里的 origin 是 push 到您的代码仓库，而不是 SOFAServerless 的代码仓库。


## 提交合并代码到主干的请求
在的代码提交到 GitHub 后，您就可以发送请求来把您改好的代码合入 SOFAServerless 或 SOFAArk 主干代码了。此时您需要进入您的 GitHub 上的对应仓库，按右上角的 `pull request`按钮。选择目标分支，一般就是 `master`，当前需要选择组件的 [Maintainer](../../role-and-promotion#member-list) 或 [PMC](../../role-and-promotion#member-list) 作为 Code Reviewer，如果 PR 流水线校验和 Code Review 都通过，您的代码就会合入主干成为 SOFAServerless 的一部分。

### PR 流水线校验
PR 流水线校验包括：

1. CLA 签署。第一次提交 PR 必须完成 CLA 协议的签署，如果打不开 CLA 签署页面请尝试使用代理。
2. 自动为每个文件追加 Apache 2.0 License 声明和作者。
3. 执行全部单元测试且必须全部通过。
4. 检测覆盖率是否达到行覆盖 >= 80%，分支覆盖 >= 60%。
5. 检测提交的代码是否存在安全漏洞。
6. 检测提交的代码是否符合基本代码规范。

以上校验必须全部通过，PR 流水线才会通过并进入到 Code Review 环节。

### Code Review
当您选择对应组件的 [Maintainer](../../role-and-promotion#member-list) 或 [PMC](../../role-member-list#member-list) 作为 Code Reviewer 数天后，仍然没有人对您的提交给予任何回复，可以在 PR 下面留言并 at 相关人员，或者在社区钉钉协作群中（钉钉群号：24970018417）直接 at 相关人员 Review 代码。对于 Code Review 的意见，Code Reviewer 会直接备注到到对应的 PR 或者 Issue 中，如果您觉得建议是合理的，也请您把这些建议更新到您的代码中并重新提交 PR。

### 合并代码到主干
在 PR 流水线校验和 Code Review 都通过后，就由 SOFAServerless 维护人员操作合入主干了，代码合并之后您会收到合并成功的提示。


<br/>
