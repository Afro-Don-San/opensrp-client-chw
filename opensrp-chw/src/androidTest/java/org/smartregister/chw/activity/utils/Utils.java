package org.smartregister.chw.activity.utils;


import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.matcher.ViewMatchers;


import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.smartregister.chw.R;

import java.util.LinkedList;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;


public class Utils  {

    View view;

    @Rule
    //public android.support.test.rule.ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);


    public void logIn(String username, String password ) throws InterruptedException {
        onView(withId(R.id.login_user_name_edit_text))
                .perform(typeText(username), closeSoftKeyboard());
        onView(withId(R.id.login_password_edit_text))
                .perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn))
                .perform(click());
        Thread.sleep(5000);
    }

    /*
    public void logOut() throws InterruptedException{
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        Thread.sleep(500);
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Log out as CHA"))
                .perform(scrollTo(), click());
    }

     */
    public void revertLanguage() throws InterruptedException{
        openDrawer();
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Français"))
                .perform(scrollTo(), click());
        Thread.sleep(500);
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("English"))
                .perform(scrollTo(), click());
    }

    public void openDrawer(){
        onView(
                allOf(withContentDescription("Open"),
                        childAtPosition(
                                allOf(withId(R.id.register_toolbar),
                                        childAtPosition(
                                                withClassName(is("com.google.android.material.appbar.AppBarLayout")),
                                                0)),
                                1),
                        isDisplayed())).perform(click());
    }
    public void logOut() throws InterruptedException{
        openDrawer();
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Log out as CHA"))
                .perform(scrollTo(), click());

    }

    public void clearPreferences() {
        try {
            // clearing app data
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("pm clear package:org.smartregister.chw.togo");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

}
