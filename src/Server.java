import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;

public class Server {
  private ServerSocket serverSocket;
  private static ArrayList<ClientHandler> connectedClients = new ArrayList<>();

  public void start(int port) throws IOException {
    serverSocket = new ServerSocket(port);
    while (true) {
      new ClientHandler(serverSocket.accept()).start();
    }
  }

  public void stop() throws IOException {
    serverSocket.close();
  }

  private class ClientHandler extends Thread {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    Calendar timeCreated;
    String id;

    public ClientHandler(Socket socket) {
      this.clientSocket = socket;
      this.id = "TEMP";
      connectedClients.add(this);
      timeCreated = Calendar.getInstance();
    }

    private long getTimeAlive() {
      long startTime = timeCreated.getTimeInMillis();
      long nowTime = Calendar.getInstance().getTimeInMillis();

      return (nowTime - startTime) / 1000L;
    }

    private void messageHandler(String msg) throws IOException {
      String cMsg = msg.toUpperCase();
      System.out.println(cMsg);

      if (cMsg.startsWith("PING")) {
        out.println("PONG");
      } else if (cMsg.startsWith("CLOSE")) {
        out.println("Connection Closed"); // I don't think this is gonna work.
        closeClient();
      } else if (msg.contains(":")) {
        String[] msgParts = msg.split(":", 3);
        if (msgParts[0].equalsIgnoreCase("STATE")) {
          int index = idExists(msgParts[1]);
          if (index == -1) {
            out.println(
                "We are sorry but a user with that id ("
                    + msgParts[1]
                    + ") is not connected to this server.");
          } else {
            out.println("Client has been alive for " + connectedClients.get(index).getTimeAlive() + " seconds.");
          }
        } else if (msgParts[0].equalsIgnoreCase("INIT")) {
          if (idExists(msgParts[1]) != -1) {
            out.println("Sorry that ID (" + msgParts[1] + ") is taken by another user. This client's ID will remain " + id);
          } else {
            id = msgParts[1];
            out.println("Client has been given the User ID " + id);
          }
        } else if (msgParts[0].equalsIgnoreCase("SAY")) {
          int recipientIndex = idExists(msgParts[1]);
          if (recipientIndex == -1) {
            out.println(
                "We are sorry but a user with that id ("
                    + msgParts[1]
                    + ") is not connected to this server.");
          } else if (msgParts[1].equals(this.id)) {
            out.println("You may not message yourself.");
          } else {
            connectedClients.get(recipientIndex).out.println(msgParts[2]);
            out.println("Message sent to User ID" + msgParts[1]);
          }
        } else if (msgParts[0].equalsIgnoreCase("BROADCAST")) {
          for (ClientHandler user : connectedClients) {
            if (!user.getThisId().equals(this.getThisId())) user.out.println(msgParts[1]);
            out.println("Message sent to all users.");
          }
        }
      } else {
        out.println("INVALID COMMAND \"" + msg + "\".");
      }
    }

    private int idExists(String id) {
      for (int i = 0; i < connectedClients.size(); i++) {
        if (connectedClients.get(i).getThisId().equals(id)) return i;
      }
      return -1;
    }

    private String getThisId() {
      return id;
    }

    private void closeClient() throws IOException {
      in.close();
      out.close();
      clientSocket.close();
    }

    public void run() {
      try {
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        String inputLine = in.readLine();
        while (true) {
          if (inputLine == null) {
            closeClient();
            break;
          }
          System.out.println(inputLine);
          messageHandler(inputLine);
          inputLine = in.readLine();
        }
      } catch (IOException e) {
        System.out.println("CLIENT (" + getThisId() + ") DISCONNECTED");
      }
    }
  }
}
