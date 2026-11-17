# Mini-compilador en Java (JFlex + CUP)

- **Materia:** Compiladores — Ingeniería en Sistemas de Información, UNViMe
- **Grupo:**
  - Torres Gil, Bianca
  - Rivarola, Agustín

Compila un lenguaje chico (una función `main`, variables `int` y `bool`, asignaciones, `+`, `*`, `if`/`else`, `while` y `return`) y genera **assembly x86-64 (GNU)**, que se ensambla con `gcc`.

```
texto -> Lexer (JFlex) -> tokens -> parser (CUP) -> AST -> semántica (tabla de símbolos)
      -> código de tres direcciones -> intérprete  /  assembly x86-64 -> gcc -> ejecutable
```

## Herramientas utilizadas

| Herramienta | Para qué |
|---|---|
| Java 17 o superior (se desarrolló con JDK 24) | lenguaje del compilador |
| Maven | compila el proyecto y descarga las librerías |
| JFlex 1.9.1 (`jflex-maven-plugin`) | genera el analizador léxico `Lexer.java` a partir de `lexico.flex` (Maven lo hace solo en cada build) |
| CUP 11b (`java-cup-11b`) | genera el parser `parser.java` y `sym.java` a partir de `parser.cup` (a mano, ver más abajo) |
| `java-cup-runtime` 11b-20160615 | librería que necesitan el Lexer y el parser (la baja Maven) |
| gcc (GNU) en Linux / WSL | ensambla el `.s` y lo convierte en un ejecutable |
| IntelliJ IDEA, Git | desarrollo y control de versiones |

## El lenguaje

```
int main() {
  int n; bool a;              // todas las declaraciones van primero (al menos una)
  a = true; n = 0;
  while (a) {                 // llaves obligatorias en if / else / while
    if (a) { n = n + 1; } else { n = n + 2; }
    a = false;
  }
  return n;
}
```

- `main` puede devolver `int`, `bool` o `void` (sin valor).
- **Operadores:** `+` y `*`, con paréntesis. Piden operandos del **mismo tipo**:

| Operación | Resultado | Significado |
|---|---|---|
| `int + int`, `int * int` | `int` | suma y producto normales |
| `bool + bool` | `bool` | **OR** (`true + true` da `true`, no 2) |
| `bool * bool` | `bool` | **AND** |
| `int` con `bool` | error semántico | tipos mezclados |

- `true` se guarda como 1 y `false` como 0.
- **La condición de `if` / `while` debe ser `bool`** (`true`, una variable `bool`, `a + b`, `a * b`...).
- Los bloques `{ ... }` pueden tener asignaciones, `return` y más `if` / `while` (anidados). Un `return` dentro de un bloque termina `main`.
- No hay comparaciones (`<`, `==`): para que un `while` termine se usan variables `bool` como estado (ver `ejemplos/while-simple.txt`).

## Cómo compilar

Desde la carpeta del proyecto:

```
mvn compile
```

Maven genera `Lexer.java` con JFlex y compila todo en `target/classes`.

## Cómo ejecutar el compilador (por consola)

Recibe uno o más archivos de programa. **Windows (PowerShell):**

```
java -cp "target/classes;$HOME\.m2\repository\com\github\vbmacher\java-cup-runtime\11b-20160615\java-cup-runtime-11b-20160615.jar" org.Bianca.Main ejemplos/ejemplo4.txt
```

**Linux / WSL:**

```
java -cp "target/classes:$HOME/.m2/repository/com/github/vbmacher/java-cup-runtime/11b-20160615/java-cup-runtime-11b-20160615.jar" org.Bianca.Main ejemplos/ejemplo4.txt
```

(En IntelliJ: ejecutar `org.Bianca.Main` con los archivos como *Program arguments*.)

Para cada programa se muestra: el AST, el código de tres direcciones, el resultado del intérprete, la tabla de símbolos y el assembly. El assembly también queda guardado en `salida/<nombre>.s`.

## Cómo generar y correr el ejecutable (gcc)

El assembly es de Linux (x86-64, sintaxis GNU). En Linux o en WSL/Ubuntu:

```
gcc salida/ejemplo4.s -o ejemplo4
./ejemplo4          # muestra el resultado de main en pantalla
echo $?             # el valor de main también queda como código de salida (0 a 255)
```

El programa generado **muestra el resultado con `printf`** (los `bool` como `true` / `false`) y además lo deja como código de salida. Si `main` es `void`, no muestra nada.
En WSL, si gcc falla con `cannot find Scrt1.o`, falta `sudo apt install libc6-dev`.

