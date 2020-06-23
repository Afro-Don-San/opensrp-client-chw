package org.smartregister.chw.interactor;

import com.adosa.opensrp.chw.fp.util.PathfinderFamilyPlanningConstants;

import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.core.interactor.CorePathfinderFpFollowUpVisitInteractor;

import java.util.LinkedHashMap;
import java.util.Map;

import timber.log.Timber;

public class PathfinderRiskAssessmentInteractor extends CorePathfinderFpFollowUpVisitInteractor {

    private Flavor flavor = new PathfinderRiskAssessmentInteractorFlv();

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
    protected String getEncounterType() {
        return "Risk assessment, HIV testing & Dual protection counseling";
    }

}
