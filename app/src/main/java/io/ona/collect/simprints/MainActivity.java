package io.ona.collect.simprints;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.Identification;
import com.simprints.libsimprints.Registration;
import com.simprints.libsimprints.Verification;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getCanonicalName();
    private static final String INTENT_FIELD_GUID = "guid";
    private static final String INTENT_FIELD_CONFIDENCE = "confidence";
    private static final String INTENT_FIELD_TIER = "tier";
    private static final String ACTION_REGISTER = "io.ona.simprints.REGISTER";
    private static final String ACTION_VERIFY = "io.ona.simprints.VERIFY";
    private static final String ACTION_IDENTIFY = "io.ona.simprints.IDENTIFY";
    private static final String FIELD_SEPARATOR = " ";
    private static final int REQUEST_CODE_REGISTER = 1;
    private static final int REQUEST_CODE_VERIFY = 2;
    private static final int REQUEST_CODE_IDENTIFY = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startSimPrints(getIntent());
    }

    /**
     * Copies data from the provided intent into a new intent to fire the SimPrints app
     *
     * @param intent    The intent to copy data from
     */
    private void startSimPrints(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            Intent simPrintsIntent = new Intent();
            simPrintsIntent.putExtras(intent.getExtras());
            if (intent.getAction().equals(ACTION_REGISTER)) {
                simPrintsIntent.setAction(Constants.SIMPRINTS_REGISTER_INTENT);
                startActivityForResult(simPrintsIntent, REQUEST_CODE_REGISTER);
            } else if (intent.getAction().equals(ACTION_VERIFY)) {
                simPrintsIntent.setAction(Constants.SIMPRINTS_VERIFY_INTENT);
                startActivityForResult(simPrintsIntent, REQUEST_CODE_VERIFY);
            } else if (intent.getAction().equals(ACTION_IDENTIFY)) {
                simPrintsIntent.setAction(Constants.SIMPRINTS_IDENTIFY_INTENT);
                startActivityForResult(simPrintsIntent, REQUEST_CODE_IDENTIFY);
            }
        } else {
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Intent intent = new Intent();
        boolean cancelled = false;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_REGISTER:
                    cancelled = !handleRegistrationIntent(data, intent);
                    break;
                case REQUEST_CODE_VERIFY:
                    cancelled = !handleVerificationIntent(data, intent);
                    break;
                case REQUEST_CODE_IDENTIFY:
                    cancelled = !handleIdentificationIntent(data, intent);
                    break;
                default:
                    cancelled = true;
                    break;
            }
        } else {
            cancelled = true;
        }

        if (cancelled) {
            setResult(RESULT_CANCELED, intent);
        } else {
            setResult(RESULT_OK, intent);
        }

        finish();
    }

    /**
     * Copies over registration data from the input intent to the output intent in a way that can be
     * easily digested by Collect.
     *
     * @param inputIntent   Intent to copy data from. Should contain an {@link Registration} object
     * @param outputIntent  Intent to write data to
     * @return {@code true} if data was successfully copied
     */
    private boolean handleRegistrationIntent(Intent inputIntent, Intent outputIntent) {
        Registration registration =
                inputIntent.getParcelableExtra(Constants.SIMPRINTS_REGISTRATION);
        if (registration != null) {
            outputIntent.putExtra(INTENT_FIELD_GUID, registration.getGuid());

            return true;
        }

        return false;
    }

    /**
     * Copies over verification data from the input intent to the output intent in a way that can be
     * easily digested by Collect.
     *
     * @param inputIntent   Intent to copy data from. Should contain an {@link Verification} object
     * @param outputIntent  Intent to write data to
     * @return {@code true} if data was successfully copied
     */
    private boolean handleVerificationIntent(Intent inputIntent, Intent outputIntent) {
        Verification verification =
                inputIntent.getParcelableExtra(Constants.SIMPRINTS_VERIFICATION);
        if (verification != null) {
            outputIntent.putExtra(INTENT_FIELD_GUID, verification.getGuid());
            outputIntent.putExtra(INTENT_FIELD_CONFIDENCE,
                    String.valueOf(verification.getConfidence()));
            outputIntent.putExtra(INTENT_FIELD_TIER, verification.getTier().toString());

            return true;
        }

        return false;
    }

    /**
     * Copies over identification data from the input intent to the output intent in a way that can
     * be easily digested by Collect.
     *
     * @param inputIntent   Intent to copy data from. Should contain an {@link ArrayList} of
     *                      {@link Identification} objects
     * @param outputIntent  Intent to write data to
     * @return {@code true} if data was successfully copied
     */
    private boolean handleIdentificationIntent(Intent inputIntent, Intent outputIntent) {
        ArrayList<Identification> identifications = inputIntent
                .getParcelableArrayListExtra(Constants.SIMPRINTS_IDENTIFICATIONS);
        if (identifications != null) {
            String guids = "";
            String confidences = "";
            String tiers = "";
            for (Identification curId : identifications) {
                String curGuid = curId.getGuid();
                String curConfidence = String.valueOf(curId.getConfidence());
                String curTier = curId.getTier().toString();
                if (!TextUtils.isEmpty(curGuid)
                        && !TextUtils.isEmpty(curConfidence)
                        && !TextUtils.isEmpty(curTier)) {
                    if (!TextUtils.isEmpty(guids)) {
                        guids += FIELD_SEPARATOR;
                        confidences += FIELD_SEPARATOR;
                        tiers += FIELD_SEPARATOR;
                    }

                    guids += curGuid;
                    confidences += curConfidence;
                    tiers += curTier;
                }
            }

            outputIntent.putExtra(INTENT_FIELD_GUID, guids);
            outputIntent.putExtra(INTENT_FIELD_CONFIDENCE, confidences);
            outputIntent.putExtra(INTENT_FIELD_TIER, tiers);

            return true;
        }

        return false;
    }
}
