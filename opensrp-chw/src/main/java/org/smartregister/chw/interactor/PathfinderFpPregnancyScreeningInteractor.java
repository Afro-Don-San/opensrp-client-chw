package org.smartregister.chw.interactor;

import android.content.Context;

import com.adosa.opensrp.chw.fp.util.PathfinderFamilyPlanningConstants;

import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.core.dao.PNCDao;
import org.smartregister.chw.core.interactor.CorePathfinderFpFollowUpVisitInteractor;

import java.util.LinkedHashMap;
import java.util.Map;

import timber.log.Timber;

public class PathfinderFpPregnancyScreeningInteractor extends CorePathfinderFpFollowUpVisitInteractor {

    private Flavor flavor = new PathfinderPathfinderFpPregnancyScreeningInteractorFlv();

    @Override
    public void calculateActions(final BaseAncHomeVisitContract.View view, final MemberObject memberObject, final BaseAncHomeVisitContract.InteractorCallBack callBack) {

        final Runnable runnable = () -> {
            // update the local database incase of manual date adjustment
            try {
                VisitUtils.processVisits(memberObject.getBaseEntityId());
            } catch (Exception e) {
                Timber.e(e);
            }

            final LinkedHashMap<String, BaseAncHomeVisitAction> actionList = new LinkedHashMap<>();
            flavor.setContext(view.getContext());
            try {
                for (Map.Entry<String, BaseAncHomeVisitAction> entry : flavor.calculateActions(view, memberObject, callBack).entrySet()) {
                    actionList.put(entry.getKey(), entry.getValue());
                }
            } catch (BaseAncHomeVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public MemberObject getMemberClient(String memberID) {
        // read all the member details from the database
        return PNCDao.getMember(memberID);
    }

    @Override
    public void submitVisit(boolean editMode, String memberID, Map<String, BaseAncHomeVisitAction> map, BaseAncHomeVisitContract.InteractorCallBack callBack) {
        super.submitVisit(editMode, memberID, map, callBack);
    }

    @Override
    protected String getEncounterType() {
        return PathfinderFamilyPlanningConstants.EventType.FAMILY_PLANNING_PREGNANCY_SCREENING;
    }

    public interface Flavor {
        void setContext(Context context);

        LinkedHashMap<String, BaseAncHomeVisitAction> calculateActions(final BaseAncHomeVisitContract.View view, MemberObject memberObject, final BaseAncHomeVisitContract.InteractorCallBack callBack) throws BaseAncHomeVisitAction.ValidationException;
    }
}
