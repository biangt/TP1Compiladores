# Mini-compilador (Pre-Proyecto de Compiladores)

Etapas: `texto → Lexer (JFlex) → tokens → parser (CUP) → AST → semántica (tabla de símbolos) → código de tres direcciones → intérprete / pseudo-assembly`.

## Cómo ejecutarlo

- En IntelliJ: ejecutar `org.Bianca.Main` (sin argumentos corre `ejemplos/ejemplo1.txt` y `ejemplo2.txt`).
  Con argumentos: `Main ejemplos/ejemplo3.txt ejemplos/error-tipos.txt`.
- Con Maven: `mvn compile` (genera `Lexer.java` desde `src/main/jflex/lexico.flex` automáticamente).

## Archivos

| Archivo | Punto del enunciado |
|---|---|
| `src/main/cup/parser.cup` | 1 (gramática), 4 (parser), 5 (construcción del AST) |
| `src/main/jflex/lexico.flex` | 2 (expresiones regulares), 3 (analizador léxico) |
| `Ast.java`, `ImprimirAst.java` | 5 (AST) |
| `TablaSimbolos.java`, `Semantico.java` | 6 (tabla de símbolos y verificaciones) |
| `GeneradorTresDirecciones.java`, `CodigoTresDirecciones.java`, `Instruccion.java`, `Operacion.java`, `Operando.java` | código intermedio de tres direcciones |
| `Interprete.java` | 6 (evaluador) |
| `GeneradorAsm.java` | 7 (pseudo-assembly) |
| `Errores.java`, `Main.java` | manejo de errores / programa principal |

## Errores que detecta la semántica

1. Variable **redeclarada** (`int x; int x;`)
2. Variable **no declarada** (`x = z + 1;`)
3. Error de **tipo** (`x = true;`, `1 + true`, `return` de otro tipo): el mensaje muestra el valor/expresión completo.

Programas de prueba: `ejemplos/error-variable-redeclarada.txt`, `error-variable-no-declarada.txt`,
`error-tipos.txt`, `error-tipo-bool-en-suma.txt`, `error-tipo-num-a-bool.txt`, `error-tipo-return.txt`.

## Si modificas `parser.cup`

`parser.java` y `sym.java` (en `src/main/java/org/Bianca/`) son archivos GENERADOS por CUP y hay que regenerarlos a mano:

```
java -jar java-cup-11b.jar -destdir src/main/java -parser parser -symbols sym src/main/cup/parser.cup
```

CUP deja los dos archivos directamente en `src/main/java`; moverlos a `src/main/java/org/Bianca/`.
`Lexer.java`, en cambio, lo regenera Maven solo.
