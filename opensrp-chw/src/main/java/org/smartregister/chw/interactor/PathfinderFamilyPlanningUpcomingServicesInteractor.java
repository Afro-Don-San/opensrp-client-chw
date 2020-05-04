package org.smartregister.chw.interactor;

import android.content.Context;

import org.jeasy.rules.api.Rules;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.interactor.BaseAncUpcomingServicesInteractor;
import org.smartregister.chw.anc.model.BaseUpcomingService;
import com.adosa.opensrp.chw.fp.dao.FpDao;
import com.adosa.opensrp.chw.fp.domain.FpAlertObject;
import com.adosa.opensrp.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.chw.rules.FpAlertRule;
import org.smartregister.chw.util.PathfinderFamilyPlanningUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class PathfinderFamilyPlanningUpcomingServicesInteractor extends BaseAncUpcomingServicesInteractor {
    protected MemberObject memberObject;
    protected Context context;

    @Override
    public List<BaseUpcomingService> getMemberServices(Context context, MemberObject memberObject) {
        this.memberObject = memberObject;
        this.context = context;
        List<BaseUpcomingService> serviceList = new ArrayList<>();
        evaluateFp(serviceList);
        return serviceList;
    }

    private void evaluateFp(List<BaseUpcomingService> serviceList) {
        String fpMethod;
        String fp_date = null;
        Integer fp_pillCycles = null;
        Rules rule = null;
        Date serviceDueDate = null;
        Date serviceOverDueDate = null;
        String serviceName = null;
        String fpMethodUsed = null;
        List<FpAlertObject> familyPlanningList = FpDao.getFpDetails(memberObject.getBaseEntityId());
        if (familyPlanningList.size() > 0) {
            for (FpAlertObject familyPlanning : familyPlanningList) {
                fpMethodUsed = familyPlanning.getFpMethod();
                fp_date = familyPlanning.getFpStartDate();
                fp_pillCycles = FpDao.getLastPillCycle(memberObject.getBaseEntityId(), fpMethodUsed);
                rule = PathfinderFamilyPlanningUtil.getFpRules(fpMethodUsed);
            }
        }
        fpMethod = PathfinderFamilyPlanningUtil.getTranslatedMethodValue(fpMethodUsed, context);
        Date lastVisitDate = null;
        Visit lastVisit;
        Date fpDate = PathfinderFamilyPlanningUtil.parseFpStartDate(fp_date);
        if (fpMethodUsed.equalsIgnoreCase(FamilyPlanningConstants.DBConstants.FP_INJECTABLE)) {
            lastVisit = FpDao.getLatestInjectionVisit(memberObject.getBaseEntityId(), fpMethodUsed);
        } else {
            lastVisit = FpDao.getLatestFpVisit(memberObject.getBaseEntityId(), FamilyPlanningConstants.EventType.FP_FOLLOW_UP_VISIT, fpMethodUsed);
        }
        if (lastVisit != null) {
            lastVisitDate = lastVisit.getDate();
        } else {
            lastVisit = FpDao.getLatestFpVisit(memberObject.getBaseEntityId(), FamilyPlanningConstants.EventType.FAMILY_PLANNING_REGISTRATION, fpMethodUsed);
            lastVisitDate = lastVisit.getDate();
        }
        FpAlertRule alertRule = PathfinderFamilyPlanningUtil.getFpVisitStatus(rule, lastVisitDate, fpDate, fp_pillCycles, fpMethod);
        if (fpMethodUsed.equalsIgnoreCase(FamilyPlanningConstants.DBConstants.FP_COC) || fpMethodUsed.equalsIgnoreCase(FamilyPlanningConstants.DBConstants.FP_POP) ||
                fpMethodUsed.equalsIgnoreCase(FamilyPlanningConstants.DBConstants.FP_MALE_CONDOM) || fpMethodUsed.equalsIgnoreCase(FamilyPlanningConstants.DBConstants.FP_FEMALE_CONDOM) || fpMethodUsed.equalsIgnoreCase(FamilyPlanningConstants.DBConstants.FP_INJECTABLE)) {
            serviceDueDate = alertRule.getDueDate();
            serviceOverDueDate = alertRule.getOverDueDate();
            if (fpMethodUsed.equalsIgnoreCase(FamilyPlanningConstants.DBConstants.FP_INJECTABLE)) {
                serviceName = fpMethod;
            } else {
                serviceName = MessageFormat.format(context.getString(R.string.refill), fpMethod);
            }
        } //TODO coze  handle this
//        else if (fpMethodUsed.equalsIgnoreCase(FamilyPlanningConstants.DBConstants.FP_IUCD) || fpMethodUsed.equalsIgnoreCase(FamilyPlanningConstants.DBConstants.FP_FEMALE_STERLIZATION)) {
//            if (lastVisit == null) {
//                serviceDueDate = alertRule.getDueDate();
//                serviceOverDueDate = alertRule.getOverDueDate();
//                serviceName = MessageFormat.format(context.getString(R.string.follow_up_one), fpMethod);
//            } else {
//                if (fpMethodUsed.equalsIgnoreCase(FamilyPlanningConstants.DBConstants.FP_IUCD)) {
//                    serviceDueDate = (DateTimeFormat.forPattern("dd-MM-yyyy").parseLocalDate(fp_date).plusMonths(4)).toDate();
//                    serviceOverDueDate = (DateTimeFormat.forPattern("dd-MM-yyyy").parseLocalDate(fp_date).plusMonths(4).plusWeeks(1)).toDate();
//                    serviceName = MessageFormat.format(context.getString(R.string.follow_up_two), fpMethod);
//                } else {
//                    count = FpDao.getCountFpVisits(memberObject.getBaseEntityId(), FamilyPlanningConstants.EventType.FP_FOLLOW_UP_VISIT, fpMethod);
//                    if (count == 2) {
//                        serviceDueDate = (DateTimeFormat.forPattern("dd-MM-yyyy").parseLocalDate(fp_date).plusMonths(1)).toDate();
//                        serviceOverDueDate = (DateTimeFormat.forPattern("dd-MM-yyyy").parseLocalDate(fp_date).plusMonths(1).plusWeeks(2)).toDate();
//                        serviceName = MessageFormat.format(context.getString(R.string.follow_up_three), fpMethod);
//                    } else {
//                        serviceDueDate = (DateTimeFormat.forPattern("dd-MM-yyyy").parseLocalDate(fp_date).plusDays(7)).toDate();
//                        serviceOverDueDate = (DateTimeFormat.forPattern("dd-MM-yyyy").parseLocalDate(fp_date).plusDays(9)).toDate();
//                        serviceName = MessageFormat.format(context.getString(R.string.follow_up_two), fpMethod);
//                    }
//                }
//            }
//        }

        BaseUpcomingService upcomingService = new BaseUpcomingService();
        if (serviceName != null) {
            upcomingService.setServiceDate(serviceDueDate);
            upcomingService.setOverDueDate(serviceOverDueDate);
            upcomingService.setServiceName(serviceName);
            serviceList.add(upcomingService);
        }

    }

}
