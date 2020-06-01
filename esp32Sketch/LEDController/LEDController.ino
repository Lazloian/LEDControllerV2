#include "BluetoothSerial.h" // bluetooth library
#include "FastLED.h" // LED library

#define NUM_LEDS 60
#define LED_PIN 32
#define BRIGHTNESS 100

BluetoothSerial SerialBT; // bluetooth communication object

String btData;
char btChars[10];

CRGB leds[NUM_LEDS];

void setup() {
  Serial.begin(115200);
  Serial.println("Booting");

  // set up bluetooth communication
  SerialBT.begin("LEDController"); // name of bluetooth device

  // set up leds
  FastLED.addLeds<NEOPIXEL, LED_PIN>(leds, NUM_LEDS);
  FastLED.setBrightness(BRIGHTNESS);

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
    Serial.println(incomingChar);

    if (incomingChar == 'C')
    {
      SerialBT.print('y');
    }
    if (incomingChar == 'h')
    {
      hueColor();
    }
    /*
    // new message starts with a period
    if (incomingChar == '.')
    {
      btData = "";
    }
    // messages end with a newline
    else if (incomingChar == '\n')
    {
      Serial.println("\nIncoming Data: " + btData);
      //btData.toCharArray(btChars, btData.length());
      btEvent();
    }
    // if it is neither it is part of the string
    else
    {
      btData += String(incomingChar);
    }*/
  }

  delay(20); // dont do this in final please
}

char getBT()
{
  char btChar = NULL;

  while (!SerialBT.available())
  {
    
  }

  btChar = SerialBT.read() + 128;
  Serial.println((int) btChar);
  return btChar;
}

void btEvent()
{
  if (btData.charAt(0) == 'C')
  {
    SerialBT.print('y'); 
  }
  else if (btData.charAt(0) == 'h')
  {
    hueColor();
  }
}

void hueColor()
{ 
  //int hue = btData.substring(1, 4).toInt();
  uint8_t hue = getBT();
  for (int i = 0; i < NUM_LEDS; i++)
  {
    leds[i] = CHSV(hue, 255, 255);
  }
  FastLED.show();

  SerialBT.print('d');
}
