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
package org.web3j.protocol.besu.crypto.crosschain;
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

import org.web3j.crypto.RawTransaction;

public class CrosschainRawTransaction extends RawTransaction {

    CrosschainTransactionType type;
    byte[][] subordinateTransactionsAndViews;

    public CrosschainRawTransaction(
            CrosschainTransactionType type,
            BigInteger nonce,
            BigInteger gasPrice,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            String data,
            byte[][] subordinateTxOrView) {
        super(nonce, gasPrice, gasLimit, to, value, data);
        this.type = type;
        this.subordinateTransactionsAndViews = subordinateTxOrView;

        // enforce empty array over null variable
        if (this.subordinateTransactionsAndViews == null) {
            this.subordinateTransactionsAndViews = new byte[][] {};
        }
    }

    public static CrosschainRawTransaction createTransaction(
            CrosschainTransactionType type,
            BigInteger nonce,
            BigInteger gasPrice,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            String data,
            byte[][] subordinateTransactionsAndViews) {

        return new CrosschainRawTransaction(
                type, nonce, gasPrice, gasLimit, to, value, data, subordinateTransactionsAndViews);
    }

    public byte[][] getSubordinateTxAndViews() {
        return this.subordinateTransactionsAndViews;
    }

    public int getType() {
        return this.type.value;
    }
}
