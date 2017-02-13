/*
 * Copyright 2016 The OpenYOLO Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openyolo.spi.assetlinks.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for {@link DeserializerUtil}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class DeserializerUtilTest {
    @Test
    public void getRelations() throws Exception {
        JSONObject statement = getAssetStatement();
        statement.getJSONArray("relation").put("delegate_permission/common.handle_all_urls");
        statement.getJSONArray("relation").put("delegate_permission/common.get_login_creds");

        final List<RelationType> relations = DeserializerUtil.getRelations(statement);
        assertNotNull(relations);
        assertTrue(relations.size() == 2);
        assertTrue(relations.get(0) == RelationType.HandleAllUrls);
        assertTrue(relations.get(1) == RelationType.GetLoginCreds);
    }

    @Test
    public void invalidRelation() throws JSONException {
        JSONObject statement = getAssetStatement();
        statement.getJSONArray("relation").put("some invalid relation");
        statement.getJSONArray("relation").put("delegate_permission/common.get_login_creds");

        final List<RelationType> relations = DeserializerUtil.getRelations(statement);
        assertNotNull(relations);
        assertTrue(relations.size() == 1);
        assertTrue(relations.get(0) == RelationType.GetLoginCreds);
    }

    @Test
    public void emptyRelations() throws JSONException {
        JSONObject statement = getAssetStatement();

        final List<RelationType> relations = DeserializerUtil.getRelations(statement);
        assertNotNull(relations);
        assertTrue(relations.isEmpty());
    }

    private JSONObject getAssetStatement() throws JSONException {
        JSONObject statement = new JSONObject();
        JSONArray relationsJson = new JSONArray();
        statement.put("relation", relationsJson);
        return statement;
    }

}