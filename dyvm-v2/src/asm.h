#ifndef ASM_H
#define ASM_H

#include <inttypes.h>
#include <fstream>
#include <string>
#include <map>
#include <utility>
#include <vector>
#include <exception>

typedef std::pair<int, int> PII;
typedef std::pair<int64_t, int> PLI;

// state *****************************************************

#define ASM_OK (0)
#define ASM_ERR_UNDEF (1) // undefined identifier
#define ASM_ERR_INS (2)
#define ASM_ERR_IMM (3)
#define ASM_ERR_REG (4)
#define ASM_ERR_MEM (5)
#define ASM_ERR_RE_LB (6) // redeclared label
#define ASM_ERR_UN_LB (7) // undeclared label
#define ASM_ERR_FMT (8) // incorrect instruction format
#define ASM_ERR_IDT (9) // not an identifier
#define ASM_ERR_LOAD (10) // errors occur in txt/ins
#define ASM_ERR_MANY (11)
#define ASM_ERR_FEW (12)

class AsmException : public std::exception {
public:
	AsmException(int _type, int _line_no, std::string code) noexcept;
	virtual const char *what() const noexcept;
	int getType() const noexcept;
	int getLineNo() const noexcept;

private:
	int type;
	int line_no;
	std::string err_message;
};

class Assembler {
public:
	Assembler();
	void load(const char *filename);
	void assemble();
	std::vector<std::string> getAsm() const;
	std::vector<uint8_t> getHex() const;
	std::vector<int> getMap() const;

private:
	std::vector<std::string> asm_code;
	std::vector<uint8_t> hex_code;
	std::vector<int> mapHtoA;

	// instruction **************************************************
	// types:
	// 0: null
	// 1: r (a single register)
	// 2: rr (double registers)
	// 3: ir
	// 4: rm
	// 5: mr
	// 6: l (label)
	std::map<std::string, PII> ins_ids;

	void load_ins();
	uint8_t find_ins(const std::string &name, uint8_t &type);

	// registers ****************************************************
	std::map<std::string, uint8_t> reg_ids;

	void load_reg();
	uint8_t find_reg(const std::string &name);

	// operation on labels ******************************************
	struct LabelRecord {
		std::string label;
		int64_t index;
		int line_no;
	};

	std::map<std::string, PLI> lb_dests;
	std::vector<LabelRecord> lb_records;

	void load_label();
	void add_src_lb(const std::string &lb, int64_t _index, int _line_no);
	void add_dest_lb(const std::string &lb, int64_t _index, int _line_no);

	// execution ****************************************************
	int64_t index;
	int line_no;

	void write_i(int64_t imme, int64_t pos);
	int64_t read_i(std::string &src);
	uint8_t read_r(std::string &src);
	uint8_t read_m(std::string &src, int64_t &imme);

	void interpret(const std::string &code);
	void link_lb();

}; // end class Assembler

#endif
