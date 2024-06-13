# Assignment Two OpenCL Throwing Darts (5%)

**Due date 17 March at 11:30 pm**

## Instruction and important things to note

* Fork the project by clicking the "Fork" button on the top-right corner.
* **Make sure that the visibility of your new repository (the one you just forked) is set to private.**
* You can obtain the URL of your forked project by clicking the "Clone" button (beside "Fork") and then the “copy to clipboard icon" at the right of the dropdown.
* Clone the new repository to your computer and open the project with IntelliJ.
* Using the Git command line interface
        You will need to install a Git client yourself if you are not using the lab machines. In a termial on your computer, clone the assignment one repository to your computer using the command “git clone <the url you copied>”.
        Open the pi_darts project into your IntelliJ IDE. (File / Select the directory of 'pi_darts' ).
* Commit your changes regularly, providing an informative commit message and using Git inside IntelliJ (Commit and Changes Tutorial: https://www.jetbrains.com/help/idea/commit-and-push-changes.html)
* You are expected to make at least 5 commits with messages to explain what have changed. 5 out of 50 marks are allocated for this. 

### Note

* Please **DO NOT** change the package structure of pi_darts, rename or remove any files/directories in the repository as we will write Python code to download, compile and run your program.      


## Your Task: Finding the value of PI

Your task for this assignment is to write an OpenCL program that calculates the value of PI (3.1415...) by a simulation of throwing thousands of darts randomly into a 1x1 square that contain a circular dart board (as shown in the red area of the graph underneath). 
 
<img src="https://upload.wikimedia.org/wikipedia/commons/8/84/Pi_30K.gif"  width="240" height="240">

The area of the square is 1, while the area of the circle is pi/4. So multiplying the proportion of darts that fall inside the circle by 4 will give us an approximation of PI. This approximation will get better as the number of darts grows larger but is a very computational intensive task. (note that Yee and kondo attempted [12.1 trillion digits](http://www.numberworld.org/misc_runs/pi-12t/) to get the most accuracy of Pi value, and it take 94 days and 70 TB of disk space to complete!!!) 

This assignment is described as follows. 

 We will use thousands of OpenCL work-items, with each one performing several hundred or thousand random throws. To do this, each work-item must generate random numbers between 0.0 ... 1.0 for the X and Y position of each dart. We will use a linear congruential pseudo-random generation method, as follows:

```
  randomSeed = (a * randomSeed + c) mod 2^32
```

where the mod 2^32 is done in hardware via integer overflow (we just ignore the overflow bits), and a=1103515245 and c=12345 (these are the values used by glibc/GCC). So to generate each random number, we can simply execute the following assignment (where 'rand' is an int):

```
  rand = 1103515245 * rand + 12345;
```

HINT: We should generate one random number for X and then another one for Y. And we can convert the resulting random integer into a floating point number between 0.0 and 1.0 by using the below equation to lower 24 bits:

```java

            /* extract 24 bits for x */
            float x = ((float) (rand & 0xFFFFFF)) / 0x1000000;
            ...
            if (x*x + y*y < 1.0 ){
                // Dart (x, y) is inside the circle
            }
```        

Or we can simply generate a random integer without lowering its bits:

```java
            /* x ranges from 0 upto 2^24 -1 */
            long x = rand & 0xFFFFFF; // 0<=x<=2^24 -1 (16777215)
```        

And then we use the below formula to check if X and Y are within the circle.

```java
            x * x + y * y <= 0xFFFFFE000001;
        
```

Note the kernel with integer or long type has potential integer overflow issues. So when calculating the squares of X and Y values and the sum of both values, we must be very careful about the range values of X and Y and the maximal values that an integer and long type can hold. Otherwise, the kernel will run into errors and output a very strange pi value.

It is usually wise to start by implementing our kernel code in C-like Java code first, so that you can debug and test it. That is, implement your 'kernel' method with Java code that only uses ints, floats and doubles (no objects), and maybe some one-dimensional arrays of those types (not necessary for this lab exercise). For this dart-throwing program, we would start by implementing a method like: 

```java
          /**
           * A dummy Java-version of our kernel.
           * This is useful so that we can test and debug it in Java first.
           *
           * @param seeds one integer seed for each work-item.
           * @param repeats the number of darts each work-item must throw.
           * @param output one integer output cell for each work-item
           * @param gid dummy global id, only needed in the Java API, not the OpenCL version.
           *            (delete this parameter when you translate this to an OpenCL kernel).
           */
          public static void dummyThrowDarts(int[] seeds, int repeats, int[] output, int gid) {
              // int gid = get_global_id(0);  // this is how we get the gid in OpenCL.
              int rand = seeds[gid];
              for (int iter = 0; iter < repeats; iter++) {
                  // TODO: write this code
              }
          }
        
```
Once we have Java version of dart-throwing function, we can rewrite this function to an OpenCL kernel, and execute the kernel using JOCL library. 
```java
public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.err.println("Usage: pi workitemsize workgroupsize repeats kernel");
			System.err.println("      (workitemsize must be a multiple of workgroupsize)");
			System.exit(1);
		}
		final int wiSize = Integer.decode(args[0]);
		final int wgSize = Integer.decode(args[1]);
		final int repeats = Integer.decode(args[2]);

		
		final String srcCode = new String(JOCLUtil.readResourceToString("/pi/kernel.cl"));
		
		// Enable openCL exceptions, so that we can avoid duplicated error checking in
		// the remaining program.
		CL.setExceptionsEnabled(true);
		final int platformIndex = 0; // Platform index
		final long deviceType = CL.CL_DEVICE_TYPE_GPU; // Show GPU device type
		final int deviceIndex = 0; // Device number

		// Obtain all platform ids on this machine.
		cl_platform_id[] platforms = JOCLUtil.getAllPlatforms();
		cl_platform_id platform = platforms[platformIndex];// Get the selected platform
		System.out.println("Selected CLPlatform: " + JOCLUtil.getPlatformInfoString(platform, CL_PLATFORM_NAME));// Show platform

		// Get all devices on the selected 'platform'
		cl_device_id[] devices = JOCLUtil.getAllDevices(platform, deviceType);
		cl_device_id device = devices[deviceIndex]; // Get a single device id
		System.out.println("Selected CLDevice: " + JOCLUtil.getDeviceInfoString(device, CL_DEVICE_NAME) + "\nDevice Version:"
				+ JOCLUtil.getDeviceInfoString(device, CL.CL_DEVICE_VERSION));// Show device
		// Initialize the context properties
		cl_context_properties contextProperties = new cl_context_properties();
		contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
		// Create a context for the selected device with contextProperties
		cl_context context = clCreateContext(contextProperties, 1, new cl_device_id[] { device }, null, null, null);
		// Create a OpenCL 1.2 command-queue on the selected device with specified
		// context
		@SuppressWarnings("deprecation")
		cl_command_queue commandQueue = clCreateCommandQueue(context, device, 0, null); // OpenCL 1.2

		// Create input- and output array

		int seeds[] = new int[wiSize];// Array 'seeds' stores the seed number
		int output[] = new int[wiSize];// Array 'output' stores the total number of dart inside the circle

		// Fill array 'seeds' with 0 .. workitemSize-1
		for (int i = 0; i < wiSize; i++) {
			seeds[i] = i; // seeds[0] = 0, seeds[1] = 1, seeds[2] = 2, ....
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////
		// The below is the OpenCL-related code
		////////////////////////////////////////////////////////////////////////////////////////////////////
		Pointer ptrSeeds = Pointer.to(seeds);
		Pointer ptrOutput = Pointer.to(output);

		// Allocate OpenCL-hosted memory for inputs and output
		// Create a read-only memory on OpenCL device and copy the array 'seeds' from
		// host to device
		cl_mem memIn1 = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * wiSize,
				ptrSeeds, null);
		// Create a read-write memory on OpenCL device (default value).
		cl_mem memOut = clCreateBuffer(context, CL_MEM_READ_WRITE, Sizeof.cl_int * wiSize, null, null);
		// Write the opencl kernel as a Java string

		// Load the source code 'srcCode' to the program object
		cl_program program = clCreateProgramWithSource(context, 1, new String[] { srcCode }, null, null);
		// Build the program
		clBuildProgram(program, 0, null, null, null, null);
		// Create the kernel
		cl_kernel kernel = clCreateKernel(program, "throwDarts", null);
		// Set the arguments for the kernel
		clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memIn1)); // Array 'seeds'
		clSetKernelArg(kernel, 1, Sizeof.cl_int, Pointer.to(new int[] { repeats }));// 'repeat'
		clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(memOut));// Array 'output'

		// Set the work-item dimensions
		long global_work_size[] = new long[] { wiSize }; // Global work size is the number of work-items
		long local_work_size[] = new long[] { wgSize }; //the number of work-items in a work group

		// Execute the kernel
		System.out.println("Starting with " + wiSize + " work-items, each doing " + repeats + " repeats.");
		System.out.flush();

		// Start to measure the time
		final long time0 = System.nanoTime();
		// Start to execute the kernel with global and local workgroup size
		clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, global_work_size, local_work_size, 0, null, null);

		// Read and copy OpenCL 'memOut' array to host-allocated array 'output' that
		// 'ptrOutput' references to.
		clEnqueueReadBuffer(commandQueue, memOut, CL_TRUE, 0, wiSize * Sizeof.cl_int, ptrOutput, 0, null, null);

		// Release memory objects, kernel, program, queue and context
		clReleaseMemObject(memIn1);
		clReleaseMemObject(memOut);
		clReleaseKernel(kernel);
		clReleaseProgram(program);
		clReleaseCommandQueue(commandQueue);
		clReleaseContext(context);

		final long time1 = System.nanoTime();
		System.out.println("Done in " + (time1 - time0) / 1000 + " microseconds");// Get the elapsed time.
		////////////////////////////////////////////////////////////////////////////////////////////////////
		// End of OpenCL-related code
		////////////////////////////////////////////////////////////////////////////////////////////////////

		// Calculate PI
		long inside = 0;
		long total = (long) wiSize * repeats;
		for (int i = 0; i < wiSize; i++) {
//			System.out.println("[work-items]: work-item " + i + " gives " + output[i]);
			inside += output[i];
		}
		final double pi = 4.0 * inside / total;
		System.out.println("Estimate PI = " + inside + "/" + total + " = " + pi);

	}

```

## Tasks

The project repository contains a copy of the above code, plus a Results.xlsx spreadsheet to record your results and answers. It also includes the JOCL.jar file. You can access the JOCL API docs at http://www.jocl.org/doc/index.html. Note that our lab machine is installed with OpenCL 1.2, so we cannot use OpenCL 2.0 APIs , such as clCreateCommandQueueWithProperties.

Implement the dummyThrowDarts method. Use floats since speed is more important than accuracy. Insert comments to explain what your code does.

Run JOCLPiTest class to test dummyThrowDarts function with two following test cases:

```
  1.  int[] results = new int[6];
      JOCLPi.dummyThrowDarts(new int[] { 0, 1, 2, 3, 4, 5 }, 1000, results, 0);
      // check that results[0] is around 750 and results[1..5] are 0.

  2.  int[] results = new int[6];
      JOCLPi.dummyThrowDarts(new int[] { 0, 1, 2, 3, 4, 5 }, 1000, results, 5);
      // check that results[5] is around 750 and results[0..4] are 0.
```

After your tests pass, rewrite your Java dummyThrowDarts code to kernel.cl file.

```C++

            // OpenCL kernel 'throwDarts' using float type
            __kernel void throwDarts(__global int *seeds,
                                       const int repeats,
                                     __global int *output){
	            // Your code goes here

            }
```            

So that our program can read it to srcCode string using the below code:

```java

            String srcCode = new String(Files.readAllBytes(Paths.get("kernel.cl")));
```        

Then, we can convert the syntax into OpenCL (also, remove the* gid* parameter, since this will be obtained via calling *get_global_id(0)*), and run your OpenCL program in Lab machines! Experiment with different numbers of work-items and different repeat values, and notice how the value of PI gets more accurate as you throw more darts. Keep a spreadsheet of your experiments (see Results.xls in the given project), to record the number of work items, the number of repeats (dart throws) that each work item does, the total number of dart throws, the resulting value of PI, and the time taken for that run. (Ideally, perform several runs with a given parameter value, and record the minimum or median time, to get more reliable results). Keep on increasing the total number of darts until you reach some time limit (like the 5 second timeout on Windows from the maximal kernel execution time on CUDA ).

For your submission, perform the following three experiments, and show the results of each experiment on a separate graph. (Hint: Before you start these experiments, optimize your kernel code to remove any square root calls, by comparing the squared numbers rather than taking the square roots and comparing the results.)

**Ex1.** Using at least 10,000 work-items and a large enough repeat number to make your whole program take several seconds to execute, experiment with different workgroup sizes. That is, hold the number of work-items and repeats constant, and vary the workgroup size to find the most efficient workgroup size on the Lab One graphics cards.
Q1: What is the optimal workgroup size?
    
**Ex2.** Using the optimal workgroup size that you found in the previous experiment, vary the number of work-items up and down so it covers at least two orders of decimal magnitude (eg. from 256 to 32768 in powers of two). Plot the average execution time per dart (in nanoseconds) on your graph.
Q2: Over what range of work-items is the execution time proportional to the number of darts? That is, how many work-items do you need to make the time per dart roughly constant?
    
**Ex3.** Create a third version of your kernel procedure called throwDartsInt that uses the 'int' type for all calculations. Then repeat the previous experiment and plot the additional curve.
Q3: Are ints faster than floats? What speedup do they give? (e.g 2.0X means twice as fast, 0.5X means twice as slow)
    
Your final spreadsheet should have three graphs, one for each of the above experiments, plus the answers to the above questions.


## Submission checklists
* Make sure that your pi darts program compiles and runs correctly (i.e. no run-time exceptions). 
* Make sure that your program prints out the following messages in the same format as the original program. For example:
```                    
                    ...
                    Starting with 12800 work-items, each doing 100000 repeats
                    Done in 207919 microseconds
                    Estimate PI = 1005332658/1280000000 = 3.14166455625
```                    
           
* Make sure you have filled in the pi_darts/Results.xlsx in the project directory and committed and pushed all the changes to the file.  
* Make sure you have pushed all changes to the repository. Ensure that you can see your changes on GitLab before submission.

## Grading (50 marks in total)

|  Marks | Allocated to |
| ------ | ------ |
| 5 | At least **five informative** Commit comments  |
| 5  | Your program runs and outputs a reasonable Pi estimate.|
| 24 | Graphs and questions (8 marks each) in Results.xlsx. |
| 16  | Correct implementation of throwDartsFloat and throwDartsInt kernels (8 marks each) | 

