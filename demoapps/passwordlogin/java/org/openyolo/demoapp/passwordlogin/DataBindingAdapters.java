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

package org.openyolo.demoapp.passwordlogin;

import android.databinding.BindingAdapter;
import android.support.design.widget.TextInputLayout;
import android.widget.ImageView;

/**
 * Android data binding extensions which make facilitate binding to more advanced field types.
 */
public final class DataBindingAdapters {

    private DataBindingAdapters() {}

    /**
     * Facilitates loading images into an ImageView using Glide, by binding a string URI.
     */
    @BindingAdapter("android:src")
    public static void setImageUri(ImageView view, String imageUri) {
        GlideApp.with(view)
                .load(imageUri)
                .fitCenter()
                .into(view);
    }

    /**
     * Facilitates binding error text to a TextInputLayout.
     */
    @BindingAdapter("errorText")
    public static void setErrorText(TextInputLayout layout, String errorText) {
        layout.setError(errorText);
    }
}
