package dalbers.com.places.realm;


import io.realm.RealmObject;

/**
 * Created by davidalbers on 1/22/17.
 */

public class Event extends RealmObject {
    public Event(String name, java.util.Date date, boolean entered) {
        this.name = name;
        this.date = date;
        this.entered = entered;
    }

    public Event() {
        //required for realm
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;
    private java.util.Date date;

    public boolean isEntered() {
        return entered;
    }

    public void setEntered(boolean entered) {
        this.entered = entered;
    }

    public java.util.Date getDate() {
        return date;
    }

    public void setDate(java.util.Date date) {
        this.date = date;
    }

    private boolean entered;

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Event && ((Event) obj).getName().equals(getName());
    }
}
