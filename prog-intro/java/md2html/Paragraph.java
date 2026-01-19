package md2html;

import java.util.List;

public class Paragraph extends AbstractMarkup {
    public Paragraph(List<Markup> list, int count) {
        super(list, "", "", "", "", "");
        String s;
        if(count == 0) {
            this.html = "<p>";
            this.htmlEnd = "</p>";
        } else {
            this.html = "<h" + count + ">";
            this.htmlEnd = "</h" + count + ">";
        }

    }

}
