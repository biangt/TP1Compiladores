package org.Bianca;

import org.Bianca.Ast.*;
import org.Bianca.TablaSimbolos.Simbolo;

/**
 * Análisis semántico (nota del punto 6): arma la tabla de símbolos y verifica
 * que el programa tenga sentido. Detecta exactamente tres errores:
 *
 *   1. variable REDECLARADA      (la detecta TablaSimbolos.declarar)
 *   2. variable NO DECLARADA     (la detecta TablaSimbolos.buscar)
 *   3. error de TIPO             (la detecta esta clase: int/bool mezclados)
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
        for (Sentencia s : p.sentencias()) {
            if (s instanceof Asignacion a) {
                Simbolo destino = resolver(a.variable(), tabla);           // la variable de la izquierda
                Tipo origen = tipoDe(a.valor(), tabla);                    // tipo de la expresión
                if (destino.tipo != origen) {
                    throw new Errores.Semantico("Error semántico en línea " + a.linea()
                            + ": no se puede asignar '" + ImprimirAst.texto(a.valor()) + "' (" + nombre(origen)
                            + ") a la variable '" + destino.nombre + "' de tipo " + nombre(destino.tipo));
                }
            } else if (s instanceof Retorno r) {
                verificarRetorno(p.tipoRetorno(), r, tabla);
            }
        }
        return tabla;
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

    /** + y * solo operan enteros y dan un entero. Si un operando es bool, el error muestra su valor completo. */
    private static Tipo tipoOperacion(OpBinaria op, TablaSimbolos tabla) {
        Tipo izq = tipoDe(op.izq(), tabla);
        Tipo der = tipoDe(op.der(), tabla);
        if (izq != Tipo.INT) {
            throw errorOperando(op, op.izq(), izq);
        }
        if (der != Tipo.INT) {
            throw errorOperando(op, op.der(), der);
        }
        return Tipo.INT;
    }

    private static Errores.Semantico errorOperando(OpBinaria op, Expr operando, Tipo tipo) {
        return new Errores.Semantico("Error semántico en línea " + op.linea() + ": el operador '" + op.operador()
                + "' solo se puede usar con enteros, pero '" + ImprimirAst.texto(operando) + "' es "
                + nombre(tipo) + " (en la expresión '" + ImprimirAst.texto(op) + "')");
    }

    private static String nombre(Tipo t) {
        return t.name().toLowerCase();
    }
}
