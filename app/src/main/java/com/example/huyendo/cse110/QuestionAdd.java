package com.example.sondo.cse110;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
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

import java.util.ArrayList;
import java.util.List;

public class QuestionAdd extends AppCompatActivity {
    private FloatingActionButton Add_button;
    private ListView questions_list_view;
    private ArrayList<listViewItem> questions;
    private ListViewAdapter listAdapter;
    private TextView title;
    private SwipeRefreshLayout swipeContainer;
    private ParseQuery QuestionDatabase;
    private Intent oldIntent; //retrive the Intent from previous activity
    //Add Handler to auto update activity
    private Handler mHandler;
    //refresh the activity after 120s
    private final Runnable m_Runnable = new Runnable()
    {
        public void run()

        {
            //update the list
            displayList();
            QuestionAdd.this.mHandler.postDelayed(m_Runnable,120000);
        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_add);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayoutQuestion);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                displayList();
                swipeContainer.setRefreshing(false);
            }
        });
        oldIntent = getIntent(); //Retrieve intent from previous study group
        //Constraint the query to show only the question belong to a chosen study group
        mHandler = new Handler();

        Add_button = (FloatingActionButton) findViewById(R.id.AddButton);

        questions_list_view = (ListView) findViewById(R.id.QuestionList);
        title = (TextView) findViewById(R.id.StudyGroupLabel);

        questions= new ArrayList<listViewItem>();
        listAdapter = new ListViewAdapter(QuestionAdd.this,R.layout.row_layout,questions);

        //Set group Name on title
        Typeface font = Typeface.createFromAsset(getAssets(),"fonts/kulminoituva.ttf");
        title.setTypeface(font);
        title.setTextColor(Color.RED);
        title.setText(oldIntent.getStringExtra("groupname").toUpperCase());

        Add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity("QuestionAdd", NoteEdit.class, "");
            }
        });

        questions_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                gotoActivity("QuestionAdd",AnswerAdd.class,listAdapter.getItem(position).ItemName);
            }
        });
        //Display question list
        displayList();
    }
    @Override
    public void onBackPressed(){
        gotoActivity("QuestionAdd",NoteOrQuestion.class,"");
    }

    @Override
    protected  void onPause(){
        QuestionAdd.this.mHandler.removeCallbacks(m_Runnable);
        super.onPause();
    }

    //run thread again on resume
    @Override
    protected void onResume()
    {
        m_Runnable.run();
        super.onResume();
    }

    public void displayList(){
        QuestionDatabase = ParseQuery.getQuery("QuestionList");
        QuestionDatabase.whereEqualTo("FromStudyGroup", oldIntent.getStringExtra("groupname"));
        QuestionDatabase.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> StudyGroupList, ParseException e) {
                if (e == null) {
                    //refresh the list
                    questions.clear();
                    for (ParseObject studygroup : StudyGroupList) {
                        listViewItem item = new listViewItem("lv_question",
                                studygroup.getString("Question"),
                                studygroup.getString("FromUser"),
                                studygroup.getCreatedAt().toString());
                        questions.add(0, item);
                    }
                    //update the list when database have change
                    listAdapter.notifyDataSetChanged();
                    questions_list_view.setAdapter(listAdapter);
                    Log.d("Success", "Question List retrieved");
                } else {
                    Log.d("Fail", "Question List fail");
                }
            }
        });
    }

    private void gotoActivity(String thisActivity, Class nextActivity, String ListViewItem){
        Intent myIntent = new Intent(QuestionAdd.this, nextActivity);
        myIntent.putExtra("question", ListViewItem);
        myIntent.putExtra("groupname", oldIntent.getStringExtra("groupname"));
        myIntent.putExtra("FromActivity", thisActivity);
        startActivity(myIntent);

    }
}
