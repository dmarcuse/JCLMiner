long hashToLong(byte* hash) {
	return hash[5] + (hash[4] << 8) + (hash[3] << 16) + ((long)hash[2] << 24) + ((long) hash[1] << 32) + ((long) hash[0] << 40);
}

__kernel void krist_miner_basic(
		__global const byte* prefix,
		__global const byte* suffix,
		__global const byte* lastblock,
		__global const byte* address,
		const ulong work,
		const ulong base,
		__global byte* output) {
	/* init vars */
	uint id = get_global_id(0);
	ulong nonce = base + id;
	byte hashed[32];
	byte input[64] = {0};
	input[0] = address[0];
	input[1] = address[1];
	input[2] = address[2];
	input[3] = address[3];
	input[4] = address[4];
	input[5] = address[5];
	input[6] = address[6];
	input[7] = address[7];
	input[8] = address[8];
	input[9] = address[9];
	input[10] = lastblock[0];
	input[11] = lastblock[1];
	input[12] = lastblock[2];
	input[13] = lastblock[3];
	input[14] = lastblock[4];
	input[15] = lastblock[5];
	input[16] = lastblock[6];
	input[17] = lastblock[7];
	input[18] = lastblock[8];
	input[19] = lastblock[9];
	input[20] = lastblock[10];
	input[21] = lastblock[11];
	input[22] = prefix[0];
	input[23] = prefix[1];
	input[24] = (nonce >> 56) & 0xFF;
	input[25] = (nonce >> 48) & 0xFF;
	input[26] = (nonce >> 40) & 0xFF;
	input[27] = (nonce >> 32) & 0xFF;
	input[28] = (nonce >> 24) & 0xFF;
	input[29] = (nonce >> 16) & 0xFF;
	input[30] = (nonce >> 8) & 0xFF;
	input[31] = nonce & 0xFF;
	input[32] = suffix[0];
	input[33] = suffix[1];
	/* perform hash */
	digest(input, 34, hashed);
	long score = hashToLong(hashed);
	if (score <= work) {
		output[0] = id;
	}
}
