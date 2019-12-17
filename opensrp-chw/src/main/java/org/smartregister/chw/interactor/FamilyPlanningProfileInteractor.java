package org.smartregister.chw.interactor;

import android.content.Context;

import org.joda.time.LocalDate;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.model.BaseUpcomingService;
import org.smartregister.chw.core.dao.AncDao;
import org.smartregister.chw.core.dao.PNCDao;
import org.smartregister.chw.core.dao.VisitDao;
import org.smartregister.chw.core.interactor.CoreFamilyPlanningProfileInteractor;
import org.smartregister.chw.dao.FamilyDao;
import org.smartregister.chw.fp.contract.BaseFpProfileContract;
import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.dao.AbstractDao;
import org.smartregister.domain.Alert;
import org.smartregister.domain.AlertStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class FamilyPlanningProfileInteractor extends CoreFamilyPlanningProfileInteractor {
    private Context context;

    public FamilyPlanningProfileInteractor(Context context) {
        this.context = context;
    }

    @Override
    public void updateProfileFpStatusInfo(final FpMemberObject memberObject, final BaseFpProfileContract.InteractorCallback callback) {
        Runnable runnable = new Runnable() {

            Date lastVisitDate = getLastVisitDate(memberObject); // CoreFamilyPlanningProfileInteractor#getLastVisitDate
            AlertStatus familyAlert = FamilyDao.getFamilyAlertStatus(memberObject.getBaseEntityId());
            Alert upcomingService = getAlerts(context, memberObject.getBaseEntityId());

            @Override
            public void run() {
                appExecutors.mainThread().execute(() -> {
                    callback.refreshLastVisit(lastVisitDate);
                    callback.refreshFamilyStatus(familyAlert);
                    callback.refreshMedicalHistory(VisitDao.memberHasNonFamilyPlanningVisits(memberObject.getBaseEntityId()));
                    if (upcomingService == null) {
                        callback.refreshUpComingServicesStatus("", AlertStatus.complete, new Date());
                    } else {
                        callback.refreshUpComingServicesStatus(upcomingService.scheduleName(), upcomingService.status(), new LocalDate(upcomingService.startDate()).toDate());
                    }
                });
            }
        };
        appExecutors.diskIO().execute(runnable);
    }

    private Alert getAlerts(Context context, String baseEntityID) {
        List<BaseUpcomingService> baseUpcomingServices = new ArrayList<>();
        try {
            baseUpcomingServices.addAll(new PncUpcomingServicesInteractorFlv().getMemberServices(context, PNCDao.getMember(baseEntityID)));
            baseUpcomingServices.addAll(new AncUpcomingServicesInteractorFlv().getMemberServices(context, AncDao.getMember(baseEntityID)));
            //TODO :: Add upcoming services for malaria, child & family planning
            if (baseUpcomingServices.size() > 0) {
                Comparator<BaseUpcomingService> comparator = (o1, o2) -> o1.getServiceDate().compareTo(o2.getServiceDate());
                Collections.sort(baseUpcomingServices, comparator);

                BaseUpcomingService baseUpcomingService = baseUpcomingServices.get(0);
                return new Alert(
                        baseEntityID,
                        baseUpcomingService.getServiceName(),
                        baseUpcomingService.getServiceName(),
                        baseUpcomingService.getServiceDate().before(new Date()) ? AlertStatus.urgent : AlertStatus.normal,
                        AbstractDao.getDobDateFormat().format(baseUpcomingService.getServiceDate()),
                        "",
                        true
                );
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        return null;
    }
}
