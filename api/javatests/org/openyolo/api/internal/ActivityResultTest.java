package org.openyolo.api.internal;

import android.content.Intent;
import android.os.Parcel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.protocol.CredentialDeleteResult;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * Units tests for {@link ActivityResult}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ActivityResultTest {

    @Test
    public void parcelAndUnparcel_withValidInput_returnsEquivalent() throws Exception {
        Parcel parcel = Parcel.obtain();

        int resultCode = CredentialDeleteResult.CODE_BAD_REQUEST;
        Intent intent = CredentialDeleteResult.BAD_REQUEST.toResultDataIntent();

        // Act
        ActivityResult givenActivityResult = ActivityResult.of(resultCode, intent);
        givenActivityResult.writeToParcel(parcel, 0 /* flags */);

        parcel.setDataPosition(0);
        ActivityResult outputActivityResult = ActivityResult.CREATOR.createFromParcel(parcel);

        // Assert
        assertThat(outputActivityResult.getResultCode()).isEqualTo(resultCode);

        CredentialDeleteResult credentialDeleteResult =
                CredentialDeleteResult.fromResultIntentData(outputActivityResult.getData());
        assertThat(credentialDeleteResult.getResultCode()).isEqualTo(resultCode);
    }
}
