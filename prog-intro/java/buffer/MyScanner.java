import java.io.*;
import static java.lang.Integer.parseInt;
import static java.lang.Character.isDigit;
import static java.lang.Character.isWhitespace;

public class MyScanner {
    public static final int DEF_SIZE  = 4096;
    final Reader reader;
    private int read, type, indLine = 0, indWord = 0;
    private int firstPos = -1;
    private char[] buffer = new char[DEF_SIZE];
    private String word = "", line = "";

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
        boolean flag = true;
        for(int i = 0; i < word.length(); i++) {
            if(i == 0 && (word.charAt(i) == '-' || word.charAt(i) == '+')) {
                continue;
            } else {
                if(!Character.isDigit(word.charAt(i))) {
                    System.err.println("|" + word.charAt(i) + "|");
                    flag = false;
                }
            }
            
        }

        if(flag) {
            return 1;
        } else {
            return 0;
        }
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
            i++;
        } else {
            read = reader.read(buffer);
            i = 0;
        }
        return i;
    }

    private int findNextLine() throws IOException {
        StringBuilder line = new StringBuilder();
        char lastChar;
        while (read >= 0) {
            for (int i = indLine; i < read; i++) {
                if (buffer[i] != '\n' && buffer[i] != '\r') {
                    line.append(buffer[i]);
                } else {
                    lastChar = buffer[i];
                    i = updateInd(i);
                    if(lastChar == '\r' && i < read && buffer[i] == '\n') {
                        i = updateInd(i);
                    }
                    indLine = i;
                    this.line = line.toString();
                    return line.length();
                }
            }
            indLine = 0; 
            read = reader.read(buffer);
            if(read == -1 && line.length() > 0) {
                this.line = line.toString();
                return line.length();
            }
        }
        return -1;
    }
                        

    public boolean hasNextLine() {
        if(read > -1) {
            return true;
        }
        return false;
    }

    public String nextLine() throws IOException{
        int size = findNextLine();
        return line;
    }

    public boolean hasNext() throws IOException{
        if(word.length() > 0) {
            return true;
        }
        if(!findNextWord()) {
            return false;
        }
        return true;
    }

    public boolean hasNextInt() throws IOException{
        if(word.length() > 0) {
            if(type > 0) {
                return true;
            }
            return false;
        }
        if(!findNextWord() || type < 1) {
            return false;
        }
        return true;
    }

    public String next() throws IOException{
        if(hasNext()) {
            String word = this.word;
            this.word = "";
            return word;
        } 
        throw new RuntimeException();
    }

    public int nextInt() throws IOException{
        // if(hasNextInt()) {
            String word = this.word;
            this.word = "";
            return parseInt(word);
        // } 
        // throw new RuntimeException();
    }

    public void close() throws IOException{
        reader.close();
    }

}