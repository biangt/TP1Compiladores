package org.Bianca;

import org.Bianca.Ast.*;
import org.Bianca.TablaSimbolos.Simbolo;

import java.util.List;

/**
 * Análisis semántico (nota del punto 6): arma la tabla de símbolos y verifica
 * que el programa tenga sentido. Detecta exactamente tres errores:
 *
 *   1. variable REDECLARADA      (la detecta TablaSimbolos.declarar)
 *   2. variable NO DECLARADA     (la detecta TablaSimbolos.buscar)
 *   3. error de TIPO             (la detecta esta clase: int/bool mezclados; int+int y bool+bool están bien;
 *                                 y la condición de un if / while que no es bool)
 *
 * Recorre TODAS las sentencias, incluso las que están después de un return.
 * Al terminar, cada nodo Declaracion y cada nodo Var quedó apuntando a su Simbolo.
 */
public final class Semantico {

    private Semantico() { }

    /** Devuelve la tabla de símbolos del programa, o lanza Errores.Semantico. */
    public static TablaSimbolos analizar(Programa p) {
        TablaSimbolos tabla = new TablaSimbolos();

        // 1) Cada declaración agrega una entrada a la tabla (y el nodo guarda su ficha)
        for (Declaracion d : p.declaraciones()) {
            d.simbolo = tabla.declarar(d.nombre, d.tipo, d.linea);
        }

        // 2) Cada sentencia se revisa contra la tabla
        verificarSentencias(p.sentencias(), p.tipoRetorno(), tabla);
        return tabla;
    }

    /**
     * Revisa una lista de sentencias. Los bloques de if / while son listas de sentencias, así que
     * para revisarlos esta misma función se llama a sí misma (recursión). Como todas las variables
     * se declaran al principio de main, no hay variables locales de bloque: la tabla es una sola.
     */
    private static void verificarSentencias(List<Sentencia> sentencias, Tipo tipoMain, TablaSimbolos tabla) {
        for (Sentencia s : sentencias) {
            if (s instanceof Asignacion a) {
                Simbolo destino = resolver(a.variable(), tabla);           // la variable de la izquierda
                Tipo origen = tipoDe(a.valor(), tabla);                    // tipo de la expresión
                if (destino.tipo != origen) {
                    throw new Errores.Semantico("Error semántico en línea " + a.linea()
                            + ": no se puede asignar '" + ImprimirAst.texto(a.valor()) + "' (" + nombre(origen)
                            + ") a la variable '" + destino.nombre + "' de tipo " + nombre(destino.tipo));
                }
            } else if (s instanceof Retorno r) {
                verificarRetorno(tipoMain, r, tabla);
            } else if (s instanceof Si si) {
                verificarCondicion("if", si.condicion(), si.linea(), tabla);
                verificarSentencias(si.entonces(), tipoMain, tabla);
                if (si.sino() != null) {
                    verificarSentencias(si.sino(), tipoMain, tabla);
                }
            } else if (s instanceof Mientras m) {
                verificarCondicion("while", m.condicion(), m.linea(), tabla);
                verificarSentencias(m.cuerpo(), tipoMain, tabla);
            }
        }
    }

    /** La condición de un if / while tiene que ser bool (true, false, una variable bool, a + b, a * b...). */
    private static void verificarCondicion(String palabra, Expr condicion, int linea, TablaSimbolos tabla) {
        Tipo tipo = tipoDe(condicion, tabla);
        if (tipo != Tipo.BOOL) {
            throw new Errores.Semantico("Error semántico en línea " + linea + ": la condición del '" + palabra
                    + "' debe ser bool, pero '" + ImprimirAst.texto(condicion) + "' es " + nombre(tipo));
        }
    }

    private static void verificarRetorno(Tipo esperado, Retorno r, TablaSimbolos tabla) {
        if (esperado == Tipo.VOID) {
            if (r.valor() != null) {
                throw new Errores.Semantico("Error semántico en línea " + r.linea()
                        + ": main es void y no puede devolver '" + ImprimirAst.texto(r.valor()) + "'");
            }
        } else if (r.valor() == null) {
            throw new Errores.Semantico("Error semántico en línea " + r.linea()
                    + ": main debe devolver un valor de tipo " + nombre(esperado));
        } else {
            Tipo real = tipoDe(r.valor(), tabla);
            if (real != esperado) {
                throw new Errores.Semantico("Error semántico en línea " + r.linea() + ": main devuelve "
                        + nombre(esperado) + " pero se retorna '" + ImprimirAst.texto(r.valor())
                        + "' (" + nombre(real) + ")");
            }
        }
    }

    /** Calcula el tipo de una expresión (y verifica que sus variables existan). */
    static Tipo tipoDe(Expr e, TablaSimbolos tabla) {
        if (e instanceof Num) {
            return Tipo.INT;
        }
        if (e instanceof Bool) {
            return Tipo.BOOL;
        }
        if (e instanceof Var v) {
            return resolver(v, tabla).tipo;
        }
        if (e instanceof OpBinaria op) {
            return tipoOperacion(op, tabla);
        }
        throw new IllegalStateException("Nodo desconocido: " + e);
    }

    /**
     * Busca la variable en la tabla (error si no fue declarada) y deja el resultado dentro del
     * nodo: así el árbol queda apuntando a la ficha de la variable.
     */
    private static Simbolo resolver(Var v, TablaSimbolos tabla) {
        v.simbolo = tabla.buscar(v.nombre, v.linea);
        return v.simbolo;
    }

    /**
     * + y * piden que los DOS operandos tengan el mismo tipo y el resultado conserva ese tipo:
     *   int  op int  -> int   (suma / producto normales)
     *   bool op bool -> bool  (+ funciona como OR, * funciona como AND)
     * Mezclar int con bool es error de tipo.
     */
    private static Tipo tipoOperacion(OpBinaria op, TablaSimbolos tabla) {
        Tipo izq = tipoDe(op.izq(), tabla);
        Tipo der = tipoDe(op.der(), tabla);
        if (izq != der) {
            throw new Errores.Semantico("Error semántico en línea " + op.linea() + ": el operador '" + op.operador()
                    + "' necesita operandos del mismo tipo, pero '" + ImprimirAst.texto(op.izq()) + "' es "
                    + nombre(izq) + " y '" + ImprimirAst.texto(op.der()) + "' es " + nombre(der)
                    + " (en la expresión '" + ImprimirAst.texto(op) + "')");
        }
        return izq;
    }

    private static String nombre(Tipo t) {
        return t.name().toLowerCase();
    }
}
