long hashToLong(byte* hash) {
	return hash[5] + (hash[4] << 8) + (hash[3] << 16) + ((long)hash[2] << 24) + ((long) hash[1] << 32) + ((long) hash[0] << 40);
}

// for easy changing later
#define SL 24

__kernel void krist_miner_basic(
		__global const byte* start,		// 24 characters - last block (12), address (10), prefix (2)
		const long base,
		const long work,
		__global long* output) {
	byte input[64];
	byte hashed[32];
	int id = get_global_id(0);
	int i;
	long nonce = id + base;
	// copy start array
#pragma unroll
	for (i = 0; i < SL; i++) {
		input[i] = start[i];
	}
	// convert nonce to 10 bytes
#pragma unroll
	for (i = SL; i < 34; i++) {
		input[i] = (nonce >> ((i - SL) * 5) & 31) + 48;
	}
	digest(input, 34, hashed);
	long score = hashToLong(hashed);
	if (score <= work) {
#pragma unroll
		output[0] = nonce;
	}
}
