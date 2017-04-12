package nraghav.example.com.uvoteadmin;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by N RAGHAV on 08-04-17.
 */

public class survey {
    String description;
    String picture;
    HashMap<String,String>votes;

    String sid;


    public survey()
    {

    }

    public survey(String description, String picture, HashMap<String, String> votes, String sid) {
        this.description = description;
        this.picture = picture;
        this.votes = votes;
        this.sid = sid;
    }

    public HashMap<String, String> getVotes() {
        return votes;
    }

    public void setVotes(HashMap<String, String> votes) {
        this.votes = votes;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }


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
