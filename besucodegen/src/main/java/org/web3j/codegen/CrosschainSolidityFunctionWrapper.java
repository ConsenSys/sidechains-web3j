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
package org.web3j.codegen;
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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.besu.Besu;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.CrosschainContext;
import org.web3j.tx.CrosschainTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.utils.Strings;

/** Generate Java Classes based on generated Solidity bin and abi files. */
public class CrosschainSolidityFunctionWrapper extends SolidityFunctionWrapper {

    private static final String BESU = "besu";
    private static final String CROSSCHAIN_TRANSACTION_MANAGER = "crosschainTransactionManager";
    private static final String CROSSCHAIN_CONTEXT = "crosschainContext";

    private static final String CODEGEN_WARNING =
            "<p>Auto generated code.\n"
                    + "<p><strong>Do not modify!</strong>\n"
                    + "<p>Please use the "
                    + CrosschainSolidityFunctionWrapperGenerator.class.getName()
                    + " in the \n"
                    + "<a href=\"https://github.com/PegaSysEng/sidechains-web3j/tree/master/besucodegen\">"
                    + "codegen module</a> to update.\n";

    private static final ClassName LOG = ClassName.get(Log.class);
    private static final Logger LOGGER =
            LoggerFactory.getLogger(CrosschainSolidityFunctionWrapper.class);

    public CrosschainSolidityFunctionWrapper(
            boolean useNativeJavaTypes,
            boolean useJavaPrimitiveTypes,
            boolean generateSendTxForCalls,
            int addressLength) {
        this(
                useNativeJavaTypes,
                useJavaPrimitiveTypes,
                generateSendTxForCalls,
                addressLength,
                new LogGenerationReporter(LOGGER));
    }

    public CrosschainSolidityFunctionWrapper(
            boolean useNativeJavaTypes,
            boolean useJavaPrimitiveTypes,
            boolean generateSendTxForCalls,
            int addressLength,
            GenerationReporter reporter) {
        super(
                useNativeJavaTypes,
                useJavaPrimitiveTypes,
                generateSendTxForCalls,
                addressLength,
                reporter,
                Besu.class,
                BESU,
                CrosschainTransactionManager.class,
                CROSSCHAIN_TRANSACTION_MANAGER);
    }

    public void generateJavaFiles(
            Class<? extends Contract> contractClass,
            String contractName,
            String bin,
            List<AbiDefinition> abi,
            String destinationDir,
            String basePackageName,
            Map<String, String> addresses)
            throws IOException, ClassNotFoundException {

        if (!java.lang.reflect.Modifier.isAbstract(contractClass.getModifiers())) {
            throw new IllegalArgumentException("Contract base class must be abstract");
        }

        String className = Strings.capitaliseFirstLetter(contractName);
        TypeSpec.Builder classBuilder = createClassBuilder(contractClass, className, bin);

        classBuilder.addAnnotation(
                AnnotationSpec.builder(SuppressWarnings.class)
                        .addMember("value", "\"rawtypes\"")
                        .build());

        classBuilder.addMethod(
                buildConstructor(
                        this.transactionManagerClass, this.transactoinManagerVariableName, false));
        classBuilder.addMethod(
                buildConstructor(
                        this.transactionManagerClass, this.transactoinManagerVariableName, true));
        classBuilder.addFields(buildFuncNameConstants(abi));

        // Build the functions for transactions, views, and events.
        classBuilder.addMethods(buildFunctionDefinitions(className, classBuilder, abi));

        classBuilder.addMethod(
                buildLoad(
                        className,
                        this.transactionManagerClass,
                        this.transactoinManagerVariableName,
                        false));
        classBuilder.addMethod(
                buildLoad(
                        className,
                        this.transactionManagerClass,
                        this.transactoinManagerVariableName,
                        true));
        if (!bin.equals(Contract.BIN_NOT_PROVIDED)) {
            classBuilder.addMethods(buildDeployMethods(className, classBuilder, abi));
        }

        addAddressesSupport(classBuilder, addresses);

        write(basePackageName, classBuilder.build(), destinationDir);
    }

