    .globl main
main:
    enter $64, $0
    movq $0, -8(%rbp)
    movq $0, -16(%rbp)
    movq $0, -24(%rbp)
    movq $0, -32(%rbp)

    # a = true
    mov $1, %r10
    mov %r10, -16(%rbp)

    # b = true
    mov $1, %r10
    mov %r10, -24(%rbp)

    # c = true
    mov $1, %r10
    mov %r10, -32(%rbp)

    # n = 0
    mov $0, %r10
    mov %r10, -8(%rbp)

.L1:

    # t1 = a + b
    mov -16(%rbp), %r10
    or -24(%rbp), %r10
    mov %r10, -40(%rbp)

    # t2 = t1 + c
    mov -40(%rbp), %r10
    or -32(%rbp), %r10
    mov %r10, -48(%rbp)

    # ifFalse t2 goto L2
    mov -48(%rbp), %r10
    cmp $0, %r10
    je .L2

    # t3 = n + 1
    mov -8(%rbp), %r10
    add $1, %r10
    mov %r10, -56(%rbp)

    # n = t3
    mov -56(%rbp), %r10
    mov %r10, -8(%rbp)

    # c = b
    mov -24(%rbp), %r10
    mov %r10, -32(%rbp)

    # b = a
    mov -16(%rbp), %r10
    mov %r10, -24(%rbp)

    # a = false
    mov $0, %r10
    mov %r10, -16(%rbp)

    # goto L1
    jmp .L1

.L2:

    # return n
    lea .Lformato_int(%rip), %rdi
    mov -8(%rbp), %rsi
    xor %eax, %eax
    call printf@PLT
    mov $10, %edi
    call putchar@PLT
    mov -8(%rbp), %rax
    leave
    ret

    .section .rodata
.Lformato_int:
    .string "%ld"
.Ltexto_true:
    .string "true"
.Ltexto_false:
    .string "false"

    .section .note.GNU-stack,"",@progbits
