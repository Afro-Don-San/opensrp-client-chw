package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.Intent;

import com.adosa.opensrp.chw.fp.util.PathfinderFamilyPlanningConstants;

import org.smartregister.chw.core.activity.CorePathfinderFamilyPlanningRegisterActivity;

public class PathfinderRegisterFormActivity extends CorePathfinderFamilyPlanningRegisterActivity {

    public static void startMe(Activity activity, String baseEntityID, String dob, String formName, String payloadType) {
        Intent intent = new Intent(activity, PathfinderRegisterFormActivity.class);
        intent.putExtra(PathfinderFamilyPlanningConstants.ActivityPayload.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(PathfinderFamilyPlanningConstants.ActivityPayload.DOB, dob);
        intent.putExtra(PathfinderFamilyPlanningConstants.ActivityPayload.FP_FORM_NAME, formName);
        intent.putExtra(PathfinderFamilyPlanningConstants.ActivityPayload.ACTION, payloadType);
        activity.startActivityForResult(intent, org.smartregister.chw.anc.util.Constants.REQUEST_CODE_HOME_VISIT);
    }

    @Override
    public void onFormSaved() {
        super.onFormSaved();

        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        this.finish();
    }

    @Override
    protected Activity getFpRegisterActivity() {
        return this;
    }


}