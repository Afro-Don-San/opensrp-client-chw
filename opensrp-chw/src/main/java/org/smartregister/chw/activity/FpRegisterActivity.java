package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.BuildConfig;
import org.smartregister.chw.R;
import org.smartregister.chw.core.activity.CoreFpRegisterActivity;
import org.smartregister.chw.core.dataloader.FPDataLoader;
import org.smartregister.chw.core.form_data.NativeFormsDataBinder;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.chw.fragment.FpRegisterFragment;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class FpRegisterActivity extends CoreFpRegisterActivity {

    private static String baseEntityId;
    private static String fpFormName;

    public static void startFpRegistrationActivity(Activity activity, String baseEntityID, String dob, String formName, String payloadType) {
        Class fpRegisterActivityClass;
        fpRegisterActivityClass = FpRegisterActivity.class;

        Intent intent = new Intent(activity, fpRegisterActivityClass);
        intent.putExtra(FamilyPlanningConstants.ActivityPayload.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(FamilyPlanningConstants.ActivityPayload.DOB, dob);
        intent.putExtra(FamilyPlanningConstants.ActivityPayload.FP_FORM_NAME, formName);
        intent.putExtra(FamilyPlanningConstants.ActivityPayload.ACTION, payloadType);
        fpFormName = formName;
        baseEntityId = baseEntityID;
        activity.startActivity(intent);
    }

    @Override
    protected void registerBottomNavigation() {
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(org.smartregister.R.id.bottom_navigation);
        FamilyRegisterActivity.registerBottomNavigation(bottomNavigationHelper, bottomNavigationView, this);
    }

    @Override
    public void onFormSaved() {
        Class fpRegisterActivityClass;
        if (BuildConfig.USE_PATHFINDERS_FP_MODULE) {
            fpRegisterActivityClass = PathfinderFamilyPlanningRegisterActivity.class;
        } else {
            fpRegisterActivityClass = FpRegisterActivity.class;
        }
        startActivity(new Intent(this, fpRegisterActivityClass));
        super.onFormSaved();
        this.finish();
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new FpRegisterFragment();
    }

    @Override
    protected Activity getFpRegisterActivity() {
        return this;
    }

    @Override
    public JSONObject getFpFormForEdit() {

        NativeFormsDataBinder binder = new NativeFormsDataBinder(this, baseEntityId);
        binder.setDataLoader(new FPDataLoader(getString(R.string.fp_update_family_planning)));

        JSONObject form = binder.getPrePopulatedForm(fpFormName);

        try {
            form.put(JsonFormUtils.ENCOUNTER_TYPE, FamilyPlanningConstants.EventType.UPDATE_FAMILY_PLANNING_REGISTRATION);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return form;
    }
}