// This file contains various OpenCL kernels to be used for
// unit testing, to ensure proper behavior.
// Make sure gpu_miner.cl is included when compiling!

// compilation test - does nothing
__kernel void testCompile(){}

// tests zero fill right shift operation
__kernel void testZFRS_INT(__global const int* input, __global const int* shift, __global int* output, int worksize){
	int id = get_global_id(0);
	if (id < worksize) {
		output[id] = ZFRS_INT(input[id], shift[id]);
	}
}

// tests zero fill right shift operation on chars
__kernel void testZFRS_CHAR(__global const char* input, __global const int* shift, __global char* output, int worksize){
	int id = get_global_id(0);
	if (id < worksize) {
		output[id] = ZFRS_CHAR(input[id], shift[id]);
	}
}

// tests rotate right operation
__kernel void testRR(__global const int* input, __global const int* dist, __global int* output, int worksize) {
	int id = get_global_id(0);
	if (id < worksize) {
		output[id] = RR(input[id], dist[id]);
	}
}

// tests rotate left operation
__kernel void testRL(__global const int* input, __global const int* dist, __global int* output, int worksize) {
	int id = get_global_id(0);
	if (id < worksize) {
		output[id] = RL(input[id], dist[id]);
	}
}

__kernel void testK(__global int* output) {
	int id = get_global_id(0);
	if (id < sizeof(K)) {
		output[id] = K[id];
	}
}

// input should be PRE-PADDED! input length should ALWAYS BE 64!
__kernel void testHash(__global const uchar* input, __global uchar* output) {
	
}