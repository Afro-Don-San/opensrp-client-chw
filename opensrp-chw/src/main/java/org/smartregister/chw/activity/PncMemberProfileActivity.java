package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.Intent;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.core.activity.CoreFamilyProfileActivity;
import org.smartregister.chw.core.activity.CorePncMemberProfileActivity;
import org.smartregister.chw.core.activity.CorePncRegisterActivity;
import org.smartregister.chw.core.contract.PncMemberProfileContract;
import org.smartregister.chw.core.interactor.CorePncMemberProfileInteractor;
import org.smartregister.chw.core.listener.OnClickFloatingMenu;
import org.smartregister.chw.core.rule.PncVisitAlertRule;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.custom_view.AncFloatingMenu;
import org.smartregister.chw.interactor.ChildProfileInteractor;
import org.smartregister.chw.interactor.FamilyProfileInteractor;
import org.smartregister.chw.interactor.PncMemberProfileInteractor;
import org.smartregister.chw.model.ChildRegisterModel;
import org.smartregister.chw.model.FamilyProfileModel;
import org.smartregister.chw.presenter.PncMemberProfilePresenter;
import org.smartregister.chw.schedulers.ChwScheduleTaskExecutor;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;

import timber.log.Timber;

public class PncMemberProfileActivity extends CorePncMemberProfileActivity implements PncMemberProfileContract.View {

