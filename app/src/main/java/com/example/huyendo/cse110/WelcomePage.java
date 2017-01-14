package com.example.sondo.cse110;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class WelcomePage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);
        //delay in ms
        int DELAY = 5000;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ParseUser CurrentUser = ParseUser.getCurrentUser();
                Intent myIntent;
                if(CurrentUser == null)
                    myIntent = new Intent(WelcomePage.this, LoginAndSignUp.class);
                else
                    myIntent = new Intent(WelcomePage.this, StudyGroup.class);
                startActivity(myIntent);
            }
        }, DELAY);
    }
}
