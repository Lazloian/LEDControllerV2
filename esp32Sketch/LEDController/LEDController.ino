#include "BluetoothSerial.h" // bluetooth library
#include "FastLED.h" // LED library

#define NUM_LEDS 330 // EPIC
#define LED_PIN 32
#define BRIGHTNESS 10
#define TIME_MULT 10
#define DATA_TASK_DELAY 200

BluetoothSerial SerialBT; // bluetooth communication object

char btChars[10]; // stores the data sent over bluetooth, max size of ten
boolean newPattern = false; // set to true when a new pattern is sent, set to false when the new pattern has been set

CRGB leds[NUM_LEDS]; // led array

TaskHandle_t taskHandler = NULL; // handler for the currently running led pattern task

void setup() {
  Serial.begin(115200);
  Serial.println("Booting");

  // set up bluetooth communication
  SerialBT.begin("LEDController"); // name of bluetooth device

  // set up leds
  FastLED.addLeds<NEOPIXEL, LED_PIN>(leds, NUM_LEDS);
  FastLED.setBrightness(BRIGHTNESS);

  // set up task scheduler (all tasks run on core 1 to not slow down bluetooth server running on core 0)

  // task for checking and sorting BT data
  xTaskCreatePinnedToCore(
    checkBTData, // function to run
    "CheckBTData", // name of task
    1000, // stack size (bytes)
    NULL, // parameter to pass
    4, // task priority
    NULL, // task handle
    0 // the core to run on
  );

  // task for updating the pattern
  xTaskCreatePinnedToCore(
    updatePattern, // function to run
    "updatePattern", // name of task
    1000, // stack size (bytes)
    NULL, // parameter to pass
    3, // task priority
    NULL, // task handle
    0 // core to run on
  );

  Serial.println("Bluetooth and setup finished");
}

// loop doesnt do anything, it has the lowest priority (will only run every 10 seconds)
// might do something later like check a switch or led
void loop() {
  vTaskDelay(100000 / portTICK_PERIOD_MS); // pause for 100 seconds (other tasks can be run)
}

// check for new BT Data every second and sorts data based on type
void checkBTData(void * parameter)
{
  for(;;)
  {
    if (SerialBT.available())
    {
      // get pattern type
      btChars[0] = SerialBT.read();
      Serial.print("Pattern: ");
      Serial.println(btChars[0]);

      switch (btChars[0])
      {
        // phone checking if esp32 is still connected
        case 'C':
          SerialBT.print('y');
          break;

        // simple and fade are formatted the same
        case 's':
        case 'f':
          btChars[1] = getBTChar();
          btChars[2] = getBTChar();
          btChars[3] = getBTChar();
          // get colors based on number of colors
          for (uint8_t i = 0; i < btChars[3]; i++)
          {
            btChars[4 + i] = getBTChar();
          }
          newPattern = true; // since this a new pattern
          break;
      }
      Serial.println("\nCode Done");
    }
    // pause this task for a second (other tasks can be run)
    vTaskDelay(DATA_TASK_DELAY / portTICK_PERIOD_MS);
  }
}

void updatePattern(void * parameter)
{
  for(;;)
  {
    if (newPattern)
    {
      Serial.println("Updating Pattern");
      newPattern = false;
      // delete currently running pattern task if there is one running
      if (taskHandler != NULL)
      {
        vTaskDelete(taskHandler);
        Serial.println("task deleted");
      }
      // start new task based on pattern type
      switch (btChars[0])
      {
        case 's':
          xTaskCreatePinnedToCore(simple, "simple", 1000, NULL, 2, &taskHandler, 1);
          break;
        case 'f':
          xTaskCreatePinnedToCore(fade, "fade", 1000, NULL, 2, &taskHandler, 1);
          break;
      }
      Serial.println("Update Done");
    }
     
    vTaskDelay(DATA_TASK_DELAY / portTICK_PERIOD_MS); // pause for one second (other tasks can be run)
  }
}

