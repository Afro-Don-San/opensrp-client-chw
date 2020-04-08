package org.smartregister.chw.activity.wcaro;

import android.Manifest;

import android.app.Activity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.test.espresso.core.internal.deps.guava.collect.Iterables;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import com.vijay.jsonwizard.activities.JsonFormActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartregister.chw.R;
import org.smartregister.chw.activity.LoginActivity;
import org.smartregister.chw.activity.utils.Configs;
import org.smartregister.chw.activity.utils.Constants;
import org.smartregister.chw.activity.utils.OrderedRunner;
import org.smartregister.chw.activity.utils.Utils;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.doubleClick;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.smartregister.chw.activity.utils.Utils.getViewId;

@LargeTest
//@RunWith(AndroidJUnit4.class)
@RunWith(OrderedRunner.class)
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
    public void addfamily() throws Throwable {
            onView(withId(R.id.action_register)).perform(click());
            Thread.sleep(1000);
            Activity activity = getCurrentActivity();
            onView(withId(getViewId((JsonFormActivity) activity, "step1:fam_name")))
                .perform(typeText(Configs.TestConfigs.familyName), closeSoftKeyboard());
            onView(withId(getViewId((JsonFormActivity) activity, "step1:village_town")))
                .perform(typeText("ThePlace"), closeSoftKeyboard());
            onView(withId(getViewId((JsonFormActivity) activity, "step1:quarter_clan")))
                .perform(typeText("ABC"), closeSoftKeyboard());
            onView(withId(getViewId((JsonFormActivity) activity, "step1:street")))
                .perform(typeText("Kilimani Avenue"), closeSoftKeyboard());
            onView(withId(getViewId((JsonFormActivity) activity, "step1:landmark")))
                .perform(typeText("A fig tree 20 meters North West of the house"), closeSoftKeyboard());
            onView(withId(getViewId((JsonFormActivity) activity, "step1:gps")))
                .perform(doubleClick());
            Thread.sleep(2000);
            onView(ViewMatchers.withSubstring("OK"))
                .perform(click());
            Thread.sleep(1000);
            onView(ViewMatchers.withSubstring("Next"))
                .perform(scrollTo(), click());
            Thread.sleep(500);
            onView(withId(getViewId((JsonFormActivity) activity, "step2:national_id")))
                .perform(typeText(Configs.TestConfigs.nationalID));
            onView(withId(getViewId((JsonFormActivity) activity, "step2:same_as_fam_name")))
                .perform(scrollTo(), click());
            onView(withId(getViewId((JsonFormActivity) activity, "step2:first_name")))
                .perform(scrollTo(), typeText(Configs.TestConfigs.aboveFiveFirstNameOne));
            onView(withId(getViewId((JsonFormActivity) activity, "step2:middle_name")))
                .perform(scrollTo(), typeText(Configs.TestConfigs.aboveFiveSecondNameOne));
            onView(withId(getViewId((JsonFormActivity) activity, "step2:dob_unknown")))
                .perform(scrollTo(), click());
            onView(withId(getViewId((JsonFormActivity) activity, "step2:age")))
                .perform(scrollTo(), typeText(Configs.TestConfigs.aboveFiveage));
            onView(ViewMatchers.withSubstring("Sex"))
                .perform(scrollTo(), click());
            Thread.sleep(500);
            onView(ViewMatchers.withSubstring("Male"))
                .perform(click());
            Thread.sleep(500);
            onView(withId(getViewId((JsonFormActivity) activity, "step2:phone_number")))
                .perform(scrollTo(), typeText(Configs.TestConfigs.phoneNumberOne));
            onView(withId(getViewId((JsonFormActivity) activity, "step2:other_phone_number")))
                .perform(scrollTo(), typeText(Configs.TestConfigs.getPhoneNumberTwo));
            onView(ViewMatchers.withSubstring("SUBMIT"))
                .perform(scrollTo(), click());
        Thread.sleep(500);
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

    Activity getCurrentActivity() throws Throwable {
        getInstrumentation().waitForIdleSync();
        final Activity[] activity = new Activity[1];
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                java.util.Collection<Activity> activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
                activity[0] = Iterables.getOnlyElement(activities);
            }
        });
        return activity[0];
    }
}