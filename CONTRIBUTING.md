# Contributing to Log Spy Kt

Thank you very much for contributing to the project.

Please take a few minutes to read the following guidelines. They will help you to get you started as quickly as possible. 

## How to Contribute
The easiest way to contribute is by reporting a bug. Other ways are suggesting a new feature, proposing an improvement,
or creating a PR to solve an existing issue.

### Reporting a Bug
First of all, have a look at the existing issues. Maybe the bug you found was already reported. If not, create a new
issue. The issue that you create should include a description of how the bug can be reproduced at other systems than
yours. An often overlooked way to do that is by writing a failing test.

Because this is a logging tool, your bug report might include log statements that failed. Please make sure that you
remove any security sensitive or privacy related data from the statements before you file your bug report. We do not
want you to get in trouble.

### Requesting New Features
Please start with a look at the existing issues. There could be already an issue covering the feature you are about to
request. If not, make sure that you include in your request a detailed description how the new feature would support
you in your developers experience.

### Requesting Improvements
As with new features, please have a look at the existing issues first. If you create a new issue, make sure to include
a description of how the proposed improvement will make your developer's life better. 

### Solving Issues
If you want to solve an issue, e.g. implementing a new feature, you have to sign the contribution license agreement (CLA).
Basically, the CLA ensures that this project only contains code that complies with the principles of free software. Our
CLA is based on the widespread [Harmony contributor agreement](http://www.harmonyagreements.org/index.html). We ask
you to submit the signed CLA digitally. For that we use a process heavily inspired by 
[Medium's](https://github.com/Medium/opensource/blob/master/sign-cla.md) time saving, no frills signing process. The
necessary steps can be found on the [Sign the CLA](sign-cla.md) page.

## Style Guides
### Git Commit Messages
We encourage you to write good commit messages. Consider each commit message as a message to your future self.
Think about what information you would like to have in the future to understand a particular change. A good
introduction into common practices can be found [here](https://chris.beams.io/posts/git-commit/).

### Kotlin and Kotlin Script
Kotlin code must comply with the
[official coding conventions](https://kotlinlang.org/docs/reference/coding-conventions.html#formatting). The project has
an [auto formatter for Kotlin](https://github.com/personio/kotlin-auto-formatter) that does all the work for you. You
can run it via `gradle formatKotlin`.

### Java
Java code must comply with the [Google Java style guide](https://google.github.io/styleguide/javaguide.html) with the
following exception. This project uses a indentation of four spaces instead of two spaces. 

## Review Process
Once you created a PR, it needs approval by at least one of the developers with merge rights. Please keep in mind that
this is a side project so the review might take some time. A reviewer will look things like the following.
- Has the author signed the CLA?
- Does the PR solve the stated issue?
- Does the PR have unwanted side effects e.g. introduce defects?
- Is the introduced/altered behaviour covered by tests?
- Does the PR comply with the style guide?

Besides the points above, a reviewer might also ask questions or suggest potential improvements. Reviewers are
requested to indicate whether a finding is an optional or required changed in the PR.

## Code of Conduct
No one likes bullies and neither do we. To make this a pleasant place for as many as people as possible, all
contributors must agree to our [code of conduct](code_of_conduct.md).