    protected TypeSpec.Builder createClassBuilder(
            Class<? extends Contract> contractClass, String className, String binary) {

        String javadoc = CODEGEN_WARNING + getWeb3jVersion();

        return TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc(javadoc)
                .superclass(contractClass)
                .addField(createBinaryDefinition(binary));
    }

    List<MethodSpec> buildDeployMethods(
            String className,
            TypeSpec.Builder classBuilder,
            List<AbiDefinition> functionDefinitions)
            throws ClassNotFoundException {
        boolean constructor = false;
        List<MethodSpec> methodSpecs = new ArrayList<>();
        for (AbiDefinition functionDefinition : functionDefinitions) {
            if (functionDefinition.getType().equals("constructor")) {
                constructor = true;
                // buildDeployMethods(methodSpecs, className, functionDefinition, false, false);
                buildDeployMethods(methodSpecs, className, functionDefinition, true, false);
                if (!functionDefinition.isPayable()) {
                    buildDeployMethods(methodSpecs, className, functionDefinition, true, true);
                }

                // TODO get as signed subordinate lockable contract deploy
            }
        }

        // constructor will not be specified in ABI file if its empty
        if (!constructor) {
            // buildDeployMethods(methodSpecs, className, null, false, false);
            buildDeployMethods(methodSpecs, className, null, true, false);
            buildDeployMethods(methodSpecs, className, null, true, true);

            // TODO get as signed subordinate lockable contract deploy
        }

        return methodSpecs;
    }

    private void buildDeployMethods(
            List<MethodSpec> methodSpecs,
            String className,
            AbiDefinition functionDefinition,
            boolean lockableContractDeploy,
            boolean withSubordinateTransactionsAndViews)
            throws ClassNotFoundException {

        Class transactionManagerClass =
                lockableContractDeploy
                        ? CrosschainTransactionManager.class
                        : TransactionManager.class;

        methodSpecs.add(
                buildDeploy(
                        className,
                        functionDefinition,
                        transactionManagerClass,
                        TRANSACTION_MANAGER,
                        true,
                        lockableContractDeploy,
                        withSubordinateTransactionsAndViews));
        methodSpecs.add(
                buildDeploy(
                        className,
                        functionDefinition,
                        transactionManagerClass,
                        TRANSACTION_MANAGER,
                        false,
                        lockableContractDeploy,
                        withSubordinateTransactionsAndViews));
    }

    private MethodSpec buildDeploy(
            String className,
            AbiDefinition functionDefinition,
            Class authType,
            String authName,
            boolean withGasProvider,
            boolean lockableContractDeploy,
            boolean withSubordinateTransactionsAndViews)
            throws ClassNotFoundException {

        boolean withConstructor = functionDefinition != null;

        boolean isPayable = false;
        if (withConstructor) {
            isPayable = functionDefinition.isPayable();
        }

        MethodSpec.Builder methodBuilder =
                getDeployMethodSpec(
                        className,
                        authType,
                        authName,
                        isPayable,
                        withGasProvider,
                        lockableContractDeploy);
        boolean hasParams = false;
        String inputParams = null;
        if (withConstructor) {
            inputParams = addParameters(methodBuilder, functionDefinition.getInputs());
            hasParams = !inputParams.isEmpty();
        }

        if (withSubordinateTransactionsAndViews) {
            // Add crosschain context as an additional parameter.
            methodBuilder.addParameter(
                    ClassName.get(CrosschainContext.class), CROSSCHAIN_CONTEXT, Modifier.FINAL);
        }

        if (hasParams) {
            if (lockableContractDeploy) {
                return buildLockableContractDeployWithParams(
                        methodBuilder,
                        className,
                        inputParams,
                        authName,
                        isPayable,
                        withGasProvider,
                        withSubordinateTransactionsAndViews);
            } else {
                return buildDeployWithParams(
                        methodBuilder,
                        className,
                        inputParams,
                        authName,
                        isPayable,
                        withGasProvider);
            }
        } else {
            if (lockableContractDeploy) {
                return buildLockableContractDeployNoParams(
                        methodBuilder,
                        className,
                        authName,
                        isPayable,
                        withGasProvider,
                        withSubordinateTransactionsAndViews);
            } else {
                return buildDeployNoParams(
                        methodBuilder, className, authName, isPayable, withGasProvider);
            }
        }
    }

