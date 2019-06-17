; function dx(x, d)
dx:
	push %rbx
	add %rdi, %rsi
	rrmov %rdi, %rax
	rrmov %rdi, %rbx
	irmov $200, %rsi
	add %rdi, %rsi
	sub %rbx, %rsi
	cmp %rax, %rsi
	cmovge %rbx, %rax
	test %rax, %rax
	cmovl %rdi, %rax
	pop %rbx
	ret

; function dy(y, d)
dy:
	push %rbx
	add %rdi, %rsi
	rrmov %rdi, %rax
	rrmov %rdi, %rbx
	irmov $125, %rsi
	add %rdi, %rsi
	sub %rbx, %rsi
	cmp %rax, %rsi
	cmovge %rbx, %rax
	test %rax, %rax
	cmovl %rdi, %rax
	pop %rbx
	ret

; function init
init:
; initialize the cell matrix
	push %rbx
	irmov $25000, %rbx
	irmov $7, %r8
	irmov $3, %r9
	irmov $1, %r10
	rrmov %rbx, %rdx
	xor %rcx, %rcx
.L2:
	call random
	and %rax, %r8
	xor %rdi, %rdi
	cmp %rax, %r9
	cmovl %r10, %rdi
	rmmovb %rdi, data_sec_pos(%rdx)
	inc %rcx
	inc %rdx
	cmp %rcx, %rbx
	jl .L2
	pop %rbx
	ret

; function next
next:
	push %rbx
	push %rbp
	push %r12
	push %r13
	push %r14
	irmov $25000, %rbx
	irmov $50000, %rbp
	xor %rcx, %rcx
; calculate
	irmov $200, %r8
	irmov $125, %r9
	irmov $1, %r12
	irmov $-1, %r11
	xor %rcx, %rcx
	xor %r10, %r10
	rrmov %rbp, %rdx
.L3:
	; store x + 1
	rrmov %rcx, %rdi
	rrmov %r12, %rsi
	call dx
	push %rax ; push x + 1
	; store x - 1
	rrmov %rcx, %rdi
	rrmov %r11, %rsi
	call dx
	push %rax ; push x - 1
	push %rcx ; push x
	; store y + 1
	rrmov %r10, %rdi
	rrmov %r12, %rsi
	call dy
	push %rax ; push y + 1
	; store y - 1
	rrmov %r10, %rdi
	rrmov %r11, %rsi
	call dy
	push %rax ; push y - 1
	push %r10 ; push y
	; count
	xor %r14, %r14 ; count = 0
		; x-1, y-1
	mrmovq 32(%rsp), %rdi
	mrmovq 8(%rsp), %rsi
	mul %rdi, %r9
	add %rdi, %rsi
	add %rdi, %rbx
	mrmovb data_sec_pos(%rdi), %r13
	add %r14, %r13 ; count++ if alive
		; x-1, y
	mrmovq 32(%rsp), %rdi
	mrmovq (%rsp), %rsi
	mul %rdi, %r9
	add %rdi, %rsi
	add %rdi, %rbx
	mrmovb data_sec_pos(%rdi), %r13
	add %r14, %r13 ; count++ if alive
		; x-1, y+1
	mrmovq 32(%rsp), %rdi
	mrmovq 16(%rsp), %rsi
	mul %rdi, %r9
	add %rdi, %rsi
	add %rdi, %rbx
	mrmovb data_sec_pos(%rdi), %r13
	add %r14, %r13 ; count++ if alive
		; x, y-1
	mrmovq 24(%rsp), %rdi
	mrmovq 8(%rsp), %rsi
	mul %rdi, %r9
	add %rdi, %rsi
	add %rdi, %rbx
	mrmovb data_sec_pos(%rdi), %r13
	add %r14, %r13 ; count++ if alive
		; x, y+1
	mrmovq 24(%rsp), %rdi
	mrmovq 16(%rsp), %rsi
	mul %rdi, %r9
	add %rdi, %rsi
	add %rdi, %rbx
	mrmovb data_sec_pos(%rdi), %r13
	add %r14, %r13 ; count++ if alive
		; x+1, y-1
	mrmovq 40(%rsp), %rdi
	mrmovq 8(%rsp), %rsi
	mul %rdi, %r9
	add %rdi, %rsi
	add %rdi, %rbx
	mrmovb data_sec_pos(%rdi), %r13
	add %r14, %r13 ; count++ if alive
		; x+1, y
	mrmovq 40(%rsp), %rdi
	mrmovq (%rsp), %rsi
	mul %rdi, %r9
	add %rdi, %rsi
	add %rdi, %rbx
	mrmovb data_sec_pos(%rdi), %r13
	add %r14, %r13 ; count++ if alive
		; x+1, y+1
	mrmovq 40(%rsp), %rdi
	mrmovq 16(%rsp), %rsi
	mul %rdi, %r9
	add %rdi, %rsi
	add %rdi, %rbx
	mrmovb data_sec_pos(%rdi), %r13
	add %r14, %r13 ; count++ if alive
	; restore rsp
	irmov $48, %rax
	add %rsp, %rax
	push %r8
	push %r9
	irmov $2, %r8
	irmov $3, %r9
	cmp %r14, %r9
	jg .L6
	cmp %r14, %r8
	jl .L6
	jg .L7
	sub %rdx, %rbx
	mrmovb data_sec_pos(%rdx), %rax
	add %rdx, %rbx
	test %rax, %rax
	jne .L7
