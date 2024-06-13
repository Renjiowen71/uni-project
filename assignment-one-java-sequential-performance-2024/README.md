# Assignment One Java Sequential Performance (10%)

**Due date Friday 8th March at 11:30 pm**

## Instructions and important things to note

*  Fork the project by clicking the "Fork" button on the top-right corner.
*  **Make sure that the visibility of your new repository (the one you just forked) is set to private.** 
*  You can obtain the URL of your forked project by clicking the "Clone" button (beside "Fork") and then the “copy to clipboard icon" at the right of the dropdown.

* Clone the new repository to your computer and import the project into IntelliJ IDEA Community. These can be done through the Git command line interface
* Using the Git command line interface: 
        You will need to install a Git client yourself if you are not using the lab machines. In a terminal on your computer, clone the assignment one repository to your computer using the command “git clone <the url you copied>”.
        Open the phototool project with IntelliJ. (*File* / *Open* / *Select the 'phototool' folder of the cloned assignment*).

* Commit and push your changes regularly within IntelliJ or using Git command line, providing an informative commit message. You are expected to make at least 10 commits with messages to explain what have changed. 5 out of 50 marks are allocated for this.
* Ref: how to commit and push within IntelliJ see here (https://www.jetbrains.com/help/idea/commit-and-push-changes.html)  


### Note

* Please **DO NOT** change the package structure of Phototool, rename or remove any files/directories in the repository as we will use ANT script (build.xml) to download, compile and run your program.      
* Please do not modify the main method in *Phototool.java* to skip or combine commands for a better speedup as this is not the goal of this assignment.


## What will you learn?

The goal of this assignment is to learn how to measure the performance of a (sequential) Java program and find out the bottlenecks that slow down the program, and how to fix these bottlenecks to improve performance. The potential bottlenecks include:

* poor data structures
* excessive object allocation
* garbage collection overhead and etc.

However, if the sequential program is poorly written and run inefficiently, then adding the parallelism to the program will not do too much help to speed up the program. So in this lab exercise, we will learn how to write **efficient sequential Java program** and **fix performance bottlenecks**. These skills are necessary for learning and writing good parallel programs. 

## Dick's Problem

Dick works for a photo-processing company, and their development team has recently developed a new program that can process hundreds of photos with one command, doing a series of transformations such as rotation, resizing, and conversion to sepia colours, on a large collection of photos.

The program is designed to be run from the command line, with a sequence of photo edit commands, followed by a sequence of photo file names. The whole sequence of commands is applied to every photo. For example, if it is run with the following command line arguments:

  ```
  grayscale undo sepia half half save Eiffel.jpg Shoes.jpg Art.jpg Eiffel.jpg Shoes.jpg Art.jpg
  ```

It will process each of the 6 photos in turn. First, the **grayscale** command converts a photo from colour into gray-scale photo. Then the **undo** command erases the last changes done to the photo and reverts it to previous state (presumably someone wanted to undo a mistake). The **sepia** command converts the photo into sepia colours. The **half** command scales the photo to half its size. Lastly, the **save** command save the final photo to the file under its original name with *_edited* appended to the end, for example, *Eiffel_edited.jpg*.

Unfortunately, the delivered program processes each photo quite slowly, and seems to get slower and slower as Dick runs it on more photos. The following graph shows **6 photos** being processed (with 20 repeats of each photo, we ignore the first 3 measurements where the Java HotSpot compiler is still optimising the code, and then measure the average time of remaining runs).

![https://elearn.waikato.ac.nz/pluginfile.php/2834991/mod_resource/content/1/photo_speed0.png](https://elearn.waikato.ac.nz/pluginfile.php/2834991/mod_resource/content/1/photo_speed0.png)

My experimental machine was Intel(R) Core(TM) i5-9500 CPU @ 3.0GHz with 16 GB of memory. The 1st and 2nd photos (Eiffel.jpg and Shoes.jpg) are processed in less than 1 seconds, the 3rd photos average 3.5 seconds, and the 4th and 5th photos Eiffel.jpg and Shoes.jpg) more than 1.2 seconds, and the last photo **more than 5 seconds**. These are all too slow, and the program gets slower as more photos are processed. Dick needs to be able to process dozens or hundreds of photos at a consistent and fast speed. 

## Your Tasks

You are being employed as a consultant to fix the performance problems of this program. You have two main tasks:

* Your first task is to measure the speed to process each photo.
* After that, your second task is to measure the bottlenecks of the program and improve its performance so that it can process several photos per second.



**Task 1.** The first thing to do with performance problems is always to accurately measure the current (bad) performance, so that we know the baseline and what to improve.
  
This is non-trivial for Java programs, because the [HotSpot JIT compiler](https://docs.oracle.com/cd/E13150_01/jrockit_jvm/jrockit/geninfo/diagnos/underst_jit.html) watches your program as it runs. Whenever a loop or method is repeatedly called at runtime, the compiler briefly pauses the program, recompile that method into more efficient code using aggressive optimization techniques, e.g. inlining many other methods that it calls, and then continues running the program. So the program gets faster and faster for the first few seconds or whenever you start executing a new part of the program! 

There are also two different versions of the Java JIT compiler: the **-client** compiler is aimed at fast startup and small footprint (memory usage) so does not optimise the code very aggressively, while the **-server** compiler does more aggressive optimisation and improve the overall performance. Since this phototool application will be processing lots of photos, and overall performance is much more important, make sure you use the **-server** compiler
**(Add -server to the VM arguments**). 

 The most important technique for measuring the time taken by a chunk of Java code, is **repeat that code at least 8 times** (preferably 20 times or more), discard the first few measurements (while the HotSpot compiler is optimising the code), and then take the average of the remaining measurements. Have a look at the timing loop in the main method in **PhotoTool.java** and understand what we are measuring and how.

```java
  for (int iter = 0; iter < 20; iter++) 
             {
                // Clean up all the previously edited data and leave the original photo on the stack.
                // So that each loop iteration starts with the same stack.
                while (tool.getStackSize() > 1) {
                    tool.undo();
                }

                final long time0 = System.nanoTime();

                // Here goes the code you want to measure the speed of...

                long time1 = System.nanoTime();
                System.out.println("Processed " + cmds.size() + " cmds in " + (time1 - time0) / 1E9 + " secs.");
            }
```
For this phototool program, this timing loop is placed just inside the photo loop (for (PhotoTool tool : photos)...), so that you are measuring the time to process all the commands on each photo. When adding a timing loop like this, it is important that each iteration of the loop starts from the same state. In this case, every edited photo is pushed to the photo tool stack, so we need to use undo command to pop all the edited photos from the stack, and to ensure that the only photo on the stack is the original input photo at the beginning of every iteration of the timing loop. 

 Then run the program on 6 photos, with a command line like the following. (ref: https://www.jetbrains.com/help/idea/compiling-applications.html#package_into_jar). Exporting jar from IntelliJ has two steps: **create an artifact configuration for jars** **Build the JAR artifact**. The jar file will be located on out/artifacts/phototool_jar/phototool.jar
 
Then run the jar file from the terminal or within IntelliJ (ref: https://www.jetbrains.com/help/idea/compiling-applications.html#create_run_config_for_jar)    

```
java -server -Xmx4G -jar phototool.jar grayscale undo sepia half half save Eiffel.jpg Shoes.jpg Art.jpg Eiffel.jpg Shoes.jpg Eiffel.jpg Art.jpg
```            
            
            
Or you can run it from within IntelliJ, edit Run Configurations dialog:

    
*  VM arguments: *-server -Xmx4G*
*  Program Arguments: *grayscale undo sepia half half save Eiffel.jpg Shoes.jpg Art.jpg ...*
  

Save the output messages into a file, so that you can refer to it later. You might like to graph the performance, by loading that file into a spreadsheet like Excel or LibreOffice Calc, and graphing the time column. 

**Task 2.** Why is the program getting slower after processing several photos? One reasonable guess is the program is using more and more memory, but the garbage collection is taking longer and longer time to find enough space. To see if this is happening, add the -verbose:gc flag to the Java VM arguments (eg. before the -Xmx4G) and rerun the program. Sample output is as follows: 


        processing Art.jpg...
        [183.917s][info][gc] GC(551) To-space exhausted
        [183.917s][info][gc] GC(551) Pause Young (Normal) (G1 Evacuation Pause) 3972M->4017M(4096M) 71.378ms
        [184.125s][info][gc] GC(552) To-space exhausted
        [184.125s][info][gc] GC(552) Pause Young (Normal) (G1 Evacuation Pause) 4090M->4090M(4096M) 80.368ms
        [185.180s][info][gc] GC(553) Pause Full (G1 Evacuation Pause) 4090M->3478M(4096M) 1055.719ms
        [185.181s][info][gc] GC(547) Concurrent Cycle 2431.129ms
        [185.449s][info][gc] GC(554) Pause Young (Normal) (G1 Evacuation Pause) 3682M->3606M(4096M) 45.095ms

Note the two different kinds of output lines:

*  Young GC lines come from the *young generation garbage collector* (which recycles young / short-lived objects)
*  Full GC lines come from the more expensive full garbage collection which garbage collects the more long-lived objects as well.

Read this [Verbose Garbage Collection](https://www.baeldung.com/java-verbose-gc) in Java to understand these output messages better. From the above sample, we can interpret the output as follows. After processing Eiffel.jpg, four full garbage collectors are running up to clean the space of long-lived objects on oldGen memory space. The last line [GC(553) Pause Full (G1 Evacuation Pause) 4090M->3478M(4096M) 1055.719ms] means that GC pauses the phototool program to reduce the heap memory form 4,090M to 3,478M, and the duration time is 1055 ms. In other words, GC spent 1.0 seconds but reclaimed only a small fraction of memory space (76MB)!!!

As the program uses more memory and gets closer to the maximal heap size, the garbage collectors will have to run more and more often. As a result, our program will go slower and slower.  

## Hints for Task 2: Improving Performance

Okay, we now have the baseline and know the performance bottleneck, and we want to make changes to the photo tool to be able to process hundreds of photos without tiring. Here is your chance to impress your customers with how much faster you can make it go!

There are lots of ideas for possible performance improvements. Make sure you measure the performance of your program before and after each try, to see if it does really improve performance. 

### Stop boxing!
Look for any places where 'boxed' objects are being used instead of primitive Java values. Like Double instead of double, or Integer instead of int. Boxed objects are created on the heap, so take up at least 24 bytes each and have to be allocated and then garbage collected, whereas primitive values are small and handled efficiently directly within the CPU. Look through the PhotoTool source code for places where boxing may be happening, and try to use primitives instead. (see [Autoboxing and Unboxing](https://docs.oracle.com/javase/tutorial/java/data/autoboxing.html)) 
Record your performance improvement, using the same command line as above.

### Go primitive in your arrays!
Replace arrays of objects by arrays of primitives, where possible. The main candidate here is that the Picture class stores each photo using arrays of Color objects, but each Color object is really just an 8-bit value (0..255) for Red, Green and Blue components and these three values could be packed into a single integer. In fact, the Color class and the BufferedImage class both have getRGB methods to return a pixel as a single integer. And you will also need to unpack the pixel integer into its separate 8-bit red, green or blue value by using Java bit-shifting operators (see how [Bit_shift](http://en.wikipedia.org/wiki/Bit_shift) works). 

```java
int red = (rgb >> 16) & 0xFF;
int green = (rgb >> 8) & 0xFF;
int blue = rgb & 0xFF;
```
 Hint: the simplest and fastest way is to store all the pixels as integer array *int[]* pixels variable, and use the *BufferedImage getRGB()* method to get all the pixels like as follows:

```java
 int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
```
Then you can access *pixel (x,y)* as *pixels[y * width + x]*. 

Record your performance improvement, using the same command line as above.

### Watch your strides!
Surprisingly, although the following two chunks of code both process every pixel in a photo, one can be dramatically faster than the other, depending upon whether your image, or 2D array, is using [row-major order, column-major order](https://en.wikipedia.org/wiki/Row-_and_column-major_order).

```java
// Column major order
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
               // code to process pixel (x,y)
            }
        }

        // Row major order
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
               // code to process pixel (x,y)
            }
        }
```
Generally, the faster one will be the one where the inner loop is accessing adjacent pixels in the array, because then the loop will read in or write to data using fast CPU cache on each access (eg. 128 bytes per 'cache line'), instead of slow main memory. So when you access one pixel, the next 31 adjacent pixels also get loaded into CPU cache (if ints is used for pixels), so these data will be sitting there ready for the next inner loop iteration.

Look for all the double loops in the program, try both options, and measure the performance to see which one is faster. 
Record your performance improvement, using the same command line as above. 

### Find the bottlenecks!
 Run with *-Xprof* (as a Java VM argument). **If *-Xprof* is not supported by your Java version, please use VisualVm. The instruction on the Moodle.** 
 This runs your Java program with a very lightweight profiler, which periodically interrupts the program and records which method is currently executing. At the end of the program, it prints out the names of the most frequently executed methods, and the percentage of time spent within each of those methods.

Some infrequently-executed methods may have been executed by interpreting the Java bytecodes rather than compiling them to machine code, so you will get a separate report on the most heavily used interpreted bytecode methods, as well as a report on the most heavily used compiled methods. The latter is the most important, since the majority of the time is usually spent in compiled methods. Note that the HotSpot compiler will have inlined some small heavily-used methods, so their time will be included in the method that calls them. By the way, if you are interested in seeing which of your methods the HotSpot compiler is inlining, you can see lots of detail about this by adding the following JVM command line arguments: 

```
-XX:+PrintCompilation  -XX:+UnlockDiagnosticVMOptions  -XX:+PrintInlining
```

Inspect the profiler output, and see if you can rewrite the most expensive method to be more efficient, but keep the existing functionalities i.e. grayscale, sepia, undo, half, show, and save. Record your performance improvement, using the same command line as above.

### Useful References

* Java Garbage Collection Basics: https://www.oracle.com/webfolder/technetwork/tutorials/obe/java/gc01/index.html . Although this tutorial is written for Java 7 (we are using Java 8), it provides a good introduction of how GC works with Hotspot JVM, and use Java VisualVM tool to monitor the running JVM. Java visualVM is moved to JDK8, so we can directly use jvisualvm command to launch it and monitor our photo tool. (see Java VisualVM).
* Tutorial on Java G1 Garbage Collector: https://www.oracle.com/technetwork/tutorials/tutorials-1876574.html . G1 garbage collection is fully supported in Java 8, and is made as the default gc in Java 9 and later version. So it is worthy of taking some time to go through this tutorial (roughly one hour). In particular, Command Line Option and Best practice section provides a good guideline to set up parameters for GC.
*HotSpot Compiler FAQ: http://www.oracle.com/technetwork/java/hotspotfaq-138619.html.

*Java virtual machine uses the garbage collector to reclaim the memory of our phototool programs at the cost of the performance. The new and state-of-art virtual GraalVM machine employed the advanced garbage collector to aggressively remove un-used objects and improve the efficiency. You can try out GraalVM (https://www.graalvm.org/) with the original phototool programs to see whether you can gain any speedup.


## Submission checklists

* Make sure that your phototool program compiles and runs correctly (i.e. no run-time exceptions). 
* Make sure that your program still prints out the progress messages in the same format as the original program. For example:

            processing Eiffel.jpg...
            Processed 6 cmds in 0.932751663 secs.
            processing Shoes.jpg...
            Processed 6 cmds in 0.834148987 secs.
            processing Art.jpg...
            Processed 6 cmds in 3.943968155 secs.
* Make sure you have completed 9 questions on Moodle. 
* Make sure you have pushed all changes to the repository. Ensure that you can see your changes on GitLab before submission.

## Grading (50 marks in total)

|  Marks | Allocated to |
| ------ | ------ |
| 5 | At least **ten informative** Commit comments  |
| 10 | Your program runs and prints out the progress messages in the same format as the original program|
| 25 | Speedup | 
| 10 | Moodle Questions|