    private MethodSpec buildLockableContractDeployWithParams(
            MethodSpec.Builder methodBuilder,
            String className,
            String inputParams,
            String authName,
            boolean isPayable,
            boolean withGasProvider,
            boolean withSubordinateTransactionsAndViews) {

        if (!withSubordinateTransactionsAndViews) {
            methodBuilder.addStatement(
                    "$T $L = null", ClassName.get(CrosschainContext.class), CROSSCHAIN_CONTEXT);
        }
        methodBuilder.addStatement(
                "$T encodedConstructor = $T.encodeConstructor($T.<$T>asList($L))",
                String.class,
                FunctionEncoder.class,
                Arrays.class,
                Type.class,
                inputParams);
        if (isPayable && !withGasProvider) {
            methodBuilder.addStatement(
                    "return deployLockableContractRemoteCall("
                            + "$L.class, $L, $L, $L, $L, $L, encodedConstructor, $L, $L)",
                    className,
                    this.web3jVariableName,
                    authName,
                    GAS_PRICE,
                    GAS_LIMIT,
                    BINARY,
                    INITIAL_VALUE,
                    CROSSCHAIN_CONTEXT);
            methodBuilder.addAnnotation(Deprecated.class);
        } else if (isPayable && withGasProvider) {
            methodBuilder.addStatement(
                    "return deployLockableContractRemoteCall("
                            + "$L.class, $L, $L, $L, $L, encodedConstructor, $L, $L)",
                    className,
                    this.web3jVariableName,
                    authName,
                    CONTRACT_GAS_PROVIDER,
                    BINARY,
                    INITIAL_VALUE,
                    CROSSCHAIN_CONTEXT);
        } else if (!isPayable && !withGasProvider) {
            methodBuilder.addStatement(
                    "return deployLockableContractRemoteCall($L.class, $L, $L, $L, $L, $L, encodedConstructor, $L)",
                    className,
                    this.web3jVariableName,
                    authName,
                    GAS_PRICE,
                    GAS_LIMIT,
                    BINARY,
                    CROSSCHAIN_CONTEXT);
            methodBuilder.addAnnotation(Deprecated.class);
        } else {
            methodBuilder.addStatement(
                    "return deployLockableContractRemoteCall($L.class, $L, $L, $L, $L, encodedConstructor, $L)",
                    className,
                    this.web3jVariableName,
                    authName,
                    CONTRACT_GAS_PROVIDER,
                    BINARY,
                    CROSSCHAIN_CONTEXT);
        }

        return methodBuilder.build();
    }

    private MethodSpec buildLockableContractDeployNoParams(
            MethodSpec.Builder methodBuilder,
            String className,
            String authName,
            boolean isPayable,
            boolean withGasProvider,
            boolean withSubordinateTransactionsAndViews) {
        if (!withSubordinateTransactionsAndViews) {
            methodBuilder.addStatement(
                    "$T $L = null", ClassName.get(CrosschainContext.class), CROSSCHAIN_CONTEXT);
        }
        if (isPayable && !withGasProvider) {
            methodBuilder.addStatement(
                    "return deployLockableContractRemoteCall($L.class, $L, $L, $L, $L, $L, \"\", $L, $L)",
                    className,
                    this.web3jVariableName,
                    authName,
                    GAS_PRICE,
                    GAS_LIMIT,
                    BINARY,
                    INITIAL_VALUE,
                    CROSSCHAIN_CONTEXT);
            methodBuilder.addAnnotation(Deprecated.class);
        } else if (isPayable && withGasProvider) {
            methodBuilder.addStatement(
                    "return deployLockableContractRemoteCall($L.class, $L, $L, $L, $L, \"\", $L, $L)",
                    className,
                    this.web3jVariableName,
                    authName,
                    CONTRACT_GAS_PROVIDER,
                    BINARY,
                    INITIAL_VALUE,
                    CROSSCHAIN_CONTEXT);
        } else if (!isPayable && !withGasProvider) {
            methodBuilder.addStatement(
                    "return deployLockableContractRemoteCall($L.class, $L, $L, $L, $L, $L, \"\", $L)",
                    className,
                    this.web3jVariableName,
                    authName,
                    GAS_PRICE,
                    GAS_LIMIT,
                    BINARY,
                    CROSSCHAIN_CONTEXT);
            methodBuilder.addAnnotation(Deprecated.class);
        } else {
            methodBuilder.addStatement(
                    "return deployLockableContractRemoteCall($L.class, $L, $L, $L, $L, \"\", $L)",
                    className,
                    this.web3jVariableName,
                    authName,
                    CONTRACT_GAS_PROVIDER,
                    BINARY,
                    CROSSCHAIN_CONTEXT);
        }

        return methodBuilder.build();
    }

