package analizador;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Espinoza Castro Javier Fernando 21630220
//Analizador
//NOTA IMPORTANTE: PONGA LA DIRECCION DEL ARCHIVO PRUEBA, AL FINAL DE ESTE PROGRAMA, ESTA DONDE SE PONE LA DIRECCION
public class ANALIZADOR {

    //Lista para guardar los errores que tengamos
    static List<fail> fails = new ArrayList<>();
    //Lista para guardar cada variable con su tipo
    static HashMap<String, String> var = new HashMap<>();
    //Lista inidice cada tipo de error
    static HashMap<Integer, String> errores = new HashMap<>();
    //Lista para sincronizar las lineas, de esta lista se saca el numero de linea para decir donde esta el error
    static List<String> lineaLex = new ArrayList<>();
    static List<String> codigo = new ArrayList<>();
    static List<String> data = new ArrayList<>();

    static Stack<pila> pila = new Stack<>();

    static Pattern letra = Pattern.compile("[a-z]");
    static Pattern num = Pattern.compile("[0-9]");
    static Pattern ID = Pattern.compile(letra + "+_");
    static Pattern OpArit = Pattern.compile("\\+|\\-|\\*|\\/|\\%");
    static Pattern OpLogic = Pattern.compile("&&| \\|\\|");
    static Pattern OpRelacional = Pattern.compile("<=|>=|<|>|!=|==");
    static Pattern tipo = Pattern.compile("(int|string|float)");
    static Pattern CteInt = Pattern.compile(num + "+");
    static Pattern CteFloat = Pattern.compile(CteInt + "\\." + CteInt + "+");
    static Pattern CteString = Pattern.compile("\"([^\"]*)\"");
    static Pattern decla = Pattern.compile("\\s*(" + tipo + ")\\s(" + ID + ")\\s*;?");
    static Pattern constante = Pattern.compile("(" + CteInt + ")|(" + CteFloat + ")|(" + CteString + ")");
    static Pattern operando = Pattern.compile("(" + ID + ")|(" + constante + ")");
    static Pattern EXP = Pattern.compile("(" + operando + ")|((" + operando + ")\\s*\\+\\s*(" + operando + "))+");
    static Pattern Asig = Pattern.compile("\\s*(" + ID + ")\\s*=\\s*(" + operando + ")\\s*;?\\s*");
    static Pattern OperacionArit = Pattern.compile("\\s*(" + ID + ")\\s*=\\s*(" + operando + ")\\s*(" + OpArit + ")\\s*(" + operando + ")\\s*;?");
    static Pattern condicionSimple = Pattern.compile("(" + operando + ")\\s*(" + OpRelacional + ")\\s*(" + operando + ")");
    static Pattern condicionSimplefor = Pattern.compile("(" + operando + ")\\s*~\\s*(" + operando + ")");
    static Pattern condicion = Pattern.compile("(" + condicionSimple + ")\\s*((" + OpLogic + ")\\s*(" + condicionSimple + "))*");
    static Pattern leer = Pattern.compile("\\s*leer (" + ID + ")\\s*;?\\s*");
    static Pattern escribir = Pattern.compile("\\s*escribir (" + EXP + ")\\s*;?\\s*");
    static Pattern ends = Pattern.compile("\\s*(ewhile|eif|eelse|efor)\\s*");
    static Pattern IF = Pattern.compile("\\s*if \\(\\s*(" + condicion + ")\\s*\\)\\s*");
    static Pattern IFf = Pattern.compile("\\s*if+(.*)\\s*");
    static Pattern ELSE = Pattern.compile("\\s*else\\s*");
    static Pattern incre = Pattern.compile("\\s*" + ID + "\\+\\+\\s*");
    static Pattern decre = Pattern.compile("\\s*" + ID + "\\-\\-\\s*");
    static Pattern FOR = Pattern.compile("\\s*for\\s*\\(\\s*(" + Asig + ")\\s*;\\s*(" + condicionSimplefor + ")\\s*;\\s*(" + incre + "|" + decre + ")\\s*\\)\\s*");
    static Pattern FORr = Pattern.compile("\\s*for\\s*(.*)\\s*");
    static Pattern WHILEe = Pattern.compile("\\s*while (.*)\\s*");
    static Pattern WHILE = Pattern.compile("\\s*while \\(\\s*(" + condicion + ")\\s*\\)\\s*");
    static Pattern ini = Pattern.compile("\\s*inicio\\s*");
    static Pattern fin = Pattern.compile("\\s*fin\\s*");
    static Pattern vacio = Pattern.compile("");
    static Pattern sentencia = Pattern.compile("((" + vacio + ")|(" + OperacionArit + ")|(" + leer + ")|(" + escribir + ")|(" + IF + ")|(" + ELSE + ")|(" + FOR + ")|(" + WHILE + ")|(" + ends + ")|(" + Asig + ")|(" + decla + ")|(" + ini + ")|(" + fin + "))");

    public static void Rellenar() {
        errores.put(1, "Tipos de operandos incompatibles.");
        errores.put(2, "Variable no declarada.");
        errores.put(3, "Variable declarada mas de una vez.");
        errores.put(4, "Bloque no abierto.");
        errores.put(5, "Bloque no cerrado.");
        errores.put(6, "Elemento no reconocido.");
        errores.put(7, "Simbolo no reconocido.");
        errores.put(8, "Error de sintaxis.");
        errores.put(9, "Operacion invalida.");
        errores.put(10, "Condicion mal formada.");
        errores.put(11, "Bloque no cerrado correctamente.");
        errores.put(12, "Bloque cerrado sin inicio correspondiente.");
        errores.put(13, "Tipo de dato invalido en la sentencia.");
        errores.put(14, "Falta etiqueda de inicio.");
        errores.put(15, "Falta etiqueda de fin.");
    }

