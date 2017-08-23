package org.openyolo.spi;

import org.openyolo.protocol.AuthenticationDomain;

/** Collection of test constants. */
public final class Constants {
    private Constants() {}

    public static final class ValidApplication {
        private ValidApplication() {}

        public static String PACKAGE_NAME = "com.super.cool.app";

        private static String AUTHENTICATION_DOMAIN_STRING = "https://accounts.google.com";
        public static AuthenticationDomain AUTHENTICATION_DOMAIN =
                new AuthenticationDomain(AUTHENTICATION_DOMAIN_STRING);
    }
}
