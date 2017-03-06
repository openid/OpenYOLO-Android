/*
 * Copyright 2017 The OpenYOLO Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openyolo.testapp;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import java.util.ArrayList;
import java.util.List;

/**
 * The main OpenYolo test activity.
 */
public final class OpenYoloTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_layout);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        DemoPagerAdapter demoPagerAdapter = new DemoPagerAdapter(getSupportFragmentManager());
        demoPagerAdapter.addPage(new RetrieveTestPageFragment());
        demoPagerAdapter.addPage(new SaveTestPageFragment());
        demoPagerAdapter.addPage(new HintTestPageFragment());

        viewPager.setAdapter(demoPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private static class DemoPagerAdapter extends FragmentPagerAdapter {

        private final List<TestPageFragment> mPages = new ArrayList<>();

        DemoPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return mPages.size();
        }

        public void addPage(TestPageFragment pageFragment) {
            mPages.add(pageFragment);
        }

        @Override
        public Fragment getItem(int position) {
            return mPages.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mPages.get(position).getPageTitle();
        }
    }
}
