package wspp;

import java.io.*;
import static java.lang.Integer.parseInt;
import static java.lang.Character.isDigit;
import static java.lang.Character.isWhitespace;
import java.util.Arrays;
import static java.util.Arrays.copyOf;
import static java.lang.Character.DASH_PUNCTUATION;
import static java.lang.Character.isLetter;

public class MyScanner {
    private static final int DEF_SIZE  = 4096;
    private static final int DEF_ARR_SIZE  = 100;
    final Reader reader;
    private int read, type, indLine = 0, indWord = 0;
    private int firstPos = -1;
    private char[] buffer = new char[DEF_SIZE];
    private String word = "", sep = System.lineSeparator();;

    public MyScanner() throws IOException{
        this.reader = new BufferedReader(new InputStreamReader(
            System.in
        )); 
        this.read = reader.read(buffer);
    }

    public MyScanner(String file, String code) throws IOException, FileNotFoundException{
        this.reader = new BufferedReader(new InputStreamReader(
            new FileInputStream(file), 
            code
        )); 
        this.read = reader.read(buffer);
    }

    ;
    public MyScanner(String string) throws IOException{
        this.reader = new BufferedReader(new InputStreamReader(
            new ByteArrayInputStream(string.getBytes())
        )); 
        this.read = reader.read(buffer);
    }

    private int analise(String word) {
        this.word = word;
        boolean flagInt = true, flagIntAbc = true;
        if(word.length() == 0) {
            return -1;
        }
        for(int i = 0; i < word.length(); i++) {
            if(i == 0 && (word.charAt(i) == '-' || word.charAt(i) == '+')) {
                continue;
            } else {
                if(!Character.isDigit(word.charAt(i))) {
                    flagInt = false;
                }
                if(Character.isDigit(word.charAt(i)) || (int) word.charAt(i) >= (int) 'a' + 10) {
                    flagInt = false;
                }
            }
            
        }

        if(flagInt) {
            return 1;
        } 
        if(flagIntAbc) {
            return 2;
        }
        return 0;
    }

    private boolean findNextWord() throws IOException{
        StringBuilder word = new StringBuilder();
        while (read >= 0) {
            for (int i = indWord; i <= read; i++) {
                if (i < read && !isWhitespace(buffer[i]) && firstPos == -1) {
                    firstPos = i;
                }
                if ((i >= read || isWhitespace(buffer[i])) && firstPos != -1) {
                    word.append(buffer, firstPos, i - firstPos);
                    firstPos = 0;
                    if (i < read) {
                        firstPos = -1;
                        indWord = i + 1;
                        type = analise(word.toString());
                        return true;
                    }
                }
            }
            indWord = 0;
            read = reader.read(buffer);
            if(read == -1 && word.length() > 0) {
                type = analise(word.toString());
                return true;
            }
        }
        return false;
    }


    private int updateInd(int i) throws IOException {
        if(i + 1 < read || read == -1) {
            i += 1;
        } else {
            read = reader.read(buffer);
            i = 0;
        }
        return i;
    }
                        

    public boolean hasNextLine() {
        if(read > -1) {
            return true;
        }
        return false;
    }

    public String nextLine() throws IOException {
        StringBuilder line = new StringBuilder();
        char lastChar;
        while (read >= 0) {
            for (int i = indLine; i < read; i++) {
                if (buffer[i] != sep.charAt(0)) {
                    line.append(buffer[i]);
                } else {
                    StringBuilder check = new StringBuilder();
                    indLine = i;
                    for(int j = 0; j < sep.length(); j++) {
                        if(indLine >= read) {
                            indLine = 0;
                            read = reader.read(buffer);
                        }
                        if(read != -1){
                            check.append(buffer[indLine]);
                            indLine++;
                        }
                        
                    }
                    if(sep.equals(check.toString())) {
                        return line.toString();
                    } 
                    line.append(check);
                }
            }
            indLine = 0; 
            read = reader.read(buffer);
            if(read == -1 && line.length() > 0) {
                return line.toString();
            }
        }
        return "";
    }

