package util;

import android.app.Application;

public class JournalUser extends Application {
    private String username;
    private String userId;

    private static JournalUser instance;

    // FOllowing the Singleton Design Pattern

    public static synchronized JournalUser getInstance(){
        if (instance == null){
            instance = new JournalUser();
        }
        return instance;
    }

    public JournalUser(){
        // Empty Constructor
    }

    // Getter
    public String getUsername(){
        return username;
    }

    public String getUserId() {
        return userId;
    }

    // Setter
    public void setUsername(String username) {
        this.username = username;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

