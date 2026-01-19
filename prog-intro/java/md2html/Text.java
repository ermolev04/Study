package md2html;

public class Text implements Markup {
    String inputString;
    protected Text (String inputString) {
        this.inputString = inputString;
    }

    public void toMarkdown(StringBuilder string) {
        string.append(inputString);
    }

    public void toBBCode(StringBuilder string) {
        string.append(inputString);
    }

    public void toHTML(StringBuilder string) {
        string.append(inputString);
    }
}
