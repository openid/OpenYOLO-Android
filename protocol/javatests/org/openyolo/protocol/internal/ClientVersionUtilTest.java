package org.openyolo.protocol.internal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ClientVersionUtilTest {

    @Test
    public void testParseVersionPart() {
        assertThat(ClientVersionUtil.parseVersionPart("0")).isEqualTo(0);
        assertThat(ClientVersionUtil.parseVersionPart("2")).isEqualTo(2);
        assertThat(ClientVersionUtil.parseVersionPart("1024")).isEqualTo(1024);
    }

    @Test
    public void testParseVersionPart_nullValue() {
        assertThat(ClientVersionUtil.parseVersionPart(null)).isEqualTo(0);
    }

    @Test
    public void testParseVersionPart_emptyValue() {
        assertThat(ClientVersionUtil.parseVersionPart("")).isEqualTo(0);
    }

    @Test
    public void testParseVersionPart_negativeValue() {
        assertThat(ClientVersionUtil.parseVersionPart("-1")).isEqualTo(0);
    }

    @Test
    public void testParseVersionPart_tooBigValue() {
        assertThat(ClientVersionUtil.parseVersionPart("10000000000")).isEqualTo(0);
    }
}
