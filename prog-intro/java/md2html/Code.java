package md2html;

import java.util.List;

public class Code extends AbstractMarkup {

    public Code (List<Markup> list) {
        super(list, "", "", "", "<code>", "</code>");
    }

}