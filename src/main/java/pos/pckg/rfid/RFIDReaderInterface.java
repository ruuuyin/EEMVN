package pos.pckg.rfid;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import pos.pckg.misc.DataBridgeDirectory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * <h1>RFID Reader Interface</h1>
 * This class allows the simplified implementation of the RFID Scanner Device
 * into a Point-of-Sale System. It facilitates the communication between Java
 * and the device through Serial communication.
 * @author Jeremy Andrews Zantua
 * @version 1.0
 * @since 2019-11-5
 */

public class RFIDReaderInterface {
    private SerialPort selectedPort; // The selected port to be used
    private Scanner serialReader;
    private PrintWriter serialWriter;
    private byte[] bytesRead;
    private String RFIDCacheFilePath = DataBridgeDirectory.DOCUMENT+"etc\\rfid-cache.file";
    private String DeviceSignalFilePath = DataBridgeDirectory.DOCUMENT+"etc\\status\\rfid-device-signal.file";
    private String GSMSignalFilePath = DataBridgeDirectory.DOCUMENT+"etc\\status\\rfid-gsm-signal.file";
    private boolean interpretNextByte = false;
    private boolean deviceReady = false;
    private boolean serialCommDebugging = true; // Set to true when checking pckg.data sent/received through serial
    private int lastCommand = 0;
    private long time;
    private int smsConfirmationNet = 0;
    private boolean interpretNextByteStream = false;
    private boolean byteStreamActive = false;
    // Stores the current byte stream in a buffer which will be returned as a String when the complete pckg.data has arrived
    private ArrayList<Byte> byteStreamBuffer = new ArrayList<>();
    private boolean SMSMode = false; // Keeps track if the device is in SMS Sending mode
    private String SMSRecipientNumber;
    private String SMSContent;
    // Is true when the thread waiting for the device's response to a connection inquiry (the interface checking if the
    // device is still there, or has been disconnected. It will revert back to false when the reply has been received
    // within the allocated time in the wait thread
    private boolean deviceConnectionQueryIsWaiting = false;


