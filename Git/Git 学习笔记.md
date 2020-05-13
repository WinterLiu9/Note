---
title: Git 学习笔记
date: {date}
tags: 开发工具
categories: 开发工具
---
Git是目前世界上最先进的分布式版本控制系统
# 时光机穿梭

**$ git config --global user.name "Your Name"
$ git config --global user.email "email@example.com"**

***

* 初始化一个Git仓库，使用**git init**命令。
* 添加文件到Git仓库，分两步：
    1. 使用命令**git add <file>**，注意，可反复多次使用，添加多个文件；
    2. 使用命令**git commit -m <message>**，完成。

***

* 要随时掌握工作区的状态，使用**git status**命令。
* 如果git status告诉你有文件被修改过，用**git diff**可以查看修改内容。

***
## 版本回退
* HEAD指向的版本就是当前版本，因此，Git允许我们在版本的历史之间穿梭，
使用命令**git reset --hard commit_id**。
* 穿梭前，用**git log**可以查看提交历史，以便确定要回退到哪个版本。
* 要重返未来，用**git reflog**查看命令历史，以便确定要回到未来的哪个版本。

***
## 工作区和暂存区
**暂存区**是Git非常重要的概念
[外链图片转存失败,源站可能有防盗链机制,建议将图片保存下来直接上传(img-hcAUgRih-1587607662307)(en-resource://database/1321:1)]
***
## 撤销修改
1. 当你改乱了工作区某个文件的内容，想直接丢弃工作区的修改时，用命令**git checkout -- file**。
2. 当你不但改乱了工作区某个文件的内容，还添加到了暂存区时，想丢弃修改，
分两步，第一步用命令**git reset HEAD <file>**，就回到了场景1，第二步按场景1操作。
3. 已经提交了不合适的修改到版本库时，想要撤销本次提交，参考版本回退一节，不过前提是没有推送到远程库。

# 远程仓库
## 添加远程库
* 要关联一个远程库，使用命令
**git remote add origin git@server-name:path/repo-name.git；**

* 关联后，使用命令**git push -u origin master**第一次推送master分支的所有内容；

* 此后，每次本地提交后，只要有必要，就可以使用命令**git push origin master**推送最新修改；
***
## 从远程库克隆
* git clone git@github.com:Winter-XJTU/gitskills.git

# 分支管理
## 创建与合并分支

Git鼓励大量使用分支：
* 查看分支：git branch
* 创建分支：git branch <name>
* 切换分支：git checkout <name>
* 创建+切换分支：git checkout -b <name>
* 合并某分支到当前分支：git merge <name>
* 删除分支：git branch -d <name>
***
## 解决冲突

当Git无法自动合并分支时，就必须首先解决冲突。
解决冲突后，再提交，合并完成。
解决冲突就是把Git合并失败的文件手动编辑为我们希望的内容，再提交。
用**git log --graph**命令可以看到分支合并图。

* 分支策略
分支策略在实际开发中，我们应该按照几个基本原则进行分支管理：
首先，master分支应该是非常稳定的，也就是仅用来发布新版本，平时不能在上面干活；那在哪干活呢？干活都在dev分支上，也就是说，dev分支是不稳定的，到某个时候，比如1.0版本发布时，再把dev分支合并到master上，在master分支发布1.0版本；你和你的小伙伴们每个人都在dev分支上干活，每个人都有自己的分支，时不时地往dev分支上合并就可以了。

**git merge --no-ff -m "merge with no ff" dev**
Git分支十分强大，在团队开发中应该充分应用。合并分支时，加上--no-ff参数就可以用普通模式合并，合并后的历史有分支，能看出来曾经做过合并，而fast forward合并就看不出来曾经做过合并。

* bug分支

修复bug时，我们会通过创建新的bug分支进行修复，然后合并，最后删除；当手头工作没有完成时，先把工作现场git stash一下，然后去修复bug，修复后，再git stash pop，回到工作现场；在master分支上修复的bug，想要合并到当前dev分支，可以用git cherry-pick <commit>命令，把bug提交的修改“复制”到当前分支，避免重复劳动。

```bash
  168  vim hello.py
  170  git add hello.py
  171  git commit -m "hello.py"
  172  git stash
  //保留工作现场
  
  176  git checkout master
  177  git checkout -b issue
  179  vim readme.txt
  180  git add readme.txt
  181  git commit -m "fix bug"
  182  git checkout master
  183  git merge --no-ff -m "merge bug fix issue with no ff" issue
  //修复bug并且合并到master

  186  git checkout dev
  190  git stash list
  191  git stash pop
  //恢复工作现场


  201  git cherry-pick 891c0cf
  //在master分支上修复的bug，想要合并到当前dev分支
```





## 多人协作
因此，多人协作的工作模式通常是这样：
首先，可以试图用git push origin <branch-name>推送自己的修改；
如果推送失败，则因为远程分支比你的本地更新，需要先用git pull试图合并；
如果合并有冲突，则解决冲突，并在本地提交；没有冲突或者解决掉冲突后，再用git push origin <branch-name>推送就能成功！如果git pull提示no tracking information，则说明本地分支和远程分支的链接关系没有创建，用命令git branch --set-upstream-to <branch-name> origin/<branch-name>。这就是多人协作的工作模式，一旦熟悉了，就非常简单。

小结

查看远程库信息，使用git remote -v；
本地新建的分支如果不推送到远程，对其他人就是不可见的；
从本地推送分支，使用git push origin branch-name，如果推送失败，先用git pull抓取远程的新提交；
在本地创建和远程分支对应的分支，使用git checkout -b branch-name origin/branch-name，本地和远程分支的名称最好一致；
建立本地分支和远程分支的关联，使用git branch --set-upstream branch-name origin/branch-name；
从远程抓取分支，使用git pull，如果有冲突，要先处理冲突。