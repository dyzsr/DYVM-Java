; function foo(v) v:color
foo:
	rrmov %rdi, %rdx
	irmov $200, %r8
	irmov $125, %r9
	xor %rcx, %rcx
	xor %r10, %r10
.L1:
	rrmov %rcx, %rdi
	rrmov %r10, %rsi
	call draw
	inc %r10
	cmp %r10, %r9
	jl .L1
	xor %r10, %r10
	inc %rcx
	cmp %rcx, %r8
	jl .L1
	call repaint
	ret

; function main: endless loop
main:
.L0:
	irmov $0xff0000ff, %rdi
	call foo
	irmov $0xffff00ff, %rdi
	call foo
	irmov $0x00ff00ff, %rdi
	call foo
	irmov $0x00ffffff, %rdi
	call foo
	irmov $0x0000ffff, %rdi
	call foo
	irmov $0xff00ffff, %rdi
	call foo
	jmp .L0
	halt
