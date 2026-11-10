package org.Bianca;

import org.Bianca.Ast.Programa;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Programa principal: conecta todas las etapas del compilador.
 *
 *   texto -> Lexer -> tokens -> parser -> AST -> Semantico (tabla de símbolos)
 *                                              -> GeneradorTresDirecciones (código intermedio)
 *                                              -> Interprete (ejecuta)
 *                                              -> GeneradorAsm64 (assembly x86-64 desde la lista de 3 direcciones)
 *
 * Uso:  Main archivo1.txt archivo2.txt ...   (sin argumentos corre los ejemplos del enunciado)
 */
public class Main {

    public static void main(String[] args) {
        String[] archivos = (args.length > 0)
                ? args
                : new String[] {"ejemplos/ejemplo1.txt", "ejemplos/ejemplo2.txt"};
        for (String archivo : archivos) {
            procesar(archivo);
        }
    }

    private static void procesar(String archivo) {
        System.out.println("==================== " + archivo + " ====================");
        try (Reader lector = Files.newBufferedReader(Paths.get(archivo))) {
            System.out.println("Programa:");
            System.out.println("  " + Files.readString(Paths.get(archivo)).trim());

            // Etapas 1 y 2: léxico + sintaxis. parse() devuelve la raíz del AST.
            Lexer lexer = new Lexer(lector);
            parser parser = new parser(lexer);
            Programa programa = (Programa) parser.parse().value;

            System.out.println("\n--- AST ---");
            System.out.print(ImprimirAst.imprimir(programa));

            // Etapa 3: semántica (arma la tabla de símbolos y verifica)
            TablaSimbolos tabla = Semantico.analizar(programa);

            // Etapa 4a: código intermedio de tres direcciones (recorre el AST de las hojas a la raíz)
            System.out.println("\n--- Código de tres direcciones ---");
            CodigoTresDirecciones codigo = GeneradorTresDirecciones.generar(programa);
            System.out.print(codigo);

            // Etapa 4b: interpretar
            System.out.println("\n--- Ejecución (intérprete) ---");
            Object resultado = Interprete.ejecutar(programa);
            System.out.println("Resultado de main: " + (resultado == null ? "(sin valor)" : resultado));
            System.out.println("\n--- Tabla de símbolos (después de ejecutar) ---");
            System.out.print(tabla);

            // Etapa 5: assembly x86-64, generado recorriendo la lista de tres direcciones
            System.out.println("\n--- Assembly x86-64 (GNU) ---");
            String asm = GeneradorAsm64.generar(codigo, tabla);
            System.out.print(asm);

            // También se guarda en salida/<nombre>.s para ensamblarlo con gcc
            Path carpeta = Paths.get("salida");
            Files.createDirectories(carpeta);
            String nombre = Paths.get(archivo).getFileName().toString().replaceAll("\\.[^.]*$", "");
            Files.writeString(carpeta.resolve(nombre + ".s"), asm);

        } catch (Errores.ErrorCompilador e) {
            System.out.println("\n" + e.getMessage());
        } catch (IOException e) {
            System.out.println("No se pudo leer el archivo: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println();
    }
}
