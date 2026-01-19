package md2html;

import java.util.List;

public class Quote extends AbstractMarkup {

    public Quote  (List<Markup> list) {
        super(list, "", "", "", "<q>", "</q>");
    }

}