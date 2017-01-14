package com.example.sondo.cse110;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class StudyGroup extends AppCompatActivity {
    private ListView group_list;
    private ListViewAdapter mListAdapter;
    private ArrayList<listViewItem> m_group_name;
    private FloatingActionButton newGroup;
    private ParseQuery<ParseObject> StudyGroupDatabase;
    private TextView userText;
    private ImageView log_out_button;
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
            StudyGroup.this.mHandler.postDelayed(m_Runnable,120000);
        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_group);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                displayList();
                swipeContainer.setRefreshing(false);
                Log.d("refresh", "list just updated");
            }
        });

        newGroup = (FloatingActionButton) findViewById(R.id.new_study_group);
        group_list = (ListView) findViewById(R.id.StudyGroup);
        userText = (TextView) findViewById(R.id.UserText);
        userText.setText("Welcome, " + ParseUser.getCurrentUser().getString("username"));
        userText.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/yahoo.ttf"));
        userText.setTextColor(Color.RED);
        log_out_button = (ImageView) findViewById(R.id.log_out_button);
        log_out_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.getCurrentUser().logOut();
                DialogBuilder dialog = new DialogBuilder();
                dialog.dialogMessage = "You are being logged out";
                dialog.show(getFragmentManager(),"dialog");
                startActivity(new Intent(StudyGroup.this, LoginAndSignUp.class));
            }
        });
        m_group_name = new ArrayList<listViewItem>();
        mListAdapter = new ListViewAdapter(StudyGroup.this,R.layout.row_layout,m_group_name);

        //initialize Handler
        mHandler = new Handler();

        newGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity("StudyGroup",NoteEdit.class,"");
            }
        });

        group_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                gotoActivity("StudyGroup",NoteOrQuestion.class,mListAdapter.getItem(position).ItemName);
            }
        });
        //display list
        displayList();
    }
    @Override
    public void onBackPressed() {
        gotoActivity("StudyGroup",WelcomePage.class,"");
    }
    //Stop the thread when paused
    @Override
    protected  void onPause(){
        StudyGroup.this.mHandler.removeCallbacks(m_Runnable);
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
        StudyGroupDatabase = ParseQuery.getQuery("StudyGroup");
        StudyGroupDatabase.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> StudyGroupList, ParseException e) {
                if (e == null) {
                    //refresh the list
                    m_group_name.clear();
                    for (ParseObject studygroup : StudyGroupList) {
                        listViewItem newItem = new listViewItem("lv_study_logo",
                                studygroup.getString("StudyGroup"),
                                studygroup.getString("FromUser"),
                                studygroup.getCreatedAt().toString());
                        m_group_name.add(0,newItem);
                    }
                    group_list.setAdapter(mListAdapter);
                    Log.d("Success", "Study Group List retrieved");
                } else {
                    Log.d("Fail", "StudyGRoup List fail");
                }
            }
        });
    }

    private void gotoActivity(String thisActivity, Class nextActivity,String listViewItem){
        Intent myIntent = new Intent(StudyGroup.this, nextActivity);
        myIntent.putExtra("groupname", listViewItem);
        myIntent.putExtra("FromUser",ParseUser.getCurrentUser().getString("username"));
        myIntent.putExtra("FromActivity",thisActivity);
        startActivity(myIntent);
    }
}
