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

import java.io.IOException;
import java.math.BigInteger;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.ens.EnsResolver;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.gas.ContractGasProvider;

public abstract class CrosschainContract extends Contract {
    CrosschainTransactionManager crosschainTransactionManager;

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

        return this.crosschainTransactionManager.createSignedSubordinateView(
                gasPrice,
                gasLimit,
                contractAddress,
                FunctionEncoder.encode(function),
                weiValue,
                nestedSubordinateViews);
    }

    protected byte[] createSignedSubordinateTransaction(
            Function function, byte[][] nestedSubordinateTransactionsAndViews) throws IOException {
        String method = function.getName();
        BigInteger weiValue = BigInteger.ZERO;
        BigInteger gasPrice = this.gasProvider.getGasPrice(method);
        BigInteger gasLimit = this.gasProvider.getGasLimit(method);

        return this.crosschainTransactionManager.createSignedSubordinateTransaction(
                gasPrice,
                gasLimit,
                contractAddress,
                FunctionEncoder.encode(function),
                weiValue,
                nestedSubordinateTransactionsAndViews);
    }

    public TransactionReceipt executeCrossChainTransaction(
            Function function, byte[][] subordinateTransactionsAndViews)
            throws TransactionException, IOException {
        String method = function.getName();
        BigInteger weiValue = BigInteger.ZERO;
        BigInteger gasPrice = this.gasProvider.getGasPrice(method);
        BigInteger gasLimit = this.gasProvider.getGasLimit(method);

        return this.crosschainTransactionManager.executeCrosschainTransaction(
                gasPrice,
                gasLimit,
                contractAddress,
                FunctionEncoder.encode(function),
                weiValue,
                subordinateTransactionsAndViews);
    }

    //
    //    public byte[] getAsSignedLockableContractDeploy(byte[][] subordinateTransactionsAndViews)
    //            throws IOException {
    //
    //        // TODO I don't know what a contract with a constructor, which might take parameters,
    // is
    //        // supposed to look like.
    //        // As such, for the moment, have no constructor.
    //        //        final Function constructor = new Function(
    //        //                "init",
    //        //                Arrays.asList(constructorParams),
    //        //                Collections.<TypeReference<?>>emptyList());
    //
    //        String method = "deploy";
    //        BigInteger weiValue = BigInteger.ZERO;
    //        BigInteger gasPrice = contractGasProvider.getGasPrice(method);
    //        BigInteger gasLimit = contractGasProvider.getGasLimit(method);
    //
    //        return transactionManager.createSignedSubordinateDeployLockable(
    //                gasPrice,
    //                gasLimit,
    //                this.contractBinary // + FunctionEncoder.encode(constructor)
    //                ,
    //                weiValue,
    //                subordinateTransactionsAndViews);
    //    }
    //
    //
    //    // TODO try to use RemoteCall<T> syntax.
    //    public BigInteger executeCrossChainSubordianteViewUint256(
    //            String method, Uint256[] params, byte[][] subordinateTransactionsAndViews)
    //            throws IOException {
    //        final Function function =
    //                new Function(
    //                        method,
    //                        Arrays.asList(params),
    //                        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    //
    //        BigInteger weiValue = BigInteger.ZERO;
    //        BigInteger gasPrice = contractGasProvider.getGasPrice(method);
    //        BigInteger gasLimit = contractGasProvider.getGasLimit(method);
    //
    //        Uint256 result =
    //                transactionManager.executeSubordinateView(
    //                        gasPrice,
    //                        gasLimit,
    //                        contractAddress,
    //                        function,
    //                        weiValue,
    //                        subordinateTransactionsAndViews);
    //
    //        return result.getValue();
    //    }
    //
    //    public static <T extends Contract> RemoteCall<T> deployLockable(
    //            Class<T> type,
    //            Web3j web3j,
    //            CrosschainTransactionManager transactionManager,
    //            ContractGasProvider contractGasProvider,
    //            String binary,
    //            String encodedConstructor) {
    //        return deployLockableRemoteCall(
    //                type,
    //                web3j,
    //                transactionManager,
    //                contractGasProvider,
    //                binary,
    //                encodedConstructor,
    //                BigInteger.ZERO,
    //                null);
    //    }
    //
    //    public static <T extends Contract> RemoteCall<T> deployLockable(
    //            Class<T> type,
    //            Web3j web3j,
    //            CrosschainTransactionManager transactionManager,
    //            ContractGasProvider contractGasProvider,
    //            String binary,
    //            String encodedConstructor,
    //            byte[][] subordinateTransactionsAndViews) {
    //        return deployLockableRemoteCall(
    //                type,
    //                web3j,
    //                transactionManager,
    //                contractGasProvider,
    //                binary,
    //                encodedConstructor,
    //                BigInteger.ZERO,
    //                subordinateTransactionsAndViews);
    //    }
    //
    //    public static <T extends Contract> RemoteCall<T> deployLockable(
    //            Class<T> type,
    //            Web3j web3j,
    //            CrosschainTransactionManager transactionManager,
    //            ContractGasProvider contractGasProvider,
    //            String binary) {
    //        return deployLockableRemoteCall(
    //                type,
    //                web3j,
    //                transactionManager,
    //                contractGasProvider,
    //                binary,
    //                "",
    //                BigInteger.ZERO,
    //                null);
    //    }
    //
    //    public static <T extends Contract> RemoteCall<T> deployLockable(
    //            Class<T> type,
    //            Web3j web3j,
    //            CrosschainTransactionManager transactionManager,
    //            ContractGasProvider contractGasProvider,
    //            String binary,
    //            byte[][] subordinateTransactionsAndViews) {
    //        return deployLockableRemoteCall(
    //                type,
    //                web3j,
    //                transactionManager,
    //                contractGasProvider,
    //                binary,
    //                "",
    //                BigInteger.ZERO,
    //                subordinateTransactionsAndViews);
    //    }
    //
    //    public static <T extends Contract> RemoteCall<T> deployLockableRemoteCall(
    //            Class<T> type,
    //            Web3j web3j,
    //            CrosschainTransactionManager transactionManager,
    //            ContractGasProvider contractGasProvider,
    //            String binary,
    //            String encodedConstructor,
    //            BigInteger value) {
    //        return new RemoteCall<>(
    //                () ->
    //                        deploy(
    //                                type,
    //                                web3j,
    //                                transactionManager,
    //                                contractGasProvider,
    //                                binary,
    //                                encodedConstructor,
    //                                value,
    //                                null));
    //    }
    //
    //    public static <T extends Contract> RemoteCall<T> deployLockableRemoteCall(
    //            Class<T> type,
    //            Web3j web3j,
    //            CrosschainTransactionManager transactionManager,
    //            ContractGasProvider contractGasProvider,
    //            String binary,
    //            String encodedConstructor,
    //            BigInteger value,
    //            byte[][] subordinateTransactionsAndViews) {
    //        return new RemoteCall<>(
    //                () ->
    //                        deploy(
    //                                type,
    //                                web3j,
    //                                transactionManager,
    //                                contractGasProvider,
    //                                binary,
    //                                encodedConstructor,
    //                                value,
    //                                subordinateTransactionsAndViews));
    //    }
    //
    //    private static <T extends Contract> T deploy(
    //            Class<T> type,
    //            Web3j web3j,
    //            CrosschainTransactionManager transactionManager,
    //            ContractGasProvider contractGasProvider,
    //            String binary,
    //            String encodedConstructor,
    //            BigInteger value,
    //            byte[][] subordinateTransactionsAndViews)
    //            throws RuntimeException, TransactionException {
    //
    //        try {
    //            Constructor<T> constructor =
    //                    type.getDeclaredConstructor(
    //                            String.class,
    //                            Web3j.class,
    //                            TransactionManager.class,
    //                            ContractGasProvider.class);
    //            constructor.setAccessible(true);
    //
    //            // we want to use null here to ensure that "to" parameter on message is not
    // populated
    //            T contract =
    //                    constructor.newInstance(null, web3j, transactionManager,
    // contractGasProvider);
    //
    //            return create(
    //                    contract,
    //                    transactionManager,
    //                    contractGasProvider,
    //                    binary,
    //                    encodedConstructor,
    //                    value,
    //                    subordinateTransactionsAndViews);
    //        } catch (TransactionException e) {
    //            throw e;
    //        } catch (Exception e) {
    //            throw new RuntimeException(e);
    //        }
    //    }
    //
    //    private static <T extends Contract> T create(
    //            T contract,
    //            CrosschainTransactionManager transactionManager,
    //            ContractGasProvider contractGasProvider,
    //            String binary,
    //            String encodedConstructor,
    //            BigInteger value,
    //            byte[][] subordinateTransactionsAndViews)
    //            throws IOException, TransactionException {
    //
    //        String data = binary + encodedConstructor;
    //        String method = "deploy";
    //        BigInteger gasPrice = contractGasProvider.getGasPrice(method);
    //        BigInteger gasLimit = contractGasProvider.getGasLimit(method);
    //
    //        // Note: use the "to" parameter from her on to ensure not too many changes needed when
    //        // merging this code with Web3J
    //        TransactionReceipt transactionReceipt =
    //                transactionManager.executeLockableContractDeploy(
    //                        gasPrice, gasLimit, data, value, subordinateTransactionsAndViews);
    //
    //        String contractAddress = transactionReceipt.getContractAddress();
    //        if (contractAddress == null) {
    //            throw new RuntimeException("Empty contract address returned");
    //        }
    //        contract.setContractAddress(contractAddress);
    //        contract.setTransactionReceipt(transactionReceipt);
    //
    //        return contract;
    //    }
}
