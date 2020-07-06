package org.smartregister.chw.interactor;

import android.content.Context;

import com.adosa.opensrp.chw.fp.dao.PathfinderFpDao;
import com.adosa.opensrp.chw.fp.domain.PathfinderFpAlertObject;
import com.adosa.opensrp.chw.fp.util.PathfinderFamilyPlanningConstants;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.activity.PathfinderFamilyPlanningFollowUpVisitActivity;
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
import org.smartregister.util.FormUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class PathfinderFpFollowUpVisitInteractorFlv extends DefaultPathfinderFpFollowUpVisitInteractorFlv {
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
            evaluateCounselling();
        } catch (BaseAncHomeVisitAction.ValidationException e) {
            throw (e);
        } catch (Exception e) {
            Timber.e(e);
        }
        return actionList;
    }

    private void evaluateCounselling() throws Exception {
        JSONObject resupplyJsonObject = FormUtils.getInstance(context).getFormJson(Constants.JSON_FORM.PathfinderFamilyPlanningFollowUpVisitUtils.getFamilyPlanningFollowupResupply());
        injectFamilyPlaningMethod(resupplyJsonObject);

        String familyPlanningMethodTranslated = null;
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
            default:
                familyPlanningMethodTranslated = " ";
                break;
        }
        BaseAncHomeVisitAction resupplyAction = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.resupply, familyPlanningMethodTranslated))
                .withOptional(false)
                .withDetails(null)
                .withBaseEntityID(memberObject.getBaseEntityId())
                .withHelper(new ResupplyHelper(context))
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.SEPARATE)
                .withFormName(resupplyJsonObject.toString())
                .build();

        JSONObject counselingJsonObject = FormUtils.getInstance(context).getFormJson(Constants.JSON_FORM.PathfinderFamilyPlanningFollowUpVisitUtils.getFamilyPlanningFollowupCounsel());
        injectFamilyPlaningMethod(counselingJsonObject);

        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.counseling))
                .withOptional(false)
                .withDetails(null)
                .withBaseEntityID(memberObject.getBaseEntityId())
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.SEPARATE)
                .withHelper(new CounsellingHelper(context, familyPlanningMethodTranslated, resupplyAction))
                .withFormName(counselingJsonObject.toString())
                .build();

        actionList.put(context.getString(R.string.counseling), action);
    }

    private JSONObject injectFamilyPlaningMethod(JSONObject form) throws Exception {
        if (form == null) {
            return null;
        } else {

            JSONObject fp_method = new JSONObject();
            fp_method.put("fp_method", familyPlanningMethod);
            form.put("global", fp_method);
            return form;
        }
    }

    private class ResupplyHelper extends HomeVisitActionHelper {
        private String no_condoms;
        private String no_pillcycles;
        private String IsfpMethodGiven;
        private Context context;

        public ResupplyHelper(Context context) {
            this.context = context;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                no_condoms = JsonFormUtils.getValue(jsonObject, "number_of_condoms");
                no_pillcycles = JsonFormUtils.getValue(jsonObject, "number_of_pills");
                IsfpMethodGiven = JsonFormUtils.getValue(jsonObject, "fp_method_given");
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        @Override
        public String evaluateSubTitle() {
            String resupply = getNonBlankString(no_condoms, no_pillcycles);

            if (StringUtils.isBlank(IsfpMethodGiven)) {
                return null;
            }
            StringBuilder builder = new StringBuilder();
            if (resupply.equalsIgnoreCase(no_condoms)) {
                builder.append(context.getString(R.string.no_of_condoms)).append(" ").append(resupply);
            } else if (resupply.equalsIgnoreCase(no_pillcycles)) {
                builder.append(context.getString(R.string.no_of_pill_cycles)).append(" ").append(resupply);

            }

            return builder.toString();
        }

        private String getNonBlankString(String... strings) {
            if (strings == null || strings.length == 0)
                return "";

            for (String s : strings) {
                if (StringUtils.isNotBlank(s)) return s;
            }
            return "";
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(IsfpMethodGiven)) {
                return BaseAncHomeVisitAction.Status.PENDING;
            }

            if (!StringUtils.isBlank(no_condoms) || !StringUtils.isBlank(no_pillcycles)) {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            } else
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
        }
    }

    private class CounsellingHelper extends HomeVisitActionHelper {
        private Context context;
        private String satisfaction_with_current_method;
        private String client_need_refill;
        private String familyPlanningMethodTranslated;
        private BaseAncHomeVisitAction resupplyAction;

        public CounsellingHelper(Context context, String familyPlanningMethodTranslated, BaseAncHomeVisitAction resupplyAction) {
            this.context = context;
            this.resupplyAction = resupplyAction;
            this.familyPlanningMethodTranslated = familyPlanningMethodTranslated;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                satisfaction_with_current_method = JsonFormUtils.getValue(jsonObject, "satisfaction_with_current_method");
                client_need_refill = JsonFormUtils.getValue(jsonObject, "client_need_refill");
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        @Override
        public String evaluateSubTitle() {
            if (StringUtils.isBlank(satisfaction_with_current_method)) {
                return null;
            }
            StringBuilder builder = new StringBuilder();
            if (satisfaction_with_current_method.equalsIgnoreCase("yes"))
                builder.append(context.getString(R.string.counseling)).append(":").append(" ").append(context.getString(R.string.yes));
            else if (satisfaction_with_current_method.equalsIgnoreCase("no")) {
                builder.append(context.getString(R.string.counseling)).append(":").append(" ").append(context.getString(R.string.no));
            }
            return builder.toString();
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(satisfaction_with_current_method)) {
                return BaseAncHomeVisitAction.Status.PENDING;
            } else if ("yes".equalsIgnoreCase(client_need_refill)) {
                try {
                    if (
                            familyPlanningMethod.equalsIgnoreCase("coc") ||
                                    familyPlanningMethod.equalsIgnoreCase("pop") ||
                                    familyPlanningMethod.equalsIgnoreCase("sdm")
                    ) {
                        JSONObject jsonObject = FormUtils.getInstance(context).getFormJson(Constants.JSON_FORM.PathfinderFamilyPlanningFollowUpVisitUtils.getFamilyPlanningFollowupResupply());
                        injectFamilyPlaningMethod(jsonObject);

                        LinkedHashMap<String, BaseAncHomeVisitAction> additionalList = new LinkedHashMap<>();
                        additionalList.put(context.getString(R.string.resupply, familyPlanningMethodTranslated), resupplyAction);
                        ((PathfinderFamilyPlanningFollowUpVisitActivity) context).initializeActions(additionalList);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return BaseAncHomeVisitAction.Status.COMPLETED;
            } else {
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
            }
        }
    }


}
