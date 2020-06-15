package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.Intent;

import com.adosa.opensrp.chw.fp.domain.PathfinderFpMemberObject;
import com.adosa.opensrp.chw.fp.util.PathfinderFamilyPlanningConstants;

import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.core.activity.CorePathfinderFollowupVisitActivity;
import org.smartregister.chw.core.presenter.CorePathfinderFpFollowupVisitPresenter;
import org.smartregister.chw.core.task.RunnableTask;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.interactor.PathfinderFpPregnancyScreeningInteractor;
import org.smartregister.chw.schedulers.ChwScheduleTaskExecutor;

import java.text.MessageFormat;
import java.util.Date;

public class PathfinderFamilyPlanningPregnancyScreeningActivity extends CorePathfinderFollowupVisitActivity {

    public static void startMe(Activity activity, PathfinderFpMemberObject memberObject, Boolean isEditMode) {
        Intent intent = new Intent(activity, PathfinderFamilyPlanningPregnancyScreeningActivity.class);
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ANC_MEMBER_OBJECTS.MEMBER_PROFILE_OBJECT, toMember(memberObject));
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ANC_MEMBER_OBJECTS.EDIT_MODE, isEditMode);
        activity.startActivityForResult(intent, CoreConstants.ProfileActivityResults.CHANGE_COMPLETED);
    }

    private static MemberObject toMember(PathfinderFpMemberObject memberObject) {
        MemberObject res = new MemberObject();
        res.setBaseEntityId(memberObject.getBaseEntityId());
        res.setFirstName(memberObject.getFirstName());
        res.setLastName(memberObject.getLastName());
        res.setMiddleName(memberObject.getMiddleName());
        res.setDob(memberObject.getAge());
        return res;
    }

    @Override
    public void redrawHeader(MemberObject memberObject) {
        tvTitle.setText(MessageFormat.format("{0}, {1} \u00B7 {2}", memberObject.getFullName(), memberObject.getAge(), getString(com.adosa.opensrp.chw.fp.R.string.fp_pregnancy_screening)));
    }

    @Override
    protected void registerPresenter() {
        presenter = new CorePathfinderFpFollowupVisitPresenter(memberObject, this, new PathfinderFpPregnancyScreeningInteractor());
    }

    @Override
    public void submittedAndClose() {
        // recompute schedule
        Runnable runnable = () -> ChwScheduleTaskExecutor.getInstance().execute(memberObject.getBaseEntityId(), PathfinderFamilyPlanningConstants.EventType.FP_FOLLOW_UP_VISIT, new Date());
        org.smartregister.chw.util.Utils.startAsyncTask(new RunnableTask(runnable), null);
        super.submittedAndClose();
    }

}