### Cómo se traduce `if` / `while`

El código de tres direcciones usa **etiquetas** (`L1:`) y **saltos**: `goto L1` (siempre) e `ifFalse c goto L1` (solo si `c` es false). En assembly: `jmp .L1` y `cmp $0, ...` + `je .L1`.

```
if (c) { A } else { B }          while (c) { A }

    ifFalse c goto L2              L1:
    A                                ifFalse c goto L2
    goto L1                          A
  L2:                                goto L1
    B                              L2:
  L1:
```

## Casos de prueba (`ejemplos/`)

Se corren con el comando de la sección *Cómo ejecutar*. Resultado esperado:

| Archivo | Qué prueba | Resultado |
|---|---|---|
| `ejemplo1.txt` | `void main`, asignaciones | sin valor |
| `ejemplo2.txt` | `return` temprano, variable usada sin asignar (aviso) | `1` |
| `ejemplo3.txt` | `bool main`, mezcla de variables `int` y `bool` | `true` |
| `ejemplo4.txt` | enteros: `b = a * (3 + a)` | `11` |
| `bool-suma-or.txt` | `true + false` (OR) | `true` |
| `bool-mult-and.txt` | `true * false` (AND) | `false` |
| `bool-mixto.txt` | `a * b + true` | `true` |
| `bool-suma-falsa.txt` | `false + false` | `false` |
| `if-simple.txt` | `if` sin `else` | `10` |
| `if-else.txt` | `if` / `else` | `2` |
| `if-anidado.txt` | `if` dentro de `if` | `2` |
| `if-return.txt` | `return` dentro de un `if` | `5` |
| `while-simple.txt` | `while` con tres booleanos que "se corren" | `3` |
| `while-con-if.txt` | `if` dentro de un `while` | `21` |
| `while-bool.txt` | `while` con `bool main` | `true` |
| `while-infinito.txt` | ciclo infinito: el intérprete lo corta | error de ejecución |

### Programas que deben dar error

| Archivo | Error |
|---|---|
| `error-lexico.txt` | léxico: carácter no válido |
| `error-sintaxis.txt` | sintáctico |
| `error-sintaxis-sin-llaves.txt` | sintáctico: `if` sin llaves |
| `error-variable-redeclarada.txt` | semántico: variable redeclarada |
| `error-variable-no-declarada.txt` | semántico: variable no declarada |
| `error-tipos.txt`, `error-tipo-num-a-bool.txt`, `error-tipo-return.txt` | semántico: tipos incompatibles en asignación / `return` |
| `error-tipo-bool-en-suma.txt`, `error-tipo-bool-por-int.txt` | semántico: `int` mezclado con `bool` en `+` / `*` |
| `error-if-condicion-int.txt`, `error-while-condicion-int.txt` | semántico: la condición no es `bool` |
| `error-tipo-dentro-bloque.txt` | semántico: error de tipo dentro de un bloque |

## Archivos

| Archivo | Qué hace |
|---|---|
| `src/main/jflex/lexico.flex` | expresiones regulares y analizador léxico |
| `src/main/cup/parser.cup` | gramática, parser y construcción del AST |
| `Ast.java`, `ImprimirAst.java` | árbol sintáctico y su impresión |
| `TablaSimbolos.java`, `Semantico.java` | tabla de símbolos y verificaciones |
| `GeneradorTresDirecciones.java`, `CodigoTresDirecciones.java`, `Instruccion.java`, `Operacion.java`, `Operando.java` | código intermedio |
| `Interprete.java` | ejecuta el programa sobre el AST |
| `GeneradorAsm.java` | pseudo-assembly de pila (versión anterior) |
| `GeneradorAsm64.java` | assembly x86-64 (GNU) desde la lista de tres direcciones |
| `Errores.java`, `Main.java` | errores y programa principal |

## Si se modifica `parser.cup`

`parser.java` y `sym.java` (en `src/main/java/org/Bianca/`) los genera CUP y hay que regenerarlos a mano. Con el jar `java-cup-11b.jar`, desde la carpeta del proyecto:

```
java -cp java-cup-11b.jar java_cup.Main -destdir src/main/java -parser parser -symbols sym src/main/cup/parser.cup
```

CUP deja los dos archivos en `src/main/java`: hay que moverlos a `src/main/java/org/Bianca/`. `Lexer.java`, en cambio, lo regenera Maven solo. No se editan a mano los archivos de `target/`.
