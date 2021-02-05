package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import org.jetbrains.annotations.NotNull;
import org.smartregister.chw.R;
import org.smartregister.chw.core.fragment.FamilyCallDialogFragment;
import org.smartregister.chw.core.listener.OnClickFloatingMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.model.ReferralTypeModel;
import org.smartregister.chw.util.Constants;
import org.smartregister.chw.util.UtilsFlv;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.chw.core.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class FamilyOtherMemberProfileActivityFlv implements FamilyOtherMemberProfileActivity.Flavor {

    @Override
    public OnClickFloatingMenu getOnClickFloatingMenu(final Activity activity, final String familyBaseEntityId, String baseEntityId) {
        return viewId -> {
            switch (viewId) {
                case R.id.call_layout:
                    FamilyCallDialogFragment.launchDialog(activity, familyBaseEntityId);
                    break;
                case R.id.refer_to_facility_layout:
                    launchClientReferralActivity(activity, getCommonReferralTypes(activity), baseEntityId);
                    break;
                default:
                    break;
            }
        };
    }

    @Override
    public void updateMalariaMenuItems(String baseEntityId, Menu menu) {
        UtilsFlv.updateMalariaMenuItems(baseEntityId, menu);
    }

    @Override
    public void updateMaleFpMenuItems(String baseEntityId, Menu menu) {
        UtilsFlv.updateFpMenuItems(baseEntityId, menu);
    }

    @Override
    public void updateFpMenuItems(String baseEntityId, Menu menu) {
        UtilsFlv.updateFpMenuItems(baseEntityId, menu);
    }

    @Override
    public boolean isOfReproductiveAge(CommonPersonObjectClient commonPersonObject, String gender) {
        if (gender.equalsIgnoreCase("Female")) {
            return Utils.isMemberOfReproductiveAge(commonPersonObject, 10, 49);
        } else if (gender.equalsIgnoreCase("Male")) {
            return Utils.isMemberOfReproductiveAge(commonPersonObject, 15, 49);
        } else {
            return false;
        }
    }

    public boolean isWra(CommonPersonObjectClient commonPersonObject) {
        return Utils.isMemberOfReproductiveAge(commonPersonObject, 10, 49);

    }

    @Override
    public boolean hasANC() {
        return false;
    }

    public static void launchClientReferralActivity(Activity activity, List<ReferralTypeModel> referralTypeModels, String baseEntityId) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.ENTITY_ID, baseEntityId);
        bundle.setClassLoader(ReferralTypeModel.class.getClassLoader());
        bundle.putParcelableArrayList(Constants.REFERRAL_TYPES, (ArrayList<ReferralTypeModel>) referralTypeModels);
        activity.startActivity(new Intent(activity, ClientReferralActivity.class).putExtras(bundle));
    }

    @NotNull
    public static List<ReferralTypeModel> getCommonReferralTypes(Activity activity) {
        List<ReferralTypeModel> referralTypeModels = new ArrayList<>();

        referralTypeModels.add(new ReferralTypeModel(activity.getString(R.string.referral_to_community_conservation), org.smartregister.chw.util.Constants.JSON_FORM.getPathfinderCommunityConservation(), CoreConstants.TASKS_FOCUS.COMMUNITY_CONSERVATION));
        referralTypeModels.add(new ReferralTypeModel(activity.getString(R.string.referral_to_loan_unit), org.smartregister.chw.util.Constants.JSON_FORM.getPathfinderLoanUnitReferral(), CoreConstants.TASKS_FOCUS.LOAN_MANAGEMENT_UNIT));
        referralTypeModels.add(new ReferralTypeModel(activity.getString(R.string.referral_to_beach_management_unit), org.smartregister.chw.util.Constants.JSON_FORM.getPathfinderBeachManagementUnitReferral(), CoreConstants.TASKS_FOCUS.BEACH_MANAGEMENT_UNIT));
        referralTypeModels.add(new ReferralTypeModel(activity.getString(R.string.referral_to_client_smart_agriculture), org.smartregister.chw.util.Constants.JSON_FORM.getPathfinderClimateSmartAgricultureReferral(), CoreConstants.TASKS_FOCUS.CLIMATE_SMART_AGRICULTURE));
        referralTypeModels.add(new ReferralTypeModel(activity.getString(R.string.referral_to_model_household), org.smartregister.chw.util.Constants.JSON_FORM.getPathfinderModelHouseholdReferral(), CoreConstants.TASKS_FOCUS.MODEL_HOUSEHOLD));

        return referralTypeModels;
    }
}
