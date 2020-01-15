package org.smartregister.chw.service;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.dao.ScheduleDao;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.chw.schedulers.ChwScheduleTaskExecutor;
import org.smartregister.chw.util.WashCheckFlv;

import java.util.Date;
import java.util.List;

import timber.log.Timber;

/**
 * Recompute all schedules to adjust the new dates
 */
public class SchedulesIntentService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     * <p>
     * Used to name the worker thread, important only for debugging.
     */

    private Flavor flavor = new SchedulesIntentServiceFlv();

    public SchedulesIntentService() {
        super("SchedulesIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // execute all children schedules
        executeChildVisitSchedules();

        // execute all anc schedules
        executeAncVisitSchedules();

        // execute all pnc schedules
        executePncVisitSchedules();

        // execute all wash check
        executeWashCheckSchedules();

        // execute all fp schedules
        executeFpVisitSchedules();
    }

    private void executeChildVisitSchedules() {
        Timber.v("Computing child schedules");
        ChwApplication.getInstance().getScheduleRepository().deleteScheduleByName(CoreConstants.SCHEDULE_TYPES.CHILD_VISIT);
        List<String> baseEntityIDs = ScheduleDao.getActiveChildren();
        if (baseEntityIDs == null) return;

        for (String baseID : baseEntityIDs) {
            Timber.v("  Computing child schedules for %s", baseID);
            ChwScheduleTaskExecutor.getInstance().execute(baseID, CoreConstants.EventType.CHILD_HOME_VISIT, new Date());
        }
    }

    private void executeAncVisitSchedules() {
        Timber.v("Computing ANC schedules");
        ChwApplication.getInstance().getScheduleRepository().deleteScheduleByName(CoreConstants.SCHEDULE_TYPES.ANC_VISIT);
        List<String> baseEntityIDs = ScheduleDao.getActiveANCWomen();
        if (baseEntityIDs == null) return;

        for (String baseID : baseEntityIDs) {
            Timber.v("  Computing ANC schedules for %s", baseID);
            ChwScheduleTaskExecutor.getInstance().execute(baseID, CoreConstants.EventType.ANC_REGISTRATION, new Date());
        }
    }

    private void executePncVisitSchedules() {
        Timber.v("Computing PNC schedules");
        ChwApplication.getInstance().getScheduleRepository().deleteScheduleByName(CoreConstants.SCHEDULE_TYPES.PNC_VISIT);
        List<String> baseEntityIDs = ScheduleDao.getActivePNCWomen();
        if (baseEntityIDs == null) return;

        for (String baseID : baseEntityIDs) {
            Timber.v("  Computing PNC schedules for %s", baseID);
            ChwScheduleTaskExecutor.getInstance().execute(baseID, CoreConstants.EventType.PREGNANCY_OUTCOME, new Date());
        }
    }

    private void executeWashCheckSchedules() {
        WashCheckFlv flv = new WashCheckFlv();
        if (!flv.isWashCheckVisible()) return;

        Timber.v("Computing Wash Check schedules");
        ChwApplication.getInstance().getScheduleRepository().deleteScheduleByName(CoreConstants.SCHEDULE_TYPES.WASH_CHECK);
        List<String> baseEntityIDs = ScheduleDao.getActiveWashCheckFamilies();
        if (baseEntityIDs == null) return;

        for (String baseID : baseEntityIDs) {
            Timber.v("  Computing Wash Check schedules for %s", baseID);
            ChwScheduleTaskExecutor.getInstance().execute(baseID, CoreConstants.EventType.WASH_CHECK, new Date());
        }
    }

    private void executeFpVisitSchedules() {
        if (!flavor.hasFamilyPlanning()) return;

        Timber.v("Computing Fp schedules");
        ChwApplication.getInstance().getScheduleRepository().deleteScheduleByName(CoreConstants.SCHEDULE_TYPES.FP_VISIT);
        List<String> baseEntityIDs = ScheduleDao.getActiveFPWomen();
        if (baseEntityIDs == null) return;

        for (String baseID : baseEntityIDs) {
            Timber.v("  Computing Fp schedules for %s", baseID);
            ChwScheduleTaskExecutor.getInstance().execute(baseID, FamilyPlanningConstants.EventType.FAMILY_PLANNING_REGISTRATION, new Date());
        }
    }

    public interface Flavor {
        boolean hasFamilyPlanning();
    }

}
