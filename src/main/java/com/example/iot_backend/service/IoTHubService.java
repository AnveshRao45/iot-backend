package com.example.iot_backend.service;

import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwin;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.devicetwin.Pair;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.Device;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;

@Service
public class IoTHubService {

    private final RegistryManager registryManager;
    private final DeviceTwin deviceTwinClient;

    public IoTHubService(@Value("${azure.iot.hub.connection-string}") String connectionString) throws IOException {
        this.registryManager = RegistryManager.createFromConnectionString(connectionString);
        this.deviceTwinClient = DeviceTwin.createFromConnectionString(connectionString);
    }

    public void registerDevice(String deviceId) throws IotHubException, IOException {
        Device device = Device.createFromId(deviceId, null, null);
        registryManager.addDevice(device);
    }

    public void updateDeviceTwin(String deviceId, String status) throws IOException, IotHubException {
        DeviceTwinDevice twin = new DeviceTwinDevice(deviceId);
        deviceTwinClient.getTwin(twin);

        twin.setTags(Collections.singleton(new Pair("status", status)));

        deviceTwinClient.updateTwin(twin);
    }

    public void removeDevice(String deviceId) throws IotHubException, IOException {
        registryManager.removeDevice(deviceId);
    }
}
