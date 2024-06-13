// OpenCL kernel 'throwDarts' using float type
__kernel void throwDarts(__global int *seeds,        
                     const int repeats,    
                     __global int *output){  
	int gid = get_global_id(0);
	int total = 0;
	int rand = seeds[gid];
    for (int iter = 0; iter < repeats; iter++) {

        //random x
        rand = 1103515245 * rand + 12345;
        float x = ((float) (rand & 0xFFFFFF)) / 0x1000000;
        //random y
        rand = 1103515245 * rand + 12345;
        float y = ((float) (rand & 0xFFFFFF)) / 0x1000000;

        if (x*x + y*y < 1.0 ){
            total++;
        }
    }
    output[gid] = total;
}

// OpenCL kernel 'throwDarts' using integer type
__kernel void throwDartsInt(__global int *seeds,
                     const int repeats,
                     __global int *output){
	int gid = get_global_id(0);
	int total = 0;
	int rand = seeds[gid];
    for (int iter = 0; iter < repeats; iter++) {
        //random x
        rand = 1103515245 * rand + 12345;
        long x = rand & 0xFFFFFF;
        //random y
        rand = 1103515245 * rand + 12345;
        long y = rand & 0xFFFFFF;

        if (x*x + y*y < 0xFFFFFE000001 ){
            total++;
        }
    }
    output[gid] = total;

}

// Optional: OpenCL kernel 'throwDarts' using double type
// Implement this function only if your GPU supports double type.
__kernel void throwDartsDouble(__global int *seeds,
                     const int repeats,
                     __global int *output){
	// Your code goes here

}
