package org.smartregister.chw.presenter;

import android.app.Activity;

import com.adosa.opensrp.chw.fp.contract.BaseFpProfileContract;
import com.adosa.opensrp.chw.fp.domain.PathfinderFpMemberObject;

import org.smartregister.chw.activity.PathfinderFamilyPlanningMemberProfileActivity;
import org.smartregister.chw.core.presenter.CorePathfinderFamilyPlanningMemberProfilePresenter;
import org.smartregister.chw.model.ReferralTypeModel;
import org.smartregister.chw.util.Utils;
import org.smartregister.util.FormUtils;

import java.lang.ref.WeakReference;
import java.util.List;

public class PathfinderFamilyPlanningMemberProfilePresenter extends CorePathfinderFamilyPlanningMemberProfilePresenter implements org.smartregister.chw.contract.AncMemberProfileContract.Presenter {
    private BaseFpProfileContract.Interactor interactor;
    private WeakReference<BaseFpProfileContract.View> view;
    private FormUtils formUtils;
    private PathfinderFpMemberObject fpMemberObject;

    public PathfinderFamilyPlanningMemberProfilePresenter(BaseFpProfileContract.View view, BaseFpProfileContract.Interactor interactor, PathfinderFpMemberObject fpMemberObject) {
        super(view, interactor, fpMemberObject);
        this.interactor = interactor;
        this.view = new WeakReference<>(view);
        this.fpMemberObject = fpMemberObject;
    }


    @Override
    public void referToFacility() {
        List<ReferralTypeModel> referralTypeModels = ((PathfinderFamilyPlanningMemberProfileActivity) getView()).getReferralTypeModels();
        if (referralTypeModels.size() == 1) {
            startFamilyPlanningReferral();
        } else {
            Utils.launchClientReferralActivity((Activity) getView(), referralTypeModels, fpMemberObject.getBaseEntityId());
        }
    }
}
