package scal.io.liger;

/**
 * Created by mnbogner on 7/10/14.
 */
public class CardModel {
    public String type;
    public String id;
    public String title;

    public CardModel(){

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
