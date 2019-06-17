foo:
	or %r8, %r8
	ret

main:
	irmov $123, %rdi
	call println
	halt
