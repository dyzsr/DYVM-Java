	.file	"a.c"
	.text
	.globl	foo
	.type	foo, @function
foo:
.LFB0:
	.cfi_startproc
	addq	%rdi, %rsi
	addq	%rsi, %rdx
	addq	%rdx, %rcx
	addq	%rcx, %r8
	addq	%r8, %r9
	movq	%r9, %rax
	addq	8(%rsp), %rax
	addq	16(%rsp), %rax
	ret
	.cfi_endproc
.LFE0:
	.size	foo, .-foo
	.globl	bar
	.type	bar, @function
bar:
.LFB1:
	.cfi_startproc
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset 6, -16
	pushq	%rbx
	.cfi_def_cfa_offset 24
	.cfi_offset 3, -24
	movq	%rsi, %rbx
	movq	24(%rdi), %rcx
	movq	16(%rdi), %rdx
	movq	8(%rdi), %rsi
	pushq	56(%rdi)
	.cfi_def_cfa_offset 32
	pushq	48(%rdi)
	.cfi_def_cfa_offset 40
	movq	40(%rdi), %r9
	movq	32(%rdi), %r8
	movq	(%rdi), %rdi
	call	foo
	addq	$16, %rsp
	.cfi_def_cfa_offset 24
	movq	%rax, %rbp
	movq	24(%rbx), %rcx
	movq	16(%rbx), %rdx
	movq	8(%rbx), %rsi
	pushq	56(%rbx)
	.cfi_def_cfa_offset 32
	pushq	48(%rbx)
	.cfi_def_cfa_offset 40
	movq	40(%rbx), %r9
	movq	32(%rbx), %r8
	movq	(%rbx), %rdi
	call	foo
	addq	$16, %rsp
	.cfi_def_cfa_offset 24
	addq	%rbp, %rax
	popq	%rbx
	.cfi_def_cfa_offset 16
	popq	%rbp
	.cfi_def_cfa_offset 8
	ret
	.cfi_endproc
.LFE1:
	.size	bar, .-bar
	.ident	"GCC: (Ubuntu 5.4.0-6ubuntu1~16.04.9) 5.4.0 20160609"
	.section	.note.GNU-stack,"",@progbits
