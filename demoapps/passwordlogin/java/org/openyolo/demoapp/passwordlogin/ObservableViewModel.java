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

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.databinding.Observable;
import android.databinding.PropertyChangeRegistry;
import android.support.annotation.NonNull;

/**
 * A base class for use by view models that utilize Android data binding.
 */
public abstract class ObservableViewModel extends AndroidViewModel implements Observable {

    private final PropertyChangeRegistry mRegistry = new PropertyChangeRegistry();

    /**
     * Creates the view model, with the required application reference.
     */
    public ObservableViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    public void addOnPropertyChangedCallback(@NonNull OnPropertyChangedCallback callback) {
        mRegistry.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(@NonNull OnPropertyChangedCallback callback) {
        mRegistry.remove(callback);
    }

    /**
     * Notify listeners that the view model has changed.
     */
    public void notifyChange() {
        mRegistry.notifyCallbacks(this, 0, null);
    }

    /**
     * Notify listeners that a specific property in the view model has changed.
     */
    public void notifyPropertyChanged(int fieldId) {
        mRegistry.notifyCallbacks(this, fieldId, null);
    }
}