    public static String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    public static String cleaner(String code) {
        // Eliminar comentarios de una línea
        String noSingleLineComments = code.replaceAll("//.*", "");

        // Eliminar espacios en blanco adicionales y líneas en blanco vacías
        String noExtraSpacesOrBlankLines = noSingleLineComments.replaceAll("(?m)^[ \t]*\r?\n", "");

        // Reemplazar múltiples espacios por un solo espacio
        String noExtraSpaces = noExtraSpacesOrBlankLines.replaceAll(" +", " ");
        String clean = noExtraSpaces.replaceAll("", "");

        return clean;

    }

    public static void revisionLex(String code) {

        int lineNumber = 1;

        String[] keywords = {"escribir", "if", "eif",
            "else", "eelse", "for", "efor", "while", "ewhile", "leer",
            "int", "string", "float", "inicio", "fin",}; // lista de palabras reservadas de mi lenguaje
        String[] type = {"int", "string", "float"}; // lista de palabras reservadas de mi lenguaje

        Pattern reservedPattern = Pattern.compile("\\b(" + String.join("|", keywords) + ")\\b"); // ER que machara con las palabras clave
        Pattern types = Pattern.compile("\\b(" + String.join("|", type) + ")\\b"); // ER que machara con las palabras clave
        Pattern variablesPattern = Pattern.compile("\\b[a-z]+\\_\\b");
        Pattern stringPattern = Pattern.compile("\"(.*?)\"");  //Este sirve para machar los souts
        Pattern commentPattern = Pattern.compile("//.*|/\\*[\\s\\S]*?\\*/|/\\*\\*[\\s\\S]*?\\*/");
        Pattern jumpLinePattern = Pattern.compile("\n");
        Pattern numberPattern = Pattern.compile("\\b-?\\d+(\\.\\d+)?\\b");
        Pattern operatorPattern = Pattern.compile("[+\\-*/<>%=!&|]+");
        Pattern alphabetCheckPattern = Pattern.compile("[a-z0-9()<>!=+\\.\\_*\\/\"\\\";\\s:~]+");
        Pattern puntuactionCheckPattern = Pattern.compile("[\\,\\;\\!\"\\(\\)\\-]+");

        //Con esto junto todas las expresiones en una sola y se separan con OR'S
        Matcher matcher = Pattern.compile(
                String.join("|",
                        reservedPattern.pattern(),
                        variablesPattern.pattern(),
                        stringPattern.pattern(),
                        commentPattern.pattern(),
                        jumpLinePattern.pattern(),
                        operatorPattern.pattern(),
                        puntuactionCheckPattern.pattern(),
                        types.pattern(),
                        numberPattern.pattern())
        ).matcher(code);

        //Aqui se guarda el tipo de variable
        String currentVariableType = "";
        //Para tomar el principio de las partes que no machan
        int currentIndex = 0;
        while (matcher.find()) {//El matcher find encuentra todas y cada una de las machaciones que haya en el texto y agarra una por una
            String substring = "";
            String token = matcher.group();

            int start = matcher.start();
            int end = matcher.end();

            if (start > currentIndex) {
                substring = code.substring(currentIndex, start);
                String[] words = substring.split("\\s+");
                for (String word : words) {
                    if (word.length() > 1) {
                        fails.add(new fail(lineNumber, errores.get(6)));
                        for (int i = 0; i < substring.length(); i++) {
                            char character = substring.charAt(i);
                            // Verifica si el carácter no es válido según tu alfabeto
                            if (!alphabetCheckPattern.matcher(String.valueOf(character)).matches()) {
                                // Agrega un mensaje de error de símbolo

                                fails.add(new fail(lineNumber, errores.get(7)));
                            }
                        }
                    } else if (word.length() == 1) {
                        if (!alphabetCheckPattern.matcher(String.valueOf(word)).matches()) {
                            fails.add(new fail(lineNumber, errores.get(7)));
                        }
                    }
                }
            }
            // Itera por cada carácter en el token
            //Estos if clasifican cada token y verifica que la variable este declarada
            if (types.matcher(token).matches()) {
                currentVariableType = token;
            } else if (variablesPattern.matcher(token).matches()) {
                if (!var.containsKey(token)) {
                    //Metemos la variable con su tipo a la lista
                    if (currentVariableType.equals("int") | currentVariableType.equals("string") | currentVariableType.equals("float")) {
                        var.put(token, currentVariableType);
                    } else {
                        fails.add(new fail(lineNumber, errores.get(2)));
                    }
                } else {
                    if ((currentVariableType.equals("int") | currentVariableType.equals("string") | currentVariableType.equals("float"))) {
                        fails.add(new fail(lineNumber, errores.get(3)));
                    }
                }
            } else if (jumpLinePattern.matcher(token).matches()) {// Actualiza el número de línea 
                lineNumber++;
                currentVariableType = "";
            } else if (token.equals(";")) {// Actualiza el número de línea 
                currentVariableType = "";
            }
            currentIndex = end;
        }

        if (currentIndex < code.length()) {
            fails.add(new fail(lineNumber, errores.get(6)));

        }

    }

    public static void revisionSintac(String code) {
        // Dividir el texto por saltos de línea
        String[] lineas = code.split("\n");

        // Convertir el array a una lista
        List<String> listaDeLineas = Arrays.asList(lineas);

        for (String linea : listaDeLineas) {
            Matcher matcher = sentencia.matcher(linea);

            int linenumber = listaDeLineas.indexOf(linea);

            if (linenumber + 1 == 1) {
                if (!linea.equals("inicio")) {
                    linenumber = lineaLex.indexOf(linea);
                    fails.add(new fail(linenumber, errores.get(14)));
                }
            }
            if (linenumber + 1 == (listaDeLineas.size())) {
                if (!linea.equals("fin")) {

                    fails.add(new fail((listaDeLineas.size()) + 1, errores.get(15)));
                }
            }

            if (!matcher.matches()) {
                linenumber = lineaLex.indexOf(linea);
                fails.add(new fail(linenumber + 1, errores.get(8)));
            }
        }
    }

