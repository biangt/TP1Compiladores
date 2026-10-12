package org.Bianca;

import org.Bianca.Ast.*;
import org.Bianca.TablaSimbolos.Simbolo;

/**
 * Intérprete / evaluador (punto 6): recorre el AST y EJECUTA el programa.
 * Usa la tabla de símbolos para guardar y leer el valor de cada variable.
 */
public final class Interprete {

    private Interprete() { }

    /**
     * Ejecuta las sentencias en orden. Al llegar a un return, termina y devuelve su valor
     * (Integer, Boolean, o null si es "return;" o si main termina sin return).
     */
    public static Object ejecutar(Programa p) {
        for (Sentencia s : p.sentencias()) {
            if (s instanceof Asignacion a) {
                Object valor = evaluar(a.valor());
                Simbolo var = a.variable().simbolo;   // la ficha ya está colgada del nodo
                var.valor = valor;
                var.asignada = true;
            } else if (s instanceof Retorno r) {
                // return termina la función: lo que haya después NO se ejecuta
                return (r.valor() == null) ? null : evaluar(r.valor());
            }
        }
        return null;   // se acabaron las sentencias sin ningún return
    }

    /** Calcula el valor de una expresión recorriendo el árbol de abajo hacia arriba. */
    private static Object evaluar(Expr e) {
        if (e instanceof Num n) {
            return n.valor();
        }
        if (e instanceof Bool b) {
            return b.valor();
        }
        if (e instanceof Var v) {
            Simbolo s = v.simbolo;   // el nodo ya apunta a la ficha: no hay que buscar nada
            if (!s.asignada) {
                System.out.println("  [aviso] línea " + v.linea + ": '" + v.nombre
                        + "' se usa sin haberle asignado un valor; se toma su valor inicial (" + s.valor + ")");
            }
            return s.valor;
        }
        if (e instanceof Suma s) {
            return (Integer) evaluar(s.izq()) + (Integer) evaluar(s.der());
        }
        if (e instanceof Mult m) {
            return (Integer) evaluar(m.izq()) * (Integer) evaluar(m.der());
        }
        throw new IllegalStateException("Nodo desconocido: " + e);
    }
}
