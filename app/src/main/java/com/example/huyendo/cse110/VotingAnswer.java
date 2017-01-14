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
import com.github.clans.fab.FloatingActionMenu;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

public class VotingAnswer extends AppCompatActivity {
    private TextView title;
    private TextView numVoteText;
    private Intent oldIntent;
    private TextView answerText;
    private FloatingActionButton editbutton;
    private FloatingActionButton deleteButton;
    private FloatingActionButton votingButton;
    private ParseQuery<ParseObject> AnswerDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting_answer);
        oldIntent = getIntent();

        title = (TextView) findViewById(R.id.UserText);
        title.setTextColor(Color.RED);
        title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/STREH.ttf"));

        numVoteText = (TextView) findViewById(R.id.numVoteText);
        answerText = (TextView) findViewById(R.id.AnswerText);
        answerText.setText(oldIntent.getStringExtra("answer"));

        votingButton = (FloatingActionButton) findViewById(R.id.VotingButton);
        editbutton = (FloatingActionButton) findViewById(R.id.edit_question);
        deleteButton = (FloatingActionButton) findViewById(R.id.deleteButton);
        setVoted(); //set the author of the answer and check the box if the current user voted this answer

        votingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (votingButton.getColorNormal() == Color.RED) {
                    UncheckOtherAnswer();
                    Vote("increase");
                    votingButton.setColorNormal(Color.GREEN);
                    Log.d("User", "User just vote yes");
                } else {
                    Vote("decrease");
                    votingButton.setColorNormal(Color.RED);
                    Log.d("User", "User just uncheck yes");
                }
            }
        });

        editbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Answer("edit");
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Answer("delete");
            }
        });
    }

    @Override
    public void onBackPressed(){
        gotoActivity("VotingAnswer", AnswerAdd.class);
    }
    //Add a Vote to the database if current user voted or remove a vote if current user uncheck the vote box
    public void Vote(final String decision){
        AnswerDatabase = ParseQuery.getQuery("AnswerList");
        AnswerDatabase.whereEqualTo("Answer", oldIntent.getStringExtra("answer"));
        AnswerDatabase.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                for (final ParseObject answer : objects) {
                    //Retrive the relation of list of answer voted by current user
                    ParseRelation<ParseObject> AnsVotebyUser = ParseUser.getCurrentUser().getRelation("AnswerVoted");
                    //Add user to relation database if voted, else remove the user from the relation.
                    switch (decision) {
                        case "increase":
                            //Add the answer whom user voted to the relation
                            AnsVotebyUser.add(answer);
                            answer.increment("numVote");
                            break;
                        case "decrease":
                            //Remove the current user if they unvoted the answer
                            AnsVotebyUser.remove(answer);
                            answer.increment("numVote", -1);
                            break;
                    }
                    answer.saveInBackground();
                    ParseUser.getCurrentUser().saveInBackground();
                }
            }
        });
    }

    //Check if the answer is voted by this user
    public void setVoted(){
        AnswerDatabase = ParseQuery.getQuery("AnswerList");
        AnswerDatabase.whereEqualTo("Answer", answerText.getText().toString());
        //Retrieve the answer
        AnswerDatabase.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                for (final ParseObject answer : objects) {
                    title.setText("Answered by " + answer.getString("FromUser").toUpperCase());
                    numVoteText.setText(answer.getInt("numVote") + " people vote this answer");

                    //Retrieve the list of answer voted by current user
                    ParseQuery<ParseObject> AnsVotedByUser = ParseUser.getCurrentUser().getRelation("AnswerVoted").getQuery();
                    AnsVotedByUser.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> answerList, ParseException e) {
                            if (!answerList.contains(answer)) {
                                Log.d("User", "Current user did not vote this answer");
                                votingButton.setColorNormal(Color.RED);
                            } else {
                                Log.d("User", "Current user vote this answer");
                                votingButton.setColorNormal(Color.GREEN);
                            }
                        }
                    });
                }
            }
        });
    }

    //UnCheck if current user already voted other answer in the same question
    private void UncheckOtherAnswer(){
        //Retrieve the Relation of list of answer voted by current user
        final ParseRelation<ParseObject> AnsVotedByUser = ParseUser.getCurrentUser().getRelation("AnswerVoted");
        //Retrieve the query
        ParseQuery<ParseObject> AnsVotedByUserQuery = AnsVotedByUser.getQuery();
        AnsVotedByUserQuery.whereEqualTo("FromQuestion", oldIntent.getStringExtra("question"));
        AnsVotedByUserQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (!objects.isEmpty()) {
                    Log.d("Answer", "There is" + objects.size() + " answer in same question voted");
                    DialogBuilder dialog = new DialogBuilder();
                    dialog.dialogMessage = "By voting this answer, other answers voted in the same " +
                            "question by current user will be unvoted!";
                    dialog.show(getFragmentManager(),"dialog");
                    objects.get(0).increment("numVote", -1);
                    AnsVotedByUser.remove(objects.get(0));
                    objects.get(0).saveInBackground();
                    ParseUser.getCurrentUser().saveInBackground();
                }
            }
        });
    }

    //Delete Answer in database
    private void Answer(final String decision){
        AnswerDatabase = ParseQuery.getQuery("AnswerList");
        AnswerDatabase.whereEqualTo("Answer", answerText.getText().toString());
        AnswerDatabase.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (ParseUser.getCurrentUser().getString("username").equals(objects.get(0).getString("FromUser")))
                    switch (decision) {
                        case "edit":
                            gotoActivity("VotingAnswer", NoteEdit.class);
                            break;
                        case "delete":
                            objects.get(0).deleteInBackground();
                            gotoActivity("VotingAnswer", AnswerAdd.class);
                            break;
                    }
                else {
                    DialogBuilder dialog = new DialogBuilder();
                    dialog.dialogMessage = "Only the author of this answer can modify!";
                    dialog.show(getFragmentManager(),"dialog");
                }
            }
        });
    }

    private void gotoActivity(String thisActivity, Class nextActivity) {
        Intent intent = new Intent(VotingAnswer.this,nextActivity);
        intent.putExtra("answer",answerText.getText().toString());
        intent.putExtra("FromActivity",thisActivity);
        intent.putExtra("question", oldIntent.getStringExtra("question"));
        intent.putExtra("groupname", oldIntent.getStringExtra("groupname"));
        startActivity(intent);
    }
}

