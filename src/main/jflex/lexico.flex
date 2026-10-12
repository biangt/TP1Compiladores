/* ============================================================
   lexico.flex  -  ANALIZADOR LÉXICO (puntos 2 y 3)
   JFlex lee este archivo y genera la clase Lexer.java.
   El Lexer corta el texto del programa en TOKENS.
   ============================================================ */

/* ---------- Sección 1: código de usuario (se copia tal cual a Lexer.java) ---------- */
package org.Bianca;

import java_cup.runtime.Symbol;

%%

/* ---------- Sección 2: opciones y macros ---------- */

/* Nombre de la clase que se genera: Lexer */
%class Lexer

/* Entiende acentos y cualquier carácter Unicode */
%unicode

/* Habilita la variable yyline (número de línea, desde 0) */
%line

/* Habilita la variable yycolumn (número de columna, desde 0) */
%column

/* Modo compatible con CUP: el Lexer devuelve objetos Symbol */
%cup

/* Código Java que se copia dentro de la clase Lexer */
%{
    /* Crea un token: tipo (constante de sym), posición y el texto que lo originó. */
    private Symbol simbolo(int tipo) {
        return new Symbol(tipo, yyline, yycolumn, yytext());
    }

    /* Igual, pero el "valor" del token es un objeto propio (ej.: el Integer de un número). */
    private Symbol simbolo(int tipo, Object valor) {
        return new Symbol(tipo, yyline, yycolumn, valor);
    }
%}

/* Macros: nombres para expresiones regulares que usamos abajo */
LETRA   = [a-zA-Z]
DIGITO  = [0-9]
NUMERO  = {DIGITO}+
ID      = {LETRA}({LETRA}|{DIGITO})*
/* espacios, tabuladores, saltos de línea y la marca BOM que algunos editores de Windows agregan */
ESPACIO = [ \t\r\n\f﻿]+

%%

/* ---------- Sección 3: reglas (expresión regular  ->  acción) ---------- */

/* Palabras reservadas. Van ANTES que {ID}: si hay empate de longitud, gana la regla que aparece primero. */
"int"      { return simbolo(sym.INT); }
"bool"     { return simbolo(sym.BOOL); }
"void"     { return simbolo(sym.VOID); }
"main"     { return simbolo(sym.MAIN); }
"return"   { return simbolo(sym.RETURN); }
"true"     { return simbolo(sym.TRUE); }
"false"    { return simbolo(sym.FALSE); }

/* Operadores */
"+"        { return simbolo(sym.PLUS); }
"*"        { return simbolo(sym.TIMES); }
"="        { return simbolo(sym.ASSIGN); }

/* Delimitadores */
"("        { return simbolo(sym.LPAREN); }
")"        { return simbolo(sym.RPAREN); }
"{"        { return simbolo(sym.LBRACE); }
"}"        { return simbolo(sym.RBRACE); }
";"        { return simbolo(sym.SEMI); }

/* Constantes numéricas: el valor del token es un Integer ya convertido */
{NUMERO}   {
               try {
                   return simbolo(sym.NUMBER, Integer.valueOf(yytext()));
               } catch (NumberFormatException e) {
                   throw new Errores.Lexico("Error léxico en línea " + (yyline + 1) + ", columna " + (yycolumn + 1)
                           + ": el número " + yytext() + " es demasiado grande");
               }
           }

/* Identificadores (nombres de variables): el valor del token es el String con el nombre */
{ID}       { return simbolo(sym.ID, yytext()); }

/* Espacios en blanco: se ignoran (no devolvemos nada, el Lexer sigue leyendo) */
{ESPACIO}  { /* nada */ }

/* Cualquier otro carácter no pertenece al lenguaje: error léxico */
[^]        {
               throw new Errores.Lexico("Error léxico en línea " + (yyline + 1) + ", columna " + (yycolumn + 1)
                       + ": carácter no válido '" + yytext() + "'");
           }
