package com.example.sondo.cse110;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class NoteAdd extends AppCompatActivity {
    private FloatingActionButton NoteButton;
    private TextView title;
    private ListView note_list_view;
    private ArrayList<listViewItem> note_array;
    private ListViewAdapter listAdapter;
    private Intent oldIntent;
    private Intent newIntent;
    private SwipeRefreshLayout swipeContainer;

    //Add Handler to auto update activity every 120s
    private Handler mHandler;
    private final Runnable m_Runnable = new Runnable()
    {
        public void run()
        {
            //update the list
            displayList();
            mHandler.postDelayed(m_Runnable, 120000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_add);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshNote);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                displayList();
                swipeContainer.setRefreshing(false);
            }
        });
        mHandler = new Handler();
        oldIntent = getIntent();
        NoteButton = (FloatingActionButton) findViewById(R.id.AddNoteButton);
        note_list_view = (ListView) findViewById(R.id.NoteList);
        note_array = new ArrayList<listViewItem>();
        listAdapter = new ListViewAdapter(NoteAdd.this,R.layout.row_layout,note_array);
        title = (TextView) findViewById(R.id.StudyGroupLabel);

        title.setTextColor(Color.RED);
        title.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/kulminoituva.ttf"));
        title.setText(oldIntent.getStringExtra("groupname"));
        NoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity("NoteAdd", NoteEdit.class, "");
            }
        });
        note_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                gotoActivity("NoteAdd", NoteView.class, listAdapter.getItem(position).ItemName);
            }
        });

        //Display note list
        displayList();
    }

    @Override
    public void onBackPressed() {
        gotoActivity("StudyGroup", NoteOrQuestion.class, "");
    }

    //Stop the thread when paused
    @Override
    protected  void onPause(){
        mHandler.removeCallbacks(m_Runnable);
        super.onPause();
    }

    //run thread again on resume
    @Override
    protected void onResume()
    {
        super.onResume();
        m_Runnable.run();
    }

    private void displayList(){
        //Query Note from specified studyGroup
        ParseQuery<ParseObject> NoteDatabase = ParseQuery.getQuery("NoteList");
        NoteDatabase.whereEqualTo("FromStudyGroup", oldIntent.getStringExtra("groupname"));
        NoteDatabase.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> noteList, ParseException e) {
                if (e == null) {
                    note_array.clear();
                    for (ParseObject note : noteList) {
                        listViewItem newItem = new listViewItem("lv_note",
                                note.getString("NoteTitle"),
                                note.getString("FromUser"),
                                note.getCreatedAt().toString());
                        note_array.add(0,newItem);
                    }
                    listAdapter.notifyDataSetChanged();
                    note_list_view.setAdapter(listAdapter);
                    Log.d("NoteList", "NoteList retrieved successfully");
                } else {
                    Log.d("NoteList", "NoteList retrieved fail");
                }
            }
        });
    }

    private void gotoActivity(String thisActivity, Class nextActivity, String NoteTitle){
        newIntent = new Intent(NoteAdd.this, nextActivity);
        newIntent.putExtra("groupname", oldIntent.getStringExtra("groupname"));
        newIntent.putExtra("FromActivity","NoteAdd");
        newIntent.putExtra("NoteTitle", NoteTitle);
        startActivity(newIntent);
    }
}
