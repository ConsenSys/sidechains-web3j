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

import java.util.ArrayList;
import java.util.List;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Bytes;
import org.web3j.utils.Numeric;

import static org.web3j.crypto.TransactionEncoder.createEip155SignatureData;

public class CrosschainTransactionEncoder {

    public static byte[] signMessage(
            RawTransaction rawTransaction, long _chainId, Credentials credentials) {
        byte chainId = (byte) _chainId;
        byte[] encodedTransaction = encode(rawTransaction, chainId);
        Sign.SignatureData signatureData =
                Sign.signMessage(encodedTransaction, credentials.getEcKeyPair());

        Sign.SignatureData eip155SignatureData = createEip155SignatureData(signatureData, chainId);
        return encode(rawTransaction, eip155SignatureData);
    }

    private static Sign.SignatureData createEip155SignatureData(
            Sign.SignatureData signatureData, byte chainId) {
        byte v = (byte) (signatureData.getV()[0] + (chainId << 1) + 8);

        return new Sign.SignatureData(v, signatureData.getR(), signatureData.getS());
    }

    private static byte[] encode(RawTransaction rawTransaction, byte chainId) {
        Sign.SignatureData signatureData =
                new Sign.SignatureData(chainId, new byte[] {}, new byte[] {});
        return encode(rawTransaction, signatureData);
    }

    private static byte[] encode(RawTransaction rawTransaction, Sign.SignatureData signatureData) {
        List<RlpType> values = asRlpValues(rawTransaction, signatureData);
        RlpList rlpList = new RlpList(values);
        return RlpEncoder.encode(rlpList);
    }

    private static List<RlpType> asRlpValues(
            RawTransaction rawTransaction, Sign.SignatureData signatureData) {
        boolean crossChainTransaction = rawTransaction instanceof CrosschainRawTransaction;

        List<RlpType> result = new ArrayList<>();
        if (crossChainTransaction) {
            result.add(RlpString.create(((CrosschainRawTransaction) rawTransaction).getType()));
        }
        result.add(RlpString.create(rawTransaction.getNonce()));
        result.add(RlpString.create(rawTransaction.getGasPrice()));
        result.add(RlpString.create(rawTransaction.getGasLimit()));

        // an empty to address (contract creation) should not be encoded as a numeric 0 value
        String to = rawTransaction.getTo();
        if (to != null && to.length() > 0) {
            // addresses that start with zeros should be encoded with the zeros included, not
            // as numeric values
            result.add(RlpString.create(Numeric.hexStringToByteArray(to)));
        } else {
            result.add(RlpString.create(""));
        }

        result.add(RlpString.create(rawTransaction.getValue()));

        // value field will already be hex encoded, so we need to convert into binary first
        byte[] data = Numeric.hexStringToByteArray(rawTransaction.getData());
        result.add(RlpString.create(data));

        // If there are any subordinate transactions or views, add them here as an RLP Array.
        if (crossChainTransaction) {
            byte[][] subordinateTransactionsAndViews =
                    ((CrosschainRawTransaction) rawTransaction).subordinateTransactionsAndViews;
            List<RlpType> rlpSubordinateTransactionsAndViews = new ArrayList<>();
            if (subordinateTransactionsAndViews != null) {
                for (byte[] signedTransactionOrView : subordinateTransactionsAndViews) {
                    rlpSubordinateTransactionsAndViews.add(
                            RlpString.create(signedTransactionOrView));
                }
            }
            RlpList rlpListSubordinateTransactionsAndViews =
                    new RlpList(rlpSubordinateTransactionsAndViews);
            result.add(rlpListSubordinateTransactionsAndViews);
        }

        if (signatureData != null) {
            result.add(RlpString.create(signatureData.getV()));
            result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getR())));
            result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getS())));
        }

        return result;
    }

    //
    //
    //    public static byte[] signMessage(
    //            CrosschainRawTransaction rawTransaction, long chainId, Credentials credentials) {
    //        byte[] encodedTransaction = encode(rawTransaction, chainId);
    //        Sign.SignatureData signatureData =
    //                Sign.signMessage(encodedTransaction, credentials.getEcKeyPair());
    //
    //        Sign.SignatureData eip155SignatureData = createEip155SignatureData(signatureData,
    // chainId);
    //        return encode(rawTransaction, eip155SignatureData);
    //    }
    //
    //    private static byte[] encode(CrosschainRawTransaction rawTransaction, long chainId) {
    //        Sign.SignatureData signatureData =
    //                new Sign.SignatureData(longToBytes(chainId), new byte[] {}, new byte[] {});
    //        return encode(rawTransaction, signatureData);
    //    }
    //
    //    private static byte[] encode(
    //            CrosschainRawTransaction rawTransaction, Sign.SignatureData signatureData) {
    //        List<RlpType> values = asRlpValues(rawTransaction, signatureData);
    //        RlpList rlpList = new RlpList(values);
    //        return RlpEncoder.encode(rlpList);
    //    }
    //
    //    private static byte[] longToBytes(long x) {
    //        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    //        buffer.putLong(x);
    //        return buffer.array();
    //    }
    //
    //    private static List<RlpType> asRlpValues(
    //            CrosschainRawTransaction rawTransaction, Sign.SignatureData signatureData) {
    //
    //        List<RlpType> result = new ArrayList<>();
    //        result.add(RlpString.create(rawTransaction.getType()));
    //        result.add(RlpString.create(rawTransaction.getNonce()));
    //        result.add(RlpString.create(rawTransaction.getGasPrice()));
    //        result.add(RlpString.create(rawTransaction.getGasLimit()));
    //
    //        // an empty to address (contract creation) should not be encoded as a numeric 0 value
    //        String to = rawTransaction.getTo();
    //        if (to != null && to.length() > 0) {
    //            // addresses that start with zeros should be encoded with the zeros included, not
    //            // as numeric values
    //            result.add(RlpString.create(Numeric.hexStringToByteArray(to)));
    //        } else {
    //            result.add(RlpString.create(""));
    //        }
    //
    //        result.add(RlpString.create(rawTransaction.getValue()));
    //
    //        // value field will already be hex encoded, so we need to convert into binary first
    //        byte[] data = Numeric.hexStringToByteArray(rawTransaction.getData());
    //        result.add(RlpString.create(data));
    //
    //        // If there are any subordinate transactions or views, add them here as an RLP Array.
    //        byte[][] subordinateTransactionsAndViews =
    // rawTransaction.subordinateTransactionsAndViews;
    //        List<RlpType> rlpSubordinateTransactionsAndViews = new ArrayList<>();
    //        if (subordinateTransactionsAndViews != null) {
    //            for (byte[] signedTransactionOrView : subordinateTransactionsAndViews) {
    //
    // rlpSubordinateTransactionsAndViews.add(RlpString.create(signedTransactionOrView));
    //            }
    //        }
    //        RlpList rlpListSubordinateTransactionsAndViews =
    //                new RlpList(rlpSubordinateTransactionsAndViews);
    //        result.add(rlpListSubordinateTransactionsAndViews);
    //
    //        if (signatureData != null) {
    //            result.add(RlpString.create(signatureData.getV()));
    //            result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getR())));
    //            result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getS())));
    //        }
    //
    //        return result;
    //    }
}