.L6:
	xor %rax, %rax
	jmp .L8
.L7:
	irmov $1, %rax
.L8:
	rmmovb %rax, data_sec_pos(%rdx)
	pop %r9
	pop %r8
	; increment
	inc %rdx
	inc %r10
	cmp %r10, %r9
	jl .L3
	xor %r10, %r10
	inc %rcx
	cmp %rcx, %r8
	jl .L3
; copy
	xor %rcx, %rcx
.L4:
	rrmov %rcx, %rdx
	add %rdx, %rbp
	mrmovb data_sec_pos(%rdx), %rax
	sub %rdx, %rbx
	rmmovb %rax, data_sec_pos(%rdx)
	inc %rcx
	cmp %rcx, %rbx
	jl .L4
	pop %r14
	pop %r13
	pop %r12
	pop %rbp
	pop %rbx
	ret
; end function next

; function paint(v) v:color
paint:
	push %rbx
	irmov $25000, %rbx
	irmov $0x000000ff, %r10
	rrmov %rdi, %r9
	rrmov %rbx, %rax	; address
	xor %rcx, %rcx		; pos = 0
.L1:
	mrmovb data_sec_pos(%rax), %r8
	rrmov %r9, %rsi
	test %r8, %r8
	cmove %r10, %rsi
	rrmov %rcx, %rdi
	call _draw
	inc %rcx				 ; pos++
	inc %rax				 ; address++
	cmp %rcx, %rbx
	jl .L1
	pop %rbx
	call repaint
	ret

; function color: a scheme of changing color
color:
	irmov $0xff000000, %r8
	irmov $0x00ff0000, %r9
	irmov $0x0000ff00, %r10
	rrmov %rdi, %rsi
	and %rsi, %r8
	cmp %rsi, %r8
	je .L21						; (255,x,x)
	rrmov %rdi, %rsi 
	and %rsi, %r9
	cmp %rsi, %r9
	je .L22						; (x,255,x)
	rrmov %rdi, %rsi
	and %rsi, %r10
	cmp %rsi, %r10
	je .L23						; (x,x,255)
.L21:
	rrmov %rdi, %rsi
	and %rsi, %r9
	cmp %rsi, %r9
	je .L32						; (255,255,0)
	rrmov %rdi, %rsi
	test %rsi, %r10  
	je .L24						; (255,x,0)
.L31:		; reduce blue
	irmov $0x00000500, %rsi
	sub %rdi, %rsi
	jmp .L27
.L22:
	rrmov %rdi, %rsi
	and %rsi, %r10
	cmp %rsi, %r10
	je .L33						; (0,255,255)
	rrmov %rdi, %rsi
	test %rsi, %r8
	je .L25						; (0,255,x)
.L32:		; reduce red
	irmov $0x05000000, %rsi
	sub %rdi, %rsi
	jmp .L27
.L23:
	rrmov %rdi, %rsi
	and %rsi, %r8
	cmp %rsi, %r8
	je .L31						; (255,0,255)
	rrmov %rdi, %rsi
	test %rsi, %r9
	je .L26						; (x,0,255)
.L33:		; reduce green
	irmov $0x00050000, %rsi
	sub %rdi, %rsi
	jmp .L27
.L24:		; add green
	irmov $0x00050000, %rsi
	add %rdi, %rsi
	jmp .L27
.L25:		; add blue
	irmov $0x00000500, %rsi
	add %rdi, %rsi
	jmp .L27
.L26:		; add red
	irmov $0x05000000, %rsi
	add %rdi, %rsi
.L27:
	rrmov %rdi, %rax
	ret

; function main
main:
	call init
	irmov $0x00ffffff, %rdi
	push %rdi
	call paint
.L0:
	call next
	pop %rdi
	call color
	push %rax
	rrmov %rax, %rdi
	call paint
	jmp .L0
	halt
