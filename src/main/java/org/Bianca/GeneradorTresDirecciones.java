package org.Bianca;

import org.Bianca.Ast.*;
import org.Bianca.Operando.Constante;
import org.Bianca.Operando.Temporal;

import java.util.List;

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
 * Como en el intérprete y en GeneradorAsm, lo que está después de un return (en la misma lista
 * de sentencias) no se genera (código muerto).
 *
 * if y while se traducen con ETIQUETAS y SALTOS (ver generarSi y generarMientras).
 */
public final class GeneradorTresDirecciones {

    private GeneradorTresDirecciones() { }

    public static CodigoTresDirecciones generar(Programa p) {
        CodigoTresDirecciones codigo = new CodigoTresDirecciones();
        sentencias(p.sentencias(), codigo);
        return codigo;
    }

    /**
     * Genera una lista de sentencias (el cuerpo de main o el bloque de un if / while).
     * Después de un return DE ESTA MISMA LISTA no se genera nada más (código muerto). Un return
     * adentro de un if no corta el resto del programa, porque el if puede no cumplirse.
     */
    private static void sentencias(List<Sentencia> lista, CodigoTresDirecciones codigo) {
        for (Sentencia s : lista) {
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
                return;                         // todo lo posterior al return es código muerto
            } else if (s instanceof Si si) {
                generarSi(si, codigo);
            } else if (s instanceof Mientras m) {
                generarMientras(m, codigo);
            }
        }
    }

    /**
     * if (c) { A }                        if (c) { A } else { B }
     *
     *     ifFalse c goto Lfin                 ifFalse c goto Lsino
     *     A                                   A
     *   Lfin:                                 goto Lfin
     *                                       Lsino:
     *                                         B
     *                                       Lfin:
     *
     * Si la condición es false se "salta por encima" del bloque A.
     */
    private static void generarSi(Si si, CodigoTresDirecciones codigo) {
        Operando condicion = expresion(si.condicion(), codigo);
        Operando.Etiqueta fin = codigo.nuevaEtiqueta();
        if (si.sino() == null) {
            codigo.agregar(new Instruccion(Operacion.SALTAR_SI_FALSO, condicion, null, fin));
            sentencias(si.entonces(), codigo);
        } else {
            Operando.Etiqueta sino = codigo.nuevaEtiqueta();
            codigo.agregar(new Instruccion(Operacion.SALTAR_SI_FALSO, condicion, null, sino));
            sentencias(si.entonces(), codigo);
            codigo.agregar(new Instruccion(Operacion.SALTAR, null, null, fin));    // terminó A: salta por encima de B
            codigo.agregar(new Instruccion(Operacion.ETIQUETA, null, null, sino));
            sentencias(si.sino(), codigo);
        }
        codigo.agregar(new Instruccion(Operacion.ETIQUETA, null, null, fin));
    }

    /**
     * while (c) { A }
     *
     *   Linicio:
     *     ifFalse c goto Lfin       <- si la condición ya no se cumple, sale del ciclo
     *     A
     *     goto Linicio              <- vuelve arriba y la condición se evalúa de nuevo
     *   Lfin:
     *
     * La condición se calcula DENTRO del ciclo (después de Linicio), porque tiene que
     * recalcularse en cada vuelta con los valores nuevos de las variables.
     */
    private static void generarMientras(Mientras m, CodigoTresDirecciones codigo) {
        Operando.Etiqueta inicio = codigo.nuevaEtiqueta();
        Operando.Etiqueta fin = codigo.nuevaEtiqueta();
        codigo.agregar(new Instruccion(Operacion.ETIQUETA, null, null, inicio));
        Operando condicion = expresion(m.condicion(), codigo);
        codigo.agregar(new Instruccion(Operacion.SALTAR_SI_FALSO, condicion, null, fin));
        sentencias(m.cuerpo(), codigo);
        codigo.agregar(new Instruccion(Operacion.SALTAR, null, null, inicio));
        codigo.agregar(new Instruccion(Operacion.ETIQUETA, null, null, fin));
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
            // Semantico garantiza que los dos operandos tienen el mismo tipo: ese es el tipo del resultado
            Tipo tipo = izq.tipo();
            Temporal resultado = codigo.nuevoTemporal(tipo);
            Operacion operacion;
            if (tipo == Tipo.BOOL) {
                operacion = (op instanceof Suma) ? Operacion.SUMA_BOOL : Operacion.MULT_BOOL;   // OR / AND
            } else {
                operacion = (op instanceof Suma) ? Operacion.SUMA_INT : Operacion.MULT_INT;
            }
            codigo.agregar(new Instruccion(operacion, izq, der, resultado));   // ...después la operación
            return resultado;
        }
        throw new IllegalStateException("Nodo desconocido: " + e);
    }
}
