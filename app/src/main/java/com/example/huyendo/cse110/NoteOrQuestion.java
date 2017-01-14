package com.example.sondo.cse110;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class NoteOrQuestion extends AppCompatActivity{
    private TextView welcomeText;
    private Intent oldIntent;
    private Intent newIntent;
    private Button NoteButton;
    private Button QuestionButton;
    private FloatingActionButton deleteButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_or_question);
        NoteButton = (Button) findViewById(R.id.buttonNote);
        QuestionButton = (Button) findViewById(R.id.buttonQuestion);
        deleteButton = (FloatingActionButton) findViewById(R.id.deleteButton);
        oldIntent = getIntent();
        welcomeText = (TextView) findViewById(R.id.StudyGroupLabel);
        welcomeText.setTextColor(Color.RED);
        welcomeText.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/kulminoituva.ttf"));
        welcomeText.setText(oldIntent.getStringExtra("groupname"));

        QuestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewActivity(NoteOrQuestion.this, QuestionAdd.class);
            }
        });

        NoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewActivity(NoteOrQuestion.this, NoteAdd.class);
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseQuery<ParseObject> studygroupDB = ParseQuery.getQuery("StudyGroup");
                studygroupDB.whereEqualTo("StudyGroup", oldIntent.getStringExtra("groupname"));
                studygroupDB.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        for (ParseObject studygroup : objects) {
                            if (!studygroup.getString("FromUser").equals(ParseUser.getCurrentUser().getString("username"))) {
                                Log.d("del", "cannot delete this group");
                                DialogBuilder diaglog = new DialogBuilder();
                                diaglog.dialogMessage = "You are not the author of this study group!";
                                diaglog.show(getFragmentManager(),"No");
                            } else {
                                deleteGroup();
                                startNewActivity(NoteOrQuestion.this, StudyGroup.class);
                            }
                        }
                    }
                });
            }
        });
    }
    @Override
    public void onBackPressed() {
        startNewActivity(NoteOrQuestion.this, StudyGroup.class);
        return;
    }
    private void startNewActivity(Context thisIntent, Class nextIntent){
        newIntent = new Intent(thisIntent,nextIntent);
        newIntent.putExtra("groupname", oldIntent.getStringExtra("groupname"));
        startActivity(newIntent);
    }

    private void deleteGroup(){
        ParseQuery<ParseObject> studygroupDB = ParseQuery.getQuery("StudyGroup");
        ParseQuery<ParseObject> questionDB = ParseQuery.getQuery("QuestionList");
        ParseQuery<ParseObject> answerDB = ParseQuery.getQuery("AnswerList");
        ParseQuery<ParseObject> noteDB = ParseQuery.getQuery("NoteList");

        //Delete all the note from this study group
        delete(noteDB,"FromStudyGroup",oldIntent.getStringExtra("groupname"));
        //Delete all the answer from this study group
        delete(answerDB,"FromStudyGroup",oldIntent.getStringExtra("groupname"));
        //Delete all question from this study group
        delete(questionDB,"FromStudyGroup",oldIntent.getStringExtra("groupname"));
        //Delete the study group created by this user

        studygroupDB.whereEqualTo("StudyGroup",oldIntent.getStringExtra("groupname"));
        delete(studygroupDB, "FromUser", ParseUser.getCurrentUser().getString("username"));

    }
    private void delete(ParseQuery<ParseObject> DB, String query, String value){
        DB.whereEqualTo(query, value);
        DB.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                for (ParseObject note : objects) {
                    note.deleteInBackground();
                }
            }
        });
    }
}
