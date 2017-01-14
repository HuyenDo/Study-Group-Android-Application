package com.example.sondo.cse110;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class NoteEdit extends AppCompatActivity {
    private EditText NoteTitleInput;
    private EditText NoteContentInput;
    private TextView NoteTitle;
    private TextView NoteContentTitle;
    private Button submitButton;
    private Intent oldIntent;
    private ParseQuery<ParseObject> Database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);
        oldIntent = getIntent();
        NoteTitleInput = (EditText) findViewById(R.id.TitleInput);
        NoteContentInput = (EditText) findViewById(R.id.NoteInput);
        submitButton = (Button) findViewById(R.id.SubmitButton);
        NoteTitle = (TextView) findViewById(R.id.NoteTitle);
        NoteContentTitle = (TextView) findViewById(R.id.NoteContextTitle);
        NoteTitle.setTextColor(Color.YELLOW);
        NoteTitle.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/SplendidB.ttf"));
        NoteContentTitle.setTextColor(Color.YELLOW);
        NoteContentTitle.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/SplendidN.ttf"));

        //Switch the front view based on previous intent activity
        switch (oldIntent.getStringExtra("FromActivity")) {
            case "NoteView":
                Log.d("activity", "Presvious activity is NoteView");
                NoteTitleInput.setText(oldIntent.getStringExtra("NoteTitle"));
                NoteContentInput.setText(oldIntent.getStringExtra("NoteContent"));
                break;
            case "VotingAnswer":
                NoteTitle.setVisibility(View.GONE);
                NoteTitleInput.setVisibility(View.GONE);
                NoteContentTitle.setText("Add New Content:");
                NoteContentInput.setText(oldIntent.getStringExtra("answer"));
                break;
            case "StudyGroup":
                NoteTitle.setVisibility(View.GONE);
                NoteTitleInput.setVisibility(View.GONE);
                NoteContentTitle.setText("Add New Study Group:");
                break;
            case "AnswerAdd":
                NoteTitle.setVisibility(View.GONE);
                NoteTitleInput.setVisibility(View.GONE);
                if(oldIntent.getStringExtra("editable").equals("edit")){
                    NoteContentTitle.setText("Edit Question:");
                    NoteContentInput.setText(oldIntent.getStringExtra("question"));
                }
                else{
                    NoteContentTitle.setText("Add New Answer:");
                }
                break;
            case "QuestionAdd":
                NoteTitle.setVisibility(View.GONE);
                NoteTitleInput.setVisibility(View.GONE);
                NoteContentTitle.setText("Add New Question:");
                NoteContentInput.setText(oldIntent.getStringExtra("question"));
                break;
        }

        //Set submit button based on the previous activity
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //user cannot left input blank
                if (NoteContentInput.getText().toString().isEmpty()){
                    DialogBuilder dialog = new DialogBuilder();
                    dialog.dialogMessage = "Cannot left the input blank!";
                    dialog.show(getFragmentManager(),"dialog");
                    return;
                }
                //if this is in editable mode, delete the old note from database
                switch(oldIntent.getStringExtra("FromActivity")){
                    case "NoteView":
                        deleteOldContent("NoteList");
                        createNewContent("NoteList");
                        gotoActivity("NoteEdit", NoteAdd.class);
                        break;
                    case "VotingAnswer":
                        deleteOldContent("AnswerList");
                        createNewContent("AnswerList");
                        gotoActivity("NoteEdit",AnswerAdd.class);
                        break;
                    case "StudyGroup":
                        createNewContent("StudyGroup");
                        Log.d("group", "Study Group");
                        gotoActivity("NoteEdit",StudyGroup.class);
                        break;
                    case "QuestionAdd":
                        createNewContent("QuestionList");
                        gotoActivity("NoteEdit", QuestionAdd.class);
                        break;
                    case "AnswerAdd":
                        if(oldIntent.getStringExtra("editable").equals("edit")){
                            deleteOldContent("QuestionList");
                            createNewContent("QuestionList");
                            gotoActivity("NoteEdit",QuestionAdd.class);
                        }
                        else{
                            createNewContent("AnswerList");
                            gotoActivity("NoteEdit",AnswerAdd.class);
                        }
                        break;
                    case "NoteAdd":
                        createNewContent("NoteList");
                        gotoActivity("NoteEdit",NoteAdd.class);
                        break;
                }
            }
        });
    }

    private void deleteOldContent(String database) {
        Database = ParseQuery.getQuery(database);
        switch (database){
            case "NoteList":
                Database.whereEqualTo("NoteTitle", oldIntent.getStringExtra("NoteTitle"));
                break;
            case "AnswerList":
                Database.whereEqualTo("Answer",oldIntent.getStringExtra("answer"));
                break;
            case "QuestionList":
                Database.whereEqualTo("Question",oldIntent.getStringExtra("question"));
                break;
            case "StudyGroup":
                Database.whereEqualTo("StudyGroup",oldIntent.getStringExtra("groupname"));
                break;
        }
        Database.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                objects.get(0).deleteInBackground();
            }
        });
    }

    private void createNewContent(String DatabaseObject){
        ParseObject note = new ParseObject(DatabaseObject);
        switch (DatabaseObject){
            case "StudyGroup":
                note.put("StudyGroup",NoteContentInput.getText().toString());
                break;
            case "QuestionList":
                note.put("Question",NoteContentInput.getText().toString());
                note.put("FromStudyGroup",oldIntent.getStringExtra("groupname"));
                break;
            case "NoteList":
                note.put("NoteTitle",NoteTitleInput.getText().toString());
                note.put("NoteContent",NoteContentInput.getText().toString());
                note.put("FromStudyGroup",oldIntent.getStringExtra("groupname"));
                break;
            case "AnswerList":
                note.put("Answer", NoteContentInput.getText().toString());
                note.put("numVote", 0);
                note.put("FromQuestion", oldIntent.getStringExtra("question"));
                note.put("FromStudyGroup",oldIntent.getStringExtra("groupname"));
                break;
        }
        note.put("FromUser", ParseUser.getCurrentUser().getString("username"));
        note.saveInBackground();
    }

    private void gotoActivity(String thisActivity, Class nextActivity){
        Intent newIntent = new Intent(NoteEdit.this,nextActivity);
        newIntent.putExtra("groupname",oldIntent.getStringExtra("groupname"));
        if (oldIntent.getStringExtra("FromActivity").equals("AnswerAdd") ||
                oldIntent.getStringExtra("FromActivity").equals("VotingAnswer"))
            newIntent.putExtra("question",oldIntent.getStringExtra("question"));
        newIntent.putExtra("FromActivity",thisActivity);
        startActivity(newIntent);
    }
}
