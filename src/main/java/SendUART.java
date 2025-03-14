import com.fazecast.jSerialComm.*;

public class SendUART {
    public static void main(String[] args) {
        // Identify available serial ports

        SerialPort[] ports = SerialPort.getCommPorts();
        if(ports.length == 0) System.out.println("No COM ports found ;(");

        for (SerialPort port : ports) {
            System.out.println("Found Port: " + port.getSystemPortName());
        }

        // Open the serial port (Change COM3 to your actual port)
        SerialPort serialPort = SerialPort.getCommPort("COM3"); //Alexander's laptop had COM7
        serialPort.setBaudRate(9600);  // Set baud rate (match with receiver)
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        serialPort.setParity(SerialPort.NO_PARITY);

        if (!serialPort.openPort()) {
            System.out.println("Failed to open port.");
            return;
        }

        SerialPort serialPort4 = SerialPort.getCommPort("COM4");
        if (!serialPort4.openPort()) {
            System.out.println("Failed to open port.");
            return;
        }

        System.out.println("Port opened successfully.");

        // Send data
        String message = "Hello, UART!";
        byte[] data = message.getBytes();
        serialPort.writeBytes(data, data.length);
        System.out.println("Data sent: " + message);

        // Close the port
        boolean varb = true;
        while(varb)
        {
            varb = true;
        }
        serialPort.closePort();
        System.out.println("Port closed.");

    }
}
