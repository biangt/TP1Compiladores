package org.Bianca;

import org.Bianca.Ast.*;

import java.util.List;

/** Dibuja el AST como texto con sangría: cada nivel más adentro es un hijo del nodo de arriba. */
public final class ImprimirAst {

    private ImprimirAst() { }

    public static String imprimir(Programa p) {
        StringBuilder sb = new StringBuilder();
        sb.append("Programa (main devuelve ").append(p.tipoRetorno().name().toLowerCase()).append(")\n");
        for (Declaracion d : p.declaraciones()) {
            linea(sb, 1, "Declaracion: " + d.tipo.name().toLowerCase() + " " + d.nombre);
        }
        sentencias(p.sentencias(), 1, sb);
        return sb.toString();
    }

    /** Dibuja una lista de sentencias; los bloques de if / while se dibujan con esta misma función, un nivel más adentro. */
    private static void sentencias(List<Sentencia> lista, int nivel, StringBuilder sb) {
        for (Sentencia s : lista) {
            if (s instanceof Asignacion a) {
                linea(sb, nivel, "Asignacion: " + a.variable().nombre + " =");
                expresion(a.valor(), nivel + 1, sb);
            } else if (s instanceof Retorno r) {
                linea(sb, nivel, "Return");
                if (r.valor() != null) {
                    expresion(r.valor(), nivel + 1, sb);
                }
            } else if (s instanceof Si si) {
                linea(sb, nivel, "If");
                linea(sb, nivel + 1, "Condicion:");
                expresion(si.condicion(), nivel + 2, sb);
                linea(sb, nivel + 1, "Entonces:");
                sentencias(si.entonces(), nivel + 2, sb);
                if (si.sino() != null) {
                    linea(sb, nivel + 1, "Sino:");
                    sentencias(si.sino(), nivel + 2, sb);
                }
            } else if (s instanceof Mientras m) {
                linea(sb, nivel, "While");
                linea(sb, nivel + 1, "Condicion:");
                expresion(m.condicion(), nivel + 2, sb);
                linea(sb, nivel + 1, "Cuerpo:");
                sentencias(m.cuerpo(), nivel + 2, sb);
            }
        }
    }

    private static void expresion(Expr e, int nivel, StringBuilder sb) {
        if (e instanceof Num n) {
            linea(sb, nivel, "Num " + n.valor());
        } else if (e instanceof Bool b) {
            linea(sb, nivel, "Bool " + b.valor());
        } else if (e instanceof Var v) {
            linea(sb, nivel, "Var " + v.nombre);
        } else if (e instanceof Suma s) {
            linea(sb, nivel, "Suma (+)");
            expresion(s.izq(), nivel + 1, sb);
            expresion(s.der(), nivel + 1, sb);
        } else if (e instanceof Mult m) {
            linea(sb, nivel, "Mult (*)");
            expresion(m.izq(), nivel + 1, sb);
            expresion(m.der(), nivel + 1, sb);
        }
    }

    /**
     * Una expresión escrita como texto en una sola línea, para los mensajes de error.
     * Los valores se muestran completos (true, false, 5, x...). Las operaciones anidadas
     * llevan paréntesis, lo que deja a la vista cómo las agrupó el parser:  x + ((3 * 2) * y)
     */
    public static String texto(Expr e) {
        return texto(e, false);
    }

    private static String texto(Expr e, boolean anidada) {
        if (e instanceof Num n) {
            return String.valueOf(n.valor());
        }
        if (e instanceof Bool b) {
            return String.valueOf(b.valor());       // "true" / "false", nunca una abreviatura
        }
        if (e instanceof Var v) {
            return v.nombre;
        }
        if (e instanceof OpBinaria op) {
            String s = texto(op.izq(), true) + " " + op.operador() + " " + texto(op.der(), true);
            return anidada ? "(" + s + ")" : s;
        }
        throw new IllegalStateException("Nodo desconocido: " + e);
    }

    private static void linea(StringBuilder sb, int nivel, String texto) {
        sb.append("  ".repeat(nivel)).append(texto).append('\n');
    }
}
