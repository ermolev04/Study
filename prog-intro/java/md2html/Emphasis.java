package md2html;

import java.util.List;

public class Emphasis extends AbstractMarkup {

    public Emphasis (List<Markup> list) {
        super(list, "*", "[i]", "[/i]", "<em>", "</em>");
    }

}
