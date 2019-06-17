; Standard Library of DyVM's Assembly Language ^_^

; function print(n)
println:
	push %r14
	push %r13
	push %r12
	push %rbp
	irmov $10, %r14
	irmov $48, %r13
	irmov $1, %r12
	xor %rbp, %r11  ; count = 0
	mrmovq out_buf_size(), %rdx ; load size
	test %rdi, %rdi	; if n == 0
	jg .LIB1
	jl .LIB3
; zero
	rmmovb %r13, out_buf_pos(%rdx)
	inc %rdx
	rmmovq %r14, out_buf_pos(%rdx)
	inc %rdx
	rmmovq %rdx, out_buf_size()
	pop %rbp
	pop %r12
	pop %r13
	pop %r14
	ret
; positive
.LIB1:
	idiv %rdi, %r14 ; r = n % 10, n = n / 10
	add %rax, %r13   ; r += '0'
	inc %rbp   ; count++
	push %rax
	test %rdi, %rdi
	jne .LIB1
	jmp .LIB2
; negative
.LIB3:
	idiv %rdi, %r14
	test %rax, %rax
	jle .LIB4
	sub %rax, %r14
.LIB4:
	neg %rax ; make the remainder positive
	add %rax, %r13
	inc %rbp     ; count++
	push %rax
	test %rdi, %rdi
	jne .LIB3
	inc %rbp		; count++
	irmov $45, %rax ; '-'
	push %rax
.LIB2:
	pop %rax
	rmmovb %rax, out_buf_pos(%rdx)
	inc %rdx			; size++
	dec %rbp			; count--
	jne .LIB2
	rmmovq %r14, out_buf_pos(%rdx) ; add '\n'
	inc %rdx			; size++
	rmmovq %rdx, out_buf_size()
	int 2	; make system call print()
	pop %rbp
	pop %r12
	pop %r13
	pop %r14
	ret

; function _draw(p, v)
_draw:
	add %rdi, %rdi
	add %rdi, %rdi
	rmmovl %rsi, disp_buf_pos(%rdi)
	ret

; function draw(x, y, v)
draw:
	irmov $125, %rax
	mul %rdi, %rax
	add %rdi, %rsi
	add %rdi, %rdi
	add %rdi, %rdi
	rmmovl %rdx, disp_buf_pos(%rdi)
	ret

; function repaint()
repaint:
	int 3
	ret

; function random() return an integer
random:
	push %rbx
	mrmovl random_seed_pos(), %rax
	irmov $16801, %rbx
	mul %rax, %rbx
	irmov $4095, %rbx
	add %rax, %rbx
	irmov $48271, %rbx
	idiv %rax, %rbx
	rmmovl %rax, random_seed_pos()
	pop %rbx
	ret