    List<MethodSpec> buildFunctions(AbiDefinition functionDefinition)
            throws ClassNotFoundException {
        return buildFunctions(functionDefinition, true);
    }

    List<MethodSpec> buildFunctions(AbiDefinition functionDefinition, boolean useUpperCase)
            throws ClassNotFoundException {

        List<MethodSpec> results = new ArrayList<>(2);
        String functionName = functionDefinition.getName();

        if (generateSendTxForCalls) {
            final String funcNamePrefix;
            if (functionDefinition.isConstant()) {
                funcNamePrefix = "call";
            } else {
                funcNamePrefix = "send";
            }
            // Prefix function name to avoid naming collision
            functionName = funcNamePrefix + "_" + functionName;
        } else {
            // If the solidity function name is a reserved word
            // in the current java version prepend it with "_"
            if (!SourceVersion.isName(functionName)) {
                functionName = "_" + functionName;
            }
        }

        MethodSpec.Builder methodBuilder =
                MethodSpec.methodBuilder(functionName).addModifiers(Modifier.PUBLIC);

        String inputParams = addParameters(methodBuilder, functionDefinition.getInputs());
        final List<TypeName> outputParameterTypes =
                buildTypeNames(functionDefinition.getOutputs(), useJavaPrimitiveTypes);

        if (functionDefinition.isConstant()) {
            // Avoid generating runtime exception call
            if (functionDefinition.hasOutputs()) {
                // Create the constant call / view for a single chain call.
                buildConstantFunction(
                        functionDefinition,
                        methodBuilder,
                        outputParameterTypes,
                        inputParams,
                        useUpperCase);
                results.add(methodBuilder.build());

                // Create the get as a signed subordinate view method.
                functionName = functionDefinition.getName() + "_AsSignedCrosschainSubordinateView";
                methodBuilder =
                        MethodSpec.methodBuilder(functionName).addModifiers(Modifier.PUBLIC);
                inputParams = addParameters(methodBuilder, functionDefinition.getInputs());
                // Add crosschain context as an additional parameter.
                methodBuilder.addParameter(
                        ClassName.get(CrosschainContext.class), CROSSCHAIN_CONTEXT, Modifier.FINAL);
                methodBuilder.addException(IOException.class);
                buildConstantFunctionAsSubordinateView(
                        functionDefinition,
                        methodBuilder,
                        outputParameterTypes,
                        inputParams,
                        useUpperCase);
                results.add(methodBuilder.build());
            }
            if (generateSendTxForCalls) {
                AbiDefinition sendFuncDefinition = new AbiDefinition(functionDefinition);
                sendFuncDefinition.setConstant(false);
                results.addAll(buildFunctions(sendFuncDefinition));
            }
        } else {
            // Create the single blockchain transaction function.
            buildTransactionFunction(functionDefinition, methodBuilder, inputParams, useUpperCase);
            results.add(methodBuilder.build());

            if (!functionDefinition.isPayable()) {
                // Create the function as a signed subordinate transaction method.
                functionName =
                        functionDefinition.getName() + "_AsSignedCrosschainSubordinateTransaction";
                methodBuilder =
                        MethodSpec.methodBuilder(functionName).addModifiers(Modifier.PUBLIC);
                inputParams = addParameters(methodBuilder, functionDefinition.getInputs());
                // Add crosschain context as an additional parameter.
                methodBuilder.addParameter(
                        ClassName.get(CrosschainContext.class), CROSSCHAIN_CONTEXT, Modifier.FINAL);
                methodBuilder.addException(IOException.class);
                buildTransactionFunctionAsSubordinateTransaction(
                        functionDefinition, methodBuilder, inputParams, useUpperCase);
                results.add(methodBuilder.build());

                // Create the function as an originating crosschain transaction method.
                functionName = functionDefinition.getName() + "_AsCrosschainOriginatingTransaction";
                methodBuilder =
                        MethodSpec.methodBuilder(functionName).addModifiers(Modifier.PUBLIC);
                inputParams = addParameters(methodBuilder, functionDefinition.getInputs());
                // Add crosschain context as an additional parameter.
                methodBuilder.addParameter(
                        ClassName.get(CrosschainContext.class), CROSSCHAIN_CONTEXT, Modifier.FINAL);
                buildTransactionFunctionAsOriginatingTransaction(
                        functionDefinition, methodBuilder, inputParams, useUpperCase);
                results.add(methodBuilder.build());
            } else {
                System.out.println(
                        "UNIMPLEMENTED: requested wrapper for payable transaction: "
                                + functionDefinition.getName());
            }
        }

        return results;
    }