    public static void revisionSeman(String code) {

        String[] lineas = code.split("\n");

        // Convertir el array a una lista
        List<String> listaDeLineas = Arrays.asList(lineas);

        String end = "";
        for (String line : listaDeLineas) {
            end = "";
            int linenumber = listaDeLineas.indexOf(line);
            linenumber = lineaLex.indexOf(line);
            Matcher matcher;

            //Comienzo de analisis por linea
            if ((matcher = Asig.matcher(line)).matches()) {
                //Si la linea es una asugnacion sea toman las partes y se mandan a un metodo que las compara si son iguales en tipo
                typesEval(matcher.group(1), matcher.group(2), linenumber);
                //System.out.println(matcher.group(1)+matcher.group(2));

            } else if ((matcher = OperacionArit.matcher(line)).matches()) {
                if (matcher.group(4) == null) {
                    evalOperacionArit(matcher.group(1), matcher.group(2), matcher.group(9), matcher.group(3), linenumber);
                } else {
                    evalOperacionArit(matcher.group(1), matcher.group(2), matcher.group(9), matcher.group(4), linenumber);
                }

            } else if ((matcher = leer.matcher(line)).matches()) {
                if (!var.containsKey(matcher.group(1))) {
                    // Si es un ID y está en el HashMap, obtener su tipo
                    fails.add(new fail((1 + linenumber), errores.get(2)));
                }

            } else if ((matcher = ends.matcher(line)).matches()) {
                // Si es el final de un bloque, realiza acciones semánticas y desapila
                end = matcher.group(1);
                if (!pila.isEmpty()) {

                    if (!(("e" + pila.pop().getStart()).equals(end.trim()))) {
                        fails.add(new fail((1 + linenumber), errores.get(12)));
                    }
                } else {
                    fails.add(new fail((1 + linenumber), errores.get(12)));
                }

            } else if ((matcher = IFf.matcher(line)).matches()) {

                pila.push(new pila(linenumber, "if"));

                if ((matcher = IF.matcher(line)).matches()) {
                    evalCond(matcher.group(1), linenumber);
                }

            } else if ((matcher = WHILEe.matcher(line)).matches()) {

                pila.push(new pila(linenumber, "while"));
                if ((matcher = WHILE.matcher(line)).matches()) {
                    evalCond(matcher.group(1), linenumber);
                }

            } else if ((matcher = FORr.matcher(line)).matches()) {

                pila.push(new pila(linenumber, "for"));

                //for (i_=0; i_<a_; i_++)
                if ((matcher = FOR.matcher(line)).matches()) {
                    Matcher validar = Pattern.compile(
                            String.join("|",
                                    ID.pattern(),
                                    CteFloat.pattern(),
                                    CteInt.pattern(),
                                    CteString.pattern())
                    ).matcher(line);

                    while (validar.find()) {
                        String tipo = typesOper(validar.group());
                        if (!(tipo.equals("int"))) {
                            fails.add(new fail((1 + linenumber), errores.get(13)));
                        }
                    }
                }

            } else if ((matcher = ELSE.matcher(line)).matches()) {
                pila.push(new pila(linenumber, "else"));
            }

        }
        if (!pila.empty()) {
            for (pila elemento : pila) {
                if (!end.trim().equals("e" + elemento.getStart())) {
                    int num = elemento.getLine();
                    fails.add(new fail(num, errores.get(11)));
                }
            }

        }
    }

    // Método para verificar si los tipos de dos operandos coinciden
    public static void typesEval(String operando1, String operando2, int linenumber) {
        // Obtener tipos de los operandos desde el HashMap
        String tipoOperando1 = typesOper(operando1);
        String tipoOperando2 = typesOper(operando2);

        if (tipoOperando1 != null && tipoOperando2 != null) {
            if (!(tipoOperando1.equals(tipoOperando2))) {
                // Los tipos no coinciden
                fails.add(new fail((1 + linenumber), errores.get(1)));
            }
        }
    }

    // Método auxiliar para obtener el tipo de un operando desde el HashMap  y si es una variable, verificar que exista
    private static String typesOper(String operando) {

        if (ID.matcher(operando).matches()) {
            if (var.containsKey(operando)) {
                // Si es un ID y está en el HashMap, obtener su tipo
                return var.get(operando);
            }
        } else if (CteInt.matcher(operando).matches() || CteFloat.matcher(operando).matches() || CteString.matcher(operando).matches()) {
            // Si es una constante, devolver su tipo directamente
            return typeCons(operando);
        }
        return "";
    }

    // Método auxiliar para obtener el tipo de una constante
    private static String typeCons(String constante) {
        if (CteInt.matcher(constante).matches()) {
            return "int";
        } else if (CteFloat.matcher(constante).matches()) {
            return "float";
        } else if (CteString.matcher(constante).matches()) {
            return "string";
        } else {
            return "";
        }
    }

    public static void evalOperacionArit(String id, String operando1, String op, String operando2, int linenumber) {
        // Obtener tipos de los operandos desde el HashMap
        String tipoId = typesOper(id);
        String tipoOperando1 = typesOper(operando1);
        String tipoOperando2 = typesOper(operando2);

        if (op.equals("+")) {
            if (!(tipoId.equals(tipoOperando1) && tipoOperando1.equals(tipoOperando2))) {
                fails.add(new fail((1 + linenumber), errores.get(1)));
            }
        } else {
            // Verificar que los tipos sean compatibles y ninguno sea de tipo string
            if (tipoId != null) {
                if (!tipoId.equals("string") && !tipoOperando1.equals("string") && !tipoOperando2.equals("string")) {
                    if (!(tipoId.equals(tipoOperando1) && tipoOperando1.equals(tipoOperando2))) {
                        fails.add(new fail((1 + linenumber), errores.get(1)));
                    }
                } else {
                    // Uno de los operandos o el ID es de tipo string
                    fails.add(new fail((1 + linenumber), errores.get(9)));
                }
            }
        }

    }

    public static Matcher operandos(String sentencia) {
        Matcher validar = Pattern.compile(
                String.join("|",
                        ID.pattern(),
                        CteFloat.pattern(),
                        CteInt.pattern(),
                        CteString.pattern())
        ).matcher(sentencia);
        return validar;
    }

