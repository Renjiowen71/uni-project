# Assignment Four Java Streams (5%)

## Due date 12th April at 11:30 pm


# Instructions

* Fork the project by clicking the "Fork" button on the top-right corner.
* **Make sure that the visibility of your new repository (the one you just forked) is set to private.**
* You can obtain the URL of your forked project by clicking the "Clone" button (beside "Fork") and then the “copy to clipboard icon" at the right of the dropdown.
* Clone the new repository to your computer and open the project with IntelliJ, by using command line or IntelliJ interface
   * Using the Git command line: You will need to install a Git client yourself if you are not using the lab machines. In a termial on your computer, clone the assignment one repository to your computer using the command “git clone `<url you copied>`”. Open the project into your IntelliJ workspace. (File / Open the project directory).
   * IntelliJ: Alternatively, you could get the project from GitLab repository via IntelliJ interface. From the menu bar, 'Git' > 'Clone' > Copy the url to Repository URL > 'Clone' (Referenece: Get a project from version control: https://www.jetbrains.com/help/idea/import-project-or-module-wizard.html)
* Commit your changes regularly, providing an informative commit message and using Git inside IntelliJ (Commit and Changes Tutorial: https://www.jetbrains.com/help/idea/commit-and-push-changes.html)

Commit your changes regularly, providing an informative commit message. You are expected to make at least 10 commits with messages to explain what have changed. 5 out of 60 marks are allocated for this.

## Your tasks
The goal of this assignment is to learn how to write Java lambda expressions and use Stream operations. You will implement eight empty methods provided in StreamAssignment.java and answer 10 quiz questions on Moodle. Please read the implementation specifications at the beginning of each method carefully and try to use lambda expressions (or method references), and stream operations wherever possible and appropriate.
Marks are given based on the correctness of answers, and proper use of lambda expressions (or method references) and stream operations.

The wikipedia file used is on the Moodle or at 
```
/home/compx553/stream/wiki.xml
```
and can be accessed on any Linux machines in R-block

In this assignment, we define a word that only contains English letters (i.e. a-z or A-Z), or digits, and consists of at least two characters. For example, "The, "tHe", or "1989" is a word, and "89_", and "things," (containing punctuation) are not.


## Submission Checklist
* Make sure you have pushed all changes to the repository. Ensure that you can see your changes on GitLab before submission.
* Make sure you have answered ten questions on Moodle.
* Mark sure you include the Java code to run each method of 10 questions in 'main' method of 'StreamAssignment' java file, so that we could run and test your code.

## Grading (50 marks) 

|Marks|Allocated to|
|-----|-------|
|5|At least ten informative Commit comments |
|10| Ten questions on Moodle|
|28 |#1 to #8 implementation (3.5 marks each). Marks are given based on the proper use of lambda expressions (or method references) and stream operations. |
|7 |#9 implementation. Marks are given based on the proper use of lambda expressions (or method references) and stream operations|

