package org.smartregister.chw.activity.wcaro;

import android.Manifest;

import android.text.SpannableString;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.StringRes;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.smartregister.chw.R;
import org.smartregister.chw.activity.LoginActivity;
import org.smartregister.chw.activity.utils.Constants;
import org.smartregister.chw.activity.utils.MyApplication;
import org.smartregister.chw.activity.utils.Utils;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.AllOf.allOf;


public class AddFamilyTestWcaro {

    @Rule
    public ActivityTestRule<LoginActivity> intentsTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.CALL_PHONE);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule1 = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);

    Utils utils = new Utils();

    @Before
    public void setUp() throws InterruptedException {
        utils.logIn(Constants.WcaroConfigs.wCaro_username, Constants.WcaroConfigs.wCaro_password);
    }

    @Test
    public void logOut() throws InterruptedException{
        utils.logOut();

        }

    @Test
    public void addfamily() throws InterruptedException{

        try{
            onView(withId(R.id.action_register)).perform(click());
            Thread.sleep(1000);
            onView(withContentDescription("Family name *")).perform(typeText("Another"), closeSoftKeyboard());

            //onView(withId(1)).perform(typeText("Test"), closeSoftKeyboard());
            onView(withId(5))
                .perform(typeText("Another"), closeSoftKeyboard());
            // onView(withText("Village *")).perform(typeText("Another"), closeSoftKeyboard());
            onView(withId(7))
                .perform(typeText("ThePlace"), closeSoftKeyboard());
            onView(withId(9))
                .perform(typeText("Kilimani Avenue"), closeSoftKeyboard());
            onView(withId(11))
                .perform(typeText("A fig tree 20 meters North West of the house"), closeSoftKeyboard());
            onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Record location"))
                .perform(click());
            Thread.sleep(1000);
            onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("OK"))
                .perform(click());
            Thread.sleep(1000);
            onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Family source of income"))
                .perform(click());
            Thread.sleep(500);
            onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Construction"))
                .perform(click());
            Thread.sleep(500);
            onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Next"))
                .perform(scrollTo(), click());
            Thread.sleep(500);
            onView(withId(22))
                .perform(scrollTo(), typeText("3218900111"));
            //onView(withId(24)).perform(scrollTo(), typeText("JinaLaFamilia"));
            onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Surname same as family name"))
                .perform(scrollTo(), click());
            //onView(withId(31)).perform(scrollTo(), typeText("JinaLaKwanza"));
            onView(withId(33))
                .perform(scrollTo(), typeText("JinaLaPili"));
            //onView(withId(35)).perform(scrollTo(), click());
            //Thread.sleep(1000);
            //onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("done")).perform(click());
            //Thread.sleep(500);
            onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("DOB unknown?"))
                .perform(scrollTo(), click());
            onView(withId(40))
                .perform(scrollTo(), typeText("25"));
            onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Sex"))
                .perform(scrollTo(), click());
            Thread.sleep(500);
            onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Male"))
                .perform(click());
            Thread.sleep(500);
            onView(withId(44))
                .perform(scrollTo(), typeText("088882002991"));
            onView(withId(46))
                .perform(scrollTo(), typeText("088882002992"));
            //onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("SUBMIT"))
                //.perform(scrollTo(), click());

            }
        catch (Exception ex) {
            throw ex;

        }
        //Thread.sleep(1000);
        //Espresso.pressBack();
        //Espresso.pressBack();
        //Thread.sleep(500);
        //onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Yes")).perform(click());
        //Thread.sleep(100);

    }

    @After
    public void completeTests(){
        intentsTestRule.finishActivity();
    }

    public static Matcher<View> matchesDate(final int year, final int month, final int day) {
        return new BoundedMatcher<View, DatePicker>(DatePicker.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("matches date:");
            }

            @Override
            protected boolean matchesSafely(DatePicker item) {
                return (year == item.getYear() && month == item.getMonth() && day == item.getDayOfMonth());
            }
        };
    }


    private static Matcher<View> withError(final String expected) {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View item) {
                if (item instanceof EditText) {
                    return ((EditText)item).getError().toString().equals(expected);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Not found error message" + expected + ", find it!");
            }
        };
    }
}
