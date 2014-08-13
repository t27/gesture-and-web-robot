#!/usr/bin/python
from dweet import Dweet# importing custom class for handling server communication
from datetime import datetime
from time import sleep#libraries for time and date related functions
import sys
import serial# python serial library
import RPi.GPIO as GPIO #Raspi specific GPIO code

#to use on raspberry pi ensure you have disabled the serial port for system use http://www.raspberrypi-spy.co.uk/2013/12/free-your-raspberry-pi-serial-port/

def sendCommand(cmd,s_port): #
    s_port.open()
    if cmd=='forward':
        # write the relevant commands on the serial port
        s_port.write("W");
    elif cmd=='backward':
        s_port.write("S");

    elif cmd=='right':
        s_port.write("D");

    elif cmd=='left':
        s_port.write("A");

    elif cmd=='stop':
        s_port.write("x");

 if __name__ == "__main__":
    devicename='bits279'#default device name
    if len(sys.argv)>1:#checking for any command line args
        devicename=sys.argv[1]#if found then change the device name to the first command line argument
    prev=datetime.strptime("00:00:00","%H:%M:%S");# initialise the timestamp of the previous command to 0
    test=serial.Serial("/dev/ttyAMA0",9600)#initialise serial port at 9600 baudrate
   
    #initialise the GPIO Pins 
    GPIO.setmode(GPIO.BOARD)#use the board pin numberings
    GPIO.setup(7,GPIO.OUT)#set 7,11,13,and 15 pin as output for LED status
    GPIO.setup(11,GPIO.OUT)
    GPIO.setup(13,GPIO.OUT)
    GPIO.setup(15,GPIO.OUT)

    dweet = Dweet()#initialise dweet object


    while 1==1:#infinite loop/program loop
        GPIO.output(7,False);#turning off all LEDs
        GPIO.output(11,False);
        GPIO.output(13,False);
        GPIO.output(15,True);#LED 15 is the ON indicator. Indicates the program is running
        

        #receiving command from server
       
        try:
            rdata=dweet.latest_dweet(name=devicename) #checking for HTTP connection
        except Exception, e:#if error found
            print "Connection Error Occured" # dweet class is configured to only return a Connection Error so any error thrown is a Connection error
            GPIO.output(13,True)
            sleep(0.5)
            GPIO.output(13,False)
            sleep(0.5)
            GPIO.output(13,True)#blink the RED LED to indicate this error
            continue#continue to the next iteration
            #checking the received data

        if(rdata['this']=='failed'):#if the received data contains a value of 'failed' for the 'this' key, it means the devicename has no commands
            print "No data, Check Device name"
            GPIO.output(11,True)#Light the BLUE Led to indicate this error
            sleep(0.2)
            GPIO.output(11,False)
            sleep(0.2)
            GPIO.output(11,True)
            
        if(rdata['this']=='succeeded'):
            commandreceived=rdata['with'][0]['content']['command']

            #calculating timestamp of servers command
            modifieddate=rdata['with'][0]['created']
            mtime=modifieddate.split('T')[1]
            curr=datetime.strptime(mtime.split('.')[0],"%H:%M:%S");

            #checking if timestamp is new
            if curr>prev:
                print "New Data!!"
                prev=curr

                # a platform specific function to write the serial data or write the motor commands through GPIO
                sendCommand(commandreceived,test) 
                GPIO.output(7,True);#light the GREEN LED if new data is received
                sleep(0.2)
               


            print mtime + "  data="+rdata['with'][0]['content']['command']
        sleep(1)