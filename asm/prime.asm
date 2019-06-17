; program prime
; print all prime numbers below 100

; function prime(n) returns boolean
prime:
	irmov $2, %rdx ; i = 2
	irmov $1, %rax
	cmp %rdi, %rax ; n <= 1
	jg .L3
	xor %rax, %rax ; return 0
	ret
.L3:
	cmp %rdx, %rdi
	jge .L4
	rrmov %rdx, %rsi
	mul %rsi, %rdx
	cmp %rsi, %rdi  ; i * i > n ?
	jg .L4
	rrmov %rdi, %r8
	idiv %r8, %rdx	; n / i
	test %rax, %rax ; test n % i
	je .L4
	irmov $1, %rax
	inc %rdx			 ; i++
	jmp .L3
.L4:
	ret

; function main
main:
	irmov $2, %rcx ; i = 0
	irmov $100, %r10 ; limit = 0
.L1:
	rrmov %rcx, %rdi 
	call prime			; prime(i)
	test %rax, %rax
	je .L2
	rrmov %rcx, %rdi
	push %rcx
	push %r10
	call println
	pop %r10
	pop %rcx
.L2:
	inc %rcx
	cmp %rcx, %r10
	jl .L1
	halt