    public static void evalCond(String cond, int linenum) {

        HashMap<Integer, String> lista = new HashMap<>();
        int cont = 0, cont2 = 1;
        Matcher validar = operandos(cond);
        while (validar.find()) {
            cont++;
            lista.put(cont, typesOper(validar.group()));
        }
        while (cont2 < cont) {
            String tipo1 = lista.get(cont2); //obtenemos el tipo de la variable 1           
            String tipo2 = lista.get(cont2 + 1);//obtenemos el tipo de la variable 2  
            cont2 = cont2 + 2;
            if (!(tipo1.equals(tipo2))) {
                fails.add(new fail((1 + linenum), errores.get(1)));
            }
        }

    }

    // Método para separar cadenas y generar TOKEN
    public static void ensamblador(String code) {
        String[] lineas = code.split("\n");
        // Convertir el array a una lista
        List<String> lineaDeCodigo = Arrays.asList(lineas);
        int cont = 1;
        Matcher matcher;
        int salto = 0;
        int saltomien = 0;
        boolean flag_else = false;
        int contadorif = 0;
        String aux = null;
        int contfor = 0;
        List<String> leidas = new ArrayList<>();
        List<String> tokenFor = new ArrayList<>();
        List<String> sonActualizadas = new ArrayList<>();
        boolean esIncre = true;
        //Para saber cuantos if hay en el codigo
        for (String linea : lineaDeCodigo) {
            if (linea.trim().matches(IFf.pattern())) {
                contadorif++;
            }
        }

        for (HashMap.Entry<String, String> var : var.entrySet()) {
            String id = var.getKey().replace("_", "");
            data.add(id + " db 255, ?, 255 dup(\"$\")");
        }
        data.add("salto80 db 10, 13, '$'");

        for (String linea : lineaDeCodigo) {
            ArrayList<String> lista_id = new ArrayList<>();

            matcher = operandos(linea);

            // Ejemplo: traducción simple
            switch (tipoInstruccion(linea)) {
                case LEER:
                    matcher = operandos(linea);
                    while (matcher.find()) {
                        codigo.add("mov Ah, 0Ah");
                        codigo.add("mov Dx, offset " + matcher.group().replace("_", ""));
                        codigo.add("int 21h");
                        //impresion de salto
                        codigo.add("mov Ah, 09h");
                        codigo.add("mov Dx, offset salto80");
                        codigo.add("int 21h");
                        leidas.add(matcher.group().replace("_", ""));
                    }

                    break;
                case ESCRIBIR:

                    matcher = operandos(linea);

                    codigo.add("mov Ah, 09h");

                    while (matcher.find()) {

                        if (matcher.group().matches(CteString.pattern())) {
                            data.add("VAR" + cont + " db '" + matcher.group(1) + "$'");
                            codigo.add("mov Dx, offset VAR" + cont + "");
                            codigo.add("int 21h");
                            cont++;
                        } else if (matcher.group().matches(ID.pattern())) {
                            if (leidas.contains(matcher.group().replace("_", "")) && sonActualizadas.contains(matcher.group().replace("_", ""))) {
                                codigo.add("mov Dx, offset " + matcher.group().replace("_", ""));
                            } else {
                                if (sonActualizadas.contains(matcher.group().replace("_", ""))) {
                                    codigo.add("mov Dx, offset " + matcher.group().replace("_", ""));
                                } else {
                                    codigo.add("mov Dx, offset " + matcher.group().replace("_", "") + "+2");

                                }
                            }
                            codigo.add("int 21h");

                        } else {
                            data.add("var" + cont + " db '" + matcher.group() + "$'");
                            codigo.add("mov Dx, offset VAR" + cont + "");
                            codigo.add("int 21h");
                            cont++;
                        }
                    }
                    codigo.add("mov Ah, 09h");
                    codigo.add("mov Dx, offset salto80");
                    codigo.add("int 21h");
                    break;
                case ASIGNACION:
                    while (matcher.find()) {
                        if (matcher.group().matches(ID.pattern())) {
                            lista_id.add(matcher.group().replace("_", ""));
                        } else {
                            lista_id.add(matcher.group());
                        }
                    }

                    if (lista_id.get(1).matches(CteInt.pattern())) {
                        codigo.add("mov byte ptr [" + lista_id.get(0) + "], " + lista_id.get(1));
                        codigo.add("add " + lista_id.get(0) + ", 48");
                        sonActualizadas.add(lista_id.get(0));
                    } else {
                        codigo.add("mov al, [" + lista_id.get(1) + "+2]");
                        codigo.add("mov [" + lista_id.get(0) + "], al");
                    }

                    break;
                case INICIO:

                    codigo.add(".MODEL SMALL");
                    codigo.add(".CODE");
                    codigo.add("Inicio:");
                    codigo.add("mov Ax, @Data");
                    codigo.add("mov Ds, Ax ");
                    break;
                case FIN:

                    codigo.add("mov AX, 4C00h");
                    codigo.add("int 21h");
                    codigo.add(".DATA");
                    codigo.addAll(data);
                    codigo.add(".STACK");
                    codigo.add("END Inicio");
                    break;
                case OPERACION:

                    String op_arit = "";
                    Pattern pattern = Pattern.compile(OpArit.pattern());
                    Matcher matcher2 = pattern.matcher(linea);

                    while (matcher.find()) {
                        if (matcher.group().matches(ID.pattern())) {
                            lista_id.add(matcher.group().replace("_", ""));
                        } else {
                            lista_id.add(matcher.group());
                        }
                    }

                    codigo.add("xor Cx, Cx");

                    while (matcher2.find()) {
                        op_arit = matcher2.group(); // el ultimo ope                                  
                    }
                    switch (op_arit) {
                        case "+":
                            for (int i = 1; i <= 2; i++) {
                                if (lista_id.get(i).matches("([a-z]+)")) {
                                    if (leidas.contains(lista_id.get(i))) {
                                        codigo.add("mov Dx, offset " + lista_id.get(i) + "+2");
                                    } else {
                                        codigo.add("mov Dx, offset " + lista_id.get(i));
                                    }
                                } else {
                                    data.add("Var" + cont + " db " + "'" + lista_id.get(i) + "','$'");
                                    codigo.add("mov Dx, offset " + "VAR" + cont);
                                    cont++;
                                }
                                if (i == 1) {
                                    codigo.add("mov Si, Dx");
                                    codigo.add("mov Cl, byte ptr [Si] ");
                                    codigo.add("sub Cl, 48");
                                } else {
                                    codigo.add("mov Si, Dx");
                                    codigo.add("mov Ch, byte ptr [Si] ");
                                    codigo.add("sub Ch, 48");
                                }
                            }
                            //Hacemos operacion de suma
                            codigo.add("mov " + lista_id.get(0) + ", Cl");
                            codigo.add("add " + lista_id.get(0) + ", Ch");
                            codigo.add("add " + lista_id.get(0) + ", 48");
                            sonActualizadas.add(lista_id.get(0));
                            break;
                        case "-":
                            for (int i = 1; i <= 2; i++) {
                                if (lista_id.get(i).matches("([a-z]+)")) {
                                    if (leidas.contains(lista_id.get(i))) {
                                        codigo.add("mov Dx, offset " + lista_id.get(i) + "+2");
                                    } else {
                                        codigo.add("mov Dx, offset " + lista_id.get(i));
                                    }
                                } else {
                                    data.add("Var" + cont + " db " + "'" + lista_id.get(i) + "','$'");
                                    codigo.add("mov Dx, offset " + "VAR" + cont);
                                    cont++;
                                }
                                if (i == 1) {
                                    codigo.add("mov Si, Dx");
                                    codigo.add("mov Cl, byte ptr [Si] ");
                                    codigo.add("sub Cl, 48");
                                } else {
                                    codigo.add("mov Si, Dx");
                                    codigo.add("mov Ch, byte ptr [Si] ");
                                    codigo.add("sub Ch, 48");
                                }
                            }
                            codigo.add("xor " + lista_id.get(0) + ", 0");
                            codigo.add("mov " + lista_id.get(0) + ", Cl");
                            codigo.add("sub " + lista_id.get(0) + ", Ch");
                            codigo.add("add " + lista_id.get(0) + ", 48");
                            sonActualizadas.add(lista_id.get(0));
                            break;
                        case "*":
                            for (int i = 1; i <= 2; i++) {
                                if (lista_id.get(i).matches("([a-z]+)")) {
                                    if (leidas.contains(lista_id.get(i))) {
                                        codigo.add("mov Dx, offset " + lista_id.get(i) + "+2");
                                    } else {
                                        codigo.add("mov Dx, offset " + lista_id.get(i));
                                    }
                                } else {
                                    data.add("Var" + cont + " db " + "'" + lista_id.get(i) + "','$'");
                                    codigo.add("mov Dx, offset " + "VAR" + cont);
                                    cont++;
                                }
                                if (i == 1) {
                                    codigo.add("mov Si, Dx");
                                    codigo.add("mov Al, byte ptr [Si] ");
                                    codigo.add("sub Al, 48");
                                } else {
                                    codigo.add("mov Si, Dx");
                                    codigo.add("mov Ah, byte ptr [Si] ");
                                    codigo.add("sub Ah, 48");
                                }
                            }
                            codigo.add("mul Ah");
                            codigo.add("mov " + lista_id.get(0) + ", Al");
                            codigo.add("add " + lista_id.get(0) + ", 48");
                            sonActualizadas.add(lista_id.get(0));
                            break;
                        case "/":
                            for (int i = 1; i <= 2; i++) {
                                if (lista_id.get(i).matches("([a-z]+)")) {
                                    if (leidas.contains(lista_id.get(i))) {
                                        codigo.add("mov Dx, offset " + lista_id.get(i) + "+2");
                                    } else {
                                        codigo.add("mov Dx, offset " + lista_id.get(i));
                                    }
                                } else {
                                    data.add("Var" + cont + " db " + "'" + lista_id.get(i) + "','$'");
                                    codigo.add("mov Dx, offset " + "VAR" + cont);
                                    cont++;
                                }
                                if (i == 1) {
                                    codigo.add("xor Ax, Ax");
                                    codigo.add("mov Si, Dx");
                                    codigo.add("mov Al, byte ptr [Si] ");
                                    codigo.add("sub Al, 48");
                                } else {
                                    codigo.add("xor bx, bx");
                                    codigo.add("mov Si, Dx");
                                    codigo.add("mov bl, byte ptr [Si] ");
                                    codigo.add("sub bl, 48");
                                }
                            }
                            codigo.add("div bl");
                            codigo.add("mov " + lista_id.get(0) + ", Al");
                            codigo.add("add " + lista_id.get(0) + ", 48");
                            sonActualizadas.add(lista_id.get(0));
                            break;
                        case "%":
                            for (int i = 1; i <= 2; i++) {
                                if (lista_id.get(i).matches("([a-z]+)")) {
                                    if (leidas.contains(lista_id.get(i))) {
                                        codigo.add("mov Dx, offset " + lista_id.get(i) + "+2");
                                    } else {
                                        codigo.add("mov Dx, offset " + lista_id.get(i));
                                    }
                                } else {
                                    data.add("Var" + cont + " db " + "'" + lista_id.get(i) + "','$'");
                                    codigo.add("mov Dx, offset " + "VAR" + cont);
                                    cont++;
                                }
                                if (i == 1) {
                                    codigo.add("xor Ax, Ax");
                                    codigo.add("mov Si, Dx");
                                    codigo.add("mov Al, byte ptr [Si] ");
                                    codigo.add("sub Al, 48");
                                } else {
                                    codigo.add("xor bx, bx");
                                    codigo.add("mov Si, Dx");
                                    codigo.add("mov bl, byte ptr [Si] ");
                                    codigo.add("sub bl, 48");
                                }
                            }
                            codigo.add("div bl");
                            codigo.add("mov " + lista_id.get(0) + ", Ah");
                            codigo.add("add " + lista_id.get(0) + ", 48");
                            sonActualizadas.add(lista_id.get(0));
                            break;
                    }

                    break;

                case IF:
                    ArrayList<String> lista_tokens = new ArrayList<>();
                    Pattern pattern1 = Pattern.compile(OpRelacional.pattern());
                    Matcher op_cond = pattern1.matcher(linea);
                    String op_arit_cond = "";
                    int num_oper = 0;
                    while (matcher.find()) {
                        if (matcher.group().matches(ID.pattern())) {
                            lista_tokens.add(matcher.group().replace("_", "")); //es mi ID sin los palitos
                        } else {
                            lista_tokens.add(matcher.group());
                        }
                    }
                    salto++;
                    codigo.add("Salto" + salto + ":");
                    salto++;
                    while (num_oper <= 1) {
                        if (lista_tokens.get(num_oper).matches("([a-z]+)")) { //caso para ID
                            if (sonActualizadas.contains(lista_tokens.get(num_oper))) {
                                codigo.add("mov Dx, offset " + lista_tokens.get(num_oper));
                            } else {
                                codigo.add("mov Dx, offset " + lista_tokens.get(num_oper) + "+2");
                            }

                        } else {
                            data.add("VAR" + cont + " db " + "'" + lista_tokens.get(num_oper) + "','$'"); //caso para constantes
                            codigo.add("mov Dx, offset " + "VAR" + cont);
                            cont++;
                        }
                        if (num_oper == 0) {
                            codigo.add("mov Si, Dx");
                            codigo.add("mov Cl, byte ptr [Si] ");
                        } else {
                            codigo.add("mov Si, Dx");
                            codigo.add("mov Ch, byte ptr [Si] ");
                        }
                        num_oper++;
                    }

                    codigo.add("cmp Cl, Ch");

                    while (op_cond.find()) {
                        op_arit_cond = op_cond.group(); // el ultimo operador condicional                                                     
                    }

                    switch (op_arit_cond) {
                        case "<":
                            //codigo_ensamblador.add("jb salto" + salto);    
                            codigo.add("jnb salto" + salto);
                            break;
                        case ">":
                            //codigo_ensamblador.add("ja salto" + salto);
                            codigo.add("jna salto" + salto);
                            break;
                        case ">=":
                            //codigo_ensamblador.add("jae salto" + salto);
                            codigo.add("jnae salto" + salto);
                            break;
                        case "<=":
                            //codigo_ensamblador.add("jbe salto" + salto);
                            codigo.add("jnbe salto" + salto);
                            break;
                        case "==":
                            //codigo_ensamblador.add("jz salto" + salto);
                            codigo.add("jne salto" + salto);
                            break;
                        case "!=":
                            //codigo_ensamblador.add("jne salto" + salto);  
                            codigo.add("je salto" + salto);
                            break;
                    }

                case ENDS:
                    switch (linea.trim()) {
                        case "eif":
                            if (flag_else == false) {
                                codigo.add("jmp Salto" + (salto + 1));
                                codigo.add("salto" + salto + ":");
                            }
                            codigo.add("jmp Salto" + (salto + 1));
                            codigo.add("Salto" + (salto + 1) + ":");
                            salto++;
                            flag_else = false; //esta
                            break;
                        case "else":
                            codigo.add("jmp Salto" + (salto + 1));
                            codigo.add("salto" + salto + ":");
                            flag_else = true;
                            break;
                        case "efor":
                            if (esIncre) {
                                codigo.add("inc " + aux);
                                //incrementamos j 
                                codigo.add("pop Cx");
                                codigo.add("inc " + tokenFor.get(0));
                                codigo.add("loop SaltoFor" + contfor + "\n");
                            } else {
                                codigo.add("dec " + aux);
                                //incrementamos j 
                                codigo.add("pop Cx");
                                codigo.add("dec " + tokenFor.get(0));
                                codigo.add("loop SaltoFor" + contfor + "\n");
                            }

                            contfor++;
                            break;

                        case "ewhile":
                            codigo.add("jmp SaltoMien" + (saltomien - 1) + "\n");
                            codigo.add("SaltoMien" + saltomien + ":\n");
                            saltomien++;
                            break;

                    }

                    break;

                case DECLARACION:

                    break;
                case WHILE:
                    ArrayList<String> listawhile_tokens = new ArrayList<>();
                    Pattern pattern2 = Pattern.compile(OpRelacional.pattern());
                    Matcher op_condmien = pattern2.matcher(linea);
                    String op_arit_mien = "";
                    int num_opermien = 0;
                    while (matcher.find()) {
                        if (matcher.group().matches(ID.pattern())) {
                            listawhile_tokens.add(matcher.group().replace("_", "")); //es mi ID sin los palitos
                        } else {
                            listawhile_tokens.add(matcher.group());
                        }
                    }
                    codigo.add("SaltoMien" + saltomien + ":");
                    saltomien++;

                    while (num_opermien <= 1) {
                        if (listawhile_tokens.get(num_opermien).matches("([a-z]+)")) { //caso para ID
                            if (sonActualizadas.contains(listawhile_tokens.get(num_opermien))) {
                                codigo.add("mov Dx, offset " + listawhile_tokens.get(num_opermien));
                            } else {
                                codigo.add("mov Dx, offset " + listawhile_tokens.get(num_opermien) + "+2");
                            }
                        } else {
                            data.add("Var" + cont + " db " + "'" + listawhile_tokens.get(num_opermien) + "','$'"); //caso para constantes
                            codigo.add("mov Dx, offset " + "Var" + cont);
                            cont++;
                        }
                        if (num_opermien == 0) {
                            codigo.add("mov Si, Dx");
                            codigo.add("mov Cl, byte ptr [Si]\n ");
                        } else {
                            codigo.add("mov Si, Dx");
                            codigo.add("mov Ch, byte ptr [Si] \n");
                        }
                        num_opermien++;
                    }

                    codigo.add("cmp Cl,Ch\n");

                    while (op_condmien.find()) {
                        op_arit_mien = op_condmien.group();
                        // el ultimo operador condicional                                                     
                    }

                    switch (op_arit_mien) {
                        case "<":
                            //codigo_ensamblador.add("jb salto" + salto);    
                            codigo.add("jnb SaltoMien" + saltomien);
                            break;
                        case ">":
                            //codigo_ensamblador.add("ja salto" + salto);
                            codigo.add("jna SaltoMien" + saltomien);
                            break;
                        case ">=":
                            //codigo_ensamblador.add("jae salto" + salto);
                            codigo.add("jnae SaltoMien" + saltomien);
                            break;
                        case "<=":
                            //codigo_ensamblador.add("jbe salto" + salto);
                            codigo.add("jnbe SaltoMien" + saltomien);
                            break;
                        case "==":
                            //codigo_ensamblador.add("jz salto" + salto);
                            codigo.add("jne SaltoMien" + saltomien);
                            break;
                        case "!=":
                            //codigo_ensamblador.add("jne salto" + salto);  
                            codigo.add("je SaltoMien" + saltomien);
                            break;
                    }

                    break;

                case FOR:
                    //for ( i_ = 0; i_ ~ 5; i_++ )
                    Matcher mchi = Pattern.compile(String.join("|", decre.pattern(), incre.pattern())).matcher(linea);

                    while (matcher.find()) {
                        if (matcher.group().matches(ID.pattern())) {
                            tokenFor.add(matcher.group().replace("_", ""));
                        } else {
                            tokenFor.add(matcher.group());
                        }
                    }

                    while (mchi.find()) {
                        if (mchi.group().contains("++")) {

                            //cuando sea incremento 
                            int cont2 = 1;
                            while (cont2 < 4) {
                                if (tokenFor.get(cont2).matches("([a-z]+)")) { //caso para ID
                                    if (cont2 == 1) {
                                        aux = tokenFor.get(cont2);
                                        if (leidas.contains(tokenFor.get(cont2))) {
                                            codigo.add("mov Al, " + aux + "+2");
                                        } else {
                                            codigo.add("mov Al, " + aux);
                                        }
                                        codigo.add("sub Al, '0'");

                                    } else {
                                        codigo.add("xor Cx, Cx");
                                        if (leidas.contains(tokenFor.get(cont2))) {
                                            codigo.add("mov Si, offset " + tokenFor.get(cont2) + "+2");
                                        } else {
                                            codigo.add("mov Si, offset " + tokenFor.get(cont2));
                                        }

                                        codigo.add("mov Cl, byte ptr [Si]");
                                        codigo.add("sub Cl, '0'\n");

                                    }
                                } else {
                                    data.add("VAR" + cont + " db '" + tokenFor.get(cont2) + "', '$'");
                                    if (cont2 == 1) {
                                        aux = "VAR" + cont;
                                        codigo.add("mov Al, " + aux);
                                        codigo.add("sub Al, '0'");
                                        cont++;
                                    } else {
                                        codigo.add("xor Cx, Cx");
                                        codigo.add("mov Si, offset " + "VAR" + cont);
                                        codigo.add("mov Cl, byte ptr [Si]");
                                        codigo.add("sub Cl, '0'\n");
                                        cont++;
                                    }

                                }

                                cont2 = cont2 + 2;
                            }
                            codigo.add("sub Cl, Al");
                            codigo.add("add Cl, 1\n");
                            //asignamos valor a j para simular el funcionamiento de un for
                            if (tokenFor.get(1).matches(CteInt.pattern())) {
                                codigo.add("mov byte ptr [" + tokenFor.get(0) + "], " + tokenFor.get(1));
                                codigo.add("add " + tokenFor.get(0) + ", 48\n");
                                sonActualizadas.add(tokenFor.get(0));

                            } else {
                                if (sonActualizadas.contains(tokenFor.get(1))) {
                                    codigo.add("mov al, [" + tokenFor.get(1) + "]");
                                } else {
                                    codigo.add("mov al, [" + tokenFor.get(1) + "+2]");
                                }
                                codigo.add("mov [" + tokenFor.get(0) + "], al\n");
                                sonActualizadas.add(tokenFor.get(0));
                            }
                            //etiqueta de salto
                            codigo.add("SaltoFor" + contfor + ":\n");
                            codigo.add("push Cx");
                        } else {
                            //for (i_=5; i_~0; i_--)
                            //for (i_=0; i_~5; i_++)
                            //cuando sea decremento
                            esIncre = false;
                            int cont2 = 1;
                            while (cont2 < 4) {
                                if (tokenFor.get(cont2).matches("([a-z]+)")) { //caso para ID
                                    if (cont2 == 3) {
                                        aux = tokenFor.get(cont2);
                                        codigo.add("mov Al, " + aux);
                                        codigo.add("sub Al, '0'");

                                    } else {
                                        codigo.add("xor Cx, Cx");
                                        codigo.add("mov Si, offset " + tokenFor.get(cont2));
                                        codigo.add("mov Cl, byte ptr [Si]");
                                        codigo.add("sub Cl, '0'\n");

                                    }
                                } else {
                                    data.add("VAR" + cont + " db '" + tokenFor.get(cont2) + "', '$'");
                                    if (cont2 == 3) {
                                        aux = "VAR" + cont;
                                        codigo.add("mov Al, " + aux);
                                        codigo.add("sub Al, '0'");
                                        cont++;
                                    } else {
                                        codigo.add("xor Cx, Cx");
                                        codigo.add("mov Si, offset " + "VAR" + cont);
                                        codigo.add("mov Cl, byte ptr [Si]");
                                        codigo.add("sub Cl, '0'\n");
                                        cont++;
                                    }

                                }

                                cont2 = cont2 + 2;
                            }
                            codigo.add("sub Cl, Al");
                            codigo.add("add Cl, 1\n");
                            //asignamos valor a j para simular el funcionamiento de un for
                            if (tokenFor.get(1).matches(CteInt.pattern())) {
                                codigo.add("mov byte ptr [" + tokenFor.get(0) + "], " + tokenFor.get(1));
                                codigo.add("add " + tokenFor.get(0) + ", 48\n");
                                sonActualizadas.add(tokenFor.get(0));

                            } else {

                                if (sonActualizadas.contains(tokenFor.get(1))) {
                                    codigo.add("mov al, [" + tokenFor.get(1) + "]");
                                } else {
                                    codigo.add("mov al, [" + tokenFor.get(1) + "+2]");
                                }
                                codigo.add("mov [" + tokenFor.get(0) + "], al\n");
                                sonActualizadas.add(tokenFor.get(0));
                            }
                            //etiqueta de salto
                            codigo.add("SaltoFor" + contfor + ":\n");
                            codigo.add("push Cx");
                        }
                    }

                    break;

                default:
                    System.out.println(linea);
                    System.out.println("No se reconoce la instruccion");
            }
        }
    }

