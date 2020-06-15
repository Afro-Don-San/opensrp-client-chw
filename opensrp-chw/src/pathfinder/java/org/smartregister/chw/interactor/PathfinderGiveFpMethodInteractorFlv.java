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
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.activity.CorePathfinderFollowupVisitActivity;
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

public class PathfinderGiveFpMethodInteractorFlv extends DefaultPathfinderFpPregnancyScreeningInteractorFlv {
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
            evaluateGiveFpMethod();
        } catch (BaseAncHomeVisitAction.ValidationException e) {
            throw (e);
        } catch (Exception e) {
            Timber.e(e);
        }
        return actionList;
    }

    private void evaluateGiveFpMethod() throws Exception {
        JSONObject fpMethodReferralJsonObject = FormUtils.getInstance(context).getFormJson(Constants.JSON_FORM.getPathfinderFpMethodReferral());
        injectReferralFacilities(fpMethodReferralJsonObject);

        BaseAncHomeVisitAction fpMethodReferral = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.fp_method_referral))
                .withOptional(false)
                .withDetails(null)
                .withBaseEntityID(memberObject.getBaseEntityId())
                .withHelper(new FpMethodReferralHelper(context))
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.SEPARATE)
                .withFormName(fpMethodReferralJsonObject.toString())
                .build();

        JSONObject fpGiveFpMethodJsonObject = FormUtils.getInstance(context).getFormJson(Constants.JSON_FORM.getPathfinderGiveFamilyPlanningMethod());
        injectFpMethod(fpGiveFpMethodJsonObject);
        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.give_fp_method))
                .withOptional(false)
                .withDetails(null)
                .withBaseEntityID(memberObject.getBaseEntityId())
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.SEPARATE)
                .withHelper(new GiveFpMethodHelper(context, fpMethodReferral))
                .withFormName(fpGiveFpMethodJsonObject.toString())
                .build();

        actionList.put(context.getString(R.string.give_fp_method), action);
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

    private JSONObject injectFpMethod(JSONObject form) throws Exception {
        if (form == null) {
            return null;
        } else {
            JSONObject fp_method = new JSONObject();
            fp_method.put("fp_method", familyPlanningMethod);
            form.put("global", fp_method);
            return form;
        }
    }

    private class FpMethodReferralHelper extends HomeVisitActionHelper {
        private String referral_date;

        public FpMethodReferralHelper(Context context) {
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
            builder.append(context.getString(R.string.fp_method_referral)).append(":").append(" ").append(context.getString(R.string.yes));
            return builder.toString();
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(referral_date)) {
                return BaseAncHomeVisitAction.Status.PENDING;
            } else {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            }
        }
    }

    private class GiveFpMethodHelper extends HomeVisitActionHelper {
        private String fp_method_given;
        private BaseAncHomeVisitAction fpMethodReferralAction;

        public GiveFpMethodHelper(Context context, BaseAncHomeVisitAction fpMethodReferralAction) {
            this.context = context;
            this.fpMethodReferralAction = fpMethodReferralAction;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                fp_method_given = JsonFormUtils.getValue(jsonObject, "fp_method_given");
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        @Override
        public String evaluateSubTitle() {
            if (StringUtils.isBlank(fp_method_given)) {
                return null;
            }
            StringBuilder builder = new StringBuilder();
            builder.append(context.getString(R.string.give_fp_method)).append(":").append(" ").append(context.getString(R.string.yes));
            return builder.toString();
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(fp_method_given)) {
                return BaseAncHomeVisitAction.Status.PENDING;
            } else if ("false".equalsIgnoreCase(fp_method_given)) {
                try {
                    LinkedHashMap<String, BaseAncHomeVisitAction> additionalList = new LinkedHashMap<>();
                    additionalList.put(context.getString(R.string.fp_method_referral), fpMethodReferralAction);
                    ((CorePathfinderFollowupVisitActivity) context).initializeActions(additionalList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
            } else {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            }
        }
    }

}
