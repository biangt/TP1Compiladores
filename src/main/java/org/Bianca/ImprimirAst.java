package org.Bianca;

import org.Bianca.Ast.*;

/** Dibuja el AST como texto con sangría: cada nivel más adentro es un hijo del nodo de arriba. */
public final class ImprimirAst {

    private ImprimirAst() { }

    public static String imprimir(Programa p) {
        StringBuilder sb = new StringBuilder();
        sb.append("Programa (main devuelve ").append(p.tipoRetorno().name().toLowerCase()).append(")\n");
        for (Declaracion d : p.declaraciones()) {
            linea(sb, 1, "Declaracion: " + d.tipo().name().toLowerCase() + " " + d.nombre());
        }
        for (Sentencia s : p.sentencias()) {
            if (s instanceof Asignacion a) {
                linea(sb, 1, "Asignacion: " + a.variable().nombre + " =");
                expresion(a.valor(), 2, sb);
            } else if (s instanceof Retorno r) {
                linea(sb, 1, "Return");
                if (r.valor() != null) {
                    expresion(r.valor(), 2, sb);
                }
            }
        }
        return sb.toString();
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

    private static void linea(StringBuilder sb, int nivel, String texto) {
        sb.append("  ".repeat(nivel)).append(texto).append('\n');
    }
}
