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

package okio;

import java.io.IOException;
import org.spongycastle.crypto.BlockCipher;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.paddings.BlockCipherPadding;

/**
 * A {@link Source} which decrypts data using a block cipher.
 */
public class BlockCipherSource extends ForwardingSource {

    private final BlockCipher mCipher;
    private final BlockCipherPadding mPadding;
    private final int mBlockSize;

    private final Buffer mInBuffer;
    private final byte[] mInBlock;
    private final byte[] mOutBlock;
    private int mOutBlockOffset;
    private int mOutBlockEnd;

    /**
     * Creates a source which will decrypt data from the provided input source, using the
     * provided block cipher and padding mode.
     */
    public BlockCipherSource(BlockCipher cipher, BlockCipherPadding padding, Source in) {
        super(in);
        mCipher = cipher;
        mPadding = padding;
        mBlockSize = cipher.getBlockSize();
        mInBuffer = new Buffer();
        mInBlock = new byte[mBlockSize];
        mOutBlock = new byte[mBlockSize];
        mOutBlockOffset = mOutBlock.length;
        mOutBlockEnd = mOutBlock.length;
    }

    @Override
    public long read(Buffer sink, long byteCount) throws IOException {
        int totalReadCount = 0;
        while (byteCount > 0) {
            int available = mOutBlockEnd - mOutBlockOffset;
            if (available > 0) {
                int readCount = (int)Math.min(byteCount, available);
                sink.write(mOutBlock, mOutBlockOffset, readCount);
                mOutBlockOffset += readCount;
                available -= readCount;
                byteCount -= readCount;
                totalReadCount += readCount;
            }

            if (available == 0 && byteCount > 0) {
                if (!decryptBlock()) {
                    if (totalReadCount == 0) {
                        return -1;
                    }
                    return totalReadCount;
                }
            }
        }

        return totalReadCount;
    }

    private boolean decryptBlock() throws IOException {
        if (mInBuffer.exhausted()) {
            if (!readNextBlock()) {
                return false;
            }
        }

        mInBuffer.read(mInBlock);
        mCipher.processBlock(mInBlock, 0, mOutBlock, 0);

        mOutBlockOffset = 0;
        if (!readNextBlock()) {
            // reached the last block, check for padding
            try {
                mOutBlockEnd = mOutBlock.length - mPadding.padCount(mOutBlock);
            } catch (InvalidCipherTextException ex) {
                throw new IOException("Corrupted data stream", ex);
            }
        } else {
            mOutBlockEnd = mOutBlock.length;
        }

        return true;
    }

    private boolean readNextBlock() throws IOException {
        long readCount = delegate().read(mInBuffer, mBlockSize);
        if (readCount == -1) {
            return false;
        }

        if (readCount < mBlockSize) {
            throw new IOException("Corrupted data stream: depleted inside block boundary");
        }

        return true;
    }
}
