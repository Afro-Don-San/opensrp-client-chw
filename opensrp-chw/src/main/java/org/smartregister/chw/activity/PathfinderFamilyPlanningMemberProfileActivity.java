package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.adosa.opensrp.chw.fp.domain.PathfinderFpMemberObject;
import com.adosa.opensrp.chw.fp.util.PathfinderFamilyPlanningConstants;

import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.core.activity.CorePathfinderFamilyPlanningMemberProfileActivity;
import org.smartregister.chw.core.activity.CorePathfinderFamilyPlanningRegisterActivity;
import org.smartregister.chw.core.interactor.CorePathfinderFamilyPlanningProfileInteractor;
import org.smartregister.chw.core.presenter.CorePathfinderFamilyPlanningMemberProfilePresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.PathfinderFamilyPlanningUtil;
import org.smartregister.chw.model.ReferralTypeModel;
import org.smartregister.chw.presenter.PathfinderFamilyPlanningMemberProfilePresenter;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class PathfinderFamilyPlanningMemberProfileActivity extends CorePathfinderFamilyPlanningMemberProfileActivity {

    private List<ReferralTypeModel> referralTypeModels = new ArrayList<>();

    public static void startFpMemberProfileActivity(Activity activity, PathfinderFpMemberObject memberObject) {
        Intent intent = new Intent(activity, PathfinderFamilyPlanningMemberProfileActivity.class);
        intent.putExtra(PathfinderFamilyPlanningConstants.FamilyPlanningMemberObject.MEMBER_OBJECT, memberObject);
        activity.startActivity(intent);
    }

    protected static CommonPersonObjectClient getClientDetailsByBaseEntityID(@NonNull String baseEntityId) {
        CommonRepository commonRepository = Utils.context().commonrepository(Utils.metadata().familyMemberRegister.tableName);

        final CommonPersonObject commonPersonObject = commonRepository.findByBaseEntityId(baseEntityId);
        final CommonPersonObjectClient client =
                new CommonPersonObjectClient(commonPersonObject.getCaseId(), commonPersonObject.getDetails(), "");
        client.setColumnmaps(commonPersonObject.getColumnmaps());
        return client;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addFpReferralTypes();
    }

    protected void removeMember() {
        IndividualProfileRemoveActivity.startIndividualProfileActivity(PathfinderFamilyPlanningMemberProfileActivity.this,
                getClientDetailsByBaseEntityID(pathfinderFpMemberObject.getBaseEntityId()),
                pathfinderFpMemberObject.getFamilyBaseEntityId(), pathfinderFpMemberObject.getFamilyHead(),
                pathfinderFpMemberObject.getPrimaryCareGiver(), FpRegisterActivity.class.getCanonicalName());
    }

    protected void startFamilyPlanningRegistrationActivity() {
        //TODO change the form name
        FpRegisterActivity.startFpRegistrationActivity(this, pathfinderFpMemberObject.getBaseEntityId(), pathfinderFpMemberObject.getAge(), CoreConstants.JSON_FORM.getFpChangeMethodForm(pathfinderFpMemberObject.getGender()), PathfinderFamilyPlanningConstants.ActivityPayload.CHANGE_METHOD_PAYLOAD_TYPE);
    }

    @Override
    protected void initializePresenter() {
        showProgressBar(true);
        fpProfilePresenter = new PathfinderFamilyPlanningMemberProfilePresenter(this, new CorePathfinderFamilyPlanningProfileInteractor(this), pathfinderFpMemberObject);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if (id == R.id.record_fp_followup_visit) {
            openFollowUpVisitForm(false);
        }
    }


    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public void openMedicalHistory() {
        OnMemberTypeLoadedListener onMemberTypeLoadedListener = memberType -> {

            switch (memberType.getMemberType()) {
                case CoreConstants.TABLE_NAME.ANC_MEMBER:
                    AncMedicalHistoryActivity.startMe(PathfinderFamilyPlanningMemberProfileActivity.this, memberType.getMemberObject());
                    break;
                case CoreConstants.TABLE_NAME.PNC_MEMBER:
                    PncMedicalHistoryActivity.startMe(PathfinderFamilyPlanningMemberProfileActivity.this, memberType.getMemberObject());
                    break;
                case CoreConstants.TABLE_NAME.CHILD:
                    ChildMedicalHistoryActivity.startMe(PathfinderFamilyPlanningMemberProfileActivity.this, memberType.getMemberObject());
                    break;
                default:
                    Timber.v("Member info undefined");
                    break;
            }
        };
        executeOnLoaded(onMemberTypeLoadedListener);
    }

    @Override
    public void openFamilyPlanningRegistration() {
        PathfinderFamilyPlanningRegisterActivity.startFpRegistrationActivity(this, pathfinderFpMemberObject.getBaseEntityId(), pathfinderFpMemberObject.getAge(), CoreConstants.JSON_FORM.getFpRegistrationForm(pathfinderFpMemberObject.getGender()), PathfinderFamilyPlanningConstants.ActivityPayload.UPDATE_REGISTRATION_PAYLOAD_TYPE);

    }

    @Override
    public void openFamilyPlanningIntroduction() {
        PathfinderFamilyPlanningRegisterActivity.startFpRegistrationActivity(this, pathfinderFpMemberObject.getBaseEntityId(), pathfinderFpMemberObject.getAge(), CoreConstants.JSON_FORM.getPathfinderFamilyPlanningIntroduction(), com.adosa.opensrp.chw.fp.util.PathfinderFamilyPlanningConstants.ActivityPayload.CHANGE_METHOD_PAYLOAD_TYPE);
    }

    //    @Override TODO implement this override
    public void openFamilyPlanningRiskAssessmentHivTestingAndDualCounseling() {
        Timber.e("Coze ::openFamilyPlanningRiskAssessmentHivTestingAndDualCounseling ");
        PathfinderFamilyPlanningRegisterActivity.startFpRegistrationActivity(this, pathfinderFpMemberObject.getBaseEntityId(), pathfinderFpMemberObject.getAge(), CoreConstants.JSON_FORM.getPathfinderRiskAssessmentHivTestingDualProtectionCounseling(), PathfinderFamilyPlanningConstants.ActivityPayload.CHANGE_METHOD_PAYLOAD_TYPE);
    }

    @Override
    public void openPregnancyScreening() {
        PathfinderFamilyPlanningPregnancyScreeningActivity.startMe(this, pathfinderFpMemberObject, false);
    }

    @Override
    public void openChooseFpMethod() {
        PathfinderFamilyPlanningMethodChoiceActivity.startMe(this, pathfinderFpMemberObject, false);
    }

    @Override
    public void openGiveFpMethodButton() {
        PathfinderGiveFamilyPlanningMethodActivity.startMe(this, pathfinderFpMemberObject, false);
    }

    @Override
    public void openUpcomingServices() {
        PathfinderFamilyPlanningUpcomingServicesActivity.startMe(this, PathfinderFamilyPlanningUtil.toMember(pathfinderFpMemberObject));
    }

    @Override
    public void openFamilyDueServices() {
        Intent intent = new Intent(this, FamilyProfileActivity.class);

        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, pathfinderFpMemberObject.getFamilyBaseEntityId());
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_HEAD, pathfinderFpMemberObject.getFamilyHead());
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.PRIMARY_CAREGIVER, pathfinderFpMemberObject.getPrimaryCareGiver());
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_NAME, pathfinderFpMemberObject.getFamilyName());

        intent.putExtra(CoreConstants.INTENT_KEY.SERVICE_DUE, true);
        startActivity(intent);
    }

    @Override
    public void openFollowUpVisitForm(boolean isEdit) {
        PathfinderFamilyPlanningFollowUpVisitActivity.startMe(this, pathfinderFpMemberObject, isEdit);
    }

    private void addFpReferralTypes() {
        //TODO change the form to pathfinder form
        referralTypeModels.add(new ReferralTypeModel(getString(R.string.family_planning_referral),
                org.smartregister.chw.util.Constants.JSON_FORM.getFamilyPlanningReferralForm(pathfinderFpMemberObject.getGender()), CoreConstants.TASKS_FOCUS.FP_SIDE_EFFECTS));
    }

    public List<ReferralTypeModel> getReferralTypeModels() {
        return referralTypeModels;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CoreConstants.ProfileActivityResults.CHANGE_COMPLETED:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(this, PathfinderFamilyPlanningRegisterActivity.class);
                    intent.putExtras(getIntent().getExtras());
                    startActivity(intent);
                    finish();
                }
                break;
            case JsonFormUtils.REQUEST_CODE_GET_JSON:
                if (resultCode == RESULT_OK) {
                    try {
                        String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                        JSONObject form = new JSONObject(jsonString);
                        if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(CoreConstants.EventType.FAMILY_PLANNING_REFERRAL)) {
                            ((CorePathfinderFamilyPlanningMemberProfilePresenter) fpProfilePresenter).createReferralEvent(Utils.getAllSharedPreferences(), jsonString);
                            showToast(this.getString(org.smartregister.chw.core.R.string.referral_submitted));
                        }
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

