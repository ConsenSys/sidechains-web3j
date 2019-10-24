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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.math.BigInteger;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.ens.EnsResolver;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.besu.Besu;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

public abstract class CrosschainContract extends Contract {
    @Deprecated
    protected CrosschainContract(
            String contractBinary,
            String contractAddress,
            Besu besu,
            CrosschainTransactionManager transactionManager,
            BigInteger gasPrice,
            BigInteger gasLimit) {
        super(contractBinary, contractAddress, besu, transactionManager, gasPrice, gasLimit);
    }

    protected CrosschainContract(
            String contractBinary,
            String contractAddress,
            Web3j web3j,
            CrosschainTransactionManager transactionManager,
            ContractGasProvider gasProvider) {

        super(contractBinary, contractAddress, web3j, transactionManager, gasProvider);
    }

    protected CrosschainContract(
            EnsResolver ensResolver,
            String contractBinary,
            String contractAddress,
            Web3j web3j,
            TransactionManager transactionManager,
            ContractGasProvider gasProvider) {
        super(ensResolver, contractBinary, contractAddress, web3j, transactionManager, gasProvider);
    }

    protected byte[] createSignedSubordinateView(Function function, byte[][] nestedSubordinateViews)
            throws IOException {
        String method = function.getName();
        BigInteger weiValue = BigInteger.ZERO;
        BigInteger gasPrice = this.gasProvider.getGasPrice(method);
        BigInteger gasLimit = this.gasProvider.getGasLimit(method);

        return ((CrosschainTransactionManager) this.transactionManager)
                .createSignedSubordinateView(
                        gasPrice,
                        gasLimit,
                        contractAddress,
                        FunctionEncoder.encode(function),
                        weiValue,
                        nestedSubordinateViews);
    }

    protected byte[] createSignedSubordinateTransaction(
            Function function, byte[][] nestedSubordinateTransactionsAndViews) throws IOException {
        return createSignedSubordinateTransaction(
                function, nestedSubordinateTransactionsAndViews, BigInteger.ZERO);
    }

    protected byte[] createSignedSubordinateTransaction(
            Function function, byte[][] nestedSubordinateTransactionsAndViews, BigInteger weiValue)
            throws IOException {
        String method = function.getName();
        BigInteger gasPrice = this.gasProvider.getGasPrice(method);
        BigInteger gasLimit = this.gasProvider.getGasLimit(method);

        return ((CrosschainTransactionManager) this.transactionManager)
                .createSignedSubordinateTransaction(
                        gasPrice,
                        gasLimit,
                        contractAddress,
                        FunctionEncoder.encode(function),
                        weiValue,
                        nestedSubordinateTransactionsAndViews);
    }

    protected RemoteFunctionCall<TransactionReceipt> executeRemoteCallCrosschainTransaction(
            Function function, byte[][] subordinateTransactionsAndViews) {
        return executeRemoteCallCrosschainTransaction(
                function, subordinateTransactionsAndViews, BigInteger.ZERO);
    }

    protected RemoteFunctionCall<TransactionReceipt> executeRemoteCallCrosschainTransaction(
            Function function, byte[][] subordinateTransactionsAndViews, BigInteger weiValue) {
        return new RemoteFunctionCall<>(
                function,
                () ->
                        executeCrossChainTransaction(
                                function, subordinateTransactionsAndViews, weiValue));
    }

    protected TransactionReceipt executeCrossChainTransaction(
            Function function, byte[][] subordinateTransactionsAndViews, BigInteger weiValue)
            throws TransactionException, IOException {
        String method = function.getName();
        BigInteger gasPrice = this.gasProvider.getGasPrice(method);
        BigInteger gasLimit = this.gasProvider.getGasLimit(method);

        return ((CrosschainTransactionManager) this.transactionManager)
                .executeCrosschainTransaction(
                        gasPrice,
                        gasLimit,
                        contractAddress,
                        FunctionEncoder.encode(function),
                        weiValue,
                        subordinateTransactionsAndViews);
    }

