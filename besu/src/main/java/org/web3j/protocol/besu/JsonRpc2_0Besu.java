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
package org.web3j.protocol.besu;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.web3j.protocol.Web3jService;
import org.web3j.protocol.admin.methods.response.BooleanResponse;
import org.web3j.protocol.besu.crypto.crosschain.BlsThresholdCryptoSystem;
import org.web3j.protocol.besu.request.CreatePrivacyGroupRequest;
import org.web3j.protocol.besu.response.BesuEthAccountsMapResponse;
import org.web3j.protocol.besu.response.BesuFullDebugTraceResponse;
import org.web3j.protocol.besu.response.crosschain.CrossListMultichainNodesResponse;
import org.web3j.protocol.besu.response.crosschain.CrosschainCheckUnlock;
import org.web3j.protocol.besu.response.crosschain.CrosschainIsLockable;
import org.web3j.protocol.besu.response.crosschain.CrosschainIsLocked;
import org.web3j.protocol.besu.response.crosschain.CrosschainProcessSubordinateView;
import org.web3j.protocol.besu.response.crosschain.LongResponse;
import org.web3j.protocol.besu.response.crosschain.VoidResponse;
import org.web3j.protocol.besu.response.privacy.PrivCreatePrivacyGroup;
import org.web3j.protocol.besu.response.privacy.PrivFindPrivacyGroup;
import org.web3j.protocol.besu.response.privacy.PrivGetPrivacyPrecompileAddress;
import org.web3j.protocol.besu.response.privacy.PrivGetPrivateTransaction;
import org.web3j.protocol.besu.response.privacy.PrivGetTransactionReceipt;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthAccounts;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.MinerStartResponse;
import org.web3j.protocol.eea.JsonRpc2_0Eea;
import org.web3j.utils.Base64String;

import static java.util.Objects.requireNonNull;

public class JsonRpc2_0Besu extends JsonRpc2_0Eea implements Besu {
    public JsonRpc2_0Besu(Web3jService web3jService) {
        super(web3jService);
    }

    public JsonRpc2_0Besu(Web3jService web3jService, long pollingInterval) {
        super(web3jService, pollingInterval);
    }

    @Override
    public Request<?, MinerStartResponse> minerStart() {
        return new Request<>(
                "miner_start",
                Collections.<String>emptyList(),
                web3jService,
                MinerStartResponse.class);
    }

    @Override
    public Request<?, BooleanResponse> minerStop() {
        return new Request<>(
                "miner_stop", Collections.<String>emptyList(), web3jService, BooleanResponse.class);
    }

    @Override
    public Request<?, BooleanResponse> cliqueDiscard(String address) {
        return new Request<>(
                "clique_discard", Arrays.asList(address), web3jService, BooleanResponse.class);
    }

