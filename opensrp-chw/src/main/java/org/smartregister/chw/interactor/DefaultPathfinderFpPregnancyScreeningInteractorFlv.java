package org.smartregister.chw.interactor;

import android.content.Context;

import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;

import java.util.LinkedHashMap;

public class DefaultPathfinderFpPregnancyScreeningInteractorFlv implements PathfinderFpPregnancyScreeningInteractor.Flavor {
    protected Context context;

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public LinkedHashMap<String, BaseAncHomeVisitAction> calculateActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack) throws BaseAncHomeVisitAction.ValidationException {
        return null;
    }
}
