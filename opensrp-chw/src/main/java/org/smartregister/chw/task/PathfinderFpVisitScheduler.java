package org.smartregister.chw.task;

import com.adosa.opensrp.chw.fp.dao.PathfinderFpDao;
import com.adosa.opensrp.chw.fp.domain.PathfinderFpMemberObject;
import com.adosa.opensrp.chw.fp.util.PathfinderFamilyPlanningConstants;

import org.jeasy.rules.api.Rules;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.contract.ScheduleTask;
import org.smartregister.chw.core.domain.BaseScheduleTask;
import org.smartregister.chw.core.rule.PathfinderFpAlertRule;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.FpUtil;
import org.smartregister.chw.core.utils.PathfinderFamilyPlanningUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PathfinderFpVisitScheduler extends BaseTaskExecutor {

    @Override
    public void resetSchedule(String baseEntityID, String scheduleName) {
        super.resetSchedule(baseEntityID, scheduleName);
        ChwApplication.getInstance().getScheduleRepository().deleteScheduleByGroup(getScheduleGroup(), baseEntityID);
    }

    @Override
    public List<ScheduleTask> generateTasks(String baseEntityID, String eventName, Date eventDate) {
        PathfinderFpMemberObject pathfinderFpMemberObject = PathfinderFpDao.getMember(baseEntityID);
        String fpMethod = pathfinderFpMemberObject.getFpMethod();
        String fp_date = pathfinderFpMemberObject.getFpStartDate();
        Integer fp_pillCycles = 1;
        Rules rule = PathfinderFamilyPlanningUtil.getFpRules(pathfinderFpMemberObject.getFpMethod());
        BaseScheduleTask baseScheduleTask = prepareNewTaskObject(baseEntityID);

        if (fp_date == null)
            return new ArrayList<>();
        Date lastVisitDate = null;
        Visit lastVisit;
        lastVisit = PathfinderFpDao.getLatestFpVisit(baseEntityID, PathfinderFamilyPlanningConstants.EventType.GIVE_FAMILY_PLANNING_METHOD, fpMethod);
        if (lastVisit == null) {
            lastVisit = PathfinderFpDao.getLatestFpVisit(baseEntityID, PathfinderFamilyPlanningConstants.EventType.FAMILY_PLANNING_REGISTRATION, fpMethod);
        }
        if (lastVisit != null) {
            lastVisitDate = lastVisit.getDate();
        }
        PathfinderFpAlertRule fpAlertRule = PathfinderFamilyPlanningUtil.getFpVisitStatus(rule, lastVisitDate, FpUtil.parseFpStartDate(pathfinderFpMemberObject.getFpStartDate()), fp_pillCycles, pathfinderFpMemberObject.getFpMethod());

        baseScheduleTask.setScheduleDueDate(fpAlertRule.getDueDate());
        baseScheduleTask.setScheduleExpiryDate(fpAlertRule.getExpiryDate());
        baseScheduleTask.setScheduleCompletionDate(fpAlertRule.getCompletionDate());
        baseScheduleTask.setScheduleOverDueDate(fpAlertRule.getOverDueDate());

        return toScheduleList(baseScheduleTask);
    }

    @Override
    public String getScheduleName() {
        return CoreConstants.SCHEDULE_TYPES.FP_VISIT;
    }

    @Override
    public String getScheduleGroup() {
        return CoreConstants.SCHEDULE_GROUPS.HOME_VISIT;
    }
}
