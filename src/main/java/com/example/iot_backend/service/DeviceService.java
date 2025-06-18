package com.example.iot_backend.service;

import com.example.iot_backend.model.Device;
import com.example.iot_backend.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    public List<Device> getAllDevices() {
        return (List<Device>) deviceRepository.findAll();
    }

    public Optional<Device> getDeviceById(Long id) {
        return deviceRepository.findById(id);
    }

    public Device addDevice(Device device) {
        return deviceRepository.save(device);
    }

    public Device updateDevice(Long id, Device deviceDetails) {
        return deviceRepository.findById(id).map(device -> {
            device.setName(deviceDetails.getName());
            device.setType(deviceDetails.getType());
            device.setConnected(deviceDetails.isConnected());
            return deviceRepository.save(device);
        }).orElse(null);
    }

    public void deleteDevice(Long id) {
        deviceRepository.deleteById(id);
    }
}
