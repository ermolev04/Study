package wspp;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class WsppPosition {
    public static void main(String[] args) {
        String key, word;
        int position = 1, line = 1;
        Map<String, IntList> map = new LinkedHashMap<String, IntList>();
        try {
            MyScanner scan = new MyScanner(args[0], "UTF8");
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(args[1]), 
                "UTF8"
            ));
            try {
                while (scan.hasNextLine()) {
                    String[] words = scan.wordOfLine();
                    for(int i = 0; i < words.length; i++) {
                        word = words[i].toLowerCase();
                        if (!map.containsKey(word)) {
                            map.put(word, new IntList());
                        }
                        map.get(word).add(words.length - position + 1, line);
                        position++;
                    }
                line++;
                position = 1;   
                }
                scan.close();
                    IntList vrm;
                    for (Map.Entry<String, IntList> entry: map.entrySet()) {
                        writer.write(entry.getKey());
                        vrm = entry.getValue();
                        writer.write(" " + vrm.getSize());
                        for(int i = 0; i < vrm.getSize(); i++) {
                                writer.write(" " + vrm.getSup(i) + ":" + vrm.get(i));     
                        }
                        writer.newLine();
                    }
            } finally {
                writer.close();
            }      
        } catch (IOException e) {
            System.out.println("Input file not found: " + e.getMessage()); 
        } 
    }
}
