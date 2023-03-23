#include <SoftwareSerial.h>
#include <DHT.h>

// Constant specifying the type of used sensor
#define SENSOR_TYPE DHT22

// Constant specifying the pin to which the sensor sends data
#define SENSOR_PIN 2

// Constant specifying the delay between reads of sensor data
#define READ_DELAY 600

// Constant specifying the RX pin for the bluetooth module
#define BT_SERIAL_RX 3

// Constant specifying the TX pin for the bluetooth module
#define BT_SERIAL_TX 4

// Constant specifying the format in which to send the temperature reading
#define TEMPERATURE_FORMAT "-Temperature-"

// Constant specifying the format in which to send the humidity reading
#define HUMIDITY_FORMAT "-Humidity-"

// Initialize sensor according to input pin and sensor type
DHT dhtSensor = DHT(SENSOR_PIN, SENSOR_TYPE);

// Initialize bluetooth serial according to bluetooth RX and TX pins
SoftwareSerial BluetoothSerial(BT_SERIAL_RX, BT_SERIAL_TX);

void setup()
{ 
  // Prepeare sensor object to read data 
  dhtSensor.begin();

  // Establish bluetooth communication  
  BluetoothSerial.begin(9600);
}

void loop()
{ 
  // Create a delay in order to have new data every READ_DELAY miliseconds
  delay(READ_DELAY);

  // Read the humidity data from the sensor
  float humidity = dhtSensor.readHumidity();

  // Read the temperature data from the sensor
  float temperature = dhtSensor.readTemperature();

  // Send temperature data from sensor to bluetooth module
  BluetoothSerial.print(temperature);
  BluetoothSerial.print(";");
  // Send humidity data from sensor to bluetooth module
  BluetoothSerial.print(humidity);
  BluetoothSerial.print(";");
}
