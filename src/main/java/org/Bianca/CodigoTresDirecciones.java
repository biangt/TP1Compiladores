package org.Bianca;

import org.Bianca.Ast.Tipo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * El código de tres direcciones de un programa: un vector (lista) de instrucciones, en orden.
 * También reparte los nombres de los temporales (t1, t2, t3...), sin repetir ninguno.
 *
 * Esta es la "representación intermedia": ya no es el árbol, pero todavía no es assembly.
 * Más adelante (if, while) acá se agregarían los saltos entre instrucciones.
 */
public class CodigoTresDirecciones {

    private final List<Instruccion> instrucciones = new ArrayList<>();
    private int ultimoTemporal = 0;

    /** Crea un temporal nuevo (t1, t2, ...) del tipo indicado. */
    public Operando.Temporal nuevoTemporal(Tipo tipo) {
        ultimoTemporal++;
        return new Operando.Temporal(ultimoTemporal, tipo);
    }

    /** Agrega una instrucción al final del vector. */
    public void agregar(Instruccion instruccion) {
        instrucciones.add(instruccion);
    }

    public List<Instruccion> instrucciones() {
        return Collections.unmodifiableList(instrucciones);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < instrucciones.size(); i++) {
            sb.append(String.format("  %2d:  %s%n", i, instrucciones.get(i)));
        }
        return sb.toString();
    }
}
