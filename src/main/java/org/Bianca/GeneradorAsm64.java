package org.Bianca;

import org.Bianca.TablaSimbolos.Simbolo;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Generador de assembly x86-64 (sintaxis AT&T de GNU, la que entiende gcc / as).
 *
 * ENTRADA : la lista de instrucciones de tres direcciones (CodigoTresDirecciones).
 * SALIDA  : el texto de un programa en assembly con una función main.
 *
 * NO vuelve a mirar el AST: recorre la lista y traduce UNA instrucción por vez.
 *
 * La estrategia es "simple e ineficiente pero correcta" (la que recomienda la guía de la cátedra):
 *
 *   1. Cada variable y cada temporal vive en la PILA, en una posición fija relativa a %rbp:
 *          -8(%rbp), -16(%rbp), -24(%rbp), ...      (8 bytes cada una, porque son registros de 64 bits)
 *   2. Para operar, se carga el primer operando en el registro %r10, se opera con el segundo
 *      y el resultado se guarda de nuevo en la pila. Se pasa por un registro porque x86 NO permite
 *      hacer una operación directamente entre dos posiciones de memoria.
 *   3. El valor de retorno de una función se deja en %rax (convención de llamada de x86-64).
 */
public final class GeneradorAsm64 {

    /** Dónde está cada variable (clave: su Simbolo) y cada temporal (clave: su número). */
    private final Map<Simbolo, Integer> offsetVariable = new IdentityHashMap<>();
    private final Map<Integer, Integer> offsetTemporal = new HashMap<>();
    private int proximoOffset = 0;        // lo último que se reservó: 0, -8, -16, ...

    private final StringBuilder sb = new StringBuilder();

    private GeneradorAsm64() { }

    public static String generar(CodigoTresDirecciones codigo, TablaSimbolos tabla) {
        GeneradorAsm64 g = new GeneradorAsm64();
        g.reservarMemoria(codigo, tabla);
        g.emitirPrograma(codigo, tabla);
        return g.sb.toString();
    }

    /* ------------------------------------------------------------------ */
    /* Paso 1: decidir en qué posición de la pila vive cada cosa           */
    /* ------------------------------------------------------------------ */

    private void reservarMemoria(CodigoTresDirecciones codigo, TablaSimbolos tabla) {
        // Primero las variables del usuario (la tabla de símbolos las tiene todas, en orden de declaración)
        for (Simbolo s : tabla.simbolos()) {
            proximoOffset -= 8;
            offsetVariable.put(s, proximoOffset);
        }
        // Después los temporales: no están en la tabla, hay que buscarlos dentro de las instrucciones
        for (Instruccion i : codigo.instrucciones()) {
            registrarTemporal(i.arg1());
            registrarTemporal(i.arg2());
            registrarTemporal(i.resultado());
        }
    }

    private void registrarTemporal(Operando o) {
        if (o instanceof Operando.Temporal t && !offsetTemporal.containsKey(t.numero())) {
            proximoOffset -= 8;
            offsetTemporal.put(t.numero(), proximoOffset);
        }
    }

    /* ------------------------------------------------------------------ */
    /* Paso 2: escribir el programa                                        */
    /* ------------------------------------------------------------------ */

    private void emitirPrograma(CodigoTresDirecciones codigo, TablaSimbolos tabla) {
        int bytes = -proximoOffset;
        int marco = (bytes + 15) / 16 * 16;       // la pila debe quedar alineada a 16 bytes

        // Prólogo: crea el marco de la función (guarda %rbp, fija %rbp = %rsp, reserva espacio)
        linea("    .globl main");
        linea("main:");
        instr("enter", "$" + marco + ", $0");

        // Las variables arrancan en 0 (igual que en el intérprete); la pila trae basura
        for (Simbolo s : tabla.simbolos()) {
            instr("movq", "$0, " + offsetVariable.get(s) + "(%rbp)");
        }

        boolean terminaEnReturn = false;
        for (Instruccion i : codigo.instrucciones()) {
            linea("");
            if (i.operacion() != Operacion.ETIQUETA) {
                linea("    # " + i);               // la instrucción de 3 direcciones, como comentario
            }
            traducir(i);
            terminaEnReturn = i.operacion() == Operacion.RETORNAR_INT || i.operacion() == Operacion.RETORNAR_BOOL
                    || i.operacion() == Operacion.RETORNAR_VOID;
        }

        // Si la última instrucción no es un return, el programa puede "caerse" por el final de main
        // (sin return, o con return solo dentro de un if): hay que cerrar el marco y devolver 0
        if (!terminaEnReturn) {
            linea("");
            linea("    # fin de main sin return");
            epilogo(null);
        }

        // Avisa al linker que este programa no necesita una pila ejecutable (evita un warning de gcc)
        linea("");
        linea("    .section .note.GNU-stack,\"\",@progbits");
    }

