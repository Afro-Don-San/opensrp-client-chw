package org.smartregister.chw.activity.wcaro;


import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.smartregister.chw.R;
import org.smartregister.chw.activity.LoginActivity;
import org.smartregister.chw.activity.utils.Constants;
import org.smartregister.chw.activity.utils.Utils;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;


public class FamilyMemberTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    Utils utils = new Utils();

    @Before
    public void setUp() throws InterruptedException{

        utils.logIn(Constants.WcaroConfigs.wCaro_username, Constants.WcaroConfigs.wCaro_password);
        Thread.sleep(5000);
    }

    @Test
    public void removefamilyMemberSuccessfully() throws InterruptedException{
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Patzi Family"))
                .perform(click());
        Thread.sleep(500);
        utils.openFamilyMenu();
        Thread.sleep(500);
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Remove existing family member"))
                .perform(click());
        Thread.sleep(500);
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Sunlight Patzi, 27"))
                .perform(click());
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Reason *"))
                .perform(click());
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Moved away"))
                .perform(click());
        onView(withId(R.id.action_save))
                .perform(click());
    }

    @Test
    public void removefamilyMemberWithoutReason() throws InterruptedException{
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Patzi Family"))
                .perform(click());
        Thread.sleep(500);
        utils.openFamilyMenu();
        Thread.sleep(500);
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Remove existing family member"))
                .perform(click());
        Thread.sleep(500);
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Sunlight Patzi, 27"))
                .perform(click());
        onView(withId(R.id.action_save))
                .perform(click());
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Found 1 error(s) in the form. Please correct them to submit."))
                .perform(click());
    }

    @Test
    public void changeFamilyHeadsuccessfully() throws InterruptedException{
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Patzi Family"))
                .perform(click());
        Thread.sleep(500);
        utils.openFamilyMenu();
        Thread.sleep(500);
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Change family head"))
                .perform(click());
        Thread.sleep(500);
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Sunlight Patzi, 27"))
                .perform(click());
        onView(withId(R.id.action_save))
                .perform(click());
    }

    @Test
    public void changePrimarycareGiverSuccessfully() throws InterruptedException{
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Patzi Family"))
                .perform(click());
        Thread.sleep(500);
        utils.openFamilyMenu();
        Thread.sleep(500);
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Change primary caregiver"))
                .perform(click());
        Thread.sleep(500);
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Sunlight Patzi, 27"))
                .perform(click());
        onView(withId(R.id.action_save))
                .perform(click());
    }

    @Test
    public void confirmFamilyHead() throws InterruptedException {
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Patzi Family"))
                .perform(click());
        Thread.sleep(500);
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Texi Elfo Patzi, 40"))
                .perform(click());
        onView(withId(R.id.family_head))
    .check(matches(isDisplayed()));
    }

    @Test
    public void confirmUniqueId() throws InterruptedException {
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Mutwagazi Family"))
                .perform(click());
        Thread.sleep(500);
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Mkeet Mutwagazi, 29"))
                .perform(click());
        onView(withId(R.id.textview_detail_three))
                .check(matches(isDisplayed()));
    }

    @Test
    public void confirmFabOptions() throws InterruptedException {
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Mutwagazi Family"))
                .perform(click());
        Thread.sleep(500);
        onView(withId(R.id.fab))
                .perform(click());
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Call"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Add new family member"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