    private void buildConstantFunctionAsSubordinateView(
            AbiDefinition functionDefinition,
            MethodSpec.Builder methodBuilder,
            List<TypeName> outputParameterTypes,
            String inputParams,
            boolean useUpperCase) {
        String functionName = functionDefinition.getName();

        // Return the byte array representing the signed transaction
        methodBuilder.returns(ArrayTypeName.of(TypeName.BYTE));

        TypeName typeName = outputParameterTypes.get(0);

        methodBuilder.addStatement(
                "final $T function = "
                        + "new $T($N, \n$T.<$T>asList($L), "
                        + "\n$T.<$T<?>>asList(new $T<$T>() {}))",
                Function.class,
                Function.class,
                funcNameToConst(functionName, useUpperCase),
                Arrays.class,
                Type.class,
                inputParams,
                Arrays.class,
                TypeReference.class,
                TypeReference.class,
                typeName);

        methodBuilder.addStatement(
                "return createSignedSubordinateView(function, " + CROSSCHAIN_CONTEXT + ")");
    }

    private void buildTransactionFunctionAsSubordinateTransaction(
            AbiDefinition functionDefinition,
            MethodSpec.Builder methodBuilder,
            String inputParams,
            boolean useUpperCase) {
        String weiParam = "";
        if (functionDefinition.isPayable()) {
            methodBuilder.addParameter(BigInteger.class, WEI_VALUE);
            weiParam = ", " + WEI_VALUE;
        }

        String functionName = functionDefinition.getName();

        // Return the byte array representing the signed transaction
        methodBuilder.returns(ArrayTypeName.of(TypeName.BYTE));

        methodBuilder.addStatement(
                "final $T function = new $T(\n$N, \n$T.<$T>asList($L), \n$T"
                        + ".<$T<?>>emptyList())",
                Function.class,
                Function.class,
                funcNameToConst(functionName, useUpperCase),
                Arrays.class,
                Type.class,
                inputParams,
                Collections.class,
                TypeReference.class);

        methodBuilder.addStatement(
                "return createSignedSubordinateTransaction(function, "
                        + CROSSCHAIN_CONTEXT
                        + weiParam
                        + ")");
    }

    private void buildTransactionFunctionAsOriginatingTransaction(
            AbiDefinition functionDefinition,
            MethodSpec.Builder methodBuilder,
            String inputParams,
            boolean useUpperCase) {
        String weiParam = "";
        if (functionDefinition.isPayable()) {
            methodBuilder.addParameter(BigInteger.class, WEI_VALUE);
            weiParam = ", " + WEI_VALUE;
        }

        String functionName = functionDefinition.getName();

        methodBuilder.returns(buildRemoteFunctionCall(TypeName.get(TransactionReceipt.class)));

        methodBuilder.addStatement(
                "final $T function = new $T(\n$N, \n$T.<$T>asList($L), \n$T"
                        + ".<$T<?>>emptyList())",
                Function.class,
                Function.class,
                funcNameToConst(functionName, useUpperCase),
                Arrays.class,
                Type.class,
                inputParams,
                Collections.class,
                TypeReference.class);

        methodBuilder.addStatement(
                "return executeRemoteCallCrosschainTransaction(function, "
                        + CROSSCHAIN_CONTEXT
                        + weiParam
                        + ")");
    }
}