    public static void startMe(Activity activity, MemberObject memberObject, String familyHeadName, String familyHeadPhoneNumber) {
        Intent intent = new Intent(activity, PncMemberProfileActivity.class);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.MEMBER_PROFILE_OBJECT, memberObject);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.FAMILY_HEAD_NAME, familyHeadName);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.FAMILY_HEAD_PHONE, familyHeadPhoneNumber);
        activity.startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;

        switch (requestCode) {
            case org.smartregister.chw.util.Constants.ProfileActivityResults.CHANGE_COMPLETED:
                Intent intent = new Intent(PncMemberProfileActivity.this, PncRegisterActivity.class);
                intent.putExtras(getIntent().getExtras());
                startActivity(intent);
                finish();
                break;
            case JsonFormUtils.REQUEST_CODE_GET_JSON:
                try {
                    String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                    JSONObject form = new JSONObject(jsonString);
                    if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Utils.metadata().familyMemberRegister.updateEventType)) {

                        FamilyEventClient familyEventClient =
                                new FamilyProfileModel(MEMBER_OBJECT.getFamilyName()).processUpdateMemberRegistration(jsonString, MEMBER_OBJECT.getBaseEntityId());
                        new FamilyProfileInteractor().saveRegistration(familyEventClient, jsonString, true, (FamilyProfileContract.InteractorCallBack) pncMemberProfilePresenter());
                    }

                    if (org.smartregister.chw.util.Constants.EventType.UPDATE_CHILD_REGISTRATION.equals(form.getString(JsonFormUtils.ENCOUNTER_TYPE))) {
                        Pair<Client, Event> pair = new ChildRegisterModel().processRegistration(jsonString);

                        if (pair != null) {
                            ((PncMemberProfileInteractor) pncMemberProfileInteractor).updateChild(pair, jsonString, null);
                        }

                    } else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(CoreConstants.EventType.PNC_REFERRAL)) {
                        pncMemberProfilePresenter().createReferralEvent(Utils.getAllSharedPreferences(), jsonString);
                        showToast(this.getString(R.string.referral_submitted));
                    }

                } catch (Exception e) {
                    Timber.e(e);
                }
                break;
            case Constants.REQUEST_CODE_HOME_VISIT:
                this.setupViews();
                ChwScheduleTaskExecutor.getInstance().execute(MEMBER_OBJECT.getBaseEntityId(), CoreConstants.EventType.PNC_HOME_VISIT, new Date());
                break;
            default:
                break;

        }
    }

    @Override
    public void setupViews() {
        super.setupViews();
        PncVisitAlertRule summaryVisit = getVisitDetails();
        String statusVisit = summaryVisit.getButtonStatus();
        if (statusVisit.equals("OVERDUE")) {
            textview_record_visit.setVisibility(View.VISIBLE);
            textview_record_visit.setBackgroundResource(R.drawable.rounded_red_btn);
        } else if (statusVisit.equals("DUE")) {
            textview_record_visit.setVisibility(View.VISIBLE);
            textview_record_visit.setBackgroundResource(R.drawable.rounded_blue_btn);
        } else if (ChildProfileInteractor.VisitType.VISIT_DONE.name().equals(statusVisit)) {
            Visit lastVisit = getVisit(Constants.EVENT_TYPE.PNC_HOME_VISIT);
            if (lastVisit != null) {
                boolean within24Hours;
                if ((Days.daysBetween(new DateTime(lastVisit.getCreatedAt()), new DateTime()).getDays() < 1) &&
                        (Days.daysBetween(new DateTime(lastVisit.getDate()), new DateTime()).getDays() <= 1)) {
                    within24Hours = true;
                    setEditViews(true, within24Hours, lastVisit.getDate().getTime());
                } else {
                    textview_record_visit.setVisibility(View.GONE);
                    layoutRecordView.setVisibility(View.GONE);
                }

            } else {
                textview_record_visit.setVisibility(View.VISIBLE);
                textview_record_visit.setBackgroundResource(R.drawable.rounded_blue_btn);
            }
        } else {
            textview_record_visit.setVisibility(View.GONE);
            layoutRecordView.setVisibility(View.GONE);
        }
    }

    @Override
    protected Class<? extends CoreFamilyProfileActivity> getFamilyProfileActivityClass() {
        return FamilyProfileActivity.class;
    }

    @Override
    protected CorePncMemberProfileInteractor getPncMemberProfileInteractor() {
        return new PncMemberProfileInteractor(this);
    }

    @Override
    protected void removePncMember() {
        IndividualProfileRemoveActivity.startIndividualProfileActivity(PncMemberProfileActivity.this, clientObject(), MEMBER_OBJECT.getFamilyBaseEntityId(), MEMBER_OBJECT.getFamilyHead(), MEMBER_OBJECT.getPrimaryCareGiver(), PncRegisterActivity.class.getCanonicalName());
    }

    @Override
    protected Class<? extends CorePncRegisterActivity> getPncRegisterActivityClass() {
        return PncRegisterActivity.class;
    }

    public PncMemberProfileContract.Presenter pncMemberProfilePresenter() {
        return new PncMemberProfilePresenter(this, new PncMemberProfileInteractor(this), MEMBER_OBJECT);
    }

    private CommonPersonObjectClient clientObject() {
        CommonRepository commonRepository = org.smartregister.chw.util.Utils.context().commonrepository(org.smartregister.chw.util.Utils.metadata().familyMemberRegister.tableName);
        final CommonPersonObject commonPersonObject = commonRepository.findByBaseEntityId(MEMBER_OBJECT.getBaseEntityId());
        final CommonPersonObjectClient client =
                new CommonPersonObjectClient(commonPersonObject.getCaseId(), commonPersonObject.getDetails(), "");
        client.setColumnmaps(commonPersonObject.getColumnmaps());
        return client;
    }

    private PncVisitAlertRule getVisitDetails() {
        return ((PncMemberProfileInteractor) pncMemberProfileInteractor).getVisitSummary(MEMBER_OBJECT.getBaseEntityId());
    }

    private void setEditViews(boolean enable, boolean within24Hours, Long longDate) {
        if (enable) {
            if (within24Hours) {
                Calendar cal = Calendar.getInstance();
                int offset = cal.getTimeZone().getOffset(cal.getTimeInMillis());
                new Date(longDate - (long) offset);
                String pncDay = pncMemberProfileInteractor.getPncDay(MEMBER_OBJECT.getBaseEntityId());
                layoutNotRecordView.setVisibility(View.VISIBLE);
                tvEdit.setVisibility(View.VISIBLE);
                textViewUndo.setVisibility(View.GONE);
                textViewNotVisitMonth.setVisibility(View.VISIBLE);
                textViewNotVisitMonth.setText(MessageFormat.format(getContext().getString(R.string.pnc_visit_done), pncDay));
                imageViewCross.setImageResource(R.drawable.activityrow_visited);
                textview_record_visit.setVisibility(View.GONE);
            } else {
                layoutNotRecordView.setVisibility(View.GONE);

            }
        } else {
            layoutNotRecordView.setVisibility(View.GONE);
        }

    }

    @Override
    public void openMedicalHistory() {
        PncMedicalHistoryActivity.startMe(this, MEMBER_OBJECT);
    }

    @Override
    protected void registerPresenter() {
        presenter = new PncMemberProfilePresenter(this, new PncMemberProfileInteractor(this), MEMBER_OBJECT);
    }

    @Override
    public void initializeFloatingMenu() {
        baseAncFloatingMenu = new AncFloatingMenu(this, getAncWomanName(),
                MEMBER_OBJECT.getPhoneNumber(), getFamilyHeadName(), getFamilyHeadPhoneNumber(), getProfileType());

        OnClickFloatingMenu onClickFloatingMenu = viewId -> {
            switch (viewId) {
                case R.id.anc_fab:
                    redrawFabWithNoPhone();
                    ((AncFloatingMenu) baseAncFloatingMenu).animateFAB();
                    break;
                case R.id.call_layout:
                    ((AncFloatingMenu) baseAncFloatingMenu).launchCallWidget();
                    ((AncFloatingMenu) baseAncFloatingMenu).animateFAB();
                    break;
                case R.id.refer_to_facility_layout:
                    pncMemberProfilePresenter().startPncReferralForm();
                    ((AncFloatingMenu) baseAncFloatingMenu).animateFAB();
                    break;
                default:
                    Timber.d("Unknown fab action");
                    break;
            }

        };

        ((AncFloatingMenu) baseAncFloatingMenu).setFloatMenuClickListener(onClickFloatingMenu);
        baseAncFloatingMenu.setGravity(Gravity.BOTTOM | Gravity.END);
        addContentView(baseAncFloatingMenu, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void openVisitMonthView() {
        return;
    }

    @Override
    public void openUpcomingService() {
        PncUpcomingServicesActivity.startMe(this, MEMBER_OBJECT);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);

        switch (view.getId()) {
            case R.id.textview_record_visit:
            case R.id.textview_record_reccuring_visit:
                PncHomeVisitActivity.startMe(this, MEMBER_OBJECT, false);
                break;
            case R.id.textview_edit:
                PncHomeVisitActivity.startMe(this, MEMBER_OBJECT, true);
                break;
            default:
                break;

        }
    }

    private void redrawFabWithNoPhone() {
        ((AncFloatingMenu) baseAncFloatingMenu).redraw(!StringUtils.isBlank(MEMBER_OBJECT.getPhoneNumber())
                || !StringUtils.isBlank(getFamilyHeadPhoneNumber()));
    }

    @Override
    public void startFormActivity(JSONObject formJson) {
        startActivityForResult(CoreJsonFormUtils.getJsonIntent(this, formJson, Utils.metadata().familyMemberFormActivity),
                JsonFormUtils.REQUEST_CODE_GET_JSON);
    }
}