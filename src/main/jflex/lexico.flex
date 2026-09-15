package org.Bianca;

%%

%class Lexer
%unicode
%line
%column
%standalone

DIGITO = [0-9]
LETRA = [a-zA-Z]
ENTERO = {DIGITO}+
ID = {LETRA}({LETRA}|{DIGITO})*
ESPACIO = [ \t\r\n]+

%%

{ESPACIO}    { /* ignorar espacios */ }
"+"          { System.out.println("OPERADOR +"); }
"*"          { System.out.println("OPERADOR *"); }
"("          { System.out.println("PARENTESIS ABRE"); }
")"          { System.out.println("PARENTESIS CIERRA"); }
";"          { System.out.println("PUNTO Y COMA"); }
"int"        { System.out.println("PALABRA RESERVADA: int"); }
"bool"       { System.out.println("PALABRA RESERVADA: bool"); }
{ENTERO}     { System.out.println("NUMERO: " + yytext()); }
{ID}         { System.out.println("IDENTIFICADOR: " + yytext()); }
.            { System.out.println("CARACTER NO RECONOCIDO: " + yytext()); }