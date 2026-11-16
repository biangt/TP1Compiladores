package org.Bianca;

import org.Bianca.Ast.Tipo;
import org.Bianca.TablaSimbolos.Simbolo;

/**
 * Un OPERANDO del código de tres direcciones: cada "casillero" de una instrucción
 * (los dos argumentos y el resultado) contiene una de estas cosas.
 *
 *   Constante   un valor literal                  3        true
 *   Variable    una variable del programa         x        (apunta a su Simbolo en la tabla)
 *   Temporal    un resultado parcial inventado    t1, t2   (lo crea el compilador, no el usuario)
 *
 * Además existe la Etiqueta (L1, L2...), que no es un valor sino un destino de salto (if / while).
 *
 * Los resultados intermedios no se pueden guardar en una constante ni en una variable del usuario,
 * por eso existen los temporales.
 */
public sealed interface Operando {

    Tipo tipo();

    record Constante(Object valor, Tipo tipo) implements Operando {
        @Override
        public String toString() {
            return String.valueOf(valor);          // 3, true, false: el valor completo
        }
    }

    /** Puntero a la ficha de la variable: de ahí salen su nombre, su tipo y (en ejecución) su valor. */
    record Variable(Simbolo simbolo) implements Operando {
        @Override
        public Tipo tipo() {
            return simbolo.tipo;
        }

        @Override
        public String toString() {
            return simbolo.nombre;
        }
    }

    /** Una etiqueta (L1, L2...): un nombre para un lugar del código. Es el destino de los saltos. */
    record Etiqueta(int numero) implements Operando {
        @Override
        public Tipo tipo() {
            return Tipo.VOID;                      // una etiqueta no guarda ningún valor
        }

        @Override
        public String toString() {
            return "L" + numero;
        }
    }

    record Temporal(int numero, Tipo tipo) implements Operando {
        @Override
        public String toString() {
            return "t" + numero;
        }
    }
}
