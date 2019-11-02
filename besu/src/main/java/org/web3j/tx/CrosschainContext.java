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

public class CrosschainContext {
    private BigInteger crosschainCoordinationBlockchainId;
    private String crosschainCoordinationContractAddress;
    private BigInteger crosschainTimeoutBlockNumber;

    private final BigInteger crosschainTransactionId;
    private final BigInteger originatingSidechainId;
    private final BigInteger fromSidechainId;
    private final String fromAddress;
    private final byte[][] subordinateTransactionsAndViews;

    CrosschainContext(
            final BigInteger crosschainTransactionId,
            final BigInteger originatingSidechainId,
            final BigInteger fromSidechainId,
            final String fromAddress,
            final byte[][] subordinateTransactionsAndViews) {
        this.crosschainTransactionId = crosschainTransactionId;
        this.originatingSidechainId = originatingSidechainId;
        this.fromSidechainId = fromSidechainId;
        this.fromAddress = fromAddress;
        this.subordinateTransactionsAndViews = subordinateTransactionsAndViews;
    }

    CrosschainContext(
            final BigInteger crosschainTransactionId,
            final BigInteger originatingSidechainId,
            final byte[][] subordinateTransactionsAndViews) {
        this(
                crosschainTransactionId,
                originatingSidechainId,
                null,
                null,
                subordinateTransactionsAndViews);
    }

    public void addCoordinationInformation(
            final BigInteger crosschainCoordinationBlockchainId,
            final String crosschainCoordinationContractAddress,
            final BigInteger crosschainTimeoutBlockNumber) {
        this.crosschainCoordinationBlockchainId = crosschainCoordinationBlockchainId;
        this.crosschainCoordinationContractAddress = crosschainCoordinationContractAddress;
        this.crosschainTimeoutBlockNumber = crosschainTimeoutBlockNumber;
    }

    public BigInteger getCrosschainTransactionId() {
        return this.crosschainTransactionId;
    }

    public BigInteger getOriginatingSidechainId() {
        return this.originatingSidechainId;
    }

    public BigInteger getFromSidechainId() {
        return this.fromSidechainId;
    }

    public String getFromAddress() {
        return this.fromAddress;
    }

    public byte[][] getSubordinateTransactionsAndViews() {
        if (this.subordinateTransactionsAndViews == null) {
            return new byte[][] {};
        }
        return this.subordinateTransactionsAndViews;
    }

    public BigInteger getCrosschainCoordinationBlockchainId() {
        return this.crosschainCoordinationBlockchainId;
    }

    public String getCrosschainCoordinationContractAddress() {
        return this.crosschainCoordinationContractAddress;
    }

    public BigInteger getCrosschainTimeoutBlockNumber() {
        return this.crosschainTimeoutBlockNumber;
    }

    public boolean isOriginatingTransactionContext() {
        return this.fromSidechainId == null;
    }

    public boolean hasCoordinationInformation() {
        return this.crosschainCoordinationBlockchainId != null;
    }
}
