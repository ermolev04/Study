package markup;

import java.util.List;

public abstract class AbstractMarkup implements Markup {
    protected List<Markup> list;
    String bbc, bbcEnd, mark;
    public AbstractMarkup (List<Markup> list, String mark, String bbc, String bbcEnd) {
        this.list = list;
        this.mark = mark;
        this.bbc = bbc;
        this.bbcEnd = bbcEnd;
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
}
