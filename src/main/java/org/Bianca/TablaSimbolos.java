package org.Bianca;

import org.Bianca.Ast.Tipo;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tabla de símbolos (punto 6): la "lista de invitados" del programa.
 * Guarda una entrada (un Simbolo) por cada variable declarada.
 *
 * Cada Simbolo contiene:
 *   - nombre    cómo se llama la variable                       (no cambia)
 *   - tipo      int o bool                                      (no cambia)
 *   - linea     en qué línea del programa se declaró            (no cambia)
 *   - valor     su valor ACTUAL (Integer o Boolean)             (lo cambia el intérprete)
 *   - asignada  si alguna vez se le asignó un valor             (lo cambia el intérprete)
 *
 * Los nodos del AST (Declaracion y Var) guardan un puntero a su Simbolo: por eso, después de la
 * semántica, nadie vuelve a buscar por nombre. El nodo ya sabe cuál es su ficha.
 */
public class TablaSimbolos {

    /** Una entrada de la tabla: información de UNA variable. */
    public static class Simbolo {
        public final String nombre;
        public final Tipo tipo;
        public final int linea;       // línea de la declaración (para mensajes de error)
        public Object valor;          // valor actual (Integer o Boolean)
        public boolean asignada;      // ¿alguna vez se le asignó un valor?

        Simbolo(String nombre, Tipo tipo, int linea) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.linea = linea;
            this.valor = (tipo == Tipo.INT) ? (Object) 0 : (Object) false;  // valor inicial: 0 / false
            this.asignada = false;
        }
    }

    // LinkedHashMap: búsqueda por nombre en tiempo constante, y recuerda el orden de declaración
    private final Map<String, Simbolo> simbolos = new LinkedHashMap<>();

    /** Registra una variable nueva y devuelve su ficha. Error si ya existía con ese nombre. */
    public Simbolo declarar(String nombre, Tipo tipo, int linea) {
        Simbolo previo = simbolos.get(nombre);
        if (previo != null) {
            throw new Errores.Semantico("Error semántico en línea " + linea
                    + ": la variable '" + nombre + "' ya fue declarada (primera declaración en línea "
                    + previo.linea + ")");
        }
        Simbolo nuevo = new Simbolo(nombre, tipo, linea);
        simbolos.put(nombre, nuevo);
        return nuevo;
    }

    /** Busca una variable por su nombre. Error si nunca se declaró. */
    public Simbolo buscar(String nombre, int linea) {
        Simbolo s = simbolos.get(nombre);
        if (s == null) {
            throw new Errores.Semantico("Error semántico en línea " + linea
                    + ": la variable '" + nombre + "' no fue declarada");
        }
        return s;
    }

    /** Todas las variables, en orden de declaración. */
    public Collection<Simbolo> simbolos() {
        return simbolos.values();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Simbolo s : simbolos.values()) {
            sb.append(String.format("  %-8s tipo=%-5s línea=%-3d valor=%s%n",
                    s.nombre, s.tipo.name().toLowerCase(), s.linea, s.valor));
        }
        return sb.toString();
    }
}
