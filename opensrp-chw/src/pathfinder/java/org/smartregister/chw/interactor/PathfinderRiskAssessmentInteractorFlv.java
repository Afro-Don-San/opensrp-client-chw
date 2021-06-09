package org.smartregister.chw.interactor;

import android.content.Context;

import com.adosa.opensrp.chw.fp.dao.PathfinderFpDao;
import com.adosa.opensrp.chw.fp.domain.PathfinderFpAlertObject;
import com.adosa.opensrp.chw.fp.util.PathfinderFamilyPlanningConstants;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.activity.PathfinderRiskAssessmentVisitActivity;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.domain.PncBaby;
import org.smartregister.chw.util.Constants;
import org.smartregister.chw.util.JsonFormUtils;
import org.smartregister.domain.Location;
import org.smartregister.repository.LocationRepository;
import org.smartregister.util.FormUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class PathfinderRiskAssessmentInteractorFlv extends DefaultPathfinderFpPregnancyScreeningInteractorFlv {
    protected LinkedHashMap<String, BaseAncHomeVisitAction> actionList;
    protected Context context;
    protected Map<String, List<VisitDetail>> details = null;
    protected List<PncBaby> children;
    protected MemberObject memberObject;
    protected BaseAncHomeVisitContract.View view;
    protected Boolean editMode = false;
    protected String familyPlanningMethod;

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public LinkedHashMap<String, BaseAncHomeVisitAction> calculateActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack) throws BaseAncHomeVisitAction.ValidationException {
        actionList = new LinkedHashMap<>();
        this.memberObject = memberObject;
        editMode = view.getEditMode();
        this.view = view;
        List<PathfinderFpAlertObject> memberDetails = PathfinderFpDao.getFpDetails(memberObject.getBaseEntityId());
        if (memberDetails.size() > 0) {
            for (PathfinderFpAlertObject detail : memberDetails) {
                familyPlanningMethod = detail.getFpMethod();
            }
        }
        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), PathfinderFamilyPlanningConstants.EventType.FP_FOLLOW_UP_VISIT);
            if (lastVisit != null) {
                details = Collections.unmodifiableMap(VisitUtils.getVisitGroups(AncLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId())));
            }
        }
        try {
            Constants.JSON_FORM.setLocaleAndAssetManager(ChwApplication.getCurrentLocale(), ChwApplication.getInstance().getApplicationContext().getAssets());
            evaluateRiskAssessment();
        } catch (BaseAncHomeVisitAction.ValidationException e) {
            throw (e);
        } catch (Exception e) {
            Timber.e(e);
        }
        return actionList;
    }

    private void evaluateRiskAssessment() throws Exception {
        JSONObject hivReferralJsonObject = FormUtils.getInstance(context).getFormJson(Constants.JSON_FORM.getPathfinderHivReferral());
        injectReferralFacilities(hivReferralJsonObject);

        BaseAncHomeVisitAction hivReferralAction = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.issue_hiv_referral))
                .withOptional(false)
                .withDetails(null)
                .withBaseEntityID(memberObject.getBaseEntityId())
                .withHelper(new HIVReferralHelper(context))
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.SEPARATE)
                .withJsonPayload(hivReferralJsonObject.toString())
                .withFormName(Constants.JSON_FORM.getPathfinderHivReferral())
                .build();

        JSONObject htcReferralJsonObject = FormUtils.getInstance(context).getFormJson(Constants.JSON_FORM.getPathfinderHtcReferral());
        injectReferralFacilities(htcReferralJsonObject);

        BaseAncHomeVisitAction htcReferralAction = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.issue_htc_referral))
                .withOptional(false)
                .withDetails(null)
                .withBaseEntityID(memberObject.getBaseEntityId())
                .withHelper(new HIVReferralHelper(context))
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.SEPARATE)
                .withJsonPayload(htcReferralJsonObject.toString())
                .withFormName(Constants.JSON_FORM.getPathfinderHtcReferral())
                .build();

        JSONObject stiReferralJsonObject = FormUtils.getInstance(context).getFormJson(Constants.JSON_FORM.getPathfinderStiReferral());
        injectReferralFacilities(stiReferralJsonObject);

        BaseAncHomeVisitAction stiReferralAction = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.issue_sti_referral))
                .withOptional(false)
                .withDetails(null)
                .withBaseEntityID(memberObject.getBaseEntityId())
                .withHelper(new HIVReferralHelper(context))
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.SEPARATE)
                .withFormName(stiReferralJsonObject.toString())
                .build();

        JSONObject riskAssessmentJsonObject = FormUtils.getInstance(context).getFormJson(Constants.JSON_FORM.getPathfinderRiskAssessmentHivTestingDualProtectionCounseling());

        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.risk_assessment))
                .withOptional(false)
                .withDetails(null)
                .withBaseEntityID(memberObject.getBaseEntityId())
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.SEPARATE)
                .withHelper(new RiskAssessmentHelper(context, hivReferralAction, stiReferralAction, htcReferralAction))
                .withFormName(riskAssessmentJsonObject.toString())
                .build();

        actionList.put(context.getString(R.string.risk_assessment), action);
    }

    private JSONObject injectReferralFacilities(JSONObject form) throws Exception {
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

    private class HIVReferralHelper extends HomeVisitActionHelper {
        private String referral_date;

        public HIVReferralHelper(Context context) {
            this.context = context;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                referral_date = JsonFormUtils.getValue(jsonObject, "referral_date");
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        @Override
        public String evaluateSubTitle() {
            if (StringUtils.isBlank(referral_date)) {
                return null;
            }
            StringBuilder builder = new StringBuilder();
            builder.append(context.getString(R.string.issue_anc_referral)).append(":").append(" ").append(context.getString(R.string.yes));
            return builder.toString();
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(referral_date)) {
                return BaseAncHomeVisitAction.Status.PENDING;
            }

            return BaseAncHomeVisitAction.Status.COMPLETED;
        }
    }

    private class RiskAssessmentHelper extends HomeVisitActionHelper {
        private String client_may_have_sti;
        private String receiving_care_and_treatment_from_facility;
        private String client_would_like_to_be_tested_for_hiv;
        private String has_more_than_one_sexual_partner;

        private BaseAncHomeVisitAction stiReferralAction;
        private BaseAncHomeVisitAction hivReferralAction;
        private BaseAncHomeVisitAction htcReferralAction;

        public RiskAssessmentHelper(Context context, BaseAncHomeVisitAction hivReferralAction, BaseAncHomeVisitAction stiReferralAction, BaseAncHomeVisitAction htcReferralAction) {
            this.context = context;
            this.hivReferralAction = hivReferralAction;
            this.stiReferralAction = stiReferralAction;
            this.htcReferralAction = htcReferralAction;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                client_may_have_sti = JsonFormUtils.getValue(jsonObject, "client_may_have_sti");
                receiving_care_and_treatment_from_facility = JsonFormUtils.getValue(jsonObject, "receiving_care_and_treatment_from_facility");
                client_would_like_to_be_tested_for_hiv = JsonFormUtils.getValue(jsonObject, "client_would_like_to_be_tested_for_hiv");
                has_more_than_one_sexual_partner = JsonFormUtils.getValue(jsonObject, "has_more_than_one_sexual_partner");
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        @Override
        public String evaluateSubTitle() {
            Timber.e("Coze :: am here");
            if (StringUtils.isBlank(has_more_than_one_sexual_partner)) {
                return null;
            }
            StringBuilder builder = new StringBuilder();
            builder.append(context.getString(R.string.risk_assessment)).append(":").append(" ").append(context.getString(R.string.yes));

            return builder.toString();
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(has_more_than_one_sexual_partner)) {
                return BaseAncHomeVisitAction.Status.PENDING;
            }

            if ("no".equalsIgnoreCase(client_may_have_sti)) {
                try {
                    LinkedHashMap<String, BaseAncHomeVisitAction> additionalList = new LinkedHashMap<>();
                    additionalList.put(context.getString(R.string.issue_sti_referral), stiReferralAction);
                    ((PathfinderRiskAssessmentVisitActivity) context).initializeActions(additionalList);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            if ("no".equalsIgnoreCase(receiving_care_and_treatment_from_facility)) {
                try {
                    LinkedHashMap<String, BaseAncHomeVisitAction> additionalList = new LinkedHashMap<>();
                    additionalList.put(context.getString(R.string.issue_hiv_referral), hivReferralAction);
                    ((PathfinderRiskAssessmentVisitActivity) context).initializeActions(additionalList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if ("yes".equalsIgnoreCase(client_would_like_to_be_tested_for_hiv)) {
                try {
                    LinkedHashMap<String, BaseAncHomeVisitAction> additionalList = new LinkedHashMap<>();
                    additionalList.put(context.getString(R.string.issue_htc_referral), htcReferralAction);
                    ((PathfinderRiskAssessmentVisitActivity) context).initializeActions(additionalList);
                } catch (Exception e) {
                    Timber.e(e);
                }
            }
            return BaseAncHomeVisitAction.Status.COMPLETED;
        }
    }


}
