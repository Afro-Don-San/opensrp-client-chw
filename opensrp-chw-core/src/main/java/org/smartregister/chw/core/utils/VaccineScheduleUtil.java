package org.smartregister.chw.core.utils;

import android.content.Context;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.joda.time.DateTime;
import org.smartregister.chw.anc.domain.VaccineDisplay;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.dao.AlertDao;
import org.smartregister.chw.core.model.VaccineTaskModel;
import org.smartregister.domain.Alert;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.domain.jsonmapping.VaccineGroup;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.util.VaccinateActionUtils;
import org.smartregister.immunization.util.VaccinatorUtils;
import org.smartregister.service.AlertService;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class VaccineScheduleUtil {

    private VaccineScheduleUtil() {

    }

    public static VaccineWrapper getVaccineWrapper(VaccineRepo.Vaccine vaccine, VaccineTaskModel vaccineTaskModel) {
        VaccineWrapper vaccineWrapper = new VaccineWrapper();
        vaccineWrapper.setVaccine(vaccine);
        vaccineWrapper.setName(vaccine.display());
        vaccineWrapper.setDbKey(getVaccineId(vaccine.display(), vaccineTaskModel));
        vaccineWrapper.setDefaultName(vaccine.display());
        vaccineWrapper.setAlert(vaccineTaskModel.getAlertsMap().get(vaccine.display()));
        return vaccineWrapper;
    }

    private static Long getVaccineId(String vaccineName, VaccineTaskModel vaccineTaskModel) {
        for (Vaccine vaccine : vaccineTaskModel.getVaccines()) {
            if (vaccine.getName().equalsIgnoreCase(vaccineName)) {
                return vaccine.getId();
            }
        }
        return null;
    }

    // vaccine utils
    public static Triple<DateTime, VaccineRepo.Vaccine, String> getIndividualVaccine(VaccineTaskModel vaccineTaskModel, String type) {
        // compute the due date
        Map<String, Object> map = null;
        for (Map<String, Object> mapVac : vaccineTaskModel.getScheduleList()) {
            VaccineRepo.Vaccine myVac = (VaccineRepo.Vaccine) mapVac.get("vaccine");
            String status = (String) mapVac.get("status");
            if (myVac != null && myVac.display().toLowerCase().contains(type.toLowerCase()) && status != null && status.equals("due")) {
                map = mapVac;
                break;
            }
        }

        if (map == null) {
            return null;
        }

        DateTime date = (DateTime) map.get("date");
        VaccineRepo.Vaccine vaccine = (VaccineRepo.Vaccine) map.get("vaccine");
        if (vaccine == null || date == null) {
            return null;
        }
        String vc_count = vaccine.name().substring(vaccine.name().length() - 1);

        return Triple.of(date, vaccine, vc_count);
    }

    /**
     * gets vaccines for the woman
     *
     * @param baseEntityID
     * @param anchorDate
     * @param notDoneVaccines
     * @return
     */
    public static VaccineTaskModel getWomanVaccine(String baseEntityID, DateTime anchorDate, List<VaccineWrapper> notDoneVaccines) {
        return getLocalUpdatedVaccines(baseEntityID, anchorDate, notDoneVaccines, CoreConstants.SERVICE_GROUPS.WOMAN);
    }

    /**
     * Updates locals vaccines grouped by type and deducts all those not given
     * Returns a vaccines summary object that can be used for immunization
     *
     * @param baseEntityID
     * @param anchorDate
     * @param inMemoryVaccines (this array contains the vaccines that should be treated as received but yet to be persisted in memory)
     * @param vaccineGroupName
     * @return
     */
    public static VaccineTaskModel getLocalUpdatedVaccines(String baseEntityID, DateTime anchorDate, List<VaccineWrapper> inMemoryVaccines, String vaccineGroupName) {
        AlertService alertService = CoreChwApplication.getInstance().getContext().alertService();
        VaccineRepository vaccineRepository = CoreChwApplication.getInstance().vaccineRepository();

        // updates the local vaccines and services for the mother
        VaccineScheduleUtil.updateOfflineAlerts(baseEntityID, anchorDate, vaccineGroupName);
        ChwServiceSchedule.updateOfflineAlerts(baseEntityID, anchorDate, vaccineGroupName); // get services

        // retrieve related information from the local db
        List<Alert> alerts = alertService.findByEntityIdAndAlertNames(baseEntityID, VaccinateActionUtils.allAlertNames(vaccineGroupName)); // list of all alerts
        List<Vaccine> vaccines = vaccineRepository.findByEntityId(baseEntityID); // add vaccines given to the user
        Map<String, Date> receivedVaccines = VaccinatorUtils.receivedVaccines(vaccines); // groups vaccines and date received

        // add vaccines not done to list of processed vaccines (eliminate these vaccines from the next group)
        if (inMemoryVaccines != null) {
            for (int i = 0; i < inMemoryVaccines.size(); i++) {
                receivedVaccines.put(inMemoryVaccines.get(i).getName().toLowerCase(), new Date());
            }
        }

        List<Map<String, Object>> sch = VaccinatorUtils.generateScheduleList(vaccineGroupName, anchorDate, receivedVaccines, alerts);
        VaccineTaskModel vaccineTaskModel = new VaccineTaskModel();
        vaccineTaskModel.setVaccineGroupName(vaccineGroupName);
        vaccineTaskModel.setAnchorDate(anchorDate);
        vaccineTaskModel.setAlerts(alerts);
        vaccineTaskModel.setVaccines(vaccines);
        vaccineTaskModel.setReceivedVaccines(receivedVaccines);
        vaccineTaskModel.setScheduleList(sch);

        return vaccineTaskModel;
    }

    public static List<VaccineWrapper> recomputeSchedule(VaccineTaskModel vaccineTaskModel, List<VaccineDisplay> inMemoryVaccines) {
        List<VaccineWrapper> vaccineWrappers = new ArrayList<>();

        if (inMemoryVaccines != null) {
            for (VaccineDisplay vaccineDisplay : inMemoryVaccines) {
                if (vaccineDisplay.getValid())
                    vaccineTaskModel.getReceivedVaccines().put(vaccineDisplay.getVaccineWrapper().getName().toLowerCase(), vaccineDisplay.getDateGiven());
            }
        }

        List<Map<String, Object>> sch = VaccinatorUtils.generateScheduleList(
                vaccineTaskModel.getVaccineGroupName(),
                vaccineTaskModel.getAnchorDate(),
                vaccineTaskModel.getReceivedVaccines(),
                vaccineTaskModel.getAlerts()
        );
        vaccineTaskModel.setScheduleList(sch);


        for (org.smartregister.immunization.domain.jsonmapping.Vaccine vaccine : vaccineTaskModel.getGroupMap().vaccines) {
            Triple<DateTime, VaccineRepo.Vaccine, String> individualVaccine = VaccineScheduleUtil.getIndividualVaccine(vaccineTaskModel, vaccine.type);

            if (individualVaccine == null || individualVaccine.getLeft().isAfter(new DateTime())) {
                continue;
            }

            vaccineWrappers.add(VaccineScheduleUtil.getVaccineWrapper(individualVaccine.getMiddle(), vaccineTaskModel));
        }

        return vaccineWrappers;
    }

    /**
     * gets vaccines for the child
     *
     * @param baseEntityID
     * @param anchorDate
     * @param notDoneVaccines
     * @return
     */
    public static VaccineTaskModel getChildVaccine(String baseEntityID, DateTime anchorDate, List<VaccineWrapper> notDoneVaccines) {
        return getLocalUpdatedVaccines(baseEntityID, anchorDate, notDoneVaccines, CoreConstants.SERVICE_GROUPS.CHILD);
    }

    /**
     * Returns the supported type of vaccines. for either woman or child.
     * The function loads child vaccines by default when the vaccine type is not provided
     *
     * @param context
     * @param vaccineType
     * @return
     */
    public static Map<String, VaccineGroup> getVaccineNamedGroups(Context context, String vaccineType) {
        Map<String, VaccineGroup> groupedVaccines = new LinkedHashMap<>();

        for (VaccineGroup vg : getVaccineGroups(context, vaccineType)) {
            groupedVaccines.put(vg.name, vg);
        }

        return groupedVaccines;
    }

    /**
     * Returns the supported type of vaccines. for either woman or child.
     * The function loads child vaccines by default when the vaccine type is not provided
     *
     * @param context
     * @param vaccineType
     * @return
     */
    public static List<VaccineGroup> getVaccineGroups(Context context, String vaccineType) {
        return CoreConstants.SERVICE_GROUPS.WOMAN.equals(vaccineType) ?
                VaccinatorUtils.getSupportedWomanVaccines(context) :
                VaccinatorUtils.getSupportedVaccines(context);
    }

    /**
     * returns list of vaccines that are pending
     *
     * @param baseEntityID
     * @param dob
     * @param group
     * @return
     */
    public static List<VaccineWrapper> getChildDueVaccines(String baseEntityID, Date dob, int group) {
        Pair<VaccineTaskModel, List<VaccineWrapper>> res = getChildDueVaccines(baseEntityID, dob, new ArrayList<>(), group);
        return res.getRight();
    }

    /**
     * returns list of vaccines that are pending. you can add vaccine wrappers to exclude
     *
     * @param baseEntityID
     * @param dob
     * @param excludedVaccines
     * @param group
     * @return
     */
    public static Pair<VaccineTaskModel, List<VaccineWrapper>> getChildDueVaccines(String baseEntityID, Date dob, List<VaccineWrapper> excludedVaccines, int group) {
        List<VaccineWrapper> vaccineWrappers = new ArrayList<>();
        try {
            VaccineGroup groupMap = VaccineScheduleUtil.getVaccineGroups(CoreChwApplication.getInstance().getApplicationContext(), CoreConstants.SERVICE_GROUPS.CHILD).get(group);

            // get all vaccines that are not given
            VaccineTaskModel taskModel = VaccineScheduleUtil.getLocalUpdatedVaccines(baseEntityID, new DateTime(dob), excludedVaccines, CoreConstants.SERVICE_GROUPS.CHILD);
            taskModel.setGroupMap(groupMap);

            for (org.smartregister.immunization.domain.jsonmapping.Vaccine vaccine : groupMap.vaccines) {
                Triple<DateTime, VaccineRepo.Vaccine, String> individualVaccine = VaccineScheduleUtil.getIndividualVaccine(taskModel, vaccine.type);

                if (individualVaccine == null || individualVaccine.getLeft().isAfter(new DateTime())) {
                    continue;
                }

                vaccineWrappers.add(VaccineScheduleUtil.getVaccineWrapper(individualVaccine.getMiddle(), taskModel));
            }

            return Pair.of(taskModel, vaccineWrappers);
            //

        } catch (Exception e) {
            Timber.e(e);
        }
        return Pair.of(null, vaccineWrappers);
    }

    public static void updateOfflineAlerts(String baseEntityID, DateTime anchorDate, String vaccineGroupName) {
        // recompute offline alerts
        VaccineSchedule.updateOfflineAlerts(baseEntityID, anchorDate, vaccineGroupName);
        // delete all vaccine alerts that have been administered
        AlertDao.updateOfflineVaccineAlerts(baseEntityID);
    }
}