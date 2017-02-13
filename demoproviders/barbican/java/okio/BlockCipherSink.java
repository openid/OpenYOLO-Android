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
import org.spongycastle.crypto.paddings.BlockCipherPadding;

/**
 * A {@link Sink} which encrypts data using a block cipher.
 */
public class BlockCipherSink extends ForwardingSink {

    private final BlockCipher mCipher;
    private final BlockCipherPadding mPadding;
    private final int mBlockSize;
    private byte[] mInBlock;
    private byte[] mLastBlock;
    private Buffer mOutBuffer;
    private int mInBlockOffset;

    /**
     * Creates a sink which will encrypt written data using the provided block cipher to
     * the provided output sink, and will pad the end of the data using the provided padding mode.
     */
    public BlockCipherSink(BlockCipher cipher, BlockCipherPadding padding, Sink out) {
        super(out);
        mCipher = cipher;
        mPadding = padding;
        mBlockSize = mCipher.getBlockSize();
        mInBlock = new byte[mBlockSize];
        mLastBlock = new byte[mBlockSize];
        mOutBuffer = new Buffer();
        mInBlockOffset = 0;
    }

    @Override
    public void write(Buffer source, long byteCount) throws IOException {
        Util.checkOffsetAndCount(source.size, 0, byteCount);
        while (byteCount > 0) {
            int readCount = (int)Math.min(byteCount, mInBlock.length - mInBlockOffset);
            byteCount -= readCount;
            source.read(mInBlock, mInBlockOffset, readCount);
            mInBlockOffset += readCount;

            if (mInBlockOffset == mInBlock.length) {
                processBlock();
            }
        }
    }

    @Override
    public void close() throws IOException {
        mPadding.addPadding(mInBlock, mInBlockOffset);
        processBlock();
        delegate().close();
    }

    private void processBlock() throws IOException {
        mCipher.processBlock(mInBlock, 0, mLastBlock, 0);
        mOutBuffer.write(mLastBlock);
        delegate().write(mOutBuffer, mBlockSize);
        mInBlockOffset = 0;
    }
}
