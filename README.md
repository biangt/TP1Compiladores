# Mini-compilador (Pre-Proyecto de Compiladores)

Etapas: `texto → Lexer (JFlex) → tokens → parser (CUP) → AST → semántica (tabla de símbolos) → código de tres direcciones → intérprete / assembly x86-64`.

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
| `GeneradorAsm.java` | 7 (pseudo-assembly de pila, versión anterior) |
| `GeneradorAsm64.java` | assembly x86-64 (GNU), generado desde la lista de tres direcciones |
| `Errores.java`, `Main.java` | manejo de errores / programa principal |

## Errores que detecta la semántica

1. Variable **redeclarada** (`int x; int x;`)
2. Variable **no declarada** (`x = z + 1;`)
3. Error de **tipo** (`x = true;`, `1 + true`, `true * 1`, `return` de otro tipo, condición de `if`/`while` que no es `bool`): el mensaje muestra el valor/expresión completo.

Programas de prueba: `ejemplos/error-variable-redeclarada.txt`, `error-variable-no-declarada.txt`,
`error-tipos.txt`, `error-tipo-bool-en-suma.txt`, `error-tipo-bool-por-int.txt`, `error-tipo-num-a-bool.txt`, `error-tipo-return.txt`.

## if / else y while

```
if (condicion) { ... }
if (condicion) { ... } else { ... }
while (condicion) { ... }
```

- Las **llaves son obligatorias** (así un `else` nunca queda ambiguo).
- La **condición debe ser `bool`**: `true`, una variable `bool`, o `a + b` / `a * b` con booleanos. Una condición `int` es error semántico.
- Los bloques pueden contener asignaciones, `return` y más `if` / `while` (anidados). Las variables se declaran todas al principio de `main`.
- Un `return` dentro de un `if` / `while` termina `main` en ese momento.
- El intérprete corta un `while` que se repita más de 1.000.000 de veces (ciclo infinito). El assembly generado, en cambio, sí se colgaría.

**Código de tres direcciones.** Se agregan ETIQUETAS (`L1:`) y SALTOS: `goto L1` (siempre) e `ifFalse c goto L1` (solo si `c` es false).

```
if (c) { A } else { B }          while (c) { A }

    ifFalse c goto L2              L1:
    A                                ifFalse c goto L2
    goto L1                          A
  L2:                                goto L1
    B                              L2:
  L1:
```

**Assembly.** `ifFalse c goto L` → `mov c,%r10 ; cmp $0,%r10 ; je .L` y `goto L` → `jmp .L`. Las etiquetas se escriben `.L1:`.

Programas de prueba: `ejemplos/if-simple.txt`, `if-else.txt`, `if-anidado.txt`, `if-return.txt`, `while-simple.txt`, `while-con-if.txt`, `while-bool.txt`, `while-infinito.txt`,
`error-if-condicion-int.txt`, `error-while-condicion-int.txt`, `error-tipo-dentro-bloque.txt`, `error-sintaxis-sin-llaves.txt`.

## Si modificas `parser.cup`

`parser.java` y `sym.java` (en `src/main/java/org/Bianca/`) son archivos GENERADOS por CUP y hay que regenerarlos a mano:

```
java -jar java-cup-11b.jar -destdir src/main/java -parser parser -symbols sym src/main/cup/parser.cup
```

CUP deja los dos archivos directamente en `src/main/java`; moverlos a `src/main/java/org/Bianca/`.
`Lexer.java`, en cambio, lo regenera Maven solo.

**Importante:** al agregar `if` / `else` / `while` cambiaron los tokens y las reglas, así que `parser.java` y `sym.java` hay que regenerarlos con este comando antes de compilar.

## Generar y ejecutar el assembly

`Main` escribe el assembly en `salida/<nombre>.s`. Para ensamblarlo y correrlo (Linux / WSL / MinGW con gcc):

```
gcc salida/ejemplo4.s -o ejemplo4
./ejemplo4 ; echo $?      # el valor que devuelve main queda como código de salida
```

## Booleanos

`true` se guarda como 1 y `false` como 0. Los operadores `+` y `*` piden dos operandos del **mismo tipo**:

| Operación | Resultado | Significado |
|---|---|---|
| `int + int`, `int * int` | `int` | suma y producto normales |
| `bool + bool` | `bool` | **OR** (`true + true` da `true`, no 2) |
| `bool * bool` | `bool` | **AND** |
| `int` con `bool` | error semántico | tipos mezclados |

En el código de tres direcciones son `SUMA_BOOL` / `MULT_BOOL` y en assembly se traducen a `or` / `and`.
Programas de prueba: `ejemplos/bool-suma-or.txt`, `bool-mult-and.txt`, `bool-mixto.txt`, `bool-suma-falsa.txt`.
