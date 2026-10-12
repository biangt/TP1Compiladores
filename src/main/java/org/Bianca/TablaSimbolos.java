package org.Bianca;

import org.Bianca.Ast.Tipo;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tabla de símbolos (punto 6): la "lista de invitados" del programa.
 * Guarda una entrada por cada variable declarada: su nombre, su tipo y su valor actual.
 */
public class TablaSimbolos {

    /** Una entrada de la tabla: información de UNA variable. */
    public static class Simbolo {
        public final String nombre;
        public final Tipo tipo;
        public Object valor;          // valor actual (Integer o Boolean)
        public boolean asignada;      // ¿alguna vez se le asignó un valor?

        Simbolo(String nombre, Tipo tipo) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.valor = (tipo == Tipo.INT) ? (Object) 0 : (Object) false;  // valor inicial: 0 / false
            this.asignada = false;
        }
    }

    // LinkedHashMap mantiene el orden en que se declararon las variables
    private final Map<String, Simbolo> simbolos = new LinkedHashMap<>();

    /** Registra una variable nueva. Error si ya existía con ese nombre. */
    public void declarar(String nombre, Tipo tipo, int linea) {
        if (simbolos.containsKey(nombre)) {
            throw new Errores.Semantico("Error semántico en línea " + linea
                    + ": la variable '" + nombre + "' ya fue declarada");
        }
        simbolos.put(nombre, new Simbolo(nombre, tipo));
    }

    /** Busca una variable. Error si nunca se declaró. */
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
            sb.append(String.format("  %-8s tipo=%-5s valor=%s%n", s.nombre, s.tipo.name().toLowerCase(), s.valor));
        }
        return sb.toString();
    }
}
