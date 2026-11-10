    .globl main
main:
    enter $48, $0
    movq $0, -8(%rbp)
    movq $0, -16(%rbp)

    # x = 1
    mov $1, %r10
    mov %r10, -8(%rbp)

    # t1 = 3 * 2
    mov $3, %r10
    imul $2, %r10
    mov %r10, -24(%rbp)

    # t2 = t1 * y
    mov -24(%rbp), %r10
    imul -16(%rbp), %r10
    mov %r10, -32(%rbp)

    # t3 = x + t2
    mov -8(%rbp), %r10
    add -32(%rbp), %r10
    mov %r10, -40(%rbp)

    # x = t3
    mov -40(%rbp), %r10
    mov %r10, -8(%rbp)

    # return x
    mov -8(%rbp), %rax
    leave
    ret

    .section .note.GNU-stack,"",@progbits
