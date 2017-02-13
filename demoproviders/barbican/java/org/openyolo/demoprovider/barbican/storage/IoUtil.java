/*
 * Copyright 2016 The OpenYOLO Authors. All Rights Reserved.
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

package org.openyolo.demoprovider.barbican.storage;

import static okio.Okio.buffer;
import static okio.Okio.sink;
import static okio.Okio.source;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import okio.BlockCipherSink;
import okio.BlockCipherSource;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Sink;
import okio.Source;
import org.spongycastle.crypto.engines.TwofishEngine;
import org.spongycastle.crypto.paddings.PKCS7Padding;
import org.spongycastle.crypto.params.KeyParameter;

/**
 * Utilities for reading and writing encrypted data to disk.
 */
class IoUtil {

    static final int KEY_SIZE = 16;

    static final int BLOCK_SIZE = 16;

    static Sink encryptTo(Sink sink, byte[] key) {
        TwofishEngine engine = new TwofishEngine();
        engine.init(true, new KeyParameter(key));
        return new BlockCipherSink(engine, new PKCS7Padding(), sink);
    }

    static BufferedSink encryptTo(File file, byte[] key) throws FileNotFoundException {
        return buffer(encryptTo(buffer(sink(file)), key));
    }

    static Source decryptFrom(Source source, byte[] key) {
        TwofishEngine engine = new TwofishEngine();
        engine.init(false, new KeyParameter(key));
        return new BlockCipherSource(engine, new PKCS7Padding(), source);
    }

    static BufferedSource decryptFrom(File file, byte[] key) throws FileNotFoundException {
        return buffer(decryptFrom(buffer(source(file)), key));
    }

    static void closeQuietly(@Nullable Closeable resource, @NonNull String logTag) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException ex) {
                Log.w(logTag, "Failed to close IO stream", ex);
            }
        }
    }
}
