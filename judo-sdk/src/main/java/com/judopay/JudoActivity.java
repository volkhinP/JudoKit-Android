package com.judopay;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.WindowManager;

import com.judopay.error.RootedDeviceNotPermittedError;

import static com.judopay.Judo.JUDO_OPTIONS;
import static com.judopay.arch.TextUtil.isEmpty;
import static com.judopay.arch.ThemeUtil.getStringAttr;

/**
 * Base Activity class from which all other Activities should extend from.
 * This class provides two main functions:
 * <ol>
 * <li>Detect if the device is rooted, and throws a {@link RootedDeviceNotPermittedError},
 * preventing further access since we cannot guarantee the payment transaction will be secure.</li>
 * <li>Shows the back button in the action bar, allowing the user to navigate back easily.</li>
 * </ol>
 */
abstract class JudoActivity extends AppCompatActivity {
    protected static final String TAG_JUDO_FRAGMENT = "JudoFragment";

    protected JudoFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.fragment = (JudoFragment) getSupportFragmentManager().findFragmentByTag(TAG_JUDO_FRAGMENT);
        Judo judo = getIntent().getParcelableExtra(JUDO_OPTIONS);

        if (RootDetector.isRooted() && !judo.isRootedDevicesAllowed()) {
            throw new RootedDeviceNotPermittedError();
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void setProgressListener(ProgressListener progressListener) {
        fragment.setProgressListener(progressListener);
    }

    public boolean isTransactionInProgress() {
        return fragment.isTransactionInProgress();
    }

    @Override
    public void onBackPressed() {
        if (fragment != null && !fragment.isTransactionInProgress()) {
            setResult(Judo.RESULT_CANCELED);
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Judo.JUDO_REQUEST) {
            setResult(resultCode, data);
            finish();
        }
    }

    @Override
    public void setTitle(@StringRes int titleId) {
        String activityTitle = getStringAttr(this, R.attr.activityTitle);
        if (!isEmpty(activityTitle)) {
            super.setTitle(activityTitle);
        } else {
            super.setTitle(titleId);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
