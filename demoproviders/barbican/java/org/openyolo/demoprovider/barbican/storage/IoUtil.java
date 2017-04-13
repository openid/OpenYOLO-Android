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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.io.CipherInputStream;
import org.spongycastle.crypto.io.CipherOutputStream;
import org.spongycastle.crypto.modes.SICBlockCipher;
import org.spongycastle.crypto.paddings.PKCS7Padding;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

/**
 * Utilities for reading and writing encrypted data to disk. The cipher used for Barbican
 * is AES-128/CTR/PKCS7Padding.
 */
class IoUtil {

    static final int KEY_SIZE = 16;

    static final int BLOCK_SIZE = 16;

    static final SecureRandom RANDOM = new SecureRandom();

    static CipherOutputStream encryptTo(OutputStream stream, byte[] key) throws IOException {
        byte[] randomIv = new byte[BLOCK_SIZE];
        RANDOM.nextBytes(randomIv);

        stream.write(randomIv);
        return new CipherOutputStream(
                stream,
                createAes128CtrPkcs7PaddingCipher(true, randomIv, key));
    }

    static CipherOutputStream encryptTo(File file, byte[] key) throws IOException {
        return encryptTo(new FileOutputStream(file), key);
    }

    static CipherInputStream decryptFrom(InputStream stream, byte[] key) throws IOException {
        byte[] iv = new byte[BLOCK_SIZE];
        if (stream.read(iv) != BLOCK_SIZE) {
            throw new IOException("Failed to read IV");
        }
        return new CipherInputStream(stream, createAes128CtrPkcs7PaddingCipher(false, iv, key));
    }

    static CipherInputStream decryptFrom(File file, byte[] key) throws IOException {
        return decryptFrom(new FileInputStream(file), key);
    }

    static PaddedBufferedBlockCipher createAes128CtrPkcs7PaddingCipher(
            boolean encrypting,
            byte[] iv,
            byte[] key) {
        AESEngine aes = new AESEngine();
        SICBlockCipher aesCtr = new SICBlockCipher(aes);
        PaddedBufferedBlockCipher aesCtrPkcs7 =
                new PaddedBufferedBlockCipher(aesCtr, new PKCS7Padding());
        aesCtrPkcs7.init(encrypting, new ParametersWithIV(new KeyParameter(key), iv));

        return aesCtrPkcs7;
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
