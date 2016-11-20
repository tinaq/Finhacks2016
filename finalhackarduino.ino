#include <SoftwareSerial.h>

// creates a "virtual" serial port/UART
// connect BT module TX to D10
// connect BT module RX to D11
// connect BT Vcc to 5V, GND to GND
int LEDList[] = {11,10,9,8,7,6,5,4,3,2,16,17,18,19};


void setup()  
{
  for (int i = 0; i < sizeof(LEDList) - 1; i++)
  {
    pinMode(LEDList[i], OUTPUT);
  }
  

  Serial.begin(9600);
}

String message = ""; // stores incoming character from other device
char addChar = '0'; //stores each character written
int position = 0; //loops through the lights
int waitCount = 0;
bool onState = false;
int LedCount = 0;

void loop() 
{
    // if text arrived in from BT serial...
  while (Serial.available())
  {
    position = 0;
    addChar = Serial.read();
    if (addChar != '!' && addChar != '*' && addChar != '$' && addChar != '{' && addChar != '~') //ending character reached
    {
      message += (addChar); //add everything to the message
    }
    else
    {
      //set the waitCounter to be 0 again
      waitCount = 0;    
      break; //exit the loop
    }
  }
  if (addChar == '!') //if the ending character had been reached
  {
        if (message != "") //only update the led count if there is a messsage
        {
        unsigned int value = message.toInt(); //get the int value
        Serial.println(value);
        LedCount = value;
        }
       

        
      //turn off all of the LEDS  
       for (int i = 0; i < sizeof(LEDList); i ++)
       {
        digitalWrite(LEDList[i],LOW);
       }
    
       //turn on the LEDS that should be on
       for (int i = 0; i < LedCount; i ++)
       {
        delay(10);
        //Serial.print("Turning on: ");
        //Serial.println(LEDList[i]);
        digitalWrite(LEDList[i],HIGH);
       }

       message = "";//clear the message
       addChar = '0';
  }
  else if (addChar == '*')
  {
    //perform random
    digitalWrite(LEDList[random(14)], random(2) == 1 ? HIGH : LOW);
    delay(25);
      waitCount++;
       if (waitCount == 140)
       {
        waitCount = 0;
          addChar = '!'; //go back to displaying the progress
       }
  }
  else if (addChar == '$')
  {
    if (position > 27)
    {
      position = 0;
    }
    //does the line
    if (position < 14)
    {
      digitalWrite(LEDList[position], HIGH);
    }
    else
    {
    digitalWrite(LEDList[position - 14],LOW);
     }
     delay(100);
     position++;

       waitCount++;
       if (waitCount == 50)
       {
        waitCount = 0;
          addChar = '!'; //go back to displaying the progress
       }
  
  }
  else if (addChar == '{') //blinking
  {
       onState = onState == false;
       for (int i = 0; i < sizeof(LEDList); i ++)
       {
        digitalWrite(LEDList[i],onState ? HIGH : LOW);
        delay(1);
       }
       delay(500);
       waitCount = 0;
       //waitCount++;
       //if (waitCount == 17)
      // {
      //  waitCount = 0;
      //    addChar = '!'; //go back to displaying the progress
      // }
  }
  else if (addChar == '~')
  {
     //turn off all of the LEDS  
       for (int i = 0; i < sizeof(LEDList); i ++)
       {
        digitalWrite(LEDList[i],LOW);
       }
    addChar = '0'; //turn it off 
  }
  
}
