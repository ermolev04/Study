package md2html;

public interface Markup {
    void toMarkdown(StringBuilder str);

    void toBBCode(StringBuilder str);

    void toHTML(StringBuilder str);

}
