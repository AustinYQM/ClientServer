import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Client {
  private Socket clientSocket;
  private PrintWriter output;
  private BufferedReader input;


  public void startConnection(String ip, int port){
    try {
      clientSocket = new Socket(ip, port);
      output = new PrintWriter(clientSocket.getOutputStream(), true);
      input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

    }
    catch(IOException e) {
      System.out.println("Could not open socket");
      System.out.println(e);
    }
  }

  public String sendMessage(String msg) throws IOException {
    output.println(msg);
    return input.readLine();

  }


  public void stopConnection() throws IOException {
    input.close();
    output.close();
    clientSocket.close();
  }

  public static void main(String[] args) throws IOException {

    Client tester = new Client();
    Scanner read = new Scanner(System.in);
    tester.startConnection("127.0.0.1",6666);

    String msg = "";
    while(!msg.equalsIgnoreCase("close"))
    {
      System.out.print("Send to server?: ");
      msg = read.nextLine();

      tester.sendMessage(msg);

      String repsonse = tester.sendMessage(msg);


      System.out.println("\nResponse: " + repsonse);
    }
    tester.stopConnection();
  }
}
