package org.smartregister.chw.configs;

import android.database.Cursor;

import androidx.annotation.NonNull;

import org.smartregister.chw.R;
import org.smartregister.chw.core.configs.CoreAllClientsRegisterRowOptions;
import org.smartregister.chw.core.holders.CoreAllClientsRegisterViewHolder;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.opd.holders.OpdRegisterViewHolder;
import org.smartregister.view.contract.SmartRegisterClient;

import static com.adosa.opensrp.chw.fp.util.PathfinderFamilyPlanningConstants.DBConstants.GENDER;
import static org.smartregister.chw.fragment.AllClientsRegisterFragment.REGISTER_TYPE;

public class AllClientsRegisterRowOptions extends CoreAllClientsRegisterRowOptions {

    @Override
    public void populateClientRow(@NonNull Cursor cursor, @NonNull CommonPersonObjectClient commonPersonObjectClient, @NonNull SmartRegisterClient smartRegisterClient, @NonNull OpdRegisterViewHolder opdRegisterViewHolder) {
        if (opdRegisterViewHolder instanceof CoreAllClientsRegisterViewHolder) {
            CoreAllClientsRegisterViewHolder allClientsRegisterViewHolder = (CoreAllClientsRegisterViewHolder) opdRegisterViewHolder;
            String registerType = commonPersonObjectClient.getDetails().get(REGISTER_TYPE);
            String gender = commonPersonObjectClient.getDetails().get(GENDER);


            if (gender.equalsIgnoreCase("Male")) {
                String genderString = allClientsRegisterViewHolder.tvRegisterType.getContext().getResources().getString(
                        R.string.gender_male);
                allClientsRegisterViewHolder.textViewGender.setText(genderString);
            } else {
                String genderString = allClientsRegisterViewHolder.tvRegisterType.getContext().getResources().getString(
                        R.string.gender_female);
                allClientsRegisterViewHolder.textViewGender.setText(genderString);
            }


            if (registerType != null) {
                if (registerType.equals("Independent")) {
                    String independentClientTypeString = allClientsRegisterViewHolder.tvRegisterType.getContext().getResources().getString(
                            R.string.independent_client);
                    allClientsRegisterViewHolder.tvRegisterType.setText(independentClientTypeString);

                } else if (registerType.equals("Family Planning")) {
                    String registerTypeString = allClientsRegisterViewHolder.tvRegisterType.getContext().getResources().getString(
                            R.string.notification_type_family_planning);
                    allClientsRegisterViewHolder.tvRegisterType.setText(registerTypeString);
                }
            }
        }
    }
}
