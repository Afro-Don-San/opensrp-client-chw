package org.smartregister.chw.util;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.Menu;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.Context;
import org.smartregister.chw.R;
import org.smartregister.chw.core.activity.CorePathfinderFollowupVisitActivity;
import org.smartregister.chw.core.rule.MalariaFollowUpRule;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.MalariaVisitUtil;
import org.smartregister.chw.fp.dao.FpDao;
import org.smartregister.chw.malaria.dao.MalariaDao;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Location;
import org.smartregister.repository.LocationRepository;
import org.smartregister.util.Utils;

import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class UtilsFlv {
    public static void updateMalariaMenuItems(String baseEntityId, Menu menu) {
        if (MalariaDao.isRegisteredForMalaria(baseEntityId)) {
            Utils.startAsyncTask(new UpdateFollowUpMenuItem(baseEntityId, menu), null);
        } else {
            menu.findItem(R.id.action_malaria_registration).setVisible(false);
        }
    }

    public static void updateFpMenuItems(String baseEntityId, Menu menu) {
        if (FpDao.isRegisteredForFp(baseEntityId)) {
            menu.findItem(R.id.action_fp_change).setVisible(true);
        } else {
            menu.findItem(R.id.action_fp_initiation).setVisible(true);
        }
    }

    public static boolean isClientOfReproductiveAge(CommonPersonObjectClient commonPersonObject, int fromAge, int toAge) {
        if (commonPersonObject == null) {
            Timber.e("Common is null");
            return false;
        }

        // check age
        String dobString = Utils.getValue(commonPersonObject.getColumnmaps(), "dob", false);
        if (!TextUtils.isEmpty(dobString)) {
            Period period = new Period(new DateTime(dobString), new DateTime());
            int age = period.getYears();
            return age >= fromAge && age <= toAge;
        }

        return false;
    }

    public static JSONObject injectReferralFacilities(JSONObject form) throws Exception {
        if (form == null) {
            return null;
        } else {

            JSONArray fields = form.getJSONObject("step1").getJSONArray("fields");
            for (int i = 0; i < fields.length(); i++) {
                JSONObject object = fields.getJSONObject(i);
                if (object.getString("key").equals("chw_referral_hf")) {
                    List<Location> locations = new LocationRepository().getAllLocations();

                    JSONObject openmrsIds = new JSONObject();
                    JSONArray values = new JSONArray();
                    for (Location location : locations) {
                        openmrsIds.put(location.getProperties().getName(), location.getId());
                        values.put(location.getProperties().getName());
                    }

                    object.put("values", values);
                    object.put("keys", values);
                    object.put("openmrs_choice_ids", openmrsIds);
                    break;
                }
            }
            return form;
        }
    }

    public static String getTranslatedFpMethodName(String familyPlanningMethod, Activity context) {
        String familyPlanningMethodTranslated = "";
        switch (familyPlanningMethod) {
            case "coc":
                familyPlanningMethodTranslated = context.getString(R.string.coc);
                break;
            case "pop":
                familyPlanningMethodTranslated = context.getString(R.string.pop);
                break;
            case "male_condom":
                familyPlanningMethodTranslated = context.getString(R.string.male_condom);
                break;
            case "female_condom":
                familyPlanningMethodTranslated = context.getString(R.string.female_condom);
                break;
            case "sdm":
                familyPlanningMethodTranslated = context.getString(R.string.standard_day_method);
                break;
            case "implants":
                familyPlanningMethodTranslated = context.getString(R.string.implants);
                break;
            case "tubal_ligation":
                familyPlanningMethodTranslated = context.getString(R.string.tubal_ligation);
                break;
            case "lam":
                familyPlanningMethodTranslated = context.getString(R.string.lam);
                break;
            case "injectable":
                familyPlanningMethodTranslated = context.getString(R.string.injectable);
                break;
            default:
                familyPlanningMethodTranslated = familyPlanningMethod;
                break;
        }
        return familyPlanningMethodTranslated;
    }

    public static JSONObject injectFamilyPlaningMethod(JSONObject form, String familyPlanningMethod, Activity context) throws Exception {
        JSONObject fp_method = new JSONObject();
        fp_method.put("global_fp_method", familyPlanningMethod);
        fp_method.put("global_fp_method_translated", UtilsFlv.getTranslatedFpMethodName(familyPlanningMethod, context));
        form.put("global", fp_method);
        return form;
    }

    private static class UpdateFollowUpMenuItem extends AsyncTask<Void, Void, Void> {
        private final String baseEntityId;
        private Menu menu;
        private MalariaFollowUpRule malariaFollowUpRule;

        public UpdateFollowUpMenuItem(String baseEntityId, Menu menu) {
            this.baseEntityId = baseEntityId;
            this.menu = menu;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Date malariaTestDate = MalariaDao.getMalariaTestDate(baseEntityId);
            Date followUpDate = MalariaDao.getMalariaFollowUpVisitDate(baseEntityId);
            malariaFollowUpRule = MalariaVisitUtil.getMalariaStatus(malariaTestDate, followUpDate);
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            if (malariaFollowUpRule != null && StringUtils.isNotBlank(malariaFollowUpRule.getButtonStatus()) &&
                    !CoreConstants.VISIT_STATE.EXPIRED.equalsIgnoreCase(malariaFollowUpRule.getButtonStatus())) {
                menu.findItem(R.id.action_malaria_followup_visit).setVisible(true);
            }
        }
    }

}
