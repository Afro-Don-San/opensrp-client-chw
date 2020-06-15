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
import org.smartregister.chw.activity.PathfinderFamilyPlanningPregnancyScreeningActivity;
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

public class PathfinderFpPregnancyScreeningInteractorFlv extends DefaultPathfinderFpPregnancyScreeningInteractorFlv {
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
            evaluatePregnancyScreening();
        } catch (BaseAncHomeVisitAction.ValidationException e) {
            throw (e);
        } catch (Exception e) {
            Timber.e(e);
        }
        return actionList;
    }

    private void evaluatePregnancyScreening() throws Exception {
        JSONObject ancReferralJsonObject = FormUtils.getInstance(context).getFormJson(Constants.JSON_FORM.getPathfinderAncReferral());
        injectReferralFacilities(ancReferralJsonObject);

        BaseAncHomeVisitAction ancReferralAction = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.issue_anc_referral))
                .withOptional(false)
                .withDetails(null)
                .withBaseEntityID(memberObject.getBaseEntityId())
                .withHelper(new ANCReferralHelper(context))
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.SEPARATE)
                .withFormName(ancReferralJsonObject.toString())
                .build();

        JSONObject pregnancyScreeningJsonObject = FormUtils.getInstance(context).getFormJson(Constants.JSON_FORM.getPathfinderPregnancyScreening());

        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.fp_pregnancy_screening))
                .withOptional(false)
                .withDetails(null)
                .withBaseEntityID(memberObject.getBaseEntityId())
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.SEPARATE)
                .withHelper(new PregnancyScreeningHelper(context, ancReferralAction))
                .withFormName(pregnancyScreeningJsonObject.toString())
                .build();

        actionList.put(context.getString(R.string.fp_pregnancy_screening), action);
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
                    for(Location location : locations){
                        openmrsIds.put(location.getProperties().getName(),location.getId());
                        values.put(location.getProperties().getName());
                    }

                    object.put("values",values);
                    object.put("keys",values);
                    object.put("openmrs_choice_ids",openmrsIds);
                    break;
                }
            }
            return form;
        }
    }

    private class ANCReferralHelper extends HomeVisitActionHelper {
        private String referral_date;

        public ANCReferralHelper(Context context) {
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

            if (!StringUtils.isBlank(referral_date)) {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            } else
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
        }
    }

    private class PregnancyScreeningHelper extends HomeVisitActionHelper {
        private String is_client_pregnant;
        private String started_anc;
        private BaseAncHomeVisitAction ancReferralAction;

        public PregnancyScreeningHelper(Context context, BaseAncHomeVisitAction ancReferralAction) {
            this.context = context;
            this.ancReferralAction = ancReferralAction;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                is_client_pregnant = JsonFormUtils.getValue(jsonObject, "is_client_pregnant");
                started_anc = JsonFormUtils.getValue(jsonObject, "started_anc");
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        @Override
        public String evaluateSubTitle() {
            if (StringUtils.isBlank(is_client_pregnant)) {
                return null;
            }
            StringBuilder builder = new StringBuilder();
            if (is_client_pregnant.equalsIgnoreCase("yes"))
                builder.append(context.getString(R.string.pregnancy_confirmation)).append(":").append(" ").append(context.getString(R.string.yes));
            else if (is_client_pregnant.equalsIgnoreCase("no")) {
                builder.append(context.getString(R.string.pregnancy_confirmation)).append(":").append(" ").append(context.getString(R.string.no));
            }
            return builder.toString();
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(is_client_pregnant)) {
                return BaseAncHomeVisitAction.Status.PENDING;
            } else if ("no".equalsIgnoreCase(started_anc)) {
                try {
                    LinkedHashMap<String, BaseAncHomeVisitAction> additionalList = new LinkedHashMap<>();
                    additionalList.put(context.getString(R.string.issue_anc_referral), ancReferralAction);
                    ((PathfinderFamilyPlanningPregnancyScreeningActivity) context).initializeActions(additionalList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return BaseAncHomeVisitAction.Status.COMPLETED;
            } else if ("no".equalsIgnoreCase(is_client_pregnant)) {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            }
            {
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
            }
        }
    }


}
