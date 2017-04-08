package nraghav.example.com.uvoteadmin;

/**
 * Created by N RAGHAV on 08-04-17.
 */

public class survey {

    String picture;

    public survey(String picture, String description) {
        this.picture = picture;
        this.description = description;
    }
    public survey()
    {

    }

    String description;

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
