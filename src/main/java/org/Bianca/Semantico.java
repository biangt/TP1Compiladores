package org.Bianca;

import org.Bianca.Ast.*;

/**
 * Análisis semántico (nota del punto 6): arma la tabla de símbolos y verifica
 * que el programa tenga sentido: variables declaradas y tipos compatibles.
 * Recorre TODAS las sentencias, incluso las que están después de un return.
 */
public final class Semantico {

    private Semantico() { }

    /** Devuelve la tabla de símbolos del programa, o lanza Errores.Semantico. */
    public static TablaSimbolos analizar(Programa p) {
        TablaSimbolos tabla = new TablaSimbolos();

        // 1) Cada declaración agrega una entrada a la tabla
        for (Declaracion d : p.declaraciones()) {
            tabla.declarar(d.nombre(), d.tipo(), d.linea());
        }

        // 2) Cada sentencia se revisa contra la tabla
        for (Sentencia s : p.sentencias()) {
            if (s instanceof Asignacion a) {
                Tipo destino = resolver(a.variable(), tabla).tipo;         // tipo de la variable
                Tipo origen = tipoDe(a.valor(), tabla);                    // tipo de la expresión
                if (destino != origen) {
                    throw new Errores.Semantico("Error semántico en línea " + a.linea() + ": no se puede asignar "
                            + nombre(origen) + " a la variable '" + a.variable().nombre + "' de tipo " + nombre(destino));
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
                        + ": main es void y no puede devolver un valor");
            }
        } else if (r.valor() == null) {
            throw new Errores.Semantico("Error semántico en línea " + r.linea()
                    + ": main debe devolver un valor de tipo " + nombre(esperado));
        } else {
            Tipo real = tipoDe(r.valor(), tabla);
            if (real != esperado) {
                throw new Errores.Semantico("Error semántico en línea " + r.linea() + ": main devuelve "
                        + nombre(esperado) + " pero se retorna " + nombre(real));
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
        if (e instanceof Suma s) {
            return tipoOperacion("+", s.izq(), s.der(), tabla);
        }
        if (e instanceof Mult m) {
            return tipoOperacion("*", m.izq(), m.der(), tabla);
        }
        throw new IllegalStateException("Nodo desconocido: " + e);
    }

    /**
     * Busca la variable en la tabla (error si no fue declarada) y deja el resultado dentro del
     * nodo: así el árbol queda apuntando a la ficha de la variable.
     */
    private static TablaSimbolos.Simbolo resolver(Var v, TablaSimbolos tabla) {
        v.simbolo = tabla.buscar(v.nombre, v.linea);
        return v.simbolo;
    }

    /** + y * solo operan enteros y dan un entero. */
    private static Tipo tipoOperacion(String op, Expr izq, Expr der, TablaSimbolos tabla) {
        if (tipoDe(izq, tabla) != Tipo.INT || tipoDe(der, tabla) != Tipo.INT) {
            throw new Errores.Semantico("Error semántico: el operador '" + op + "' solo se puede usar con enteros");
        }
        return Tipo.INT;
    }

    private static String nombre(Tipo t) {
        return t.name().toLowerCase();
    }
}
