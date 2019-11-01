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
import org.web3j.tx.CrosschainContext;

public class CrosschainRawTransaction extends RawTransaction {

    CrosschainTransactionType type;
    CrosschainContext crosschainContext;

    public CrosschainRawTransaction(
            CrosschainTransactionType type,
            BigInteger nonce,
            BigInteger gasPrice,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            String data,
            CrosschainContext crosschainContext) {
        super(nonce, gasPrice, gasLimit, to, value, data);
        this.type = type;
        this.crosschainContext = crosschainContext;
    }

    public static CrosschainRawTransaction createTransaction(
            CrosschainTransactionType type,
            BigInteger nonce,
            BigInteger gasPrice,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            String data,
            CrosschainContext crosschainContext) {

        return new CrosschainRawTransaction(
                type, nonce, gasPrice, gasLimit, to, value, data, crosschainContext);
    }

    public CrosschainContext getCrosschainContext() {
        return this.crosschainContext;
    }

    public int getType() {
        return this.type.value;
    }
}
