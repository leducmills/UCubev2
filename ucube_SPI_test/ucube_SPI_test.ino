/* SPI test code for shift-in boards
 * using CD4021BE IC's
 * by Ben Leduc-Mills
 */


#include <SPI.h>


                       //uno //mega
#define PIN_SCK           13//52             // SPI clock
#define PIN_MISO          12//50             // SPI data input  - ser-out
#define PIN_MOSI          11                 // SPI data output //not used
#define PIN_SS1           10//53             // SPI hardware default SS pin latch

//numBytes = number of chained boards
int numBytes = 49;
char values[392];


void setup() {
  Serial.begin(115200);
  SPI.begin();

  //establishContact();  //serial handshake function for processing
}


void loop() {

  readRegister(0x00, numBytes, values);

  for(int i = 0; i < numBytes; i++) {
    Serial.print((byte)values[i], BIN);
  }
  Serial.print('\n');

  delay(20);
}



void readRegister(char address, int numBytes, char * values) {

  digitalWrite(PIN_SS1,LOW);

  char reg = address;

  for(int i = 0; i < numBytes; i++) {

    values[i] = SPI.transfer(reg);
    delayMicroseconds(20);

  }


  digitalWrite(PIN_SS1, HIGH);

}

//void establishContact() {
//  while (Serial.available() <= 0) {
//    Serial.println("hello");   // send a starting message
//    delay(300);
//  }
}

