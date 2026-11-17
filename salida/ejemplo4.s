    .globl main
main:
    enter $48, $0
    movq $0, -8(%rbp)
    movq $0, -16(%rbp)

    # a = 2
    mov $2, %r10
    mov %r10, -8(%rbp)

    # t1 = 3 + a
    mov $3, %r10
    add -8(%rbp), %r10
    mov %r10, -24(%rbp)

    # t2 = a * t1
    mov -8(%rbp), %r10
    imul -24(%rbp), %r10
    mov %r10, -32(%rbp)

    # b = t2
    mov -32(%rbp), %r10
    mov %r10, -16(%rbp)

    # t3 = b + 1
    mov -16(%rbp), %r10
    add $1, %r10
    mov %r10, -40(%rbp)

    # return t3
    lea .Lformato_int(%rip), %rdi
    mov -40(%rbp), %rsi
    xor %eax, %eax
    call printf@PLT
    mov $10, %edi
    call putchar@PLT
    mov -40(%rbp), %rax
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
