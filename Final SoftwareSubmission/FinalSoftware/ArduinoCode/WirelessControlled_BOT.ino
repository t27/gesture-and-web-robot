//Tarang Shah
//tarang27@gmail.com
//Pin mappings on the arduino to the L293d motor driver. The C and C' pins are the control pins and the EN pin is for the enable. Connect the respective pins on the L293d to the arduino
//C1=9
//C1'=8
//EN1=10
//C2=12
//C2'=13
//En2=11

int M1[]={9,8};//Control pins or motor 1
int CM1=10;//Enable pin for motor 1
int CM2=11;//Enable pin for motor 1
int M2[]={12,13};//Control pins or motor 1
byte speed=255;


int motorMove(int motorno,char function,byte pwm=255);

int motorMove(int motorno,char function,byte pwm)//moves the given motor "motorno" in the f and b direction
{
  int dir;
 if(function=='f')
   dir=1;
 else if(function=='b')
   dir=0;  
   
 if(motorno==1)
  {
    for(int i=0;i<2;i++)
    {
      digitalWrite(M1[i],dir);
      dir=dir-1;
    }
    analogWrite(CM1,pwm);
  }
 else if(motorno==2)
  {
   for(int i=0;i<2;i++)
    {
      digitalWrite(M2[i],dir);
      dir=dir-1;
    }
    analogWrite(CM2,pwm);
  }
 
  
}

int botMove(int function)//moves the bot in f,b,r,l directions by moving the motors accordingly

{
  switch(function)
  {
    case 'w':
            //front
            motorMove(1,'f',speed); 
            motorMove(2,'f',speed);
            Serial.println("Front");
            break;
    case 's':
            //back
            motorMove(1,'b',speed); 
            motorMove(2,'b',speed);
            Serial.println("Back");
            break;
    case 'd':
            //right
            motorMove(1,'f',speed); 
            motorMove(2,'b',speed);
            Serial.println("Left");
            break;
    case 'a':
            //left
            motorMove(1,'b',speed); 
            motorMove(2,'f',speed);
            Serial.println("Right");
            break;
    case 'x':
            motorMove(1,'f',0); 
            motorMove(2,'f',0);
            Serial.println("Stop");
            break;
            
    case 'p':if(speed<=245)
                speed=speed+10;
              Serial.println("Faster");
             // Serial.println("speed="+speed);
              break;
    case 'm':if(speed>10)
               speed=speed-10;
             Serial.println("SLower");
             // Serial.println("speed="+speed);
              break;
            
    default:Serial.println("Invalid");
                     
  }
}

void setup()
{
  Serial.begin(9600);
  pinMode(M1[0],OUTPUT);
  pinMode(M1[1],OUTPUT);
  pinMode(M2[0],OUTPUT);
  pinMode(M2[1],OUTPUT);
  pinMode(CM1,OUTPUT);
  pinMode(CM2,OUTPUT);
  pinMode(7,OUTPUT);
  digitalWrite(7,HIGH);
  botMove('x');
}

void loop()
{
  if(Serial.available())
  {
    byte in=Serial.read();//read command from serial port
 
    botMove(in);//execute the command
    delay(100);
  }
}
