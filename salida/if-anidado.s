    .globl main
main:
    enter $32, $0
    movq $0, -8(%rbp)
    movq $0, -16(%rbp)
    movq $0, -24(%rbp)

    # a = true
    mov $1, %r10
    mov %r10, -16(%rbp)

    # b = false
    mov $0, %r10
    mov %r10, -24(%rbp)

    # r = 0
    mov $0, %r10
    mov %r10, -8(%rbp)

    # ifFalse a goto L2
    mov -16(%rbp), %r10
    cmp $0, %r10
    je .L2

    # ifFalse b goto L4
    mov -24(%rbp), %r10
    cmp $0, %r10
    je .L4

    # r = 1
    mov $1, %r10
    mov %r10, -8(%rbp)

    # goto L3
    jmp .L3

.L4:

    # r = 2
    mov $2, %r10
    mov %r10, -8(%rbp)

.L3:

    # goto L1
    jmp .L1

.L2:

    # r = 3
    mov $3, %r10
    mov %r10, -8(%rbp)

.L1:

    # return r
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
