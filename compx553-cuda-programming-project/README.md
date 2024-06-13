# CUDA programming Project

**Due date Friday 26 April at 11:30 pm**

**Fork this project only if it is what you have chosen to do, otherwise we don't know which project to mark.**

## Instruction and important things to note

* Fork the project by clicking the "Fork" button on the top-right corner.
* **Make sure that the visibility of your new repository (the one you just forked) is set to private.**
* You can obtain the URL of your forked project by clicking the "Clone" button (beside "Fork") and then the “copy to clipboard icon" at the right of the dropdown.
* Commit your changes regularly, providing an informative commit message(Commit and Changes Tutorial: https://www.jetbrains.com/help/idea/commit-and-push-changes.html)


## Your task: self-study CUDA programming and implement a simple CUDA program ##

To develop and run a CUDA program, you will need to use a computer with NVIDIA GPUs and have the CUDA development toolkit (https://docs.nvidia.com/cuda/cuda-installation-guide-linux/index.html) installed, or you can use the Lab 2 machines where The CUDA development toolkit has already been installed in  ```/courses/compx553/cuda```

To use it, in your home directory open _.profile_ file (please note it is a hidden file, so you might need to switch on "show hidden files" to see it), and add the following lines to the end of file. Save the file and run ```source .profile``` 
in a terminal.

```
export CUDA_PATH=/home/compx553/cuda
export PATH=$CUDA_PATH/bin:$PATH
export LD_LIBRARY_PATH=$CUDA_PATH/lib64
```

### Useful resources ###

Here are the resources I have used when studying CUDA programming and doing the project myself, which might help get you started.

* basics: https://www.nvidia.com/docs/IO/116711/sc11-cuda-c-basics.pdf
* tutorials: https://cuda-tutorial.readthedocs.io/en/latest/tutorials/tutorial01/
* CUDA programming guide: https://docs.nvidia.com/cuda/cuda-c-programming-guide/index.html
* a blog on how to use CUDA unified memory: https://developer.nvidia.com/blog/even-easier-introduction-cuda/
* a blog on CUDA's execution and memory model: https://jhui.github.io/2017/03/06/CUDA/

### Task Option 1 (5%)
The task is to reimplement the throwing darts assignment using CUDA and write a  brief report to map the terminologies used by OpenCL and CUDA. **Please note, you either choose this option or the option 2 below, NOT both. This option will only give you up to 50/100 marks, so choose it only when you run out of time or struggle with the task option 2** 

### Task Option 2 (10%)

The task is to implement the Conway's Game of Life (http://en.wikipedia.org/wiki/Conway%27s_Game_of_Life) using CUDA, and to see how much speedup you can get for a generation run on GPU. You will need to run the CUDA GPU code for at least 10 generations and make sure the run time for each generation is roughly the same after the first generation. A template file is provided to you that contains code to generate a board with a initial pattern. I would suggest you implement the CPU version of a generation first, then migrate it to the GPU version. After the GPU code works properly, try cache a region of the board in shared memory to avoid repeated access to the global memory. See "sharing data between threads" from https://www.nvidia.com/docs/IO/116711/sc11-cuda-c-basics.pdf for the reason of caching data, which of course does not always guarantee a decent speedup, however you will learn how to synchronize threads by doing it, which is not covered in the OpenCL lectures.   

Hints:
* A generation is calculated on GPU at a time. 
* You can use 2D arrays if you wish. This may make it easier for caching a region of the board using shared memory.
* You can ignore the cells on the four edges of the board and assume they are all dead cells. 

### report ###
you must also deliver a short report (i.e. a pdf document) that records the run time for each generation on CPU and GPU (only for option 2) and a table containing the mapping of OpenCL and CUDA terminologies as examples given below. 


|OpenCL|CUDA| Note |
|-----|--------|-------------------|
| __kernel__ | __global__ | CUDA uses the keyword __global__ to define a kernel|
|work item | thread| |
|word group | ... | |
| compute unit | ... | |     
| processing element | ... | |     
| local memory | ... | |     
| ... | ... | |     
| ... | ... | |     


## Grading for task option 1 (total 5 marks)

|Marks|Allocated to|
|-----|--------|
|0.5|source code comments and commits|
|1.5 | implementation|
|3 | report | 


## Grading for task option 2 (total 10 marks)

|Marks|Allocated to|
|-----|--------|
|0.5| source code comments and commits|
|4.5 | implementation|
|2 | caching a region of the board using shared memory| 
|3 | report | 

## Submission

Upload your soucre code and report to the repository.
