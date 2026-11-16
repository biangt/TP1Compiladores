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
 * Para if y while, además de instrucciones comunes hay ETIQUETAS (L1:) y SALTOS (goto, ifFalse):
 * el vector se recorre en orden, salvo cuando un salto manda a otra etiqueta.
 */
public class CodigoTresDirecciones {

    private final List<Instruccion> instrucciones = new ArrayList<>();
    private int ultimoTemporal = 0;
    private int ultimaEtiqueta = 0;

    /** Crea un temporal nuevo (t1, t2, ...) del tipo indicado. */
    public Operando.Temporal nuevoTemporal(Tipo tipo) {
        ultimoTemporal++;
        return new Operando.Temporal(ultimoTemporal, tipo);
    }

    /** Crea una etiqueta nueva (L1, L2, ...). Nunca se repite una, así los saltos son inequívocos. */
    public Operando.Etiqueta nuevaEtiqueta() {
        ultimaEtiqueta++;
        return new Operando.Etiqueta(ultimaEtiqueta);
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
            Instruccion ins = instrucciones.get(i);
            String sangria = (ins.operacion() == Operacion.ETIQUETA) ? "" : "    ";   // las etiquetas se ven al margen
            sb.append(String.format("  %2d:  %s%s%n", i, sangria, ins));
        }
        return sb.toString();
    }
}
