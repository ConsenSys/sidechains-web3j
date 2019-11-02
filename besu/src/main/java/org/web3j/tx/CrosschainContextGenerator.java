/*
 * Copyright 2019 Web3 Labs LTD.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.web3j.tx;
/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
import java.math.BigInteger;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Enumeration;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

public class CrosschainContextGenerator {
    private final BigInteger crosschainTransactionId;
    private final BigInteger originatingSidechainId;

    public CrosschainContextGenerator(
            final BigInteger crosschainTransactionId, final BigInteger originatingSidechainId) {
        this.crosschainTransactionId = crosschainTransactionId;
        this.originatingSidechainId = originatingSidechainId;
    }

    public CrosschainContextGenerator(final BigInteger originatingSidechainId) {
        this.crosschainTransactionId = generateCrosschainTransactionId();
        this.originatingSidechainId = originatingSidechainId;
    }

    /**
     * Use this method when creating a subordinate transaction or view that includes nested
     * subordinate transactions and / or views.
     *
     * @param fromSidechainId Sidechain that function calling the function which is the target of
     *     this transaction is from.
     * @param fromAddress Contract that function calling the function which is the target of this
     *     transaction is from.
     * @param subordinateTransactionsAndViews nested transactions and views.
     * @return CrosschainContext to use with a subordinate transaction or view.
     */
    public CrosschainContext createCrosschainContext(
            final BigInteger fromSidechainId,
            final String fromAddress,
            final byte[][] subordinateTransactionsAndViews) {
        return new CrosschainContext(
                this.crosschainTransactionId,
                this.originatingSidechainId,
                fromSidechainId,
                fromAddress,
                subordinateTransactionsAndViews);
    }

    /**
     * Use this method when creating a subordinate transaction or view that does not include any
     * nested subordinate transactions and / or views.
     *
     * @param fromSidechainId Sidechain that function calling the function which is the target of
     *     this transaction is from.
     * @param fromAddress Contract that function calling the function which is the target of this
     *     transaction is from.
     * @return CrosschainContext to use with a subordinate transaction or view.
     */
    public CrosschainContext createCrosschainContext(
            final BigInteger fromSidechainId, final String fromAddress) {
        return new CrosschainContext(
                this.crosschainTransactionId,
                this.originatingSidechainId,
                fromSidechainId,
                fromAddress,
                null);
    }

    /**
     * Use this method when creating the originating transaction.
     *
     * @param subordinateTransactionsAndViews nested transactions and views.
     * @return CrosschainContext to use for an originating transaction.
     */
    public CrosschainContext createCrosschainContext(
            final byte[][] subordinateTransactionsAndViews) {
        return new CrosschainContext(
                this.crosschainTransactionId,
                this.originatingSidechainId,
                subordinateTransactionsAndViews);
    }

    private static BigInteger generateCrosschainTransactionId() {
        final int SIZE_OF_WORD = 32;
        byte[] rawRandomBytes = new byte[SIZE_OF_WORD];
        rand.nextBytes(rawRandomBytes);
        BigInteger id = new BigInteger(rawRandomBytes);
        id = id.abs(); // Just in case the top bit was set, let's make the number positive
        // to ensure the RLP encoding will work.
        return id;
    }

    // TODO When Web3J supports JDKs after JDK8, change to DrbgParameters.instantiation
    private static SecureRandom rand = setupRand();

    private static SecureRandom setupRand() {
        try {
            final SecureRandom rand = SecureRandom.getInstance("DRBG");
            rand.setSeed(getPersonalizationString());
            return rand;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Use a personalisation string to help ensure the entropy going into the PRNG is unique.
    private static byte[] getPersonalizationString()
            throws SocketException, BufferOverflowException {
        final byte[] networkMacs = networkHardwareAddresses();
        final Runtime runtime = Runtime.getRuntime();
        final byte[] threadId = Longs.toByteArray(Thread.currentThread().getId());
        final byte[] availProcessors = Ints.toByteArray(runtime.availableProcessors());
        final byte[] freeMem = Longs.toByteArray(runtime.freeMemory());
        final byte[] runtimeMem = Longs.toByteArray(runtime.maxMemory());
        return Bytes.concat(threadId, availProcessors, freeMem, runtimeMem, networkMacs);
    }

    private static byte[] networkHardwareAddresses()
            throws SocketException, BufferOverflowException {
        final byte[] networkAddresses = new byte[256];
        final ByteBuffer buffer = ByteBuffer.wrap(networkAddresses);

        final Enumeration<NetworkInterface> networkInterfaces =
                NetworkInterface.getNetworkInterfaces();
        if (networkInterfaces != null) {
            while (networkInterfaces.hasMoreElements()) {
                final NetworkInterface networkInterface = networkInterfaces.nextElement();
                final byte[] hardwareAddress = networkInterface.getHardwareAddress();
                if (hardwareAddress != null) {
                    buffer.put(hardwareAddress);
                }
            }
        }
        return Arrays.copyOf(networkAddresses, buffer.position());
    }
}
