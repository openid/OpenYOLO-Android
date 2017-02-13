/*
 * Copyright 2016 The OpenYOLO Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openyolo.spi.assetlinks.data;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Tests for {@link AssetStatementsFactory}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AssetStatementsFactoryTest {

    private final String fingerprint =
                 "D5:03:9F:44:48:4A:70:C9:0A:AC:1D:3A:A7:67:07:EE:B8:BF:52:03:62:87:0C:98:B0:C9" +
                             ":E2:2A:8E:14:DF:A8";

    @Test
    public void emptyAssetStatements() {
        List<AssetStatement> list = AssetStatementsFactory.INSTANCE.createAssetStatements("");
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    public void nullAssetStatements() {
        List<AssetStatement> list = AssetStatementsFactory.INSTANCE.createAssetStatements(null);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    public void invalidNamespace() {
        String webStatement = String.format(SINGLE_WEB_STATEMENT, RelationType.GetLoginCreds, "foo",
                    "https://www.example.com");

        String stmtArray = statementsToJsonArray(webStatement);

        List<AssetStatement> list = AssetStatementsFactory.INSTANCE.createAssetStatements
                    (stmtArray);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    public void nullNamespace() {
        String webStatement = String.format(SINGLE_WEB_STATEMENT, RelationType.GetLoginCreds, null,
                    "https://www.example.com");

        String stmtArray = statementsToJsonArray(webStatement);

        List<AssetStatement> list = AssetStatementsFactory.INSTANCE.createAssetStatements
                    (stmtArray);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    public void invalidWebStatement() {
        String json = createWebStatementList(RelationType.GetLoginCreds.getDescription(), null, 1);
        List<AssetStatement> list = AssetStatementsFactory.INSTANCE.createAssetStatements(json);

        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    public void singleWebStatement() {
        String site = "https://www.example.com";

        String json = createWebStatementList(RelationType.GetLoginCreds.getDescription(), site, 1);
        List<AssetStatement> list = AssetStatementsFactory.INSTANCE.createAssetStatements(json);

        assertTrue(list.size() == 1);
        assertTrue(list.get(0) instanceof WebSiteAssetStatement);

        WebSiteAssetStatement webSiteAssetStatement = (WebSiteAssetStatement) list.get(0);
        assertTrue(webSiteAssetStatement.getRelations().size() == 1);
        assertTrue(webSiteAssetStatement.getRelations().get(0) == RelationType.GetLoginCreds);

        assertNotNull(webSiteAssetStatement.getTarget());
        assertTrue(webSiteAssetStatement.getTarget().getNamespace() == NamespaceType.Web);
        assertTrue(webSiteAssetStatement.getTarget().getSite().equals(site));
    }

    @Test
    public void multipleWebStatements() {
        String json = createWebStatementList(RelationType.GetLoginCreds.getDescription(),
                "https://www.example.com", 3);
        List<AssetStatement> list = AssetStatementsFactory.INSTANCE.createAssetStatements(json);

        assertTrue(list.size() == 3);
        assertTrue(list.get(0) instanceof WebSiteAssetStatement);
        assertTrue(list.get(1) instanceof WebSiteAssetStatement);
        assertTrue(list.get(2) instanceof WebSiteAssetStatement);
    }

    @Test
    public void invalidIncludeStatement_null_url() {
        String json = createIncludeStatementList(null, 1);

        List<AssetStatement> list = AssetStatementsFactory.INSTANCE.createAssetStatements(json);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    public void invalidIncludeStatement_invalid_url() {
        String json = createIncludeStatementList("foo", 1);

        List<AssetStatement> list = AssetStatementsFactory.INSTANCE.createAssetStatements(json);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    public void includeStatement() {
        String url = "http://example.digitalassetlinks.org/.well-known/assetlinks.json";
        String json = createIncludeStatementList(url, 1);

        List<AssetStatement> list = AssetStatementsFactory.INSTANCE.createAssetStatements(json);

        assertTrue(list.size() == 1);
        assertTrue(list.get(0) instanceof IncludeStatement);

        IncludeStatement includeStatement = (IncludeStatement) list.get(0);
        assertNotNull(includeStatement.getInclude());
        assertTrue(includeStatement.getInclude().equals(url));
    }

    @Test
    public void singleAndroidStatement() {
        String packageName = "org.example";

        String json = createAndroidAppStatementList(RelationType.GetLoginCreds.getDescription(),
                packageName, fingerprint, 1);

        List<AssetStatement> list = AssetStatementsFactory.INSTANCE.createAssetStatements(json);

        assertTrue(list.size() == 1);
        assertTrue(list.get(0) instanceof AndroidAssetStatement);

        AndroidAssetStatement androidAssetStatement = (AndroidAssetStatement) list.get(0);

        assertNotNull(androidAssetStatement.getRelations());
        assertTrue(androidAssetStatement.getRelations().size() == 1);
        assertTrue(androidAssetStatement.getRelations().get(0) == RelationType.GetLoginCreds);

        assertNotNull(androidAssetStatement.getTarget());
        assertTrue(androidAssetStatement.getTarget().getNamespace() == NamespaceType.AndroidApp);
        assertTrue(androidAssetStatement.getTarget().getPackageName().equals(packageName));
        assertNotNull(androidAssetStatement.getTarget().getSha256CertFingerprint());
        assertTrue(androidAssetStatement.getTarget().getSha256CertFingerprint().equals
                (fingerprint));
    }

    @Test
    public void multipleAndroidStatements() {
        String json = createAndroidAppStatementList(RelationType.GetLoginCreds.getDescription(),
                "org.example", fingerprint, 3);
        List<AssetStatement> list = AssetStatementsFactory.INSTANCE.createAssetStatements(json);

        assertTrue(list.size() == 3);

        assertTrue(list.get(0) instanceof AndroidAssetStatement);
        assertTrue(list.get(1) instanceof AndroidAssetStatement);
        assertTrue(list.get(2) instanceof AndroidAssetStatement);
    }

    @Test
    public void multipleFingerprints() {
        String fingerprints = "00:12:" + fingerprint
                    + "\",  \"11:12:" + fingerprint
                    + "\", \"22:12" + fingerprint;

        String stmt = createAndroidAppStatement(RelationType.GetLoginCreds.getDescription(), "org" +
                ".example", fingerprints);

        String stmtArray = statementsToJsonArray(stmt);

        List<AssetStatement> list = AssetStatementsFactory.INSTANCE.createAssetStatements
                (stmtArray);

        assertTrue(list.size() == 3);
        assertTrue(list.get(0) instanceof AndroidAssetStatement);
        assertTrue(list.get(1) instanceof AndroidAssetStatement);
        assertTrue(list.get(2) instanceof AndroidAssetStatement);

        assertNotNull(((AndroidAssetStatement) list.get(0)).getTarget().getSha256CertFingerprint());
        assertNotNull(((AndroidAssetStatement) list.get(1)).getTarget().getSha256CertFingerprint());
        assertNotNull(((AndroidAssetStatement) list.get(2)).getTarget().getSha256CertFingerprint());

        assertTrue(((AndroidAssetStatement) list.get(0)).getTarget().getSha256CertFingerprint()
                .startsWith("00:12"));
        assertTrue(((AndroidAssetStatement) list.get(1)).getTarget().getSha256CertFingerprint()
                .startsWith("11:12"));
        assertTrue(((AndroidAssetStatement) list.get(2)).getTarget().getSha256CertFingerprint()
                .startsWith("22:12"));
    }

    @Test
    public void allStatementTypes() {
        String site = "https://www.example.com";
        String firstInclude = "http://example.digitalassetlinks.org/.well-known/assetlinks.json";
        String secondInclude = "http://www.google.com/.well-known/assetlinks.json";

        String fingerprints = "00:12:" + fingerprint
                    + "\",  \"11:12:" + fingerprint
                    + "\", \"22:12" + fingerprint;

        String androidStatement = createAndroidAppStatement(RelationType.GetLoginCreds
                .getDescription(), "org.example", fingerprints);
        String webStatement = createWebStatement(RelationType.GetLoginCreds.getDescription(), site);
        String firstIncludeStmt = createIncludeStatement("http://example.digitalassetlinks.org/" +
                ".well-known/assetlinks.json");
        String secondStmt = createIncludeStatement(secondInclude);

        String stmtArray = statementsToJsonArray(androidStatement + ", " + webStatement + ", " +
                firstIncludeStmt + ", " + secondStmt);

        List<AssetStatement> list = AssetStatementsFactory.INSTANCE.createAssetStatements
                (stmtArray);

        assertTrue(list.size() == 6);
        assertTrue(list.get(0) instanceof AndroidAssetStatement);
        assertTrue(list.get(1) instanceof AndroidAssetStatement);
        assertTrue(list.get(2) instanceof AndroidAssetStatement);
        assertTrue(list.get(3) instanceof WebSiteAssetStatement);
        assertTrue(list.get(4) instanceof IncludeStatement);
        assertTrue(list.get(5) instanceof IncludeStatement);

        assertTrue(((AndroidAssetStatement) list.get(0)).getTarget().getSha256CertFingerprint()
                .startsWith("00:12"));
        assertTrue(((AndroidAssetStatement) list.get(1)).getTarget().getSha256CertFingerprint()
                .startsWith("11:12"));
        assertTrue(((AndroidAssetStatement) list.get(2)).getTarget().getSha256CertFingerprint()
                .startsWith("22:12"));

        assertEquals(site, ((WebSiteAssetStatement) list.get(3)).getTarget().getSite());

        assertEquals(firstInclude, ((IncludeStatement) list.get(4)).getInclude());
        assertEquals(secondInclude, ((IncludeStatement) list.get(5)).getInclude());

    }

    private String createWebStatementList(String relation, String site, int count) {
        String statements[] = new String[count];
        for (int i = 0; i < count; i++) {
            statements[i] = createWebStatement(relation, site);
        }
        return statementsToJsonArray(statements);
    }

    private String createAndroidAppStatementList(String relation, String packageName, String
            fingerprint, int count) {
        String statements[] = new String[count];
        for (int i = 0; i < count; i++) {
            statements[i] = createAndroidAppStatement(relation, packageName, fingerprint);
        }
        return statementsToJsonArray(statements);
    }

    private String createIncludeStatementList(String url, int count) {
        String statements[] = new String[count];
        for (int i = 0; i < count; i++) {
            statements[i] = createIncludeStatement(url);
        }
        return statementsToJsonArray(statements);
    }

    private String createAndroidAppStatement(String relation, String packageName, String
            fingerprint) {
        return String.format(SINGLE_ANDROID_APP_STATEMENT, relation, NamespaceType.AndroidApp
                .getDescription(), packageName, fingerprint);
    }

    private String createWebStatement(String relation, String site) {
        return String.format(SINGLE_WEB_STATEMENT, relation, NamespaceType.Web.getDescription(),
                site);
    }

    private String createIncludeStatement(String url) {
        return String.format(SINGLE_INCLUDE_STATEMENT, url);
    }

    private final String SINGLE_INCLUDE_STATEMENT =
            "{" +
                    "\"include\": \"%s\"" +
                    "}";

    private final String SINGLE_ANDROID_APP_STATEMENT =
            "{" +
                    "\"relation\": [\"%s\"]," +
                    "\"target\": {" +
                    "\"namespace\": \"%s\"," +
                    "\"package_name\": \"%s\"," +
                    "\"sha256_cert_fingerprints\": [\"%s\"]" +
                    "}" +
                    "}";

    private final String SINGLE_WEB_STATEMENT =
            "{" +
                    "\"relation\": [\"%s\"]," +
                    "\"target\": {" +
                    "\"namespace\": \"%s\"," +
                    "\"site\": \"%s\"" +
                    "}" +
                    "}";

    private String statementsToJsonArray(String... statements) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (String statement : statements) {
            sb.append(statement).append(",");
        }
        return sb.substring(0, sb.length() - 1) + "]";
    }

}