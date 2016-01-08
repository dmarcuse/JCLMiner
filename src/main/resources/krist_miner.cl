long hashToLong(byte* hash) {
	return hash[5] + (hash[4] << 8) + (hash[3] << 16) + ((long)hash[2] << 24) + ((long) hash[1] << 32) + ((long) hash[0] << 40);
}

__kernel void krist_miner_basic(__global const byte* prefix, __global const byte* suffix, const ulong base, __global byte* output) {
	uint id = get_global_id(0);
	ulong nonce = base + id;
	byte input[64] = {0};
	input[0] = prefix[0];
	input[1] = prefix[1];
	input[2] = (nonce >> 56) & 0xFF;
	input[3] = (nonce >> 48) & 0xFF;
	input[4] = (nonce >> 40) & 0xFF;
	input[5] = (nonce >> 32) & 0xFF;
	input[6] = (nonce >> 24) & 0xFF;
	input[7] = (nonce >> 16) & 0xFF;
	input[8] = (nonce >> 8) & 0xFF;
	input[9] = nonce & 0xFF;
	input[10] = suffix[0];
	input[11] = suffix[1];
	for (int i = 0; i < 12; i++) {
		output[i] = input[i];
	}
}
