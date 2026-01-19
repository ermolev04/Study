package wordStat;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.Character.DASH_PUNCTUATION;
import static java.lang.Character.isLetter;

public class MyScanner {
    public static final int DEF_SIZE  = 4096;
    
    public static boolean check(char a) {
        return (isLetter(a) || a == '\'' || Character.getType(a) == DASH_PUNCTUATION);
    }

    public static void putWord(Map<String, Integer> map, String key) {
        if(map.containsKey(key)){
            map.put(key, map.get(key) + 1);
        } else {
            map.put(key, 1);
        }
    }

    public static void main(String[] args){
        Map<String, Integer> map = new LinkedHashMap<String, Integer>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(args[0]), 
                "UTF8"
            ));
            try {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(args[1]), 
                    "UTF8"
                ));
                try {
                    char[] buffer = new char[DEF_SIZE];
                    int read = reader.read(buffer);
                    StringBuilder word = new StringBuilder();
                    int firstPos = -1;
                    while (read >= 0) {
                        for (int i = 0; i <= read; i++) {
                            if (i < read && check(buffer[i]) && firstPos == -1) {
                                firstPos = i;                            
                            }
                            if ((i >= read || !check(buffer[i])) && firstPos != -1) {
                                word.append(buffer, firstPos, i - firstPos);
                                firstPos = 0;
                                if (i < read) {
                                    putWord(map, word.toString().toLowerCase());                                 
                                    word.delete(0, word.length());
                                    firstPos = -1;
                                }
                                
                            }
                            
                        }
                        read = reader.read(buffer);
                        if(read == -1 && word.length() > 0) {
                            putWord(map, word.toString().toLowerCase());
                        }
                    }
                    for (Map.Entry<String, Integer> entry: map.entrySet()) {
                        writer.write(entry.getKey() + " " + entry.getValue());
                        writer.newLine();
                    }
                } finally {
                    writer.close();
                } 
            } finally {
                reader.close();
            }
            
        
        } catch (FileNotFoundException e) {
            System.out.println("Input file not found: " + e.getMessage());            
        } catch (IOException e) {
            System.out.println("Unknown error: " + e.getMessage());            
        }
        

    }  
}