package org.Bianca;

import org.Bianca.Ast.*;
import org.Bianca.TablaSimbolos.Simbolo;

import java.util.List;

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
        Retornado r = ejecutarLista(p.sentencias());
        return (r == null) ? null : r.valor();     // null = main terminó sin ningún return
    }

    /** Tope de vueltas de un while: si se pasa, casi seguro es un ciclo infinito y el intérprete se detiene. */
    private static final int MAX_VUELTAS = 1_000_000;

    /** "Se ejecutó un return y este es su valor" (valor == null si fue "return;"). */
    private record Retornado(Object valor) { }

    /**
     * Ejecuta una lista de sentencias (main entero, o el bloque de un if / while).
     * Devuelve null si la lista terminó normalmente, o un Retornado si se ejecutó un return:
     * así el return de adentro de un if / while "sube" hasta main y corta TODO lo que faltaba.
     */
    private static Retornado ejecutarLista(List<Sentencia> sentencias) {
        for (Sentencia s : sentencias) {
            if (s instanceof Asignacion a) {
                Object valor = evaluar(a.valor());
                Simbolo var = a.variable().simbolo;   // la ficha ya está colgada del nodo
                var.valor = valor;
                var.asignada = true;
            } else if (s instanceof Retorno r) {
                // return termina la función: lo que haya después NO se ejecuta
                return new Retornado((r.valor() == null) ? null : evaluar(r.valor()));
            } else if (s instanceof Si si) {
                // se evalúa la condición y se ejecuta UNO de los dos bloques
                List<Sentencia> bloque = (Boolean) evaluar(si.condicion()) ? si.entonces() : si.sino();
                if (bloque != null) {                 // un if sin else y condición false: no hace nada
                    Retornado r = ejecutarLista(bloque);
                    if (r != null) {
                        return r;
                    }
                }
            } else if (s instanceof Mientras m) {
                int vueltas = 0;
                while ((Boolean) evaluar(m.condicion())) {     // la condición se vuelve a evaluar en cada vuelta
                    if (++vueltas > MAX_VUELTAS) {
                        throw new Errores.Ejecucion("Error de ejecución en línea " + m.linea()
                                + ": el while se repitió más de " + MAX_VUELTAS + " veces (¿ciclo infinito?)");
                    }
                    Retornado r = ejecutarLista(m.cuerpo());
                    if (r != null) {
                        return r;
                    }
                }
            }
        }
        return null;
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
            Object izq = evaluar(s.izq());
            Object der = evaluar(s.der());
            if (izq instanceof Boolean a && der instanceof Boolean b) {
                return a || b;                       // bool + bool = OR
            }
            return (Integer) izq + (Integer) der;
        }
        if (e instanceof Mult m) {
            Object izq = evaluar(m.izq());
            Object der = evaluar(m.der());
            if (izq instanceof Boolean a && der instanceof Boolean b) {
                return a && b;                       // bool * bool = AND
            }
            return (Integer) izq * (Integer) der;
        }
        throw new IllegalStateException("Nodo desconocido: " + e);
    }
}
