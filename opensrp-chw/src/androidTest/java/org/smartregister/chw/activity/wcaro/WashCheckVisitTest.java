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
import static androidx.test.espresso.action.ViewActions.scrollTo;


public class WashCheckVisitTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    Utils utils = new Utils();

    @Before
    public void setUp() throws InterruptedException{

        utils.logIn(Constants.WcaroConfigs.wCaro_username, Constants.WcaroConfigs.wCaro_password);
    }

    @Test
    public void washCheckVisitTest() throws InterruptedException{

        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Yella Smith Family"))
                .perform(scrollTo(), click());
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("DUE"))
                .perform(click());
        Thread.sleep(5000);
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Smith Family . WASH check"))
                .perform(click());
    }

}
