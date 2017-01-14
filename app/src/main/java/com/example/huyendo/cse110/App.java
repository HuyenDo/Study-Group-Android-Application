package com.example.sondo.cse110;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by Son Do on 11/11/2015.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "EPHyrhvqbHUcTxJg6BDJac3i9kzfIv7UQ7GPVDR5", "W2bmtt194PWiCFK3LJPoaaRgeJdmMKY9loBNmHn9");
    }
}
