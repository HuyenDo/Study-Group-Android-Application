package com.example.sondo.cse110;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class NoteView extends AppCompatActivity {
    private TextView noteTitle;
    private TextView noteContent;
    private Intent oldIntent;
    private Intent newIntent;
    private ParseQuery<ParseObject> NoteDatabase;
    private FloatingActionButton editButton;
    private FloatingActionButton deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_view);
        noteTitle = (TextView) findViewById(R.id.NoteTitle);
        noteContent = (TextView) findViewById(R.id.NoteContentText);
        editButton = (FloatingActionButton) findViewById(R.id.edit_question);
        deleteButton = (FloatingActionButton) findViewById(R.id.Deletebutton);
        oldIntent = getIntent();
        // Set note title
        noteTitle.setTextColor(Color.RED);
        noteTitle.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/StreetwiseBuddy.ttf"));
        noteTitle.setText(oldIntent.getStringExtra("NoteTitle"));

        // Set note content
        displayNoteContent();

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Note("edit");
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Note("delete");
            }
        });
    }
    @Override
    public void onBackPressed(){
        gotoActivity("NoteView", NoteAdd.class);
    }
    private void displayNoteContent(){
        NoteDatabase = ParseQuery.getQuery("NoteList");
        NoteDatabase.whereEqualTo("NoteTitle", oldIntent.getStringExtra("NoteTitle"));
        NoteDatabase.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject note : objects) {
                        noteContent.setText(note.getString("NoteContent"));
                    }
                    Log.d("NoteContent", "Note Content retrieved");
                } else
                    Log.d("NoteContent", "Note Content failed");
            }
        });
    }

    private void Note(final String decision){
        NoteDatabase = ParseQuery.getQuery("NoteList");
        NoteDatabase.whereEqualTo("NoteTitle", noteTitle.getText());
        NoteDatabase.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                //If current user is the author of this note, then set editable to current user
                if (objects.get(0).getString("FromUser").equals(ParseUser.getCurrentUser().getString("username"))) {
                    switch (decision) {
                        case "edit":
                            gotoActivity("NoteView", NoteEdit.class);
                            Log.d("edit", "Current user can edit this note");
                            break;
                        case "delete":
                            objects.get(0).deleteInBackground();
                            gotoActivity("NoteView", NoteAdd.class);
                            break;
                    }
                } else {
                    DialogBuilder dialog = new DialogBuilder();
                    dialog.dialogMessage = "Only the author of this note can modify!";
                    dialog.show(getFragmentManager(),"dialog");
                }
            }
        });
    }
       private void gotoActivity(String thisActivity,Class nextActivity){
        Intent myIntent = new Intent(NoteView.this,nextActivity);
        myIntent.putExtra("NoteTitle",noteTitle.getText());
        myIntent.putExtra("NoteContent",noteContent.getText());
        myIntent.putExtra("groupname", oldIntent.getStringExtra("groupname"));
        myIntent.putExtra("FromActivity", thisActivity);
        startActivity(myIntent);
    }
}
