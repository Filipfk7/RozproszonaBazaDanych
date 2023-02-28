package enums;

import java.io.*;
import java.net.Socket;

public enum CommunicationHelper {
    INSTANCE;

    public String getInput(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        return bufferedReader.readLine().trim();
    }

    public void sendOutput(Socket socket, String response) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream, true);
        printWriter.println(response + "\n");
    }
}
