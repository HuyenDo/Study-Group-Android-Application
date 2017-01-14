package com.example.sondo.cse110;

import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.runner.AndroidJUnit4;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by Son Do on 12/4/2015.
 */
@RunWith(AndroidJUnit4.class)
public class StudyGroupTest {
    @Rule public final ActivityRule<StudyGroup> main = new ActivityRule<>(StudyGroup.class);

    @Test
    public void DisplayCurrentUserAfterLogIn() {
        //Test if the page display current user name correctly
        String username = ParseUser.getCurrentUser().getUsername();
        onView(withText("Welcome, " + username)).check(ViewAssertions.matches(isDisplayed()));
    }

    @Test
    public void displayStudyGroup() {
        //Test study groups are displayed properly, with date created, author of study group, study group name
        ParseQuery<ParseObject> StudyGroupDB = ParseQuery.getQuery("StudyGroup");
        StudyGroupDB.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                for (ParseObject studygroup : objects) {
                    onView(withText(studygroup.getString("StudyGroup"))).check(ViewAssertions.matches(isDisplayed()));
                    onView(withText(studygroup.getString("FromUser"))).check(ViewAssertions.matches(isDisplayed()));
                    onView(withText(studygroup.getCreatedAt().toString())).check(ViewAssertions.matches(isDisplayed()));
                }
            }
        });
    }
}