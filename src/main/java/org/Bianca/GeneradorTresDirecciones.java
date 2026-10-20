package org.Bianca;

import org.Bianca.Ast.*;
import org.Bianca.Operando.Constante;
import org.Bianca.Operando.Temporal;

/**
 * Generador de código de tres direcciones: recorre el AST (ya verificado por Semantico)
 * y va llenando un CodigoTresDirecciones.
 *
 * El recorrido es de las HOJAS hacia la RAÍZ (postorden): para una operación, primero se
 * resuelven sus dos hijos y recién después se emite la operación. Cada expresión "devuelve"
 * el Operando donde quedó su resultado, y el nodo padre lo usa como argumento:
 *
 *    x = x + 3 * 2          Asignacion(x, Suma(x, Mult(3, 2)))
 *
 *    t1 = 3 * 2             Mult(3, 2)    -> resultado en t1
 *    t2 = x + t1            Suma(x, t1)   -> resultado en t2
 *    x = t2                 la asignación guarda el resultado final en la variable
 *
 * Requiere que Semantico ya haya corrido: usa los punteros Var.simbolo que él completa.
 * Como en el intérprete y en GeneradorAsm, lo que está después del primer return no se
 * genera (código muerto).
 */
public final class GeneradorTresDirecciones {

    private GeneradorTresDirecciones() { }

    public static CodigoTresDirecciones generar(Programa p) {
        CodigoTresDirecciones codigo = new CodigoTresDirecciones();

        for (Sentencia s : p.sentencias()) {
            if (s instanceof Asignacion a) {
                Operando valor = expresion(a.valor(), codigo);             // 1) primero la expresión
                Operando destino = new Operando.Variable(a.variable().simbolo);
                codigo.agregar(new Instruccion(                            // 2) después la asignación
                        Operacion.asignar(destino.tipo()), valor, null, destino));
            } else if (s instanceof Retorno r) {
                if (r.valor() == null) {
                    codigo.agregar(new Instruccion(Operacion.RETORNAR_VOID, null, null, null));
                } else {
                    Operando valor = expresion(r.valor(), codigo);
                    codigo.agregar(new Instruccion(Operacion.retornar(valor.tipo()), valor, null, null));
                }
                return codigo;                  // todo lo posterior al return es código muerto
            }
        }
        return codigo;
    }

    /**
     * Genera las instrucciones de una expresión y devuelve el operando que contiene su resultado:
     * una constante o una variable (no generan nada) o un temporal (si fue una operación).
     */
    private static Operando expresion(Expr e, CodigoTresDirecciones codigo) {
        if (e instanceof Num n) {
            return new Constante(n.valor(), Tipo.INT);
        }
        if (e instanceof Bool b) {
            return new Constante(b.valor(), Tipo.BOOL);
        }
        if (e instanceof Var v) {
            return new Operando.Variable(v.simbolo);
        }
        if (e instanceof OpBinaria op) {
            Operando izq = expresion(op.izq(), codigo);        // primero los hijos...
            Operando der = expresion(op.der(), codigo);
            Temporal resultado = codigo.nuevoTemporal(Tipo.INT);
            Operacion operacion = (op instanceof Suma) ? Operacion.SUMA_INT : Operacion.MULT_INT;
            codigo.agregar(new Instruccion(operacion, izq, der, resultado));   // ...después la operación
            return resultado;
        }
        throw new IllegalStateException("Nodo desconocido: " + e);
    }
}
