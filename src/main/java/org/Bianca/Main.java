package org.Bianca;

import org.Bianca.Ast.Programa;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Programa principal: conecta todas las etapas del compilador.
 *
 *   texto -> Lexer -> tokens -> parser -> AST -> Semantico (tabla de símbolos)
 *                                              -> GeneradorTresDirecciones (código intermedio)
 *                                              -> Interprete (ejecuta)
 *                                              -> GeneradorAsm (traduce)
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
            System.out.print(GeneradorTresDirecciones.generar(programa));

            // Etapa 4b: interpretar
            System.out.println("\n--- Ejecución (intérprete) ---");
            Object resultado = Interprete.ejecutar(programa);
            System.out.println("Resultado de main: " + (resultado == null ? "(sin valor)" : resultado));
            System.out.println("\n--- Tabla de símbolos (después de ejecutar) ---");
            System.out.print(tabla);

            // Etapa 4c: generar pseudo-assembly (solo usa de la tabla los nombres y tipos)
            System.out.println("\n--- Pseudo-assembly ---");
            System.out.print(GeneradorAsm.generar(programa, tabla));

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
