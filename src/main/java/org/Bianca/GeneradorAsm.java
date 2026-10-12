package org.Bianca;

import org.Bianca.Ast.*;
import org.Bianca.TablaSimbolos.Simbolo;

/**
 * Generador de pseudo-assembly (punto 7): recorre el MISMO AST que el intérprete,
 * pero en vez de calcular, escribe instrucciones para una máquina de pila.
 *
 *   PUSH n    apila el número n          LOAD x   apila el valor de la variable x
 *   STORE x   desapila y guarda en x     ADD      desapila 2, apila su suma
 *   MUL       desapila 2, apila producto RET      desapila y devuelve ese valor
 *   RET0      termina sin valor          HALT     fin del programa
 */
public final class GeneradorAsm {

    private GeneradorAsm() { }

    public static String generar(Programa p, TablaSimbolos tabla) {
        StringBuilder sb = new StringBuilder();

        // Sección de datos: una línea por variable de la tabla de símbolos
        sb.append(".data\n");
        for (Simbolo s : tabla.simbolos()) {
            sb.append("    ").append(s.nombre).append(": .").append(s.tipo.name().toLowerCase()).append(" 0\n");
        }

        // Sección de código
        sb.append(".text\n");
        sb.append("main:\n");

        for (int i = 0; i < p.sentencias().size(); i++) {
            Sentencia s = p.sentencias().get(i);
            if (s instanceof Asignacion a) {
                expresion(a.valor(), sb);                 // deja el resultado en la pila
                emitir(sb, "STORE " + a.variable().simbolo.nombre);   // lo guarda en la variable
            } else if (s instanceof Retorno r) {
                if (r.valor() == null) {
                    emitir(sb, "RET0");
                } else {
                    expresion(r.valor(), sb);
                    emitir(sb, "RET");
                }
                if (i < p.sentencias().size() - 1) {
                    sb.append("    ; (las sentencias siguientes nunca se ejecutan: no se generan)\n");
                }
                return sb.toString();                     // todo lo posterior al return es código muerto
            }
        }
        emitir(sb, "HALT");
        return sb.toString();
    }

    /** Genera el código de una expresión: primero los hijos, después la operación (recorrido postorden). */
    private static void expresion(Expr e, StringBuilder sb) {
        if (e instanceof Num n) {
            emitir(sb, "PUSH " + n.valor());
        } else if (e instanceof Bool b) {
            emitir(sb, "PUSH " + (b.valor() ? 1 : 0));
        } else if (e instanceof Var v) {
            emitir(sb, "LOAD " + v.simbolo.nombre);
        } else if (e instanceof Suma s) {
            expresion(s.izq(), sb);
            expresion(s.der(), sb);
            emitir(sb, "ADD");
        } else if (e instanceof Mult m) {
            expresion(m.izq(), sb);
            expresion(m.der(), sb);
            emitir(sb, "MUL");
        } else {
            throw new IllegalStateException("Nodo desconocido: " + e);
        }
    }

    private static void emitir(StringBuilder sb, String instruccion) {
        sb.append("    ").append(instruccion).append('\n');
    }
}
