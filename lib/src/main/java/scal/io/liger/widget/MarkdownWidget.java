package scal.io.liger.widget;

/**
 * Created by mnbogner on 7/11/14.
 */
public class MarkdownWidget extends Widget {
    public String text;

    public MarkdownWidget() {
        this.type = this.getClass().getName();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
