#include "asm.h"
#include <sstream>
#include <algorithm>
#include <cctype>
#include <iostream>

AsmException::AsmException(int _type = 0, int _line_no = 0, std::string code = "") noexcept: type(_type), line_no(_line_no), err_message("") {
		if (type == ASM_ERR_LOAD) {
			err_message += "Loading exception: " + code + '\n';
	} else {
		std::stringstream ss;
		std::string str;
		err_message += "Line ";
		ss << line_no + 1; ss >> str;
		err_message += str;
		err_message += ": " + code + '\n';
		switch (_type) {
			case ASM_ERR_UNDEF:
				err_message += "Undefined identifier\n"; break;
			case ASM_ERR_INS:
				err_message += "Illegal instruction\n"; break;
			case ASM_ERR_REG:
				err_message += "Illegal register id\n"; break;
			case ASM_ERR_IMM:
				err_message += "Illegal immediate number\n"; break;
			case ASM_ERR_MEM:
				err_message += "Illegal memory address\n"; break;
			case ASM_ERR_RE_LB:
				err_message += "Label redeclared\n"; break;
			case ASM_ERR_UN_LB:
				err_message += "Label undeclared\n"; break;
			case ASM_ERR_FMT:
				err_message += "Illegal instruction format\n"; break;
			case ASM_ERR_IDT:
				err_message += "Not an identifier\n"; break;
			case ASM_ERR_MANY:
				err_message += "Too many argumant(s)\n"; break;
			case ASM_ERR_FEW:
				err_message += "Too few argument(s)\n"; break;
		}
	}
}

const char * AsmException::what() const noexcept {
	return err_message.c_str();
}

int AsmException::getType() const noexcept {
	return type;
}

int AsmException::getLineNo() const noexcept {
	return line_no;
}

const static int ins_len[] = {1, 2, 2, 10, 10, 10, 9, 9};
static int ins_type[256];

void Assembler::load_ins() {
	std::ifstream fin;
	try {
		fin.open("./txt/ins");
	} catch (std::ifstream::failure) {
		throw;
	}
	std::string str, name;
	while (std::getline(fin, str)) {
		std::stringstream ss(str);
		int no, type;
		ss >> std::hex >> no >> name >> type;
		auto it = ins_ids.find(name);
		if (it != ins_ids.end()) {
			throw AsmException(ASM_ERR_LOAD, 0, "ins");
		}
		ins_ids[name] = std::make_pair(no, type);
		ins_type[no] = type;
	}
}

uint8_t Assembler::find_ins(const std::string &name, uint8_t &type) {
	auto it = ins_ids.find(name);
	if (it == ins_ids.end()) {
		throw AsmException(ASM_ERR_INS, line_no, asm_code[line_no]);
	}
	PII p = it->second;
	uint8_t ins = p.first;
	type = p.second;
	return ins;
}

void Assembler::load_reg() {
	std::ifstream fin;
	try { 
		fin.open("./txt/reg"); 
	} catch (std::ifstream::failure e) {
		throw; 
	}
	std::string str, name;
	while (std::getline(fin, str)) {
		int no;
		std::stringstream ss(str);
		ss >> std::hex >> no >> name;
		auto it = reg_ids.find(name);
		if (it != reg_ids.end()) {
			throw AsmException(ASM_ERR_LOAD, 0, "reg");
		}
		reg_ids[name] = no;
	}

//	for (auto it = reg_ids.begin(), ie = reg_ids.end();
//			it != ie; ++it) {
//		std::cerr << (int)it->second << ' ' << it->first << '\n';
//	}
}

uint8_t Assembler::find_reg(const std::string &name) {
	auto it = reg_ids.find(name);
	if (it == reg_ids.end()) {
		throw AsmException(ASM_ERR_REG, line_no, asm_code[line_no]);
	}
	return (uint8_t)it->second;
}

void Assembler::load_label() {
	std::ifstream fin;
	try {
		fin.open("./txt/label");
	} catch (std::ifstream::failure e) {
		throw;
	}
	std::string lb;
	int64_t pos;
	while (fin >> lb >> std::hex >> pos) {
		add_dest_lb(lb, pos, -1);
	}
}

void Assembler::add_src_lb(const std::string &lb, int64_t _index, int _line_no) {
	LabelRecord record;
	record.label = lb;
	record.index = _index;
	record.line_no = _line_no;
	lb_records.push_back(record);
}

void Assembler::add_dest_lb(const std::string &lb, int64_t _index, int _line_no) {
	std::cout << lb << ' ' << std::hex << _index << '\n';
	auto it = lb_dests.find(lb);
	if (it != lb_dests.end()) {
		throw AsmException(ASM_ERR_RE_LB, _line_no, asm_code[line_no]);
	}
	lb_dests[lb] = std::make_pair(_index, _line_no);
}