    /**
     * Prepares and establishes a serial communication line with the device.
     * @param splashText_line1 is the text that will be displayed on the splash screen on line 1
     * @param splashText_line2 os the text that will be displayed on the splash screen on line 2
     */
    public RFIDReaderInterface(String splashText_line1, String splashText_line2) {

        System.out.println("Establishing connection with the device");

        System.out.println("Fetching available COM Ports...");
        // Fetch all available COM ports and store them in an array
        SerialPort availablePorts[] = SerialPort.getCommPorts();
        System.out.println(availablePorts.length);

        // Prints the number of COM ports found with proper grammar
        if (availablePorts.length == 1)  { // If only one COM port is found
            System.out.print(" COM Port found");
        }
        else { // If zero or more than one COM prots have been found
            System.out.print(" COM Ports found");
        }

        int portNumbers = 1; // gives port numbers when printed
        // lists all available ports to console
        for (SerialPort sp: availablePorts) {
            System.out.println("  " + portNumbers++ + ". " + sp.getDescriptivePortName());
        }

        // Try to look through the list of COM ports to see if a device with "Arduino" in the name exists
        boolean portOpened = false; // Will keep track if a port was opened by picking the devices with "Arduino" in its name
        for (SerialPort sp: availablePorts) {
            // If a COM device has been found with "Arduino" in its name, immediately attempt to open that port
            if (sp.getDescriptivePortName().contains("Arduino")) {
                System.out.println("Attempting to open " + sp.getDescriptivePortName());
                if (attemptOpeningPort(sp,115200)) { // If the port has been successfully opened
                    System.out.println("Successfully opened " + sp.getSystemPortName());
                    portOpened = true;
                    break;
                }
                else { // If the port could not be opened
                    System.out.println("Could not open " + sp.getSystemPortName());
                    portOpened = false;
                }
            }
        }

        // If an "Arduino" labelled-port has not been found yet
        if (!portOpened) {
            for (SerialPort sp: availablePorts) {
                // This time, only check the COM devices that DO NOT have "Arduino" in their names
                if (!sp.getDescriptivePortName().contains("Arduino")) {
                    System.out.println("Attempting to open " + sp.getDescriptivePortName());

                    // If the port has been successfully opened
                    if (attemptOpeningPort(sp,115200)) {
                        System.out.println("Successfully opened " + sp.getSystemPortName());
                        portOpened = true;
                        break;
                    }
                    // If the port could not be opened
                    else {
                        System.out.println("Could not open " + sp.getSystemPortName());
                        portOpened = false;
                    }
                }
            }
        }

        // If a port could still not be opened
        if (!portOpened) {
            System.out.println("ERROR: Could not open any ports");
        }
        // If a port was successfully opened
        else {
            selectedPort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                }

                @Override
                public void serialEvent(SerialPortEvent event) {
                    if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                        return;
                    bytesRead = new byte[selectedPort.bytesAvailable()];
                    int numRead = (selectedPort.readBytes(bytesRead, bytesRead.length));
                    // For checking pckg.data received through serial, uncomment the line below
                    if (serialCommDebugging) {
                        System.out.println("[Received " + numRead + " bytes]");
                        System.out.print("dec: ");
                        for (int x = 0; x < numRead; x++) {
                            System.out.print(bytesRead[x]);
                            if (x != (numRead - 1)) {
                                System.out.print(" ");
                            }
                        }
                        System.out.print("\nchar: \"");
                        for (int x = 0; x < numRead; x++) {
                            System.out.print((char)bytesRead[x]);
                            if (x != (numRead - 1)) {
                                System.out.print(" ");
                            }
                        }
                        System.out.println("\"");
                    }

                    if (!byteStreamActive) { // If the byte stream is inactive, check if a byte stream start marker is received
                        // Check each byte in the bytesRead array
                        for (byte b : bytesRead) {
                            if (b == (byte)2) { // If a start marker is found, set the byteStreamActive status to true
                                byteStreamActive = true;
                                byteStreamBuffer.clear(); // Clear the old contents of the byte stream buffer
                            }
                            else if (b == (byte)3) { // If an end marker is found, set the byteStreamActive status to false
                                byteStreamActive = false;
                                if (interpretNextByteStream) {
                                    interpretNextByteStream = false;
                                    byte byteStreamArray[] = new byte[byteStreamBuffer.size()];
                                    for (int x = 0; x < byteStreamBuffer.size(); x++) {
                                        byteStreamArray[x] = byteStreamBuffer.get(x);
                                    }
                                    interpretReceivedData(byteStreamArray);
                                }
                            }
                            else { // For any other byte, store it to the byteStream buffer
                                byteStreamBuffer.add(b);
                            }
                        }
                    }
                    else { // In case the pckg.data isn't complete from the last serial event, the byte stream is still active
                        // Check each byte in the bytesRead array
                        for (byte b : bytesRead) {
                            if (b == (byte)3) { // If the end marker is found
                                byteStreamActive = false;
                                if (interpretNextByteStream) {
                                    interpretNextByteStream = false;
                                    byte byteStreamArray[] = new byte[byteStreamBuffer.size()];
                                    for (int x = 0; x < byteStreamBuffer.size(); x++) {
                                        byteStreamArray[x] = byteStreamBuffer.get(x);
                                    }
                                    interpretReceivedData(byteStreamArray);
                                }
                            }
                            else { // For any other byte, store it to the byteStream buffer
                                byteStreamBuffer.add(b);
                            }
                        }
                    }

                    // Writes the next received pckg.data to a cache file
                    // This is usually true when we want to record the response of the device to the cache file
                    if (interpretNextByte) {
                        interpretNextByte = false;
                        interpretReceivedData(bytesRead);
                    }

                    // Waits for queries from the device during SMS sending mode
                    if (SMSMode) {
                        if (bytesRead[0] == 17) { // If the recipient's number is being requested
                            sendStringToDevice(SMSRecipientNumber);
                            sendByteToDevice(3);
                        }
                        else if (bytesRead[0] == 18) { // If the message content is being requested
                            sendStringToDevice(SMSContent);
                            sendByteToDevice(3);
                        }
                        else if (bytesRead[0] == 49) { // If the SMS was sent successfully
                            writeToCache("sendSMS=1", RFIDCacheFilePath);
                            SMSMode = false;
                        }
                        else if (bytesRead[0] == 48) { // If the SMS was NOT sent successfully
                            writeToCache("sendSMS=0", RFIDCacheFilePath);
                            SMSMode = false;
                        }
                        else if (bytesRead[0] == 50) { // If there is an error with setting up the SMS
                            writeToCache("sendSMS=2", RFIDCacheFilePath);
                            SMSMode = false;
                        }
                    }

                    // During startup, the device won't be ready yet
                    // This will wait for the handshake signal from the device to arrive and send a reply
                    // Therefore setting the device's status to "Ready"
                    if (!deviceReady) {
                        if (bytesRead[0] == 5) {
                            sendStringToDevice(splashText_line1);
                            sendByteToDevice(3);
                            sendStringToDevice(splashText_line2);
                            sendByteToDevice(3);
                            deviceReady = true;
                        }
                    }
                }
            });
            serialReader = new Scanner(selectedPort.getInputStream()); // Start input stream for receiving pckg.data over serial
            serialWriter = new PrintWriter(selectedPort.getOutputStream()); // Start output stream for receiving pckg.data over serial
        }

        System.out.println("Device initialization routine finished");
    }

    /**
     * Checks if the device is still connected
     */
    public void queryDevice() {
        sendByteToDevice(142);
        interpretNextByteStream = true; // Will write the reply to cache
        // Keep track of the last command that was called to help with determining what to write to the cache file
        lastCommand = 142;
    }

    /**
     * Sends a byte to the device via Serial.
     * @param input This will be the decimal form of the byte that will be sent to serial.
     */
    private void sendByteToDevice(int input) {
        // If Serial debugging is enabled, output the details to the console
        if (serialCommDebugging) {
            System.out.println("[Sent 1 byte]");
            System.out.println("dec: " + input);
            System.out.println("char: \"" + (char)input + "\"");
        }
        serialWriter.print((char)input); // Adds the byte to the output buffer
        serialWriter.flush(); // Sends the byte to the output stream
    }

    /**
     * Breaks down a String into individual characters and sends them one-by-one to the device via serial.
     * @param input is the String to be sent to the device
     */
    private void sendStringToDevice(String input) {
        for (char c : input.toCharArray()) {
            sendByteToDevice((int)c);
        }
    }

    /**
     * Interprets the received pckg.data from the device into human-readable pckg.data which will be written to a cache file.
     * @param data is the byte that will be interpreted
     */
    private void interpretReceivedData(byte data[]) {
        switch (lastCommand) {
            case 132: // Scan
                // Writes the UID of the scanned RFID tag
                writeToCache("scan=" + byteStreamBufferToString(), RFIDCacheFilePath);
                break;

            case 134: // Get GSM status
                // Writes either 0 or 1, indicating the availability of the GSM module
                writeToCache("GSMStatus=" + (char)bytesRead[0], RFIDCacheFilePath);
                break;

            case 139: // PIN Challenge
                // Writes either 0 or 1, indicating the result of the PIN challenge
                writeToCache("PINChallenge=" + (char)bytesRead[0], RFIDCacheFilePath);
                break;

            case 141: // PIN Create
                // Writes the newly-created 6-digit PIN by the user
                writeToCache("PINCreate=" + byteStreamBufferToString(), RFIDCacheFilePath);
                break;

            case 135: // Get signal quality
                writeToCache("signalQuality=" + data[1], GSMSignalFilePath);
                writeToCache("deviceConnected=1", DeviceSignalFilePath);
                break;
            case 151: // Get SIM status
                // Writes either 0 or 1, indicating the status of an inserted SIM card in the GSM module
                writeToCache("SIMStatus=" + (char)bytesRead[0], RFIDCacheFilePath);
                break;
        }
    }

    /**
     * This method writes pckg.data to a specified cache file.
     * @param data This will be the pckg.data written to the file.
     * @param path This is where the file is/will be located.
     */
    private boolean writeToCache(String data, String path) {

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(path));
            writer.write(data);
            writer.flush();
            writer.close();
            if (serialCommDebugging) {
                System.out.println("[WRITE] " + data);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (serialCommDebugging) {
                System.out.println("[WRITE UNSUCCESSFUL]");
            }
            return false;
        }
        return true;
    }

    /**
     * Cancels the current operation being performed on the device
     */
    public void cancelOperation() {
        sendByteToDevice(131);
    }

    /**
     * Sends and SMS
     * @param recipientNumber is the phone number of the recipient
     * @param message is the message to be sent to the recipient
     */
    public void sendSMS(String recipientNumber, String message) {
        sendByteToDevice(136); // Sets the device to SMS mode
        SMSMode = true;
        SMSRecipientNumber = recipientNumber;
        SMSContent = message;
    }

    /**
     * Prompts the user to scan an RFID card and writes the pckg.data to the cache when it is received
     */
    public void scan() {
        sendByteToDevice(132); // Sends the command to the device
        interpretNextByteStream = true; // Will write the reply to cache

        // Keep track of the last command that was called to help with determining what to write to the cache file
        lastCommand = 132;
    }

    /**
     * Challenges the device's user to match a PIN
     * @param PIN is the PIN that needs to be matched by the device's user
     */
    public void PINChallenge(String PIN) {
        sendByteToDevice(139);
        sendStringToDevice(PIN);
        sendByteToDevice(3);

        interpretNextByte = true; // Will write the reply to cache
        // Keep track of the last command that was called to help with determining what to write to the cache file
        lastCommand = 139;
    }

    /**
     * Prompts the device's user to enter a PIN twice
     */
    public void PINCreate() {
        sendByteToDevice(141);
        interpretNextByteStream = true; // Will write the reply to cache
        // Keep track of the last command that was called to help with determining what to write to the cache file
        lastCommand = 141;
    }

    /**
     * Queries the device of the GSM Module's status and writes to the cache file
     */
    public void getGSMStatus() {
        sendByteToDevice(134);
        interpretNextByte = true; // Will write the reply to cache
        // Keep track of the last command that was called to help with determining what to write to the cache file
        lastCommand = 134;
    }

    /**
     * Queries the device of the SIM card's status and writes to the cache file
     */
    public void getSIMStatus() {
        sendByteToDevice(151);
        interpretNextByte = true; // Will write the reply to cache
        // Keep track of the last command that was called to help with determining what to write to the cache file
        lastCommand = 151;
    }

    /**
     * Gets the current siqnal quality of the cellular connection
     */
    public void getSignalQuality() {
        // Writes the signal quality of the GSM module
        sendByteToDevice(135);
        interpretNextByteStream = true; // Will write the reply to cache
        // Keep track of the last command that was called to help with determining what to write to the cache file
        lastCommand = 135;
    }

    /**
     * Toggles the GSM module on or off
     */
    public void toggleGSMPower() {
        sendByteToDevice(137);
    }

    /**
     * Closes the currently opened serial port
     */
    public void disconnect() {
        selectedPort.closePort();
    }

    /**
     * This queries the device's connection status
     * @return is the connection of the device
     */
    public boolean isDeviceReady() {
        return deviceReady;
    }

    /**
     * Clears the main cache file
     */
    public void clearCache(){
        writeToCache("", RFIDCacheFilePath);
    }//eto wala to kinalaman sa status
                                                                    //bale magkahiwalay na file yung sa status tapos sa pagamit nugn scanner

    /**
     * Clears the device signal and GSM signal cache files
     */
    public void clearStatusCache(){
        writeToCache("",DeviceSignalFilePath);
        writeToCache("",GSMSignalFilePath);
    }

    /**
     * Attempts to open a target serial port
     * @param targetPort is the selected port that this method will attempt to open
     * @param baudRate will be the baud rate that the serial communications line will use
     * @return the result of the port opening attempt
     */
    private boolean attemptOpeningPort(SerialPort targetPort, int baudRate) {
        // Set the comp port's parameters in the following order:
        // Baud Rate, New Data Bits, New Stop Bits, New Parity
        targetPort.setComPortParameters(baudRate, 8, 1, 0);
        // Set the timeout behavior
        targetPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        if (targetPort.openPort()) { // If the target port has been successfully opened
            selectedPort = targetPort;
            return true;
        }
        else { // If the target port could not be opened
            return false;
        }
    }

    /**
     * Converts the contents of byteStreamBuffer into a String
     * @return the String of byteStreamBuffer's values
     */
    private String byteStreamBufferToString() {
        String returnValue = "";
        for (byte b : byteStreamBuffer) { // Parse each byte in byteStreamBuffer into a char and add it to a String
            returnValue += (char)b;
        }
        return returnValue;
    }

    public boolean isSMSMode() {
        return SMSMode;
    }
}