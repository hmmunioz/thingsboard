/**
 * Copyright © 2016-2022 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.transport.lwm2m.client;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.elements.config.Configuration;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.californium.LeshanClientBuilder;
import org.eclipse.leshan.client.engine.DefaultRegistrationEngineFactory;
import org.eclipse.leshan.client.object.Security;
import org.eclipse.leshan.client.object.Server;
import org.eclipse.leshan.client.observer.LwM2mClientObserver;
import org.eclipse.leshan.client.resource.DummyInstanceEnabler;
import org.eclipse.leshan.client.resource.LwM2mInstanceEnabler;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.ResponseCode;
import org.eclipse.leshan.core.model.InvalidDDFFileException;
import org.eclipse.leshan.core.model.LwM2mModel;
import org.eclipse.leshan.core.model.ObjectLoader;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.StaticModel;
import org.eclipse.leshan.core.node.codec.DefaultLwM2mDecoder;
import org.eclipse.leshan.core.node.codec.DefaultLwM2mEncoder;
import org.eclipse.leshan.core.request.BootstrapRequest;
import org.eclipse.leshan.core.request.DeregisterRequest;
import org.eclipse.leshan.core.request.RegisterRequest;
import org.eclipse.leshan.core.request.UpdateRequest;
import org.junit.Assert;
import org.mockito.Mockito;
import org.thingsboard.server.transport.lwm2m.server.client.LwM2mClient;
import org.thingsboard.server.transport.lwm2m.server.client.LwM2mClientContext;
import org.thingsboard.server.transport.lwm2m.server.uplink.DefaultLwM2mUplinkMsgHandler;
import org.thingsboard.server.transport.lwm2m.utils.LwM2mValueConverterImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.eclipse.californium.scandium.config.DtlsConfig.DTLS_RECOMMENDED_CIPHER_SUITES_ONLY;
import static org.eclipse.leshan.core.LwM2mId.ACCESS_CONTROL;
import static org.eclipse.leshan.core.LwM2mId.DEVICE;
import static org.eclipse.leshan.core.LwM2mId.FIRMWARE;
import static org.eclipse.leshan.core.LwM2mId.LOCATION;
import static org.eclipse.leshan.core.LwM2mId.SECURITY;
import static org.eclipse.leshan.core.LwM2mId.SERVER;
import static org.eclipse.leshan.core.LwM2mId.SOFTWARE_MANAGEMENT;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.BINARY_APP_DATA_CONTAINER;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.LwM2MClientState;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.LwM2MClientState.ON_BOOTSTRAP_FAILURE;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.LwM2MClientState.ON_BOOTSTRAP_STARTED;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.LwM2MClientState.ON_BOOTSTRAP_SUCCESS;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.LwM2MClientState.ON_BOOTSTRAP_TIMEOUT;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.LwM2MClientState.ON_DEREGISTRATION_FAILURE;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.LwM2MClientState.ON_DEREGISTRATION_STARTED;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.LwM2MClientState.ON_DEREGISTRATION_SUCCESS;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.LwM2MClientState.ON_DEREGISTRATION_TIMEOUT;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.LwM2MClientState.ON_EXPECTED_ERROR;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.LwM2MClientState.ON_INIT;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.LwM2MClientState.ON_REGISTRATION_FAILURE;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.LwM2MClientState.ON_REGISTRATION_STARTED;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.LwM2MClientState.ON_REGISTRATION_SUCCESS;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.LwM2MClientState.ON_REGISTRATION_TIMEOUT;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.LwM2MClientState.ON_UPDATE_FAILURE;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.LwM2MClientState.ON_UPDATE_STARTED;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.LwM2MClientState.ON_UPDATE_SUCCESS;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.LwM2MClientState.ON_UPDATE_TIMEOUT;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.OBJECT_INSTANCE_ID_0;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.OBJECT_INSTANCE_ID_1;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.OBJECT_INSTANCE_ID_12;
import static org.thingsboard.server.transport.lwm2m.Lwm2mTestHelper.TEMPERATURE_SENSOR;

@Slf4j
@Data
public class LwM2MTestClient {

    private final ScheduledExecutorService executor;
    private final String endpoint;
    private LeshanClient leshanClient;

    private Security lwm2mSecurity;
    private Security lwm2mSecurityBs;
    private Lwm2mServer lwm2mServer;
    private Lwm2mServer lwm2mServerBs;
    private SimpleLwM2MDevice lwM2MDevice;
    private FwLwM2MDevice fwLwM2MDevice;
    private SwLwM2MDevice swLwM2MDevice;
    private LwM2mBinaryAppDataContainer lwM2MBinaryAppDataContainer;
    private LwM2MLocationParams locationParams;
    private LwM2mTemperatureSensor lwM2MTemperatureSensor;
    private LwM2MClientState clientState;
    private Set<LwM2MClientState> clientStates;
    private DefaultLwM2mUplinkMsgHandler defaultLwM2mUplinkMsgHandlerTest;
    private LwM2mClientContext clientContext;

    public void init(Security security, Configuration coapConfig, int port, boolean isRpc, boolean isBootstrap,
                     int shortServerId, int shortServerIdBs, Security securityBs,
                     DefaultLwM2mUplinkMsgHandler defaultLwM2mUplinkMsgHandler,
                     LwM2mClientContext clientContext,
                     String[] resources) throws InvalidDDFFileException, IOException {
        Assert.assertNull("client already initialized", leshanClient);
        this.defaultLwM2mUplinkMsgHandlerTest = defaultLwM2mUplinkMsgHandler;
        this.clientContext = clientContext;
        List<ObjectModel> models = new ArrayList<>();
        for (String resourceName : resources) {
            models.addAll(ObjectLoader.loadDdfFile(LwM2MTestClient.class.getClassLoader().getResourceAsStream("lwm2m/" + resourceName), resourceName));
        }
        LwM2mModel model = new StaticModel(models);
        ObjectsInitializer initializer = new ObjectsInitializer(model);
        if (securityBs == null) {
            initializer.setInstancesForObject(SECURITY, this.lwm2mSecurity = security);
        } else {
            securityBs.setId(0);
            security.setId(1);
            LwM2mInstanceEnabler[] instances = new LwM2mInstanceEnabler[]{this.lwm2mSecurityBs = securityBs, this.lwm2mSecurity = security};
            initializer.setClassForObject(SECURITY, Security.class);
            initializer.setInstancesForObject(SECURITY, instances);
        }
        if (isBootstrap) {
            initializer.setInstancesForObject(SERVER, lwm2mServerBs = new Lwm2mServer(shortServerIdBs, 300));
        } else {
            if (securityBs == null) {
                initializer.setInstancesForObject(SERVER, lwm2mServer = new Lwm2mServer(shortServerId, 300));
            } else {
                lwm2mServerBs = new Lwm2mServer(shortServerIdBs, 300);
                lwm2mServerBs.setId(0);
                lwm2mServer = new Lwm2mServer(shortServerId, 300);
                lwm2mServer.setId(1);
                LwM2mInstanceEnabler[] instances = new LwM2mInstanceEnabler[]{lwm2mServerBs, lwm2mServer};
                initializer.setClassForObject(SERVER, Server.class);
                initializer.setInstancesForObject(SERVER, instances);
            }
        }
        if (models.stream().filter(obj -> obj.id == ACCESS_CONTROL).findAny().isPresent()) {
            initializer.setClassForObject(ACCESS_CONTROL, DummyInstanceEnabler.class);
        }
        if (models.stream().filter(obj -> obj.id == DEVICE).findAny().isPresent()) {
            initializer.setInstancesForObject(DEVICE, lwM2MDevice = new SimpleLwM2MDevice(executor));
        }
        if (models.stream().filter(obj -> obj.id == FIRMWARE).findAny().isPresent()) {
            initializer.setInstancesForObject(FIRMWARE, fwLwM2MDevice = new FwLwM2MDevice());
        }
        if (models.stream().filter(obj -> obj.id == SOFTWARE_MANAGEMENT).findAny().isPresent()) {
            initializer.setInstancesForObject(SOFTWARE_MANAGEMENT, swLwM2MDevice = new SwLwM2MDevice());        }

        Optional optObj19 = models.stream().filter(obj -> obj.id == BINARY_APP_DATA_CONTAINER).findAny();
        if (optObj19.isPresent()) {
            boolean dataMultipleBinaryApp = ((ObjectModel)optObj19.get()).resources.get(0).multiple;
            initializer.setInstancesForObject(BINARY_APP_DATA_CONTAINER, lwM2MBinaryAppDataContainer = new LwM2mBinaryAppDataContainer(executor, OBJECT_INSTANCE_ID_0),
                    new LwM2mBinaryAppDataContainer(executor, OBJECT_INSTANCE_ID_1));
        }
        if (models.stream().filter(obj -> obj.id == LOCATION).findAny().isPresent()) {
            locationParams = new LwM2MLocationParams();
            locationParams.getPos();
            initializer.setInstancesForObject(LOCATION, new LwM2mLocation(locationParams.getLatitude(), locationParams.getLongitude(), locationParams.getScaleFactor(), executor, OBJECT_INSTANCE_ID_0));
        }
        if (models.stream().filter(obj -> obj.id == TEMPERATURE_SENSOR).findAny().isPresent()) {
            initializer.setInstancesForObject(TEMPERATURE_SENSOR, lwM2MTemperatureSensor = new LwM2mTemperatureSensor(executor, OBJECT_INSTANCE_ID_0), new LwM2mTemperatureSensor(executor, OBJECT_INSTANCE_ID_12));
        }
        DtlsConnectorConfig.Builder dtlsConfig = new DtlsConnectorConfig.Builder(coapConfig);
        dtlsConfig.set(DTLS_RECOMMENDED_CIPHER_SUITES_ONLY, true);

        DefaultRegistrationEngineFactory engineFactory = new DefaultRegistrationEngineFactory();
        engineFactory.setReconnectOnUpdate(false);
        engineFactory.setResumeOnConnect(true);

        LeshanClientBuilder builder = new LeshanClientBuilder(endpoint);
        builder.setLocalAddress("0.0.0.0", port);
        builder.setObjects(initializer.createAll());
        builder.setCoapConfig(coapConfig);
        builder.setDtlsConfig(dtlsConfig);
        builder.setRegistrationEngineFactory(engineFactory);
        builder.setSharedExecutor(executor);
        builder.setDecoder(new DefaultLwM2mDecoder(false));

        builder.setEncoder(new DefaultLwM2mEncoder(new LwM2mValueConverterImpl(), false));
        clientState = ON_INIT;
        clientStates = new HashSet<>();
        clientStates.add(clientState);
        leshanClient = builder.build();

        LwM2mClientObserver observer = new LwM2mClientObserver() {
            @Override
            public void onBootstrapStarted(ServerIdentity bsserver, BootstrapRequest request) {
                clientState = ON_BOOTSTRAP_STARTED;
                clientStates.add(clientState);
            }

            @Override
            public void onBootstrapSuccess(ServerIdentity bsserver, BootstrapRequest request) {
                clientState = ON_BOOTSTRAP_SUCCESS;
                clientStates.add(clientState);
            }

            @Override
            public void onBootstrapFailure(ServerIdentity bsserver, BootstrapRequest request, ResponseCode responseCode, String errorMessage, Exception cause) {
                clientState = ON_BOOTSTRAP_FAILURE;
                clientStates.add(clientState);
            }

            @Override
            public void onBootstrapTimeout(ServerIdentity bsserver, BootstrapRequest request) {
                clientState = ON_BOOTSTRAP_TIMEOUT;
                clientStates.add(clientState);
            }

            @Override
            public void onRegistrationStarted(ServerIdentity server, RegisterRequest request) {
                clientState = ON_REGISTRATION_STARTED;
                clientStates.add(clientState);
            }

            @Override
            public void onRegistrationSuccess(ServerIdentity server, RegisterRequest request, String registrationID) {
                clientState = ON_REGISTRATION_SUCCESS;
                clientStates.add(clientState);
            }

            @Override
            public void onRegistrationFailure(ServerIdentity server, RegisterRequest request, ResponseCode responseCode, String errorMessage, Exception cause) {
                clientState = ON_REGISTRATION_FAILURE;
                clientStates.add(clientState);
            }

            @Override
            public void onRegistrationTimeout(ServerIdentity server, RegisterRequest request) {
                clientState = ON_REGISTRATION_TIMEOUT;
                clientStates.add(clientState);
            }

            @Override
            public void onUpdateStarted(ServerIdentity server, UpdateRequest request) {
                clientState = ON_UPDATE_STARTED;
                clientStates.add(clientState);
            }

            @Override
            public void onUpdateSuccess(ServerIdentity server, UpdateRequest request) {
                clientState = ON_UPDATE_SUCCESS;
                clientStates.add(clientState);
            }

            @Override
            public void onUpdateFailure(ServerIdentity server, UpdateRequest request, ResponseCode responseCode, String errorMessage, Exception cause) {
                clientState = ON_UPDATE_FAILURE;
                clientStates.add(clientState);
            }

            @Override
            public void onUpdateTimeout(ServerIdentity server, UpdateRequest request) {
                clientState = ON_UPDATE_TIMEOUT;
                clientStates.add(clientState);
            }

            @Override
            public void onDeregistrationStarted(ServerIdentity server, DeregisterRequest request) {
                clientState = ON_DEREGISTRATION_STARTED;
                clientStates.add(clientState);
            }

            @Override
            public void onDeregistrationSuccess(ServerIdentity server, DeregisterRequest request) {
                clientState = ON_DEREGISTRATION_SUCCESS;
                clientStates.add(clientState);
            }

            @Override
            public void onDeregistrationFailure(ServerIdentity server, DeregisterRequest request, ResponseCode responseCode, String errorMessage, Exception cause) {
                clientState = ON_DEREGISTRATION_FAILURE;
                clientStates.add(clientState);
            }

            @Override
            public void onDeregistrationTimeout(ServerIdentity server, DeregisterRequest request) {
                clientState = ON_DEREGISTRATION_TIMEOUT;
                clientStates.add(clientState);
            }

            @Override
            public void onUnexpectedError(Throwable unexpectedError) {
                clientState = ON_EXPECTED_ERROR;
                clientStates.add(clientState);
            }
        };
        this.leshanClient.addObserver(observer);

        if (!isRpc) {
            this.start(true);
        }
    }

    public void destroy() {
        if (leshanClient != null) {
            leshanClient.destroy(true);
        }
        if (lwm2mSecurityBs != null) {
            lwm2mSecurityBs = null;
        }
        if (lwm2mSecurity != null) {
            lwm2mSecurity = null;
        }
        if (lwm2mServerBs != null) {
            lwm2mServerBs = null;
        }
        if (lwm2mServer != null) {
            lwm2mServer = null;
        }
        if (lwM2MDevice != null) {
            lwM2MDevice.destroy();
        }
        if (fwLwM2MDevice != null) {
            fwLwM2MDevice.destroy();
        }
        if (swLwM2MDevice != null) {
            swLwM2MDevice.destroy();
        }
        if (lwM2MBinaryAppDataContainer != null) {
            lwM2MBinaryAppDataContainer.destroy();
        }
        if (lwM2MTemperatureSensor != null) {
            lwM2MTemperatureSensor.destroy();
        }
    }

    public void start(boolean isStartLw) {
        if (leshanClient != null) {
            leshanClient.start();
            if (isStartLw) {
                this.awaitClientAfterStartConnectLw();
            }
        }
    }

    private void awaitClientAfterStartConnectLw() {
        LwM2mClient lwM2MClient = this.clientContext.getClientByEndpoint(endpoint);
        CountDownLatch latch = new CountDownLatch(1);
        Mockito.doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(defaultLwM2mUplinkMsgHandlerTest).initAttributes(lwM2MClient, true);

        try {
            if (!latch.await(1, TimeUnit.SECONDS)) {
                throw new RuntimeException("Failed to await TimeOut lwm2m client initialization!");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Exception Failed to await lwm2m client initialization! ", e);
        }
    }
}