    protected static <T extends Contract> RemoteCall<T> deployLockableContractRemoteCall(
            Class<T> type,
            Web3j web3j,
            CrosschainTransactionManager transactionManager,
            ContractGasProvider contractGasProvider,
            String binary,
            String encodedConstructor,
            byte[][] subordinateTransactionsAndViews) {
        return deployLockableRemoteCall(
                type,
                web3j,
                transactionManager,
                contractGasProvider,
                binary,
                encodedConstructor,
                BigInteger.ZERO,
                subordinateTransactionsAndViews);
    }

    protected static <T extends Contract> RemoteCall<T> deployLockableContractRemoteCall(
            Class<T> type,
            Web3j web3j,
            CrosschainTransactionManager transactionManager,
            ContractGasProvider contractGasProvider,
            String binary,
            byte[][] subordinateTransactionsAndViews) {
        return deployLockableRemoteCall(
                type,
                web3j,
                transactionManager,
                contractGasProvider,
                binary,
                "",
                BigInteger.ZERO,
                subordinateTransactionsAndViews);
    }

    protected static <T extends Contract> RemoteCall<T> deployLockableContractRemoteCall(
            Class<T> type,
            Web3j web3j,
            CrosschainTransactionManager transactionManager,
            BigInteger gasPrice,
            BigInteger gasLimit,
            String binary,
            String encodedConstructor,
            byte[][] subordinateTransactionsAndViews) {
        return deployLockableRemoteCall(
                type,
                web3j,
                transactionManager,
                new StaticGasProvider(gasPrice, gasLimit),
                binary,
                encodedConstructor,
                BigInteger.ZERO,
                subordinateTransactionsAndViews);
    }

    protected static <T extends Contract> RemoteCall<T> deployLockableContractRemoteCall(
            Class<T> type,
            Web3j web3j,
            CrosschainTransactionManager transactionManager,
            BigInteger gasPrice,
            BigInteger gasLimit,
            String binary,
            byte[][] subordinateTransactionsAndViews) {
        return deployLockableRemoteCall(
                type,
                web3j,
                transactionManager,
                new StaticGasProvider(gasPrice, gasLimit),
                binary,
                "",
                BigInteger.ZERO,
                subordinateTransactionsAndViews);
    }

    protected static <T extends Contract> RemoteCall<T> deployLockableContractRemoteCall(
            Class<T> type,
            Web3j web3j,
            CrosschainTransactionManager transactionManager,
            ContractGasProvider contractGasProvider,
            String binary,
            String encodedConstructor,
            BigInteger initialWeiValue,
            byte[][] subordinateTransactionsAndViews) {
        return deployLockableRemoteCall(
                type,
                web3j,
                transactionManager,
                contractGasProvider,
                binary,
                encodedConstructor,
                initialWeiValue,
                subordinateTransactionsAndViews);
    }

    protected static <T extends Contract> RemoteCall<T> deployLockableContractRemoteCall(
            Class<T> type,
            Web3j web3j,
            CrosschainTransactionManager transactionManager,
            ContractGasProvider contractGasProvider,
            String binary,
            BigInteger initialWeiValue,
            byte[][] subordinateTransactionsAndViews) {
        return deployLockableRemoteCall(
                type,
                web3j,
                transactionManager,
                contractGasProvider,
                binary,
                "",
                initialWeiValue,
                subordinateTransactionsAndViews);
    }

    protected static <T extends Contract> RemoteCall<T> deployLockableContractRemoteCall(
            Class<T> type,
            Web3j web3j,
            CrosschainTransactionManager transactionManager,
            BigInteger gasPrice,
            BigInteger gasLimit,
            String binary,
            String encodedConstructor,
            BigInteger initialWeiValue,
            byte[][] subordinateTransactionsAndViews) {
        return deployLockableRemoteCall(
                type,
                web3j,
                transactionManager,
                new StaticGasProvider(gasPrice, gasLimit),
                binary,
                encodedConstructor,
                initialWeiValue,
                subordinateTransactionsAndViews);
    }