void Assembler::link_lb() {
	auto iend = lb_records.end();
	auto none = lb_dests.end();
	for (auto e = lb_records.begin(); e != iend; ++e) {
		std::string src_lb = e->label;
		int64_t src_index = e->index;
		int src_line_no = e->line_no;

		auto it = lb_dests.find(src_lb);
		if (it == none) {
			throw AsmException(ASM_ERR_UN_LB, src_line_no, asm_code[src_line_no]);
		}
		int64_t dest_index = it->second.first;
		write_i(dest_index, src_index);
	}
}

Assembler::Assembler() {
	try {
		load_ins();
		load_reg();
		load_label();
	} catch (AsmException e) {
		throw;
	}
}

#define ERR_EOF(ss); if ((ss).eof()) { \
	throw AsmException(ASM_ERR_FEW, line_no, code); \
}

#define ERR_NEOF(ss) if (!(ss).eof()) { \
	throw AsmException(ASM_ERR_MANY, line_no, code); \
}

#define ERR_IMM(x, e) if ((x) >= (e)) { \
	throw AsmException(ASM_ERR_IMM, line_no, asm_code[line_no]); \
}

#define EAT_CH(s, x) while ((s).size() && (s).back() == (x)) { \
	(s).pop_back(); \
}

int64_t Assembler::read_i(std::string &src) {
	EAT_CH(src, ',');

	int64_t val = 0;
	char ch;
	bool sgn = 0, hex = 0;
	auto iend = src.end();
	auto it = src.begin();

	ERR_IMM(it, iend);
	if (*it != '$') {
		throw AsmException(ASM_ERR_IMM, line_no, asm_code[line_no]);
	}
	++it;

	ERR_IMM(it, iend);
	if (*it == '-') sgn = 1, ++it;
	if (it + 2 < iend) {
		if (*it == '0' && *(it + 1) == 'x') hex = 1, ++it, ++it;
	}
	ERR_IMM(it, iend);
	if (hex) {
		while (it != iend) {
			ch = *it; ++it;
			ch = toupper(ch);
			if (!isxdigit(ch)) ERR_IMM(it, iend);
			val <<= 4;
			val += isdigit(ch) ? ch - '0' : ch - 'A' + 10;
		}
	} else {
		while (it != iend) {
			ch = *it; ++it;
			if (!isdigit(ch)) ERR_IMM(it, iend);
			val *= 10;
			val += ch - '0';
		}
	}
//	std::cerr << "<" << std::hex << ">\n";
	return sgn ? -val : val;
}

uint8_t Assembler::read_r(std::string &src) {
	EAT_CH(src, ',');
	return find_reg(src);
}

uint8_t Assembler::read_m(std::string &src, int64_t &imme) {
	EAT_CH(src, ',');

	int p = src.find('(');
	std::string str1 = src.substr(0, p);
	std::string str2 = src.substr(p + 1);
	EAT_CH(str2, ')');
	EAT_CH(str2, ' ');

	if (str1.empty()) {
		imme = 0;
	} else {
		int64_t val = 0;
		char ch, sgn = 0, hex = 0;
		auto iend = str1.end();
		auto it = str1.begin();

		bool allalpha = true;
		for (; it != iend; ++it) {
			char c = *it;
			if (!isalpha(c) && c != '_') {
				allalpha = false;
				break;
			}
		}

		if (allalpha) {
			imme = 0;
			add_src_lb(str1, index + 1, line_no);
		} else {
			it = str1.begin();
			ERR_IMM(it, iend);
			if (*it == '-') sgn = 1, ++it;
			if (it + 2 < iend) {
				if (*it == '0' && *(it + 1) == 'x') hex = 1, ++it, ++it;
			}
			ERR_IMM(it, iend);
			if (hex) {
				while (it < iend) {
					ch = *it; ++it;
					if (!isxdigit(ch)) ERR_IMM(it, iend);
					val <<= 4;
					val += isdigit(ch) ? ch - '0' : ch - 'a' + 10;
					val += isupper(ch) ? 0 : 'a' - 'A';
				}
			} else {
				while (it < iend) {
					ch = *it; ++it;
					if (!isdigit(ch)) ERR_IMM(it, iend);
					val *= 10;
					val += ch - '0';
				}
			}
			imme = sgn ? -val : val;
		}
	}

	if (!str2.size()) return 0xf;
	return find_reg(str2);
}

void Assembler::write_i(int64_t imme, int64_t pos) {
	for (int i = 0; i < 8; i++, pos++) {
		hex_code[pos] = imme & 0xff;
		imme >>= 8;
	}
}

