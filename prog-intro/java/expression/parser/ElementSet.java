package expression.parser;

import java.util.List;

public class ElementSet {
    private int pos;

    public List<Element> element;

    public ElementSet(List<Element> element) {
        this.element = element;
        pos = 0;
    }

    public Element next() {
        return element.get(pos++);
    }

    public void back() {
        pos--;
    }

    public int getSize() {
        return element.size();
    }

    public boolean hasNext() {
        return pos < element.size();
    }
}
