package org.Bianca;

import java.util.List;

/**
 * AST: Árbol Sintáctico Abstracto (punto 5).
 * Cada "record" es un tipo de nodo del árbol. El parser (CUP) crea estos nodos.
 */
public final class Ast {

    private Ast() { }

    /** Los tipos del lenguaje. VOID solo se usa como tipo de retorno de main. */
    public enum Tipo { INT, BOOL, VOID }

    /* ---------------- Expresiones ---------------- */

    /** Cualquier expresión: un número, un true/false, una variable, una suma o un producto. */
    public interface Expr { }

    public record Num(int valor) implements Expr { }                    // 3
    public record Bool(boolean valor) implements Expr { }               // true / false
    /**
     * El uso de una variable: x. No es un record porque "simbolo" se completa DESPUÉS de crear
     * el nodo: lo hace Semantico al buscar la variable en la tabla de símbolos.
     * Desde entonces el árbol apunta directamente a la ficha de la variable.
     */
    public static final class Var implements Expr {
        public final String nombre;
        public final int linea;
        public TablaSimbolos.Simbolo simbolo;   // null hasta que Semantico lo resuelve

        public Var(String nombre, int linea) {
            this.nombre = nombre;
            this.linea = linea;
        }

        @Override
        public String toString() {
            return "Var(" + nombre + ")";
        }
    }

    public record Suma(Expr izq, Expr der) implements Expr { }          // izq + der
    public record Mult(Expr izq, Expr der) implements Expr { }          // izq * der

    /* ---------------- Sentencias ---------------- */

    /** Cualquier sentencia: asignación o return. */
    public interface Sentencia { }

    /** x = expr;   "variable" es el nodo Var del lado izquierdo (también apunta a su símbolo). */
    public record Asignacion(Var variable, Expr valor, int linea) implements Sentencia { }
    /** valor == null significa "return;" (sin valor). */
    public record Retorno(Expr valor, int linea) implements Sentencia { }                     // return expr;

    /* ---------------- Programa ---------------- */

    /** int x;   o   bool aux1; */
    public record Declaracion(Tipo tipo, String nombre, int linea) { }

    /** Todo el programa: tipo de retorno de main, sus declaraciones y sus sentencias. */
    public record Programa(Tipo tipoRetorno, List<Declaracion> declaraciones, List<Sentencia> sentencias) { }
}
