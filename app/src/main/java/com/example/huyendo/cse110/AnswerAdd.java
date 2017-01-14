package com.example.sondo.cse110;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class AnswerAdd extends AppCompatActivity {
    private TextView question_post;
    private TextView title;
    private FloatingActionButton Add_answer;
    private FloatingActionButton deleteButton;
    private FloatingActionButton editButton;
    private ListView answers_list_view;
    private ListViewAdapter listAdapter;
    private ArrayList<listViewItem> answer_array;
    private Intent oldIntent;
    private ParseQuery<ParseObject> AnswerDatabase;
    private SwipeRefreshLayout swipeContainer;

    //Add Handler to auto update activity
    private Handler mHandler;
    //refresh the activity after 120s
    private final Runnable m_Runnable = new Runnable()
    {
        public void run()

        {
            //update the list
            displayList();
            AnswerAdd.this.mHandler.postDelayed(m_Runnable,120000);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_add);
        Add_answer = (FloatingActionButton) findViewById(R.id.Add_button);
        deleteButton = (FloatingActionButton) findViewById(R.id.DeleteButton);
        editButton = (FloatingActionButton) findViewById(R.id.edit_question);
        answers_list_view = (ListView) findViewById(R.id.Answer_list);
        answer_array = new ArrayList<listViewItem>();
        listAdapter = new ListViewAdapter(this, R.layout.row_layout, answer_array);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshAnswer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                displayList();
                swipeContainer.setRefreshing(false);
            }
        });
        //Initialize database connection
        oldIntent = getIntent(); //retrieve info from previous question

        mHandler = new Handler();

        //set title
        title = (TextView) findViewById(R.id.StudyGroupLabel);
        title.setTextColor(Color.RED);
        title.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/kulminoituva.ttf"));
        title.setText(oldIntent.getStringExtra("groupname"));

        //set the content of the question
        question_post = (TextView) findViewById(R.id.Question);
        question_post.setTextSize(20);
        question_post.setText(oldIntent.getStringExtra("question"));


        Add_answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity("AnswerAdd", NoteEdit.class, "new", "");
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Question("delete");
            }
        });
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Question("edit");
            }
        });
        answers_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               gotoActivity("AnswerAdd",VotingAnswer.class,"",listAdapter.getItem(position).ItemName);
            }
        });
        displayList();
    }
    @Override
    public void onBackPressed(){
        gotoActivity("AnswerAdd", QuestionAdd.class, "", "");
        return;
    }
    @Override
    protected  void onPause(){
        AnswerAdd.this.mHandler.removeCallbacks(m_Runnable);
        super.onPause();
    }

    //run thread again on resume
    @Override
    protected void onResume()
    {
        super.onResume();
        m_Runnable.run();
    }

    private void Question(final String decision){
        final ParseQuery<ParseObject> QuestionDB = ParseQuery.getQuery("QuestionList");
        QuestionDB.whereEqualTo("Question", question_post.getText().toString());
        QuestionDB.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                //If current user is the author of this note, then set editable to current user
                if (objects.get(0).getString("FromUser").equals(ParseUser.getCurrentUser().getString("username"))) {
                    switch (decision) {
                        case "edit":
                            gotoActivity("AnswerAdd", NoteEdit.class, "edit", "");
                            Log.d("edit", "Current user can edit this note");
                            break;
                        case "delete":
                            objects.get(0).deleteInBackground();
                            //Delete All the answer from this question
                            deleteAllAnswer();
                            gotoActivity("AnswerAdd", QuestionAdd.class, "", "");
                            break;
                    }

                } else {
                    DialogBuilder dialog = new DialogBuilder();
                    dialog.dialogMessage = "Only the author of this question can modify!";
                    dialog.show(getFragmentManager(),"dialog");
                }
            }
        });
    }
    private void deleteAllAnswer(){
        ParseQuery<ParseObject> AnswerDB = ParseQuery.getQuery("AnswerList");
        AnswerDB.whereEqualTo("FromQuestion", question_post.getText().toString());
        AnswerDB.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (!objects.isEmpty()) {
                    for (ParseObject answer : objects) {
                        answer.deleteInBackground();
                    }
                } else {
                    Log.d("del", "There is no answer to delete");
                }
            }
        });
    }
    private void displayList(){
        AnswerDatabase = ParseQuery.getQuery("AnswerList");
        AnswerDatabase.whereEqualTo("FromQuestion", oldIntent.getStringExtra("question"));
        AnswerDatabase.orderByAscending("numVote");
        AnswerDatabase.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> AnswerList, ParseException e) {
                if (e == null) {
                    //refresh the list
                    answer_array.clear();
                    for (ParseObject answer : AnswerList) {
                        listViewItem item = new listViewItem("lv_answer",
                                answer.getString("Answer"),
                                answer.getString("FromUser"),
                                answer.getCreatedAt().toString());
                        answer_array.add(0, item);
                    }
                    //update the list when database have change
                    listAdapter.notifyDataSetChanged();
                    answers_list_view.setAdapter(listAdapter);
                    Log.d("Success", "Answer List retrieved");
                } else {
                    Log.d("Fail", "Answer List retrieved fail");
                }
            }
        });
    }
    private void gotoActivity(String thisActivity, Class nextActivity,String edit, String listViewItem){
        Intent intent = new Intent(AnswerAdd.this, nextActivity);
        intent.putExtra("answer", listViewItem);
        intent.putExtra("question", oldIntent.getStringExtra("question"));
        intent.putExtra("groupname", oldIntent.getStringExtra("groupname"));
        intent.putExtra("editable", edit);
        intent.putExtra("FromActivity", thisActivity);
        startActivity(intent);
    }
}

