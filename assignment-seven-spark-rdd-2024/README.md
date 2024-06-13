# Assignment Seven Apache Spark RDD (10%)

**Due date Friday 24 May at 11:30 pm**

## Instructions and important things to note

* Fork the project by clicking the "Fork" button on the top-right corner.
* **Make sure that the visibility of your new repository (the one you just forked) is set to private.**
* You can obtain the URL of your forked project by clicking the "Clone" button (beside "Fork") and then the “copy to clipboard icon" at the right of the dropdown.
* Clone the new repository to your computer and open the project with IntelliJ, by using command line or IntelliJ interface.
    * Using the Git command line: You will need to install a Git client yourself if you are not using the lab machines. In a termial on your computer, clone the assignment one repository to your computer using the command “git clone `<url you copied>`”. Open the project into your IntelliJ workspace. (*File* / *Open* the project directory).
    * IntelliJ: Alternatively, you could get the project from GitLab repository via IntelliJ interface. From the menu bar, *Git* > *Clone* > *Copy* the url to Repository URL > *Clone* (Reference: Get a project from version control: https://www.jetbrains.com/help/idea/import-project-or-module-wizard.html)
* Commit your changes regularly, providing an informative commit message and using Git inside IntelliJ (Commit and Changes Tutorial: https://www.jetbrains.com/help/idea/commit-and-push-changes.html)

You are expected to make at least 10 commits with messages to explain what have changed. 5 out of 50 marks are allocated for this.


## Installing Scala Plugin within IntelliJ

The tutorial of how to install Scala Plugin with IntelliJ see here (https://www.jetbrains.com/help/idea/discover-intellij-idea-for-scala.html)

Alternatively, click on WikipediaStats.scala file in IntelliJ. IntelliJ displays the below suggestion message 
> <pre>"Plugins supporting *.scala found."                                      Install Plugins</pre>

Select `Install Plugins` to install Scala Plugin in IntelliJ. Restart IntelliJ to start the plugin.


## Install Apache Spark on your computer

Download Apache Spark from http://spark.apache.org/downloads.html. Unpack it somewhere on your computer. You should use version Spark 2.0.x up that supports new DataFrame and Dataset API. We will call the install directory your `SPARK_HOME` directory.

Note: A copy of Spark distribution is also available in /courses/compx553/spark-2.3.1-bin-hadoop2.7

You can use it by mounting the drive using a simple command on the terminal on lab machines:

```
$ ls /courses/compx553/spark-2.3.1-bin-hadoop2.7 
```


## Set up a Scala project in IntelliJ

Before compiling and running `WikipediaStats.scala`, you need to include **SPARK** libraries in `SPARK_HOME/jars` to IntelliJ.

From the menu bar, select *File* > *Project Structure* > *Project Setting* > *Modules* > *Dependencies* Tab

*+* Button > *Library* > *Java* > Select `SPARK_HOME/jars` folder to include all jar files to the project > OK

```diff
- !Important! Apache Spark needs to use the fixed version (2.11.11) of Scala compiler. The latest Scala compiler (>= 2.12.0) may cause errors.
```

Note: The datasets used in this assignment are available on lab machines at

```
     /courses/compx553/spark/wikipedia_meta_history1 

     /courses/compx553/spark/wikipedia_meta_history2 
```

copy them in the top level of your Eclipse workspace. If you are using lab machines, you can specify the paths in the code.


Right click on `WikipediaStats.scala` > Modify Run configuration > Add the below program arguments 

> **wikipedia_meta_history1 wikipedia_meta_history2**

See if you can run the program within IntelliJ and get the output message like the below:

<pre>
270,AcademyAwards/BestPicture
	233470,JimboWales,2001-02-06T21:08:11Z
	115708,Conversion script,2002-02-25T15:43:11Z
	15899012,Koyaanis Qatsi,2002-07-12T20:44:53Z
	160873340,Closedmouth,2007-09-28T08:07:49Z
	629654556,Jdaloner,2014-10-15T01:33:57Z

Processing generateWikipediaRdd took 511 ms.
</pre>
## Tasks
Implement empty methods specified below in WikipediaStats.scala and answer the corresponding questions in this page.

Note: There are some helper methods already implemented. For empty helper methods, I used them when doing the assignment myself, but I'll let you to decide whether to implement and use them or not.
 
 
**1.** *numOfArticlesAndRevisions*
```
Q1. How many wikipedia articles and revisions are in wikipedia_meta_history1?
Total number of revisions: 95993
Total number of articles: 50
```

**2.** *numOfUniqueContributors*
```
Q2. How many unique contributors in wikipedia_meta_history1?
34671
```

**3.** *yearsWikipediaArticlesCreated*	
```
Q3. What are the years in which the wikipedia articles were created (i.e. the first revision) in wikipedia_meta_history1?	
2002, 2001
```

**4.** *numOfArticlesWithMinRevisionsAndMinContributors*
```
Q4. How many wikipedia articles that have at least 100 (inclusive) revisions and were contributed by at least 10 (inclusive) different contributors in wikipedia_meta_history1?
50
```

**5.** *sortArticlesByNumRevisions*
```
Q5. What are the top three wikipedia articles that have the largest number of revisions	in wikipedia_meta_history1?
Article: Abraham Lincoln, Number of Revisions: 15431
Article: Ayn Rand, Number of Revisions: 11208
Article: Algeria, Number of Revisions: 9047
```

**6.** *sortContributorsByNumRevisions*
```
Q6. Who are the top three contributors that have made the largest number of revisions in wikipedia_meta_history1?
Contributor: Hoppyh, Number of Revisions: 1339
Contributor: RL0919, Number of Revisions: 948
Contributor: ClueBot NG, Number of Revisions: 945
```

**7 and 8.** *groupArticleRevisionsByYear*, *lookupYearGroupedArticleRevisions* and two functions in *testLookupYearGroupedArticleRevisions*	
```
Q7. How many revisions were made in 2014 in wikipedia_meta_history1?
3235

Q8. How many unique contributors have made revisions in 2014 in wikipedia_meta_history1? 	
1527
```

**9.** *contributorAndNumRevisionsPerYear*	
```
Q9. How many revisions were made by a contributor called "Magioladitis in 2013 in wikipedia_meta_history1?
12
```

**10 and 11.** *cogroupTwoDatasetsByContributor*, *filterContributorCogroupedDatasets* and two predicates in *testFilterContributorCogroupedDatasets*
```
Q10. How many contributors who have contributed to both wikipedia_meta_history1 and wikipedia_meta_history2?
65831

Q11. How many contributors who have contributed to both wikipedia_meta_history1 and wikipedia_meta_history2 in 2013? 	
3290
```

## Submission checklists
* Make sure you have completed 11 questions above in this page. 
* Make sure you have pushed all changes to the repository. Ensure that you can see your changes on GitLab before submission.


## Grading (50 marks)

Marks are given based on the correctness of answers, and good use of RDD and Scala collection operations.

|Marks | Allocated to |
|------|--------|
|5 | 10 commit comments|
|6  | Q1-Q6	 (1 mark each) |
|10 | Q7-Q11 (2 marks each) |
|2 |numOfArticlesAndRevisions|
|2 | numOfUniqueContributors|
|2 | yearsWikipediaArticlesCreated|
|2 | numOfArticlesWithMinRevisionsAndMinContributors|
|2 | sortArticlesByNumRevisions|
|2 | sortContributorsByNumRevisions|	
|2 |groupArticleRevisionsByYear|	
|2 | lookupYearGroupedArticleRevisions|	
|2 | contributorAndNumRevisionsPerYear|	
|4 | cogroupTwoDatasetsByContributor|	
|2 | filterContributorCogroupedDatasets|
|5 | Good use of RDD.persist to store RDDs that are used multiple times.| 	


