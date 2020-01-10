package org.smartregister.chw.activity;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartregister.chw.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginActivityTestIngine {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Test
    public void loginActivityTestIngine() {
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.login_user_name_edit_text),
                        childAtPosition(
                                allOf(withId(R.id.middle_section),
                                        childAtPosition(
                                                withId(R.id.login_layout),
                                                1)),
                                0)));
        appCompatEditText.perform(scrollTo(), replaceText("chaone"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.login_password_edit_text),
                        childAtPosition(
                                allOf(withId(R.id.middle_section),
                                        childAtPosition(
                                                withId(R.id.login_layout),
                                                1)),
                                1)));
        appCompatEditText2.perform(scrollTo(), replaceText("Wcaro123"), closeSoftKeyboard());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.login_password_edit_text), withText("Wcaro123"),
                        childAtPosition(
                                allOf(withId(R.id.middle_section),
                                        childAtPosition(
                                                withId(R.id.login_layout),
                                                1)),
                                1)));
        appCompatEditText3.perform(pressImeActionButton());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.login_login_btn), withText("Log In"),
                        childAtPosition(
                                allOf(withId(R.id.middle_section),
                                        childAtPosition(
                                                withId(R.id.login_layout),
                                                1)),
                                3)));
        appCompatButton.perform(scrollTo(), click());

        ViewInteraction linearLayout = onView(
                allOf(withId(R.id.patient_column),
                        childAtPosition(
                                allOf(withId(R.id.register_columns),
                                        childAtPosition(
                                                withId(R.id.recycler_view),
                                                1)),
                                0),
                        isDisplayed()));
        linearLayout.perform(click());

        ViewInteraction overflowMenuButton = onView(
                allOf(withContentDescription("More options"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.family_toolbar),
                                        2),
                                0),
                        isDisplayed()));
        overflowMenuButton.perform(click());
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
