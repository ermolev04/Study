package wordStat;

import java.io.*;
import static java.lang.Character.DASH_PUNCTUATION;
import static java.lang.Character.isLetter;
import java.util.Arrays;
import java.util.Map;
import java.util.LinkedHashMap;

public class WordStatCountPrefixL {
    public static final int DEF_SIZE  = 4096;
    
    public static boolean check(char a) {
        return (isLetter(a) || a == '\'' || Character.getType(a) == DASH_PUNCTUATION);
    }

    public static void putWord(Map<String, Integer> map, String key) {
        //map.put(key, map.getOrDefault(key, 0) + 1);
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
                                    if(word.length() > 2) {
                                        putWord(map, word.toString().toLowerCase().substring(0, 3));  
                                    }                               
                                    word.setLength(0);
                                    firstPos = -1;
                                }
                                
                            }
                            
                        }
                        read = reader.read(buffer);
                        if(read == -1 && word.length() > 2) {
                            putWord(map, word.toString().toLowerCase().substring(0, 3));
                        }
                    }
                    String[] arrKey = new String[map.size()];
                    int[] arrValue = new int[map.size()];
                    int ind = 0;
                    for (Map.Entry<String, Integer> entry: map.entrySet()) {
                        arrKey[ind] = entry.getKey();
                        arrValue[ind] = entry.getValue();
                        ind++;
                    }
                    for(int i = 0; i < arrKey.length; i++) {
                        for(int j = 1; j < arrKey.length - i; j++) {
                            if(arrValue[j - 1] > arrValue[j]) {
                                String supKey;
                                int supValue;
                                supKey = arrKey[j - 1];
                                arrKey[j - 1] = arrKey[j];
                                arrKey[j] = supKey;
                                supValue = arrValue[j - 1];
                                arrValue[j - 1] = arrValue[j];
                                arrValue[j] = supValue;
                            }
                        }
                    }

                    // interface Comparator {
                    //     int compare(int a, int b);
                    // }

                    // Comparator c =
                    //     (a, b) -> {
                    //         if (a > b) {
                    //             return 1;
                    //         }
                    //         ....
                    //     }

                    // TreeMap()

                    for(int i = 0; i < map.size(); i++) {
                        writer.write(arrKey[i] + " " + arrValue[i]);
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