void simple(void * parameter)
{
  Serial.println("Starting Simple");
  uint8_t stripLen = btChars[1]; // length of each strip
  int interval = (int) btChars[2] * TIME_MULT; // how fast the colors switch
  
  if (stripLen == 0) stripLen = NUM_LEDS; // if it is zero, then the whole strip is one color

  uint8_t numColors = btChars[3]; // the number of colors
  
  CRGB colors[numColors];
  int strip; // keeps track of what strip is being colored

  // get colors from btChars
  for (int i = 0; i < numColors; i++)
  {
    if (btChars[4 + i] != 255)
    {
      CHSV hsv(btChars[4 + i], 255, 255);
      hsv2rgb_spectrum(hsv, colors[i]);
    }
    else
    {
      CHSV hsv(btChars[4 + i], 0, 255);
      hsv2rgb_spectrum(hsv, colors[i]);
    }
  }

  // set colors of each strip
  for (strip = 0; strip < (NUM_LEDS / stripLen); strip++)
  {
    setColor(stripLen * strip, stripLen, colors[strip % numColors]);
  }
  // set final incomplete strip
  setColor(NUM_LEDS - (NUM_LEDS % stripLen), NUM_LEDS % stripLen, colors[strip % numColors]);
  FastLED.show();
  Serial.println("Simple setup complete");

  if (interval != 0)
  {
    uint8_t currentCol = 0;
  
    for (;;)
    {
      // set colors of each strip
      for (strip = 0; strip < (NUM_LEDS / stripLen); strip++)
      {
        setColor(stripLen * strip, stripLen, colors[(strip + currentCol) % numColors]);
      }
      
      // set final incomplete strip
      setColor(NUM_LEDS - (NUM_LEDS % stripLen), NUM_LEDS % stripLen, colors[(strip + currentCol) % numColors]);
      FastLED.show();
      
      // go to next color
      currentCol++;
      if (currentCol >= numColors)
      {
        currentCol = 0;
      }
  
      vTaskDelay(interval / portTICK_PERIOD_MS);
    }
  }
  // if the interval is zero, don't update the leds and do nothing
  else
  {
    for(;;)
    {
      vTaskDelay(10000 / portTICK_PERIOD_MS);
    }
  }
}


void fade(void * parameter)
{
  Serial.println("Starting Fade");
  
  int fade = btChars[1]; // fade of pattern
  int interval = (int) btChars[2] * TIME_MULT; // the time between fade steps

  uint8_t numColors = btChars[3]; // the number of colors
  
  uint8_t hues[numColors]; // the hues of the colors in the pattern
  CRGB rgb; // stores the next color to send
  int strip; // keeps track of what strip is being colored

  // get hues from btChars (kind of redundant)
  for (int i = 0; i < numColors; i++)
  {
    hues[i] = btChars[4 + i];
  }

  uint8_t currentCol = 0; // keeps track of the current color
  int currentFade = 255; // keeps track of the current fade (how dark the color is)
  Serial.println("Fade Setup Complete");

  for (;;)
  {
    if (hues[currentCol] != 255)
    {
      CHSV hsv(hues[currentCol], 255, currentFade); // create hsv color
      hsv2rgb_spectrum(hsv, rgb); // convert to rgb
    }
    else
    {
      CHSV hsv(hues[currentCol], 0, currentFade); // create hsv color
      hsv2rgb_spectrum(hsv, rgb); // convert to rgb
    }
    
    setColor(0, NUM_LEDS, rgb); // set the color of the leds
    FastLED.show();

    // fade by fade, if current fade is greater than 255, switch direction. If fade is less than 0, switch direction and change color
    currentFade -= fade;
    if (currentFade < 0)
    {
      currentFade = 0;
      fade *= -1;

      // switch to next color
      currentCol++;
      if (currentCol >= numColors)
      {
        currentCol = 0;
      }
    }
    else if(currentFade > 255)
    {
      currentFade = 255;
      fade *= -1;
    }

    vTaskDelay(interval / portTICK_PERIOD_MS);
  }
}

char getBTChar()
{
  char btChar;

  while (!SerialBT.available()) // wait until a new bt char comes through
  {
    vTaskDelay(10 / portTICK_PERIOD_MS); // code works without this, but I want to make sure the bluetooth server isnt held up
  }

  btChar = SerialBT.read() + 128;
  Serial.print((int) btChar);
  Serial.print(" ");
  return btChar;
}

// sets the color of a strip of leds that starts at start and is length num
void setColor(int start, int num, CRGB rgb)
{ 
  for (int i = start; i < start + num; i++)
  {
    leds[i] = rgb;
  }
  // FastLED.show() is not done here because it should only be done once all the colors have been set
}
