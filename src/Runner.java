import java.io.IOException;
import java.util.Scanner;

public class Runner {

  public static void main(String[] args) throws IOException {
    Server ourServer = new Server();
    ourServer.start(6666);

  }
}