    protected static <T extends Contract> RemoteCall<T> deployLockableContractRemoteCall(
            Class<T> type,
            Web3j web3j,
            CrosschainTransactionManager transactionManager,
            BigInteger gasPrice,
            BigInteger gasLimit,
            String binary,
            BigInteger initialWeiValue,
            byte[][] subordinateTransactionsAndViews) {
        return deployLockableRemoteCall(
                type,
                web3j,
                transactionManager,
                new StaticGasProvider(gasPrice, gasLimit),
                binary,
                "",
                initialWeiValue,
                subordinateTransactionsAndViews);
    }

    protected static <T extends Contract> RemoteCall<T> deployLockableRemoteCall(
            Class<T> type,
            Web3j web3j,
            CrosschainTransactionManager transactionManager,
            ContractGasProvider contractGasProvider,
            String binary,
            String encodedConstructor,
            BigInteger value) {
        return new RemoteCall<>(
                () ->
                        deploy(
                                type,
                                web3j,
                                transactionManager,
                                contractGasProvider,
                                binary,
                                encodedConstructor,
                                value,
                                null));
    }

    protected static <T extends Contract> RemoteCall<T> deployLockableRemoteCall(
            Class<T> type,
            Web3j web3j,
            CrosschainTransactionManager transactionManager,
            ContractGasProvider contractGasProvider,
            String binary,
            String encodedConstructor,
            BigInteger value,
            byte[][] subordinateTransactionsAndViews) {
        return new RemoteCall<>(
                () ->
                        deploy(
                                type,
                                web3j,
                                transactionManager,
                                contractGasProvider,
                                binary,
                                encodedConstructor,
                                value,
                                subordinateTransactionsAndViews));
    }

    private static <T extends Contract> T deploy(
            Class<T> type,
            Web3j web3j,
            CrosschainTransactionManager transactionManager,
            ContractGasProvider contractGasProvider,
            String binary,
            String encodedConstructor,
            BigInteger value,
            byte[][] subordinateTransactionsAndViews)
            throws RuntimeException, TransactionException {

        try {
            Constructor<T> constructor =
                    type.getDeclaredConstructor(
                            String.class,
                            Besu.class,
                            CrosschainTransactionManager.class,
                            ContractGasProvider.class);
            constructor.setAccessible(true);

            // we want to use null here to ensure that "to" parameter on message is not populated
            T contract =
                    constructor.newInstance(null, web3j, transactionManager, contractGasProvider);

            return create(
                    contract,
                    contractGasProvider,
                    binary,
                    encodedConstructor,
                    value,
                    subordinateTransactionsAndViews);
        } catch (TransactionException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T extends Contract> T create(
            T contract,
            ContractGasProvider contractGasProvider,
            String binary,
            String encodedConstructor,
            BigInteger value,
            byte[][] subordinateTransactionsAndViews)
            throws IOException, TransactionException {

        String data = binary + encodedConstructor;
        String method = "deploy";
        BigInteger gasPrice = contractGasProvider.getGasPrice(method);
        BigInteger gasLimit = contractGasProvider.getGasLimit(method);

        // Note: use the "to" parameter from her on to ensure not too many changes needed when
        // merging this code with Web3J
        TransactionReceipt transactionReceipt =
                ((CrosschainTransactionManager) contract.transactionManager)
                        .executeLockableContractDeploy(
                                gasPrice, gasLimit, data, value, subordinateTransactionsAndViews);

        String contractAddress = transactionReceipt.getContractAddress();
        if (contractAddress == null) {
            throw new RuntimeException("Empty contract address returned");
        }
        contract.setContractAddress(contractAddress);
        contract.setTransactionReceipt(transactionReceipt);

        return contract;
    }
}
