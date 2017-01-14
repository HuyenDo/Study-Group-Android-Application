package com.example.sondo.cse110;

import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class WelcomePageTest {
    @Rule public final ActivityRule<LoginAndSignUp> main = new ActivityRule<>(LoginAndSignUp.class);

    @Test
    public void shouldBeAbleToLaunchMainScreen() {
        onView(withText("Password:")).check(ViewAssertions.matches(isDisplayed()));
    }
}