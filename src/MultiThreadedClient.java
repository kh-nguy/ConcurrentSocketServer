import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

/*
 *
 *
 */
public class MultiThreadedClient {

    private static final String[] VALID_COMMANDS = {
            "date and time", "uptime", "memory use", "netstat", "current users", "running processes"
    };

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("enter server address: ");
        String serverAddress = scanner.nextLine();

        System.out.print("enter port number: ");
        int port = Integer.parseInt(scanner.nextLine());

        boolean run = true;

        while (run) {
            System.out.println("available commands:");
            System.out.println("date and time, uptime, memory use, netstat, current users, running processes, or quit");
            System.out.print("enter command to request: ");
            String command = scanner.nextLine().trim().toLowerCase();

            if (command.equalsIgnoreCase("quit") || command.equalsIgnoreCase("q")) {
                run = false;
                System.out.println("exiting ...");
                break;
            }

            if (!Arrays.asList(VALID_COMMANDS).contains(command)) {
                System.out.println("\ninvalid command. please try again.\n");
                continue;
            }

            System.out.print("enter number of client requests (1, 5, 10, 15, 20, 25, 100): ");
            int numClients;
            try {
                numClients = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("invalid number, try again.");
                continue;
            }

            long[] turnaroundTimes = new long[numClients];
            Thread[] threads = new Thread[numClients];

            for (int i = 0; i < numClients; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    try {
                        long start = System.currentTimeMillis();

                        Socket socket = new Socket(serverAddress, port);
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                        out.println(command);
                        String line;
                        System.out.println("response from server (client " + (index + 1) + ")");
                        while ((line = in.readLine()) != null) {
                            System.out.println(line);
                        }

                        socket.close();
                        long end = System.currentTimeMillis();
                        long turnaroundTime = end - start;
                        turnaroundTimes[index] = turnaroundTime;
                        System.out.println("Turn-around Time for client " + (index + 1) + ": " + turnaroundTime + " ms");

                    } catch (IOException e) {
                        System.err.println("client " + (index + 1) + " failed: " + e.getMessage());
                        turnaroundTimes[index] = -1;
                    }
                });
                threads[i].start();
            }

            // Wait for all threads to finish
            for (int i = 0; i < numClients; i++) {
                try {
                    threads[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            long totalTime = 0;
            int successfulRequests = 0;

            for (long time : turnaroundTimes) {
                if (time != -1) {
                    totalTime += time;
                    successfulRequests++;
                }
            }

            if (successfulRequests > 0) {
                double averageTime = (double) totalTime / successfulRequests;
                System.out.println("\nTotal Turn-around Time: " + totalTime + " ms");
                System.out.println("Average Turn-around Time: " + averageTime + " ms");
            } else {
                System.out.println("all client requests failed.");
            }
        } // end while
        scanner.close();
    } // end main
} // end class