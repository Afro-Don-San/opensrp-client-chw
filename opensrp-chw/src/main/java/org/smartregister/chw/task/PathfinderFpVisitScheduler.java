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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

import static com.adosa.opensrp.chw.fp.util.PathfinderFamilyPlanningConstants.EventType.FAMILY_PLANNING_PREGNANCY_SCREENING;
import static com.adosa.opensrp.chw.fp.util.PathfinderFamilyPlanningConstants.EventType.FAMILY_PLANNING_PREGNANCY_TEST_REFERRAL_FOLLOWUP;

public class PathfinderFpVisitScheduler extends BaseTaskExecutor {

    @Override
    public void resetSchedule(String baseEntityID, String scheduleName) {
        super.resetSchedule(baseEntityID, scheduleName);
        ChwApplication.getInstance().getScheduleRepository().deleteScheduleByGroup(getScheduleGroup(), baseEntityID);
    }

    @Override
    public List<ScheduleTask> generateTasks(String baseEntityID, String eventName, Date eventDate) {
        String fpMethod;
        String fp_date = null;
        Integer fp_pillCycles = null;
        Rules rule = null;
        Date serviceDueDate = null;
        Date serviceOverDueDate = null;
        Date serviceExpiryDate = null;
        Date serviceCompletionDate = null;
        String serviceName = null;
        PathfinderFpMemberObject pathfinderFpAlertObject = PathfinderFpDao.getMember(baseEntityID);

        fpMethod = pathfinderFpAlertObject.getFpMethod();
        fp_date = pathfinderFpAlertObject.getFpStartDate();
        fp_pillCycles = PathfinderFpDao.getLastPillCycle(baseEntityID, fpMethod);
        rule = PathfinderFamilyPlanningUtil.getFpRules(fpMethod);

        Date lastVisitDate;
        Visit lastVisit;

        Date fpDate = null;
        try {
            fpDate = PathfinderFamilyPlanningUtil.parseFpStartDate(fp_date);
        } catch (Exception e) {
            Timber.e(e);
        }

        if (fpDate == null) {
            try {
                fpDate = new Date(new BigDecimal(fp_date).longValue());
            } catch (Exception e) {
                Timber.e(e);
            }
        }

        lastVisit = PathfinderFpDao.getLatestFpVisit(baseEntityID, PathfinderFamilyPlanningConstants.EventType.FP_FOLLOW_UP_VISIT, fpMethod);
        if (lastVisit == null) {
            lastVisit = PathfinderFpDao.getLatestFpVisit(baseEntityID, PathfinderFamilyPlanningConstants.EventType.GIVE_FAMILY_PLANNING_METHOD, fpMethod);
        }

        if (lastVisit == null) {
            lastVisit = PathfinderFpDao.getLatestFpVisit(baseEntityID);
        }
        lastVisitDate = lastVisit.getDate();
        PathfinderFpAlertRule alertRule = PathfinderFamilyPlanningUtil.getFpVisitStatus(rule, lastVisitDate, fpDate, fp_pillCycles, fpMethod);
        if (!pathfinderFpAlertObject.getFpStartDate().equals("0") && !pathfinderFpAlertObject.getFpStartDate().equals("") && !fpMethod.isEmpty()) {

        } else if (!pathfinderFpAlertObject.getEdd().isEmpty() && pathfinderFpAlertObject.getPregnancyStatus().equals(PathfinderFamilyPlanningConstants.PregnancyStatus.PREGNANT)) {
            lastVisit = PathfinderFpDao.getLatestVisit(baseEntityID, FAMILY_PLANNING_PREGNANCY_SCREENING + "' OR visit_type = '" + FAMILY_PLANNING_PREGNANCY_TEST_REFERRAL_FOLLOWUP);
            lastVisitDate = lastVisit.getDate();
            alertRule = PathfinderFamilyPlanningUtil.getFpVisitStatus(PathfinderFamilyPlanningUtil.getPregnantWomenFpRules(), lastVisitDate, FpUtil.parseFpStartDate(pathfinderFpAlertObject.getEdd()), fp_pillCycles, fpMethod);
        } else if (pathfinderFpAlertObject.getPregnancyStatus().equals(PathfinderFamilyPlanningConstants.PregnancyStatus.NOT_UNLIKELY_PREGNANT)) {
            lastVisit = PathfinderFpDao.getLatestVisit(baseEntityID, FAMILY_PLANNING_PREGNANCY_SCREENING + "' OR visit_type = '" + FAMILY_PLANNING_PREGNANCY_TEST_REFERRAL_FOLLOWUP);
            lastVisitDate = lastVisit.getDate();

            if (pathfinderFpAlertObject.getChoosePregnancyTestReferral().equals(PathfinderFamilyPlanningConstants.ChoosePregnancyTestReferral.WAIT_FOR_NEXT_VISIT)) {
                alertRule = PathfinderFamilyPlanningUtil.getFpVisitStatus(PathfinderFamilyPlanningUtil.getPregnantScreeningFollowupRules(), lastVisitDate, lastVisitDate, 0, fpMethod);
            } else if (pathfinderFpAlertObject.isClientIsCurrentlyReferred()) {
                alertRule = PathfinderFamilyPlanningUtil.getFpVisitStatus(PathfinderFamilyPlanningUtil.getPregnantTestReferralFollowupRules(), lastVisitDate, lastVisitDate, 0, fpMethod);
            }
        } else if (pathfinderFpAlertObject.isManRequestedMethodForPartner()) {
            rule = PathfinderFamilyPlanningUtil.getManChosePartnersFpMethodFollowupRules();

            alertRule = PathfinderFamilyPlanningUtil.getFpVisitStatus(rule, lastVisitDate, FpUtil.parseFpStartDate(pathfinderFpAlertObject.getFpMethodChoiceDate()), fp_pillCycles, fpMethod);
        } else if (pathfinderFpAlertObject.isClientIsCurrentlyReferred()) {
            rule = PathfinderFamilyPlanningUtil.getReferralFollowupRules();
            alertRule = PathfinderFamilyPlanningUtil.getFpVisitStatus(rule, lastVisitDate, FpUtil.parseFpStartDate(pathfinderFpAlertObject.getFpStartDate()), fp_pillCycles, fpMethod);
        }


        BaseScheduleTask baseScheduleTask = prepareNewTaskObject(baseEntityID);
        baseScheduleTask.setScheduleDueDate(alertRule.getDueDate());
        baseScheduleTask.setScheduleExpiryDate(alertRule.getExpiryDate());
        baseScheduleTask.setScheduleCompletionDate(alertRule.getCompletionDate());
        baseScheduleTask.setScheduleOverDueDate(alertRule.getOverDueDate());

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
