---
title: Completing the First PR Submission
date: 2024-01-25T10:28:32+08:00
description: Completing the First Koupleless PR Submission
weight: 200
---

## Claim or Submit an Issue
Regardless of whether you're fixing a bug, adding a new feature, or improving an existing one, before you submit your code, please claim an issue on [Koupleless](https://github.com/koupleless/koupleless) or [SOFAArk](https://github.com/sofastack/sofa-ark) GitHub and assign yourself as the Assignee (novices are encouraged to claim tasks tagged with <b>good-first-issue</b>). Alternatively, submit a new issue describing the problem you want to fix or the feature you want to add or improve. Doing so helps avoid **duplicate work** with others.

## Obtaining the Source Code
To modify or add features, after claiming or taking an existing issue, click the `fork` button in the upper left corner to make a copy of Koupleless or SOFAArk's mainline code to your code repository.

## Creating a Branch
All modifications to Koupleless and SOFAArk are made on individual branches. After forking the source code, you need to:

- Download the code to your local machine, either via git/https:
```
git clone https://github.com/your-username/koupleless.git
```
```
git clone https://github.com/your-username/sofa-ark.git
```

-  Create a branch to prepare for modifying the code:
```
git branch add_xxx_feature
```
<br />After executing the above command, your code repository will switch to the respective branch. You can verify your current branch by executing the following command:
```
  git branch -a
```
If you want to switch back to the mainline, execute the following command:
```
  git checkout -b master
```
If you want to switch back to a branch, execute the following command:
```
  git checkout -b "branchName"
```


## Modifying and Submitting Code Locally
After creating a branch, you can start modifying the code.

### Things to Consider When Modifying Code

- Maintain consistent code style. Koupleless arklet and sofa-ark use Maven plugins to ensure consistent code formatting. Before submitting the code, make sure to execute:
```
mvn clean compile
```
The formatting capability for module-controller and arkctl's Golang code is still under development.

- Include supplementary unit test code.
- Ensure that new modifications pass all unit tests.
- If it's a bug fix, provide new unit tests to demonstrate that the previous code had bugs and that the new code fixes them. For arklet and sofa-ark, you can run all tests with the following command:
```
mvn clean test
```
For module-controller and arkctl, you can run all tests with the following command:
```
make test
```
You can also use an IDE to assist.

### Other Considerations

- Please keep the code you edit in the original style, especially spaces, line breaks, etc.
- Delete unnecessary comments. Comments must be in English.
- Add comments to logic and functionalities that are not easily understood.
- Ensure to update the relevant documents in the `docs/content/zh-cn/` directory, specifically in the `docs` and `contribution-guidelines` directories.

After modifying the code, commit all changes to your local repository using the following command:
```
git commit -am 'Add xx feature'
```


## Submitting Code to Remote Repository
After committing the code locally, it's time to synchronize the code with the remote repository. Submit your local modifications to GitHub with the following command:
```
git push origin "branchname"
```
If you used fork earlier, then here "origin" pushes to your code repository, not Koupleless's repository.


## Requesting to Merge Code into Main Branch
After submitting the code to GitHub, you can request to merge your well-improved code into Koupleless's or SOFAArk's mainline code. At this point, you need to go to your GitHub repository and click the `pull request` button in the upper right corner. Select the target branch, usually `master`, and the [Maintainer](../../role-and-promotion#member-list) or [PMC](../../role-and-promotion#member-list) of the corresponding component as the Code Reviewer. If the PR pipeline check and Code Review are both successful, your code will be merged into the mainline and become a part of Koupleless.

### PR Pipeline Check
The PR pipeline check includes:

1. CLA signing. The first time you submit a PR, you must sign the CLA agreement. If you cannot open the CLA signing page, try using a proxy.
2. Automatic appending of Apache 2.0 License declaration and author to each file.
3. Execution of all unit tests, and all must pass.
4. Checking if the coverage rate reaches line coverage >= 80% and branch coverage >= 60%.
5. Detecting if the submitted code has security vulnerabilities.
6. Checking if the submitted code complies with basic code standards.

All the above checks must pass for the PR pipeline to pass and enter the Code Review stage.

### Code Review
If you choose the [Maintainer](../../role-and-promotion#member-list) or [PMC](../../role-and-promotion#member-list) of the corresponding component as the Code Reviewer, and after several days, there is still no response to your submission, you can leave a message below the PR and mention the relevant people, or directly mention them in the community DingTalk collaboration group (DingTalk group ID: 24970018417) to review the code. The comments on the Code Review will be directly noted in the corresponding PR or Issue. If you find the suggestions reasonable, please update your code accordingly and resubmit the PR.

### Merging Code into Main Branch
After the PR pipeline check and Code Review are both successful, Koupleless maintainers will merge the code into the mainline. After the code is merged, you will receive a notification of successful merging.


<br/>
