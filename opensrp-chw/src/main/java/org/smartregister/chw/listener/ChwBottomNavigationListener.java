package org.smartregister.chw.listener;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import org.smartregister.chw.R;
import org.smartregister.chw.activity.FamilyRegisterActivity;
import org.smartregister.chw.activity.JobAidsActivity;
import org.smartregister.chw.activity.ReportsActivity;
import org.smartregister.chw.core.listener.CoreBottomNavigationListener;
import org.smartregister.view.activity.BaseRegisterActivity;

public class ChwBottomNavigationListener extends CoreBottomNavigationListener {
    private Activity context;

    public ChwBottomNavigationListener(Activity context) {
        super(context);
        this.context = context;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//        super.onNavigationItemSelected(item);

        if (item.getItemId() == R.id.action_family) {
            if (context instanceof FamilyRegisterActivity) {
                BaseRegisterActivity baseRegisterActivity = (BaseRegisterActivity) context;
                baseRegisterActivity.switchToBaseFragment();
            } else {
                Intent intent = new Intent(context, FamilyRegisterActivity.class);
                context.startActivity(intent);
                context.finish();
            }
        } else if (item.getItemId() == R.id.action_scan_qr) {
            BaseRegisterActivity baseRegisterActivity = (BaseRegisterActivity) context;
            baseRegisterActivity.startQrCodeScanner();
            return false;
        } else if (item.getItemId() == R.id.action_register) {

            if (context instanceof FamilyRegisterActivity) {
                BaseRegisterActivity baseRegisterActivity = (BaseRegisterActivity) context;
                baseRegisterActivity.startRegistration();
            } else {
                FamilyRegisterActivity.startFamilyRegisterForm(context);
            }

            return false;
        } else if (item.getItemId() == R.id.action_job_aids) {
            //view.setSelectedItemId(R.id.action_family);
            Intent intent = new Intent(context, JobAidsActivity.class);
            context.startActivity(intent);
            return false;
        } else if (item.getItemId() == R.id.action_report) {
            //view.setSelectedItemId(R.id.action_family);
            Intent intent = new Intent(context, ReportsActivity.class);
            context.startActivity(intent);
            return false;
        }

        return true;
    }
}