void Assembler::interpret(const std::string &code) {
	std::stringstream ss(code);
	std::string str;
	ss >> str;

	try {
		// label
		if (str.back() == ':') {
			str.pop_back();
			add_dest_lb(str, index, line_no);
			ERR_NEOF(ss);

		} else { // instruction
			uint8_t type;
			uint8_t ins = find_ins(str, type);
//			std::cerr << '(' << std::hex << (int)ins << ")\n";
			hex_code.resize(index + ins_len[type]);

			hex_code[index++] = ins;

			if (type == 0) { // null
				ERR_NEOF(ss);
			} else if (type == 1) { // r
				ERR_EOF(ss); ss >> str;
				uint8_t rA = read_r(str);
				uint8_t byte = rA << 4 | 0xf;
				hex_code[index++] = byte;
				ERR_NEOF(ss);
			} else if (type == 2) { // rr
				ERR_EOF(ss); ss >> str;
				uint8_t rA = read_r(str);
				ERR_EOF(ss); ss >> str;
				uint8_t rB = read_r(str);
				uint8_t byte = rA << 4 | rB;
				hex_code[index++] = byte;
				ERR_NEOF(ss);
			} else if (type == 3) { // ir
				ERR_EOF(ss); ss >> str;
				int64_t imme = read_i(str);
				ERR_EOF(ss); ss >> str;
				uint8_t rA = read_r(str);
				hex_code[index] = rA << 4 | 0xf;
				write_i(imme, index + 1);
				index += 9;
				ERR_NEOF(ss);
			} else if (type == 4) { // rm
				ERR_EOF(ss); ss >> str;
				uint8_t rA = read_r(str);
				ERR_EOF(ss); ss >> str;
//				std::cerr << "here!\n";
				int64_t imme;
				uint8_t rB = read_m(str, imme);
//				std::cerr << "ereh!\n";
				uint8_t byte = rA << 4 | rB;
				hex_code[index] = byte;
				write_i(imme, index + 1);
				index += 9;
				ERR_NEOF(ss);
			} else if (type == 5) { // mr
				ERR_EOF(ss); ss >> str;
				int64_t imme;
				uint8_t rB = read_m(str, imme);
				ERR_EOF(ss); ss >> str;
				uint8_t rA = read_r(str);
				uint8_t byte = rA << 4 | rB;
				hex_code[index] = byte;
				write_i(imme, index + 1);
				index += 9;
				ERR_NEOF(ss);
			} else if (type == 6) { // label
				ERR_EOF(ss); ss >> str;
				add_src_lb(str, index, line_no);
				index += 8;
				ERR_NEOF(ss);
			} else if (type == 7) { // immediate
				ERR_EOF(ss);
				int64_t imme;
				ss >> imme;
				write_i(imme, index);
				index += 8;
				ERR_NEOF(ss);
			}
		}

	} catch (...) {
		throw;
	}
}

void Assembler::load(const char *filename) {
	std::ifstream fin;
	fin.open(filename);
	asm_code.clear();
	std::string line;
	while (std::getline(fin, line)) {
		asm_code.push_back(line);
	}
	fin.close();

	try {
		fin.open("asm/lib.asm");
		while (std::getline(fin, line)) {
			asm_code.push_back(line);
		}
		fin.close();
	} catch (std::ifstream::failure) {
		std::cerr << "library lost!\n";
	}
}

void Assembler::assemble() {
	hex_code.clear();
	mapHtoA.clear();
	
	int hex_line = 0;
	line_no = 0;
	index = 0;

	try {
		hex_code.resize(8);
		index += 8;
		add_src_lb("main", 0, -1);

		for (auto it = asm_code.begin(), ie = asm_code.end(); 
				it != ie; ++it) {
			std::string str = *it;

			int p = str.find(';');
			if (p >= 0) str.erase(p);
			EAT_CH(str, ' ');
//			std::cout << str << '\n';
			char c;
			while (str.size()) {
				c = str.back();
				if (isalnum(c) || c == '_' || c == ':' || c == '(' || c == ')') {
					break;
				}
				str.pop_back();
			}

			if (str.size()) {
				interpret(str);
				hex_line++;
				mapHtoA.push_back(line_no); // map: hex_line -> line_no
			}
			line_no++;
		}

		link_lb();

	} catch (...) {
		throw;
	}
}

#undef ERR_EOF
#undef ERR_NEOF
#undef ERR_IMM
#undef EAT_CH

std::vector<std::string> Assembler::getAsm() const {
	return asm_code;
}

std::vector<uint8_t> Assembler::getHex() const {
	fprintf(stderr, "%#06x:", 0);
	for (int i = 0; i < 8; i++) {
		fprintf(stderr, " %02x", hex_code[i]);
	}
	fprintf(stderr, "\n");
	int len = hex_code.size();
	for (int i = 8; i < len;) {
		int type = ins_type[hex_code[i]];
		fprintf(stderr, "%#06x:", i);
		int len = ins_len[type];
		for (int j = 0; j < len; j++, i++) {
			fprintf(stderr, " %02x", hex_code[i]);
		}
		fprintf(stderr, "\n");
	}
	return hex_code;
}

std::vector<int> Assembler::getMap() const {
	return mapHtoA;
}
