package org.smartregister.chw.activity.wcaro;


import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.smartregister.chw.activity.LoginActivity;
import org.smartregister.chw.activity.utils.Constants;
import org.smartregister.chw.activity.utils.Utils;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;


public class RemovefanilyMemberTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    Utils utils = new Utils();

    @Before
    public void setUp() throws InterruptedException{

        utils.logIn(Constants.WcaroConfigs.wCaro_username, Constants.WcaroConfigs.wCaro_password);
        Thread.sleep(5000);
    }

    @Test
    public void removefamilyMember() throws InterruptedException{

        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Say Hello Family"))
                .perform(click());
        Thread.sleep(1000);
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Twoyr Hello"))
                .perform(click());
        Thread.sleep(5000);
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("test October"))
                .perform(click());

    }
}
