package org.smartregister.chw.activity.wcaro;

import android.Manifest;

import android.view.View;
import android.widget.EditText;

import androidx.annotation.StringRes;
import androidx.test.espresso.Espresso;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.smartregister.chw.R;
import org.smartregister.chw.activity.LoginActivity;
import org.smartregister.chw.activity.utils.Constants;
import org.smartregister.chw.activity.utils.Utils;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.startsWith;

public class AddFamilyMemberTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.CALL_PHONE);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule1 = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);

    Utils utils = new Utils();

    String key = "first_name";
    Object str = key;

    @Before
    public void setUp() throws InterruptedException {
        utils.logIn(Constants.WcaroConfigs.wCaro_username, Constants.WcaroConfigs.wCaro_password);
    }


    @Test
    public void
    addFamilyMember() throws InterruptedException{

        try{
            onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Amadou Diallo Family"))
                .perform(click());
            onView(withId(R.id.fab))
                .perform(click());
            Thread.sleep(500);
            onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Add new family member"))
                .perform(click());
            Thread.sleep(100);
            onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Child under 5 years"))
                .perform(click());
            Thread.sleep(500);
            //onView(withId(5)).perform(typeText("JinaLaFamilia"), closeSoftKeyboard());
            onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Surname same as family name"))
                .perform(scrollTo(), click());

            Thread.sleep(500);
            //onView(withId(firstName))
                //.perform(typeText("JinaLaKwanza"), closeSoftKeyboard());//12
            //allOf(is(instanceOf(String.class)), is("First name *")).perform(typeText("test"), closeSoftKeyboard());
            onData(hasToString(startsWith("First name *"))).perform(typeText("test"), closeSoftKeyboard());

            Thread.sleep(1000);
            onView(withId(14))
                .perform(typeText("JinaLaPili"), closeSoftKeyboard());//14
            onView(withId(16))
                .perform(scrollTo(), click());//16
            Thread.sleep(1000);
            onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("done"))
                .perform(click());
            Thread.sleep(500);
            /*onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("DOB is estimated")).perform(scrollTo(), click());
            onView(withId(21)).perform(scrollTo(), typeText("4"));
            Thread.sleep(500);*/
            onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Sex"))
                .perform(scrollTo(), click());
            Thread.sleep(500);
            onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Male"))
                .perform(click());
            Thread.sleep(500);
            onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Early initiation of breastfeeding (1 hr)?"))
                .perform(scrollTo(), click());
            onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("No"))
                .perform(click());
            onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Physically challenged?"))
                .perform(scrollTo(), click());
            Thread.sleep(500);
            onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("No"))
                .perform(click());
            Thread.sleep(5000);
            //onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Save")).perform(click());
    }
        catch (Exception ex) {
        throw ex;

    }
        //Thread.sleep(5000);
        Espresso.pressBack();
        //Espresso.pressBack();
        Thread.sleep(500);
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("YES")).perform(click());
        Thread.sleep(100);
        Espresso.pressBack();
        //utils.logOut();

}

    private String getString(@StringRes int resourceId) {
        return mActivityTestRule.getActivity().getString(resourceId);
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
