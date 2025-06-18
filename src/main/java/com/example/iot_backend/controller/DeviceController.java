package com.example.iot_backend.controller;

import com.example.iot_backend.model.Device;
import com.example.iot_backend.model.Telemetry;
import com.example.iot_backend.repository.DeviceRepository;
import com.example.iot_backend.repository.TelemetryRepository;
import com.example.iot_backend.service.IoTHubService;
import com.example.iot_backend.service.MqttService;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.example.iot_backend.service.MqttService.logger;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private TelemetryRepository telemetryRepository;
    @Autowired
    private IoTHubService ioTHubService;
    @Autowired
    private MqttService mqttService;

    @PostMapping
    public ResponseEntity<Device> createDevice(@RequestBody Device device) {
        try {
            device.setType("IoT Device");
            device.setConnected(true);
            logger.info("Trying to create device: {}", device.toString());
            Device savedDevice = deviceRepository.save(device);
            logger.info("Saved device in repo");
            ioTHubService.registerDevice(String.valueOf(device.getId()));
            logger.info("Registered Device in ioTServiceHub");

            return ResponseEntity.ok(savedDevice);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Device> updateDevice(@PathVariable String id, @RequestBody Device device) {
        try {
            logger.info("updating the device");
            ioTHubService.updateDeviceTwin(id, device.getStatus());
            logger.info("saving the device in repo");
            Device updatedDevice = deviceRepository.save(device);
            return ResponseEntity.ok(updatedDevice);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable String id) throws IOException, IotHubException {
        deviceRepository.deleteById(Long.valueOf(id));
        ioTHubService.removeDevice(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/telemetry")
    public ResponseEntity<String> sendTelemetry(@RequestBody String payload) {
        try {
            mqttService.sendTelemetry(payload);
            return ResponseEntity.ok("Telemetry sent successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send telemetry: " + e.getMessage());
        }
    }

    @GetMapping("/telemetry")
    public List<Telemetry> getTelemetry() {
        return telemetryRepository.findAll();
    }
}