package org.Bianca;

/**
 * Los tipos de error que puede detectar el compilador, uno por etapa (más uno de ejecución).
 * Todos heredan de ErrorCompilador para poder atraparlos juntos en Main.
 */
public final class Errores {

    private Errores() { }

    /** Base común: un error del programa que se está compilando (no un bug nuestro). */
    public static class ErrorCompilador extends RuntimeException {
        public ErrorCompilador(String mensaje) {
            super(mensaje);
        }
    }

    /** Carácter que no pertenece al lenguaje (lo lanza el Lexer). */
    public static class Lexico extends ErrorCompilador {
        public Lexico(String mensaje) {
            super(mensaje);
        }
    }

    /** Tokens en un orden que la gramática no permite (lo lanza el parser). */
    public static class Sintactico extends ErrorCompilador {
        public Sintactico(String mensaje) {
            super(mensaje);
        }
    }

    /** Error mientras se ejecuta el programa en el intérprete (por ejemplo, un while que no termina). */
    public static class Ejecucion extends ErrorCompilador {
        public Ejecucion(String mensaje) {
            super(mensaje);
        }
    }

    /** Programa bien escrito pero sin sentido: variable no declarada, tipos incompatibles... */
    public static class Semantico extends ErrorCompilador {
        public Semantico(String mensaje) {
            super(mensaje);
        }
    }
}