    /** UNA instrucción de tres direcciones -> varias instrucciones de assembly. */
    private void traducir(Instruccion i) {
        switch (i.operacion()) {
            case SUMA_INT -> {
                instr("mov", ubicacion(i.arg1()) + ", %r10");     // r10 = arg1
                instr("add", ubicacion(i.arg2()) + ", %r10");     // r10 = r10 + arg2
                instr("mov", "%r10, " + ubicacion(i.resultado())); // resultado = r10
            }
            case MULT_INT -> {
                instr("mov", ubicacion(i.arg1()) + ", %r10");
                instr("imul", ubicacion(i.arg2()) + ", %r10");
                instr("mov", "%r10, " + ubicacion(i.resultado()));
            }
            case SUMA_BOOL -> {                                   // bool + bool = OR (0 o 1, nunca 2)
                instr("mov", ubicacion(i.arg1()) + ", %r10");
                instr("or", ubicacion(i.arg2()) + ", %r10");
                instr("mov", "%r10, " + ubicacion(i.resultado()));
            }
            case MULT_BOOL -> {                                   // bool * bool = AND
                instr("mov", ubicacion(i.arg1()) + ", %r10");
                instr("and", ubicacion(i.arg2()) + ", %r10");
                instr("mov", "%r10, " + ubicacion(i.resultado()));
            }
            case ASIGNAR_INT, ASIGNAR_BOOL -> {
                instr("mov", ubicacion(i.arg1()) + ", %r10");
                instr("mov", "%r10, " + ubicacion(i.resultado()));
            }
            case RETORNAR_INT, RETORNAR_BOOL -> epilogo(i.arg1());
            case RETORNAR_VOID -> epilogo(null);
            case ETIQUETA -> linea(etiqueta(i.resultado()) + ":");           // .L1:
            case SALTAR -> instr("jmp", etiqueta(i.resultado()));            // salta siempre
            case SALTAR_SI_FALSO -> {                                        // salta si la condición vale 0 (false)
                instr("mov", ubicacion(i.arg1()) + ", %r10");                // se pasa por r10: así sirve también si es una constante
                instr("cmp", "$0, %r10");                                    // compara r10 con 0
                instr("je", etiqueta(i.resultado()));                        // je = "jump if equal": salta si era 0
            }
        }
    }

    /** Nombre de una etiqueta en assembly. El prefijo ".L" hace que sea local (no choca con "main"). */
    private String etiqueta(Operando o) {
        return ".L" + ((Operando.Etiqueta) o).numero();
    }

    /** Devuelve el valor en %rax y desarma el marco (leave) antes de volver (ret). */
    private void epilogo(Operando valor) {
        if (valor == null) {
            instr("mov", "$0, %rax");
        } else {
            instr("mov", ubicacion(valor) + ", %rax");
        }
        instr("leave", "");
        instr("ret", "");
    }

    /**
     * Cómo se escribe un operando en assembly:
     *   constante  ->  $3        ($ = valor inmediato; true = $1, false = $0)
     *   variable   ->  -8(%rbp)  (la posición 8 bytes por debajo de %rbp)
     *   temporal   ->  -16(%rbp)
     */
    private String ubicacion(Operando o) {
        if (o instanceof Operando.Constante c) {
            Object valor = c.valor();
            if (valor instanceof Boolean b) {
                return "$" + (b ? 1 : 0);
            }
            return "$" + valor;
        }
        if (o instanceof Operando.Variable v) {
            return offsetVariable.get(v.simbolo()) + "(%rbp)";
        }
        if (o instanceof Operando.Temporal t) {
            return offsetTemporal.get(t.numero()) + "(%rbp)";
        }
        throw new IllegalStateException("Operando desconocido: " + o);
    }

    private void instr(String nombre, String operandos) {
        sb.append("    ").append(nombre);
        if (!operandos.isEmpty()) {
            sb.append(' ').append(operandos);
        }
        sb.append('\n');
    }

    private void linea(String texto) {
        sb.append(texto).append('\n');
    }
}