    public boolean hasNext() throws IOException {
        if(word.length() > 0) {
            return true;
        }
        if(!findNextWord()) {
            return false;
        }
        return true;
    }

    public boolean hasNextInt() throws IOException {
        if(word.length() > 0) {
            if(type == 0) {
                return true;
            }
            return false;
        }
        if(!findNextWord() || type != 1) {
            return false;
        }
        return true;
    }

    public boolean hasNextIntAbc() throws IOException {
        if(word.length() > 0) {
            if(type == 2) {
                return true;
            }
            return false;
        }
        if(!findNextWord() || type != 2) {
            return false;
        }
        return true;
    }

    private String clearWord() {
        String word = this.word;
        this.word = "";
        return word;
    }

    public String next() throws IOException {
        if(hasNext()) {           
            return clearWord();
        } 
        throw new RuntimeException();
    }

//    public int nextInt() throws IOException {
//        if(hasNextInt()) {
//            return clearWord();
//        }
//        throw new RuntimeException();
//    }

    public String nextIntAbc() throws IOException {
        if(hasNextIntAbc()) {
            return clearWord();
        } 
        throw new RuntimeException();
    }

    public void close() throws IOException {
        reader.close();
    }


    String[] intOfLine() throws IOException{
        String[] arr = new String[DEF_ARR_SIZE];
        int size = 0;
        char lastChar;
        StringBuilder word = new StringBuilder();
        while (read >= 0) {
            for (int i = indLine; i < read; i++) {
                if (!isWhitespace(buffer[i])) {
                    word.append(buffer[i]);
                }
                if (isWhitespace(buffer[i])) {
                    if (analise(word.toString()) > 0) {
                        if (arr.length <= size) {
                            arr = Arrays.copyOf(arr, arr.length * 3 / 2);
                        }
                        arr[size] = word.toString();
                        size++;
                    }
                    word.delete(0, word.length());
                }
                if(buffer[i] == '\r' || buffer[i] == '\n') {
                    lastChar = buffer[i];
                    i = updateInd(i);
                    if(lastChar == '\r' && i < read && buffer[i] == '\n') {
                        i = updateInd(i);
                    }
                    indLine = i;
                    arr = Arrays.copyOf(arr, size);
                    return arr;
                }
            }
            indLine = 0; 
            read = reader.read(buffer);
        }
        arr = Arrays.copyOf(arr, size);
        return arr;
    }

    public static boolean check(char a) {
        return (isLetter(a) || a == '\'' || Character.getType(a) == DASH_PUNCTUATION);
    }

    String[] wordOfLine() throws IOException{
        String[] arr = new String[DEF_ARR_SIZE];
        int size = 0;
        char lastChar;
        StringBuilder word = new StringBuilder();
        while (read >= 0) {
            for (int i = indLine; i < read; i++) {
                if (check(buffer[i])) {
                    word.append(buffer[i]);
                }
                if (!check(buffer[i]) && word.length() > 0) {
                    if (arr.length <= size) {
                        arr = Arrays.copyOf(arr, arr.length * 3 / 2);
                    }
                    arr[size] = word.toString();
                    size++;
                    word.delete(0, word.length());
                }
                if(buffer[i] == sep.charAt(0)) {
                    int supInd = 0;
                    while(sep.length() > supInd && buffer[i] == sep.charAt(supInd)) {
                        i = updateInd(i);
                        supInd++;
                    }
                    indLine = i;
                    if(supInd == sep.length()) {
                        arr = Arrays.copyOf(arr, size);
                        return arr;
                    }
                }
            }
            indLine = 0; 
            read = reader.read(buffer);
        }
        arr = Arrays.copyOf(arr, size);
        return arr;
    }

}