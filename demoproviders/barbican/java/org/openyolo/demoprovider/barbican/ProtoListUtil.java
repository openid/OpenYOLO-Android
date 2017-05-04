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

package org.openyolo.demoprovider.barbican;

import com.google.protobuf.ByteString;
import com.google.protobuf.ByteString.Output;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for reading and writing lists of protocol buffer messages.
 */
public final class ProtoListUtil {

    /**
     * Creates a {@link ByteString} by serializing the list of protos. Use
     * {@link #readMessageList(ByteString, Parser)} to deserialize.
     */
    public static <T extends MessageLite> ByteString writeMessageList(List<T> protos) {
        Output output = ByteString.newOutput();
        try {
            writeMessageListTo(output, protos);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to write protobufs to memory");
        }

        return output.toByteString();
    }

    /**
     * Writes the provided list of protos to the provided output stream.
     * @throws IOException if the protos cannot be written to the provided output stream.
     */
    public static <T extends MessageLite> void writeMessageListTo(
            OutputStream stream,
            List<T> protos)
            throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeInt(protos.size());
        for (MessageLite proto : protos) {
            proto.writeDelimitedTo(stream);
        }
    }

    /**
     * Reads a list of protos, using the provided parser, from the provided {@link ByteString}.
     * @throws IOException if the proto list could not be parsed.
     */
    public static <T extends MessageLite> List<T> readMessageList(
            ByteString bytes,
            Parser<T> parser)
            throws IOException {
        InputStream stream = bytes.newInput();
        return readMessageList(stream, parser);
    }

    /**
     * Reads a list of protos, using the provided parser, from the provided byte array.
     * @throws IOException if the proto list could not be parsed.
     */
    public static <T extends MessageLite> List<T> readMessageList(
            byte[] bytes,
            Parser<T> parser)
            throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        return readMessageList(bais, parser);
    }

    /**
     * Reads a list of protos, using the provided parser, from the provided input stream.
     * @throws IOException if the proto list could not be parsed.
     */
    public static <T extends MessageLite> List<T> readMessageList(
            InputStream stream,
            Parser<T> parser)
            throws IOException {
        DataInputStream dis = new DataInputStream(stream);
        int messageCount = dis.readInt();

        ArrayList<T> messages = new ArrayList<>(messageCount);
        for (int i = 0; i < messageCount; i++) {
            messages.add(parser.parseDelimitedFrom(stream));
        }

        return messages;
    }
}
