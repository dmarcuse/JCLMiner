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

// tests values of K constant
__kernel void testK(__global int* output) {
	int id = get_global_id(0);
	if (id < sizeof(K)) {
		output[id] = K[id];
	}
}

// tests that the 'pad' macro works properly
__kernel void testPadding(__global const char* input, int length, __global char* output) {
	int id = get_global_id(0);
	if (id == 0) {
		char data[64];
		for (int i = 0; i < length; i++) {
			data[i] = input[i];
		}
		PAD(data, length);
		for (int i = 0; i < 64; i++) {
			output[i] = data[i];
		}
	}
}

// tests that the 'digest' function works properly
__kernel void testDigest(__global const uchar* input, int length, __global uchar* output) {
	int id = get_global_id(0);
	if (id == 0) {
		uchar data[64];
		for (int i = 0; i < length; i++) {
			data[i] = input[i];
		}
		uchar out[32];
		digest(data, length, out);
		for (int i = 0; i < 32; i++) {
			output[i] = out[i];
		}
	}
}
