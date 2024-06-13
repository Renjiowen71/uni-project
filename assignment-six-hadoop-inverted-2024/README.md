# Assignment Six Hadoop Inverted Index (15%)

**Due date Friday 17 May at 11:30 pm**

## Instructions and important things to note

* Fork the project by clicking the "Fork" button on the top-right corner.
* **Make sure that the visibility of your new repository (the one you just forked) is set to private.**
* You can obtain the URL of your forked project by clicking the "Clone" button (beside "Fork") and then the “copy to clipboard icon" at the right of the dropdown.
* Clone the new repository to your computer and open the project with IntelliJ, by using command line or IntelliJ interface.
    * Using the Git command line: You will need to install a Git client yourself if you are not using the lab machines. In a termial on your computer, clone the assignment one repository to your computer using the command “git clone `<url you copied>`”. Open the project into your IntelliJ workspace. (*File* / *Open* the project directory).
    * IntelliJ: Alternatively, you could get the project from GitLab repository via IntelliJ interface. From the menu bar, *Git* > *Clone* > *Copy* the url to Repository URL > *Clone* (Reference: Get a project from version control: https://www.jetbrains.com/help/idea/import-project-or-module-wizard.html)
* Commit your changes regularly, providing an informative commit message and using Git inside IntelliJ (Commit and Changes Tutorial: https://www.jetbrains.com/help/idea/commit-and-push-changes.html)

You are expected to make at least 20 commits with messages to explain what have changed. 5 out of 100 marks are allocated for this.

## Task 1: Counting Words


### 1. Install Hadoop on your computer (Linux machines).

Download Hadoop binary distribution from https://hadoop.apache.org/releases.html, and unpack it somewhere on your computer. I have tested version 2.10.x, but you could try the newest ones 3.x.x. The hadoop package to be downloaded is called hadoop-2.10.x.tar.gz. We will call the installation directory your HADOOP_HOME directory.

Note: If you use Linux machines in Lab one and run into the disk quota problem, a copy of hadoop-2.10.1 is available in /courses/compx553.

### 2. Add Hadoop libraries to IntelliJ 
Before you can compile and run the WordCount example, tell IntelliJ where your HADOOP_HOME directory is. (If you are using hadoop-2.10.1 in our lab machine, HADOOP_HOME is /courses/compx553/hadoop-2.10.1).

* Update the HADOOP_HOME directory (Open *File* > *Settings* > *Appearance & Behavior* > *Path Variable* > Click on HADOOP_HOME and update the value with your path)
* Open Project Structure (*File* > *Project Structure*...) and go into *Modules* tab under Project Setting.
* Add the paths of Hadoop libraries. Click on *Dependencies* > Click (+) to add library > Select *Libraries* > *Java* > Point to the below directories under your HADOOP_HOME (/courses/compx553/hadoop-2.10.1) and add each directory individually to the project.
```
        share/hadoop/common
        share/hadoop/mapreduce
        share/hadoop/hdfs
        share/hadoop/yarn
        share/hadoop/common/lib
```
Once these directories are added to the project, now IntelliJ should compile the source files without errors. 

* Configure your project library and have the access to the Hadoop Javadocs. To do this, go into *Project Structure* and then click *Libraries* tab and select one of libraries to edit its source path under *Source* tab.
  The source folder is named `sources` under the library folder, e.g. the folder `share/hadoop/common/sources` stores the source code of `share/hadoop/common library` folder. The source code folders are listed as follows.
```
        share/hadoop/common/sources
        share/hadoop/mapreduce/sources
        share/hadoop/hdfs/sources
        share/hadoop/yarn/sources
```
Now when you put your cursor over Hadoop classes like *IntWritable* or *Mapper*, you should see the Javadocs for it within Eclipse. Alternatively, you can just point your browser to your HADOOP_HOME/share/docs/hadoop/index.html and browse the docs that way. 

* ***LewisCarroll*** is available in the project directory and is our input directory containing several books by Lewis Carroll. Run the WordCount program to see what it does. If you run the main method of the WordCount class with no parameters you should see an Array out of bounds error, or the message:
```
Usage: WordCount inputdir outputdir
```
   
* Run it again with the parameters ***LewisCarroll output*** and you should see lots of log messages from the Hadoop run. You will find an output file in `output/part-r-00000`. Move the map and reduce class into their own files, called WordMapper and WordReducer, to make them easier to test. Hint: IntelliJ has a refactor command to do this. Read *WordCount.java* code and familiarize yourself with how the mapper and reducer classes work.

* Set the number of reducers to 2 or 3 in *WordCount.java*. Run your program again (each time you run it, you need to change the main arguments to refer to a new output directory, or you can delete the old output directory in code or manually). Have a look at the output files in `output/part-r-00000`, `output/part-r-00001`.... Your next task is to modify the *WordReducer* class so that it actually counts the number of occurrences of each word, rather than just outputting a constant number. It needs to loop over the input values, summing them up.

* Too many outputs! Words with only a few occurrences are not very interesting. So modify your WordReducer class so that it outputs only words that occur 500 times or more.

* Run your program again (each time you run it, you need to change the main arguments to refer to a new output directory, or you can delete the old output directory in code or manually). Your output file should be a lot smaller now, and should only contain words that occur at least 500 times. By my calculations, you should find that the word 'a' occurs 3742 times, and the word 'Alice' occurs 537 times. 

## Task 2: Building a sentence-level Inverted Index

Once you get the WordCount example running correctly,  your next task is to use Hadoop MapReduce to produce a sentence-level Inverted Index for a set of open access journal articles (https://core.ac.uk/services#dataset).

A typical Inverted Index is an index data structure storing a mapping from content, such as words or numbers, to a set of documents. In this assignment, instead of listing references to documents for each word like a typical inverted index does (https://en.wikipedia.org/wiki/Inverted_index), your program will output

* each word and the total occurrences of the word in the whole dataset,
* one or more lines (all indented by one tab) that show a document ID and the occurrences of the word in that document,
* one or more lines (all indented by one tab) that show sequences of sentences and the position of the word in a sentence. For example, the position 
of the word "What" and "reform?" in the sentence "What might be the effects of this reform?" are 0 and 7 respectively.

The following shows an example output for the word "European" in one of the output files (i.e., part-r-00000, etc.) 

```
European 42
		100880  1
			36  0
		100608  1
			8   7
		100738  3
			2   20
			4   5
			6   3
        .....        
```
The results above indicate the total count of "European" is 42. It occurs

* 1 time in document 100880, in the 36th sentence at the position 0.
* 1 time in document 100608, in the 8th sentence at the position 7.
* 3 times in document 100738, in the 2th sentence at the position 20, in the 4th sentence at the position 5, in the 6th sentence at the position 3, and so on.

Apart from implementing a InvertedIndexMapper and InvertedIndexReducer to produce the correct output, you will also need to

*  In the InvertedIndexMapper class, filter all stop words (see stop_words.txt in the project folder for a list of common stop words.) That means the inverted index does't contain stop words like a, an, the etc. Your program needs to load stop words from a file rather than hard-code them in Mapper.
*  Write a combiner (*InvertedIndexCombiner*) to reduce the amount of data being shuffled (e.g., indicated by the system counters)
*  Write a comparator (*InvertedIndexComparator*) class that sorts words by length in a descending order. Word length is the number of characters in a word, for example, the length of the word "map" is 3. That means the output files will display the words from longest to shortest, together with counts and postings.
*  Find out what is the maximum length of words in the output files after setting the comparator class. Write a partitioner (*InvertedIndexPartitioner*) that uses the maximum word length (you can hardcode the maximum word length in partitioner) to partition words based on the word length and the number of reducers so that a word whose length falls in a particular range (e.g. 1-5) goes to a particular partition.
    Assuming the number of reducers is 3 and the maximum word length is 15, there will be three partitions 0, 1 and 2. A word with the length between 1-5 will go to partition 0, between 6-10 to partition 1, and between 11-15 to partition 2. Make sure your program will work for any number of reducers (i.e. don't hard-code the number of the reducers in Partitioner).
* Use custom counters to report some useful and interesting statistics.

### Data files

The data files you are using in this assignment are open access Journal articles (https://core.ac.uk/services#dataset) that have been pre-processed so that each file contains about 1000 articles separated by <Document id="doc_id">...</Document>tags. Sentences in a document are separated by a linefeed "\n".
There are four alternative directories of data files that you can use as input:
```
      /courses/compx553/CORE/core1 contains about 2% of the files (3 *.gz files, ~29Mb)
      /courses/compx553/CORE/core10 contains about 10% of the files (15 *.gz files, ~130Mb)
      /courses/compx553/CORE/core50 contains about 50% of the files (79 *.gz files, ~636Mb)
      /courses/compx553/CORE/core100 contains about 100% of the files (129 *.gz files, ~1.2G)
```   
You can start with CORESS_1 stored in the *core* directory in the project folder, 
and then move to other bigger subsets to get an idea of the run time and output size of your program. 



### Grading (total 100 marks)

|Marks|Allocated to|
|-----|--------|
|10 | task 1|
|5 | At least **twenty informative** Commit comments |
|5 | Code comments in java files | 
|10 | your program runs and finishes in a reasonable time and produces correct output | 
|15 | Mapper|
|5 |Filtering stop words|
|15 |Reducer|
|10 | Combiner|
|10 | Comparator|
|10 | Partitioner|
|5 |Counters|




