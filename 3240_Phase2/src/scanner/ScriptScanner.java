package scanner;

import java.io.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import scanner.automata.DFA;
import scanner.automata.NFA;
import scanner.conversion.NFAtoDFA;
import scanner.exceptions.SyntaxErrorException;
import scanner.generateNFA.FinalNFA;

public class ScriptScanner {
    public static List<String> identifiers;
    public static List<String> regexes;
    public static List<String> strconsts;
    public static Deque<String> tokens;

    /**
     * Creates the DFA and ScannerDriver using the files containing the regexes
     * and the tokens.
     * 
     * If no arguments are supplied, the user is prompted for inputs
     * 
     * @param args 1st argument is used as the name of the file containing all 
     * of the tokens
     * @throws SyntaxErrorException 
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public static void scan() throws FileNotFoundException, IOException, SyntaxErrorException {
        identifiers = new ArrayList<String>();
        regexes = new ArrayList<String>();
        strconsts = new ArrayList<String>();
        tokens = new ArrayDeque<String>();

        //System.out.println("Using Lexical Specification for MinRE: " + ScriptScanner.class.getResource("MiniRE_LexSpec.txt").toString().substring(6));
        //DFA dfa = generateDFA(ScriptScanner.class.getResource("MiniRE_LexSpec.txt").toString().substring(6));

        DFA dfa = generateDFA("MiniRE_LexSpec.txt");

        File inputFile;

        JOptionPane.showMessageDialog(null, "Please choose an input script.");
        JFileChooser fc = new JFileChooser();
        fc.showOpenDialog(null);
        inputFile = fc.getSelectedFile();

        try {
            ScannerDriver driver = new ScannerDriver(inputFile, dfa);
            driver.run();
        } catch (FileNotFoundException ex) {
            System.out.println("Error: Cannot find file");
            System.out.println(ex.getMessage());
        }
        
        checkIDs();
    }

    /**
     * Creates the DFA and ScannerDriver using the files containing the regexes
     * and the tokens.
     * 
     * If no arguments are supplied, the user is prompted for inputs
     * 
     * @param args 1st argument is used as the name of the file containing all 
     * of the tokens
     * @throws SyntaxErrorException 
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public static void scan(String input, File directory) throws FileNotFoundException, IOException, SyntaxErrorException {
        identifiers = new ArrayList<String>();
        regexes = new ArrayList<String>();
        strconsts = new ArrayList<String>();
        tokens = new ArrayDeque<String>();

        //System.out.println("Using Lexical Specification for MinRE: " + ScriptScanner.class.getResource("MiniRE_LexSpec.txt").toString().substring(6));
        //DFA dfa = generateDFA(ScriptScanner.class.getResource("MiniRE_LexSpec.txt").toString().substring(6));

        DFA dfa = generateDFA("MiniRE_LexSpec.txt");

        File temp_file = new File("temp.tmp");
        FileWriter f = null;
        BufferedWriter b = null;
        PrintWriter p = null;

        try {
            f = new FileWriter(temp_file, false);
            b = new BufferedWriter(f);
            p = new PrintWriter(b);

            p.print(input);
        } catch (IOException e) {
            System.out.println("Error creating temp file");
        } finally {
            if (p != null) p.close();
        }


        try {
            ScannerDriver driver = new ScannerDriver(temp_file, dfa);
            driver.run();
        } catch (FileNotFoundException ex) {
            System.out.println("Error: Cannot find file");
            System.out.println(ex.getMessage());
        }
        
        checkIDs();
    }

    private static DFA generateDFA(String fileName) throws FileNotFoundException, IOException, SyntaxErrorException {
        FinalNFA NFAgen = new FinalNFA();
        NFA nfa = NFAgen.generate(fileName);
        return NFAtoDFA.dfaFromNFA(nfa);
    }
    
    public static void checkIDs() throws SyntaxErrorException {
        for (String a : ScriptScanner.identifiers) {
            if (a.length() > 10) {
                String err = "Identifier '" + a + "' too long; must be 10 chars at most!";
                System.out.println(err);
                throw new SyntaxErrorException(err);
            }
        }
    }
}
