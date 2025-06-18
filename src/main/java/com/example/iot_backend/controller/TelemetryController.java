//package com.example.iot_backend.controller;
//
//import com.example.iot_backend.model.Telemetry;
//import com.example.iot_backend.repository.TelemetryRepository;
//import com.example.iot_backend.service.MqttService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api")
//public class TelemetryController {
//
//    @Autowired
//    private TelemetryRepository telemetryRepository;
//
//    @Autowired
//    private MqttService mqttService;
//
//    @GetMapping("/devices/telemetry")
//    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
//    public ResponseEntity<List<Telemetry>> getAllTelemetry() {
//        List<Telemetry> telemetry = telemetryRepository.findAll();
//        return ResponseEntity.ok(telemetry);
//    }
//
//    @GetMapping("/telemetry/device/{deviceId}")
//    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
//    public ResponseEntity<List<Telemetry>> getTelemetryByDeviceId(@PathVariable String deviceId) {
//        List<Telemetry> telemetry = telemetryRepository.findByDeviceId(deviceId);
//        return ResponseEntity.ok(telemetry);
//    }
//
//    @PostMapping("/devices/telemetry")
//    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
//    public ResponseEntity<Void> sendTelemetry(@RequestBody String payload) throws Exception {
//        mqttService.sendTelemetry(payload);
//        return ResponseEntity.ok().build();
//    }
//
//    @DeleteMapping("/telemetry/{id}")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    public ResponseEntity<Void> deleteTelemetry(@PathVariable Long id) {
//        telemetryRepository.deleteById(id);
//        return ResponseEntity.noContent().build();
//    }
//}
//
////class TelemetryRequest {
////    private String payload;
////
////    public String getPayload() {
////        return payload;
////    }
////
////    public void setPayload(String payload) {
////        this.payload = payload;
////    }
////}
