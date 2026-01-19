package md2html;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Character.isWhitespace;
import static java.lang.System.lineSeparator;

public class  Md2Html {
    private static int pos;
    static StringBuilder paragraph;
    private static Map<String, Integer> conv = new HashMap<>(Map.of(
            "**", 1,
            "__", 2,
            "*", 3,
            "_", 4,
            "--", 5,
            "`", 6,
            "''", 7
            ));

    private static Map<String, String> justRewrite = new HashMap<>(Map.of(
            "<", "&lt;",
            ">", "&gt;",
            "&", "&amp;"
    ));

    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(args[0]), "utf-8"));
            try {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(args[1]), "utf-8"));
                try {
                    paragraph = new StringBuilder();
                    String line;
                    while(true) {
                        line = reader.readLine();
                        if(line != null && !line.isEmpty()) {
                            if(!paragraph.isEmpty()) {
                                paragraph.append(lineSeparator());
                            }
                            paragraph.append(line);
                        }
                        if((line == null || line.isEmpty()) && !paragraph.isEmpty()) {
                            pos = 0;
                            StringBuilder out = new StringBuilder();
                            createMarkup(0).toHTML(out);
                            writer.write(out.toString());
                            writer.write(lineSeparator());
                            paragraph = new StringBuilder();
                        }
                        if(line == null){
                            break;
                        }

                    }

                } finally {
                    writer.close();
                }
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            System.out.println("No such file found: " + e.getMessage());
        }
    }

    private static Markup createMarkup(int type) {
        List<Markup> list = new ArrayList<>();
        int subType = -1;
        int count = 0;
        int firstPos;
        int extraPos = 0;
        if (type == 0) {
            while(paragraph.charAt(pos) == '#') {
                pos++;
                count++;
            }
            if(count > 0 && isWhitespace(paragraph.charAt(pos))) {
                pos++;
            } else {
                count = 0;
                pos = 0;
            }
        }
        firstPos = pos;
        while (pos < paragraph.length()) {
            if (paragraph.charAt(pos) == '\\') {
                paragraph.delete(pos, pos + 1);
                pos += 1;
                continue;
            }
            for (String key : justRewrite.keySet()) {
                if (pos + key.length() <= paragraph.length() && paragraph.substring(pos, pos + key.length()).equals(key)) {
                    paragraph.delete(pos, pos + key.length());
                    paragraph.insert(pos, justRewrite.get(key));
                    pos -= key.length();
                    pos += justRewrite.get(key).length();
                    break;
                }
            }
            for (String key : conv.keySet()) {
                if(pos + key.length() <= paragraph.length() && paragraph.substring(pos, pos + key.length()).equals(key)) {
                    subType = conv.get(key);
                    extraPos = key.length();
                    break;
                }
            }
            if (subType != -1 && firstPos < pos) {
                list.add(new Text(paragraph.substring(firstPos, pos)));
            }
            pos += extraPos;
            extraPos = 0;
            if (subType > 0) {
                if (subType == type) {
                    switch (type) {
                        case 1:
                        case 2: return new Strong(list);
                        case 3:
                        case 4: return new Emphasis(list);
                        case 5: return new Strikeout(list);
                        case 6: return new Code(list);
                        case 7: return new Quote(list);
                    }
                } else {
                    list.add(createMarkup(subType));
                    firstPos = pos;
                    subType = -1;
                    continue;
                }
            }
            pos++;
        }
        if (firstPos < paragraph.length()) {
            list.add(new Text(paragraph.substring(firstPos, paragraph.length())));
        }
        if (type == 3) {
            return new FakeEmphasis(list, "*");
        }
        if (type == 4) {
            return new FakeEmphasis(list, "_");
        }


        return new Paragraph(list, count);
    }

}