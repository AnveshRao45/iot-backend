     # Industrial IoT Platform Backend

     A Spring Boot backend for an Industrial IoT platform, integrating Azure IoT Hub for device management and telemetry processing.

     ## Setup
     1. Clone the repository:
        ```bash
        git clone https://github.com/your-username/iot-backend.git
        ```
     2. Copy `application.properties.template` to `application.properties` and fill in Azure IoT Hub credentials.
     3. Install dependencies:
        ```bash
        mvn clean install
        ```
     4. Run the application:
        ```bash
        mvn spring-boot:run
        ```

     ## Endpoints
     - POST `/api/devices/telemetry`: Send telemetry.
     - GET `/api/telemetry/device/{deviceId}`: Get telemetry for a device.
