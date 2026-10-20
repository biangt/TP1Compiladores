package org.Bianca;

import java.util.List;

/**
 * AST: Árbol Sintáctico Abstracto (punto 5).
 * Cada clase/record es un tipo de nodo del árbol. El parser (CUP) crea estos nodos.
 *
 * JERARQUÍA DE NODOS
 *
 *   Nodo
 *    |-- Programa                 raíz del árbol
 *    |-- Declaracion              DECLARA una variable   (int x;)  -> crea su ficha en la tabla
 *    |-- Sentencia
 *    |     |-- Asignacion         x = expr;
 *    |     `-- Retorno            return expr;   /   return;
 *    `-- Expr                     cualquier cosa que produce un valor
 *          |-- Num                3
 *          |-- Bool               true / false
 *          |-- Var                USA una variable   (x)  -> apunta a su ficha en la tabla
 *          `-- OpBinaria          operación con dos hijos
 *                |-- Suma         izq + der
 *                `-- Mult         izq * der
 *
 * Declaracion y Var son nodos DISTINTOS a propósito: declarar (crear la variable) y usar
 * (consultar una variable que ya existe) son acciones diferentes y se verifican distinto.
 */
public final class Ast {

    private Ast() { }

    /** Los tipos del lenguaje. VOID solo se usa como tipo de retorno de main. */
    public enum Tipo { INT, BOOL, VOID }

    /** Raíz de la jerarquía: todo lo que cuelga del árbol es un Nodo. */
    public interface Nodo { }

    /* ---------------- Expresiones ---------------- */

    /** Cualquier expresión: un número, un true/false, una variable, una suma o un producto. */
    public interface Expr extends Nodo { }

    public record Num(int valor) implements Expr { }                    // 3
    public record Bool(boolean valor) implements Expr { }               // true / false

    /**
     * El USO de una variable: x. No es un record porque "simbolo" se completa DESPUÉS de crear
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

    /** Operación con dos hijos (izquierdo y derecho). Suma y Mult comparten este molde. */
    public interface OpBinaria extends Expr {
        Expr izq();
        Expr der();
        int linea();            // línea donde está el operador (para los mensajes de error)
        String operador();      // "+" o "*"
    }

    public record Suma(Expr izq, Expr der, int linea) implements OpBinaria {      // izq + der
        @Override
        public String operador() {
            return "+";
        }
    }

    public record Mult(Expr izq, Expr der, int linea) implements OpBinaria {      // izq * der
        @Override
        public String operador() {
            return "*";
        }
    }

    /* ---------------- Sentencias ---------------- */

    /** Cualquier sentencia: asignación o return. */
    public interface Sentencia extends Nodo { }

    /** x = expr;   "variable" es el nodo Var del lado izquierdo (también apunta a su símbolo). */
    public record Asignacion(Var variable, Expr valor, int linea) implements Sentencia { }
    /** valor == null significa "return;" (sin valor). */
    public record Retorno(Expr valor, int linea) implements Sentencia { }                     // return expr;

    /* ---------------- Programa ---------------- */

    /**
     * La DECLARACIÓN de una variable:  int x;   o   bool aux1;
     * Cuando Semantico la procesa, crea la ficha de la variable en la tabla de símbolos y la deja
     * guardada en "simbolo" (así este nodo también queda enlazado con la tabla).
     */
    public static final class Declaracion implements Nodo {
        public final Tipo tipo;
        public final String nombre;
        public final int linea;
        public TablaSimbolos.Simbolo simbolo;   // null hasta que Semantico la registra

        public Declaracion(Tipo tipo, String nombre, int linea) {
            this.tipo = tipo;
            this.nombre = nombre;
            this.linea = linea;
        }

        @Override
        public String toString() {
            return "Declaracion(" + tipo.name().toLowerCase() + " " + nombre + ")";
        }
    }

    /** Todo el programa: tipo de retorno de main, sus declaraciones y sus sentencias. */
    public record Programa(Tipo tipoRetorno, List<Declaracion> declaraciones, List<Sentencia> sentencias)
            implements Nodo { }
}
