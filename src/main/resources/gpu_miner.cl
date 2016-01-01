__kernel void testHash(__global const char* input, __global const int inputLength, __global char* output) {
	if (get_global_id(0) == 0) {
		// do hashing stuff
	}
}

char* sha256(char* input, int inputLength) {

}

// This is only for testing compilation of the CL code - doesn't actually do anything
__kernel void testCompile(){}