    public static TipoInstruccion tipoInstruccion(String linea) {
        Pattern etiqueta_fin = Pattern.compile("\\s*(else|eelse|eif|ewhile|efor)\\s*");
        if (leer.matcher(linea).matches()) {
            return TipoInstruccion.LEER;
        } else if (OperacionArit.matcher(linea).matches()) {
            return TipoInstruccion.OPERACION;
        } else if (decla.matcher(linea).matches()) {
            return TipoInstruccion.DECLARACION;
        } else if (escribir.matcher(linea).matches()) {
            return TipoInstruccion.ESCRIBIR;
        } else if (Asig.matcher(linea).matches()) {
            return TipoInstruccion.ASIGNACION;
        } else if (IF.matcher(linea).matches()) {
            return TipoInstruccion.IF;
        } else if (etiqueta_fin.matcher(linea).matches()) {
            return TipoInstruccion.ENDS;
        } else if (FOR.matcher(linea).matches()) {
            return TipoInstruccion.FOR;
        } else if (WHILE.matcher(linea).matches()) {
            return TipoInstruccion.WHILE;
        } else if (linea.equals("inicio")) {
            return TipoInstruccion.INICIO;
        } else if (linea.equals("fin")) {
            return TipoInstruccion.FIN;
        } else {
            return TipoInstruccion.NO_RECONOCIDA;

        }
    }

