    .globl main
main:
    enter $16, $0
    movq $0, -8(%rbp)
    movq $0, -16(%rbp)

    # b = false
    mov $0, %r10
    mov %r10, -16(%rbp)

    # ifFalse b goto L2
    mov -16(%rbp), %r10
    cmp $0, %r10
    je .L2

    # x = 1
    mov $1, %r10
    mov %r10, -8(%rbp)

    # goto L1
    jmp .L1

.L2:

    # x = 2
    mov $2, %r10
    mov %r10, -8(%rbp)

.L1:

    # return x
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
