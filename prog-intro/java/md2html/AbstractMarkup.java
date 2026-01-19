package md2html;

import java.util.List;

public abstract class AbstractMarkup implements Markup {
    protected List<Markup> list;
    String bbc, bbcEnd, mark, html, htmlEnd;
    public AbstractMarkup (List<Markup> list, String mark, String bbc, String bbcEnd, String html, String htmlEnd) {
        this.list = list;
        this.mark = mark;
        this.bbc = bbc;
        this.bbcEnd = bbcEnd;
        this.html = html;
        this.htmlEnd = htmlEnd;
    }

    public void toMarkdown(StringBuilder string) {
        string.append(mark);
        for (Markup El: list) {
            El.toMarkdown(string);
        }
        string.append(mark);
    }

    public void toBBCode(StringBuilder string) {
        string.append(bbc);
        for (Markup El: list) {
            El.toBBCode(string);
        }
        string.append(bbcEnd);

    }

    public void toHTML(StringBuilder string) {
        string.append(html);
        for (Markup El: list) {
            El.toHTML(string);
        }
        string.append(htmlEnd);

    }
}
