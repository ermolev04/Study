package md2html;

import java.util.List;

public class Strikeout extends AbstractMarkup {

    public Strikeout (List<Markup> list) {
        super(list, "~", "[s]", "[/s]", "<s>", "</s>");
    }

}
