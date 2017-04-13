package org.openyolo.spi;

import static org.mockito.Mockito.mock;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.bbq.Protobufs.BroadcastQuery;
import com.google.bbq.QueryResponseSender;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link QueryResponseSender}
 */
@Implements(QueryResponseSender.class)
public class ShadowQueryResponseSender {
    public ShadowQueryResponseSender() {}

    public static QueryResponseSender mockQueryResponseSender = mock(QueryResponseSender.class);

    public static void intializeForTest() {
        mockQueryResponseSender = mock(QueryResponseSender.class);
    }

    @Implementation
    public void sendResponse(@NonNull BroadcastQuery query, @Nullable byte[] responseMessage) {
        mockQueryResponseSender.sendResponse(query, responseMessage);
    }
}