    public enum TipoInstruccion {
        LEER,
        ESCRIBIR,
        ASIGNACION,
        NO_RECONOCIDA,
        INICIO,
        FIN,
        DECLARACION,
        OPERACION,
        IF,
        ENDS,
        ELSE,
        FOR,
        WHILE
    }

    public static ArrayList<String> separarCad(String linea) {
        ArrayList<String> misTokens = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        boolean dentroComillas = false;

        // Iterar sobre cada caracter de la línea
        for (int i = 0; i < linea.length(); i++) {
            char caracter = linea.charAt(i);

            // Verificar si el caracter es una comilla simple o doble
            if (caracter == '\'' || caracter == '"') {
                // Cambiar el estado de dentroComillas
                dentroComillas = !dentroComillas;
                // Si el token no está vacío, agregarlo a la lista de tokens
                if (token.length() > 0) {
                    misTokens.add(token.toString());
                    token.setLength(0); // Limpiar el StringBuilder
                }
            } else if (Character.isWhitespace(caracter) && !dentroComillas) {
                // Si encontramos un espacio y no estamos dentro de comillas, agregar el token actual a la lista
                if (token.length() > 0) {
                    misTokens.add(token.toString());
                    token.setLength(0); // Limpiar el StringBuilder
                }
            } else if (caracter == ';' && !dentroComillas) {
                // Si encontramos un punto y coma y no estamos dentro de comillas, agregar el token actual a la lista
                if (token.length() > 0) {
                    misTokens.add(token.toString());
                    token.setLength(0); // Limpiar el StringBuilder
                }
                // Agregar el punto y coma como un token separado
                misTokens.add(";");
            } else if (Character.isLetterOrDigit(caracter) || dentroComillas) {
                // Agregar el caracter al token actual si es una letra, dígito o estamos dentro de comillas
                token.append(caracter);
            } else if (caracter == '_' && token.length() > 0 && Character.isLetter(token.charAt(token.length() - 1))) {
                // Ignorar el caracter '_' si está precedido por una letra
                continue;
            } else {
                // Si encontramos un carácter que no es letra ni dígito, agregar el token actual a la lista y luego agregar el carácter como un token separado
                if (token.length() > 0) {
                    misTokens.add(token.toString());
                    token.setLength(0); // Limpiar el StringBuilder
                }
                misTokens.add(String.valueOf(caracter));
            }
        }

        // Agregar el último token si no está vacío
        if (token.length() > 0) {
            misTokens.add(token.toString());
        }
        return misTokens; // Devolver el ArrayList de tokens
    }

    public static void main(String[] args) {

        Rellenar();

        try {
           Path currentDir = Paths.get("").toAbsolutePath();

            // Define las rutas relativas
            String inputFile = currentDir.resolve("src/analizador/Prueba.txt").toString();
            String file = currentDir.resolve("src/analizador/file.asm").toString();

            String code = readFile(inputFile);

            revisionLex(code);
            String noSingleLineComments = code.replaceAll("//.*", "");
            String noExtraSpaces = noSingleLineComments.replaceAll(" +", " ");
            String clean = noExtraSpaces.replaceAll("", "");
            String[] lineas = clean.split("\\r?\\n");
            for (String linea : lineas) {
                lineaLex.add(linea);
            }
            String cleanCode = cleaner(code);

            revisionSintac(cleanCode);

            revisionSeman(cleanCode);

            if (!fails.isEmpty()) {
                fails.forEach(elemento -> System.out.println("ERROR en la linea : " + elemento.getLine() + ". " + elemento.getMsg()));
            } else {
                ensamblador(cleanCode);

            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (String line : codigo) {
                    writer.write(line);
                    writer.newLine(); // Agrega un salto de línea después de cada línea escrita
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
