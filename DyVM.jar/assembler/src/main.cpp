#include <cstdio>
#include <cassert>
#include <inttypes.h>
#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include "asm.h"

int main(int argc, char *argv[]) {
	assert(argc == 3);
	std::string inputfile = std::string(argv[1]);
	char *outputfile = argv[2];

	freopen("err_log.txt", "w", stderr);

	Assembler as;
	try {
		as.load(inputfile.c_str());
		as.assemble();
	} catch (AsmException e) {
		std::cerr << e.what() << '\n';
		std::cout << e.what() << '\n';
		exit(0);
	} catch (...) {
		std::cerr << "bad!\n";
		std::cout << "bad!\n";
		exit(0);
	}

	std::vector<uint8_t> hexCode = as.getHex();

	FILE* fpo = fopen(outputfile, "wb");
	int len = hexCode.size();
	for (int i = 0; i < len; i++) {
		fprintf(fpo, "%c", hexCode[i]);
//		printf(" %02x", hexCode[i]);
	}
	fclose(fpo);
	fclose(stderr);

	return 0;
}
