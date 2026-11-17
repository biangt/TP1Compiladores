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
    mov -8(%rbp), %rax
    leave
    ret

    .section .note.GNU-stack,"",@progbits
