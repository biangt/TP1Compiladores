package org.Bianca;

import org.Bianca.Ast.Tipo;

/**
 * El OPERADOR de una instrucción de tres direcciones.
 *
 * Cada operación lleva el tipo en el nombre (SUMA_INT, ASIGNAR_BOOL...). Así la instrucción dice
 * por sí sola con qué tipo de dato trabaja y quien la ejecute después (un intérprete o un
 * generador de código final) no tiene que adivinarlo.
 */
public enum Operacion {
    SUMA_INT("+"),
    MULT_INT("*"),
    ASIGNAR_INT("="),
    ASIGNAR_BOOL("="),
    RETORNAR_INT("return"),
    RETORNAR_BOOL("return"),
    RETORNAR_VOID("return");

    /** Cómo se escribe al imprimir la instrucción. */
    public final String simbolo;

    Operacion(String simbolo) {
        this.simbolo = simbolo;
    }

    /** La asignación que corresponde al tipo de la variable destino. */
    public static Operacion asignar(Tipo t) {
        return (t == Tipo.INT) ? ASIGNAR_INT : ASIGNAR_BOOL;
    }

    /** El return que corresponde al tipo del valor devuelto. */
    public static Operacion retornar(Tipo t) {
        return (t == Tipo.INT) ? RETORNAR_INT : RETORNAR_BOOL;
    }
}
