package com.example.sondo.cse110;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class LoginAndSignUp extends AppCompatActivity {
    private Button login;
    private Button signup;
    private EditText username;
    private EditText password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_and_sign_up);

        login = (Button) findViewById(R.id.LogIn);
        signup = (Button)findViewById(R.id.SignUp);

        username = (EditText) findViewById(R.id.Username);
        password = (EditText) findViewById(R.id.Password);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logInInBackground(username.getText().toString(), password.getText().toString(),
                        new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if (user != null) {
                                    final ProgressDialog dlg = new ProgressDialog(LoginAndSignUp.this);
                                    dlg.setTitle("Please wait.");
                                    dlg.setMessage("Logging in.  Please wait.");
                                    dlg.show();
                                    startActivity(new Intent(LoginAndSignUp.this, StudyGroup.class));
                                } else {
                                    if(InputisBlank())
                                        showDialog("Cannot left username or password blank!");
                                    else
                                        showDialog("This account does not exist. Please Sign Up!");
                                }
                            }
                        });
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    ParseUser user = new ParseUser();
                    user.setUsername(username.getText().toString());
                    user.setPassword(password.getText().toString());
                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                final ProgressDialog dlg = new ProgressDialog(LoginAndSignUp.this);
                                dlg.setTitle("Please wait.");
                                dlg.setMessage("Signing up.  Please wait.");
                                dlg.show();
                                startActivity(new Intent(LoginAndSignUp.this, StudyGroup.class));
                            } else{
                                if(InputisBlank())
                                    showDialog("Cannot left username or password blank!");
                                else
                                    showDialog("This account already exists. Please Log In");
                            }
                        }
                    });
            }
        });
    }

    @Override
    public void onBackPressed(){
        startActivity(new Intent(this,WelcomePage.class));
    }

    private boolean InputisBlank(){
        return username.getText().toString().isEmpty() || password.getText().toString().isEmpty();
    }
    private void showDialog(String message){
        DialogBuilder dialog = new DialogBuilder();
        dialog.dialogMessage = message;
        dialog.show(getFragmentManager(),"dialog");
    }
}