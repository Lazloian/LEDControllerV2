#include "BluetoothSerial.h" // bluetooth library

BluetoothSerial SerialBT; // bluetooth communication object

String btData;

void setup() {
  Serial.begin(115200);
  Serial.println("Booting");

  // set up bluetooth communication
  SerialBT.begin("LEDController"); // name of bluetooth device

  Serial.println("Bluetooth and setup finished");
}

void loop() {
  // data is sent to the other device through the serial port
  if (Serial.available())
  {
    SerialBT.write(Serial.read()); // send data
  }

  // data is read from bluetooth serial
  if (SerialBT.available())
  {
    char incomingChar = SerialBT.read();

    if (incomingChar == '.')
    {
      btData = "";
    }
    else if (incomingChar == '\n')
    {
      Serial.println("\nIncoming Data: " + btData);
      btEvent();
    }
    else
    {
      btData += String(incomingChar);
    }

    //Serial.print(incomingChar);
  }

  delay(20); // dont do this in final please
}

void btEvent()
{
  if (btData == "Connection")
  {
    SerialBT.print('y'); 
  }
}
