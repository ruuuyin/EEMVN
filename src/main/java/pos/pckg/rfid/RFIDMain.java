package pos.pckg.rfid;

import java.util.Scanner;

public class RFIDMain {
    public static Scanner sc = new Scanner(System.in);
    public static RFIDReaderInterface device;
    public static void main(String[] args) throws InterruptedException {
        System.out.print("Splash Screen Line 1 : ");
        String splashText_line1 = sc.nextLine();
        System.out.print("Splash Screen Line 2 : ");
        String splashText_line2 = sc.nextLine();

        device = new RFIDReaderInterface(splashText_line1, splashText_line2);

        int menuState = 0;

        while (menuState != 100) {
            if (menuState == 0) {
                System.out.println("[MENU]");
                System.out.print("SMSMode is ");
                if (device.isSMSMode()) {
                    System.out.println("ON");
                }
                else {
                    System.out.println("OFF");
                }

                System.out.println("1 - Scan");
                System.out.println("2 - Challenge");
                System.out.println("3 - New Passcode");
                System.out.println("4 - GSM Status");
                System.out.println("5 - Toggle GSM Power");
                System.out.println("6 - Get Signal Quality");
                System.out.println("7 - Send SMS");
                System.out.println("99 - Cancel");

                System.out.println("\n100 - Exit");
                System.out.print("Input : ");
                menuState = Integer.parseInt(sc.nextLine());
            }
            else if (menuState == 1) {
                device.scan();
                menuState = 0;
            }
            else if (menuState == 2) {
                System.out.print("PIN : ");
                device.PINChallenge(sc.nextLine());
                menuState = 0;
            }
            else if (menuState == 3) {
                device.PINCreate();
                menuState = 0;
            }
            else if (menuState == 4) {
                device.getGSMStatus();
                menuState = 0;
            }
            else if (menuState == 5) {
                device.toggleGSMPower();
                menuState = 0;
            }
            else if (menuState == 6) {
                device.getSignalQuality();
                menuState = 0;
            }
            else if (menuState == 7) {
                System.out.print("Recipient : +");
                String recipient = sc.nextLine();
                System.out.print("Message (enter <END> to mark the end of the message) : ");
                String tempInput;
                String message = "";

                while (true) {
                    tempInput = sc.nextLine();

                    if (tempInput.equals("<END>")) {
                        break;
                    }
                    else {
                        message += tempInput;
                        message += (char)10;
                    }
                }

                /*
                System.out.println("[START OF MESSAGE]");
                System.out.println(message);
                System.out.println("[END OF MESSAGE]");
                */

                device.sendSMS(recipient,message);

                menuState = 0;
            }
            else if (menuState == 99) {
                device.cancelOperation();
                menuState = 0;
            }
            else {
                System.out.println("Invalid Menu Option");
                menuState = 0;
            }
        }
        System.out.println("Exiting...");
        device.disconnect();
        System.exit(0);
    }
}