package org.Bianca;

/**
 * UNA instrucción de tres direcciones: un operador, como máximo dos operandos y un resultado.
 *
 *   t1 = 3 * 2        operacion=MULT_INT   arg1=3    arg2=2     resultado=t1
 *   x  = t3           operacion=ASIGNAR    arg1=t3   arg2=null  resultado=x
 *   return t2         operacion=RETORNAR   arg1=t2   arg2=null  resultado=null
 *
 * Los casilleros que no se usan quedan en null. Los operandos no son texto: son objetos que
 * apuntan a la constante, al Simbolo de la variable o al temporal correspondiente.
 */
public record Instruccion(Operacion operacion, Operando arg1, Operando arg2, Operando resultado) {

    @Override
    public String toString() {
        return switch (operacion) {
            case SUMA_INT, MULT_INT -> resultado + " = " + arg1 + " " + operacion.simbolo + " " + arg2;
            case ASIGNAR_INT, ASIGNAR_BOOL -> resultado + " = " + arg1;
            case RETORNAR_INT, RETORNAR_BOOL -> "return " + arg1;
            case RETORNAR_VOID -> "return";
        };
    }
}