    @Override
    public Request<?, EthAccounts> cliqueGetSigners(DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "clique_getSigners",
                Arrays.asList(defaultBlockParameter.getValue()),
                web3jService,
                EthAccounts.class);
    }

    @Override
    public Request<?, EthAccounts> cliqueGetSignersAtHash(String blockHash) {
        return new Request<>(
                "clique_getSignersAtHash",
                Arrays.asList(blockHash),
                web3jService,
                EthAccounts.class);
    }

    @Override
    public Request<?, BooleanResponse> cliquePropose(String address, Boolean signerAddition) {
        return new Request<>(
                "clique_propose",
                Arrays.asList(address, signerAddition),
                web3jService,
                BooleanResponse.class);
    }

    @Override
    public Request<?, BesuEthAccountsMapResponse> cliqueProposals() {
        return new Request<>(
                "clique_proposals",
                Collections.<String>emptyList(),
                web3jService,
                BesuEthAccountsMapResponse.class);
    }

    @Override
    public Request<?, BesuFullDebugTraceResponse> debugTraceTransaction(
            String transactionHash, Map<String, Boolean> options) {
        return new Request<>(
                "debug_traceTransaction",
                Arrays.asList(transactionHash, options),
                web3jService,
                BesuFullDebugTraceResponse.class);
    }

    @Override
    public Request<?, EthGetTransactionCount> privGetTransactionCount(
            final String address, final Base64String privacyGroupId) {
        return new Request<>(
                "priv_getTransactionCount",
                Arrays.asList(address, privacyGroupId.toString()),
                web3jService,
                EthGetTransactionCount.class);
    }

    @Override
    public Request<?, PrivGetPrivateTransaction> privGetPrivateTransaction(
            final String transactionHash) {
        return new Request<>(
                "priv_getPrivateTransaction",
                Collections.singletonList(transactionHash),
                web3jService,
                PrivGetPrivateTransaction.class);
    }

    @Override
    public Request<?, PrivGetPrivacyPrecompileAddress> privGetPrivacyPrecompileAddress() {
        return new Request<>(
                "priv_getPrivacyPrecompileAddress",
                Collections.emptyList(),
                web3jService,
                PrivGetPrivacyPrecompileAddress.class);
    }

    @Override
    public Request<?, PrivCreatePrivacyGroup> privCreatePrivacyGroup(
            final List<Base64String> addresses, final String name, final String description) {
        requireNonNull(addresses);
        return new Request<>(
                "priv_createPrivacyGroup",
                Collections.singletonList(
                        new CreatePrivacyGroupRequest(addresses, name, description)),
                web3jService,
                PrivCreatePrivacyGroup.class);
    }

    @Override
    public Request<?, PrivFindPrivacyGroup> privFindPrivacyGroup(
            final List<Base64String> addresses) {
        return new Request<>(
                "priv_findPrivacyGroup",
                Collections.singletonList(addresses),
                web3jService,
                PrivFindPrivacyGroup.class);
    }

    @Override
    public Request<?, BooleanResponse> privDeletePrivacyGroup(final Base64String privacyGroupId) {
        return new Request<>(
                "priv_deletePrivacyGroup",
                Collections.singletonList(privacyGroupId.toString()),
                web3jService,
                BooleanResponse.class);
    }

    @Override
    public Request<?, PrivGetTransactionReceipt> privGetTransactionReceipt(
            final String transactionHash) {
        return new Request<>(
                "priv_getTransactionReceipt",
                Collections.singletonList(transactionHash),
                web3jService,
                PrivGetTransactionReceipt.class);
    }

    public Request<?, EthSendTransaction> crosschainSendCrossChainRawTransaction(
            String signedTransactionData) {
        return new Request<>(
                "cross_sendRawCrosschainTransaction",
                Arrays.asList(signedTransactionData),
                web3jService,
                EthSendTransaction.class);
    }

    public Request<?, CrosschainProcessSubordinateView> crosschainProcessSubordinateView(
            String signedTransactionData) {
        return new Request<>(
                "cross_processSubordinateView",
                Arrays.asList(signedTransactionData),
                web3jService,
                CrosschainProcessSubordinateView.class);
    }

    public Request<?, CrosschainIsLockable> crosschainIsLockable(
            String address, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "cross_isLockable",
                Arrays.asList(address, defaultBlockParameter.getValue()),
                web3jService,
                CrosschainIsLockable.class);
    }

    public Request<?, CrosschainIsLocked> crosschainIsLocked(
            String address, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "cross_isLocked",
                Arrays.asList(address, defaultBlockParameter.getValue()),
                web3jService,
                CrosschainIsLocked.class);
    }

    public Request<?, CrosschainCheckUnlock> crosschainCheckUnlock(String address) {
        return new Request<>(
                "cross_checkUnlock",
                Arrays.asList(address),
                web3jService,
                CrosschainCheckUnlock.class);
    }

    public Request<?, VoidResponse> crossAddMultichainNode(
            final BigInteger blockchainId, final String ipAddressAndPort) {
        return new Request<>(
                "cross_addMultichainNode",
                Arrays.asList(blockchainId, ipAddressAndPort),
                web3jService,
                VoidResponse.class);
    }

    public Request<?, CrossListMultichainNodesResponse> crossListMultichainNodes() {
        return new Request<>(
                "cross_listMultichainNodes",
                Collections.<String>emptyList(),
                web3jService,
                CrossListMultichainNodesResponse.class);
    }

    public Request<?, VoidResponse> crossRemoveMultichainNode(final BigInteger blockchainId) {
        return new Request<>(
                "cross_removeMultichainNode",
                Arrays.asList(blockchainId),
                web3jService,
                VoidResponse.class);
    }

    public Request<?, LongResponse> crossStartThresholdKeyGeneration(
            final int threshold, final BlsThresholdCryptoSystem cryptoSystem) {
        return new Request<>(
                "cross_startThresholdKeyGeneration",
                Arrays.asList(threshold, cryptoSystem),
                web3jService,
                LongResponse.class);
    }
}
