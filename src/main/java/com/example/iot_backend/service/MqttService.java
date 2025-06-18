package com.example.iot_backend.service;

import com.example.iot_backend.model.Telemetry;
import com.example.iot_backend.repository.TelemetryRepository;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.util.Base64;

import java.net.URLEncoder;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * Service for MQTT communication with Azure IoT Hub.
 * Connects to IoT Hub's MQTT endpoint, sends/receives telemetry, and stores messages in H2 database.
 */
@Service
public class MqttService implements MqttCallback {
    public static final Logger logger = LoggerFactory.getLogger(MqttService.class);
    private MqttClient client;
    private final TelemetryRepository telemetryRepository;
//    @Autowired
//    private SimpMessagingTemplate messagingTemplate;
    @Value("${azure.iot.device.connection-string}")
    private String connectionString;

    @Value("${azure.iot.device-id}")
    private String deviceId;

    @Value("${azure.iot.hub-name}")
    private String hubName;

    public MqttService(TelemetryRepository telemetryRepository) {
        this.telemetryRepository = telemetryRepository;
    }

    @PostConstruct
    public void init() {
        try {
            System.setProperty("org.eclipse.paho.client.mqttv3.logging", "DEBUG");

            String clientId = deviceId;
            String mqttUri = "ssl://" + hubName + ".azure-devices.net:8883";
            String username = hubName + ".azure-devices.net/" + deviceId + "/?api-version=2021-04-12";
            String sasToken = generateSasToken();
            logger.info("Generated SAS token: {}", sasToken);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(username);
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
            options.setPassword(sasToken.toCharArray());
            options.setCleanSession(true);
            options.setConnectionTimeout(30);
            options.setKeepAliveInterval(60);
            options.setAutomaticReconnect(true);

            // Configure TLSv1.2
            try {
                SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                sslContext.init(null, null, new SecureRandom());
                SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                options.setSocketFactory(sslSocketFactory);
                logger.info("Configured TLSv1.2 for MQTT connection");
            } catch (Exception e) {
                logger.error("Failed to configure TLS: {}", e.getMessage(), e);
                throw new MqttException(e);
            }

            // Proxy configuration (uncomment and configure if needed)
            /*
            try {
                options.setSocketFactory(getProxySocketFactory("proxy-host", 8080, "proxy-user", "proxy-pass"));
                logger.info("Configured proxy for MQTT connection");
            } catch (Exception e) {
                logger.error("Failed to configure proxy: {}", e.getMessage(), e);
                throw new MqttException(e);
            }
            */

            client = new MqttClient(mqttUri, clientId, null);
            client.setCallback(this);
            logger.info("Attempting MQTT connection to {} for device {}", mqttUri, deviceId);
            client.connect(options);
            logger.info("Connected to the MQTT options");
            client.subscribe("devices/" + deviceId + "/messages/events/#", 1);
            logger.info("MQTT client connected and subscribed for device: {}", deviceId);
        } catch (MqttException e) {
            logger.error("MQTT initialization failed: {}", e.getReasonCode());
            throw new RuntimeException("Failed to initialize MQTT client", e);
        }
    }

    public void sendTelemetry(String payload) throws MqttException {
        if (!client.isConnected()) {
            logger.warn("MQTT client not connected. Attempting reconnect...");
            try {
                client.reconnect();
            } catch (MqttException e) {
                logger.error("Reconnect failed: {}", e.getMessage(), e);
                throw e;
            }
        }
        String topic = "devices/" + deviceId + "/messages/events/";
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(1);
        client.publish(topic, message);
        logger.info("Telemetry sent from device {}: {}", deviceId, payload);
    }

    private String generateSasToken() {
        try {
            String resourceUri = URLEncoder.encode(hubName + ".azure-devices.net/devices/" + deviceId, StandardCharsets.UTF_8);
            long expiry = System.currentTimeMillis() / 1000L + 3600;
            String stringToSign = resourceUri + "\n" + expiry;

            String sharedAccessKey = "";
            String[] parts = connectionString.split(";");
            for (String part : parts) {
                if (part.startsWith("SharedAccessKey=")) {
                    sharedAccessKey = part.substring("SharedAccessKey=".length());
                    break;
                }
            }
            if (sharedAccessKey.isEmpty()) {
                throw new IllegalArgumentException("SharedAccessKey not found in connection string");
            }

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(Base64.getDecoder().decode(sharedAccessKey), "HmacSHA256");
            mac.init(secretKey);
            String signature = Base64.getEncoder().encodeToString(mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));

            String sasToken = String.format("SharedAccessSignature sr=%s&sig=%s&se=%d", resourceUri, URLEncoder.encode(signature, StandardCharsets.UTF_8.toString()), expiry);
            logger.debug("Generated SAS token: {}", sasToken);
            return sasToken;
        } catch (Exception e) {
            logger.error("Failed to generate SAS token: {}", e.getMessage(), e);
            throw new RuntimeException("SAS token generation failed", e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        logger.warn("MQTT connection lost for device {}: {}", deviceId, cause.getMessage(), cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        logger.info("Received message on topic {} for device {}: {}", topic, deviceId, payload);

        Telemetry telemetry = new Telemetry();
        telemetry.setDeviceId(deviceId);
        telemetry.setPayload(payload);
        telemetry.setTimestamp(Instant.now().toString());
        telemetryRepository.save(telemetry);
//        messagingTemplate.convertAndSend("/topic/telemetry/" + deviceId, telemetry);

    }



    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        logger.debug("Message delivery complete for device: {}", deviceId);
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                client.close();
                logger.info("MQTT client disconnected for device: {}", deviceId);
            }
        } catch (MqttException e) {
            logger.error("Failed to disconnect MQTT client: {}", e.getMessage(), e);
        }
    }
}