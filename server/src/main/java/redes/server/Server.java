package redes.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author Robert Broketa
 * @author Hiago Rios
 * 
 * @apiNote Error codes
 *          <ul>
 *          <li>1 - Wrong usage / Insufficient arguments</li>
 *          <li>2 - Unsupported arguments</li>
 *          </ul>
 */
public class Server {

    static final Options options = createCLIOptions();
    static final CommandLineParser parser = new DefaultParser();
    static final char DEFAULT_VALUE_SEPARATOR = ' ';
    static final int DEFAULT_PORT = 6868;
    static final int DEFAULT_CLIENT_LIMIT = 4;

    static int port;
    static int maxConnections;

    public static void main(String[] args) {
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("h")) {
                printHelp();
                System.exit(0);
            }

            port = DEFAULT_PORT;
            if (line.getOptionValue("p") != null) {
                port = Integer.parseInt(line.getOptionValue("p"));
            }

            maxConnections = DEFAULT_CLIENT_LIMIT;
            if (line.getOptionValue("c") != null) {
                maxConnections = Integer.parseInt(line.getOptionValue("c"));
            }

            if (maxConnections <= 0) {
                System.err.println("Maximum connections must be at least 1");
                System.exit(2);
            }

            listen(port, maxConnections);
        } catch (ParseException exp) {
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            printHelp();
        }
    }

    /**
     * Initiates a server socket and handle it's clients connections
     * 
     * @param port           The port which the server will listen on
     * @param maxConnections The limit of simultaneous connections
     */
    private static void listen(int port, int maxConnections) {
        AtomicInteger connectionsCounter = new AtomicInteger(0);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);
            System.out.println("Up to " + maxConnections + " connections available");

            while (true) {
                Socket socket = serverSocket.accept();
                var writer = new PrintWriter(socket.getOutputStream(), true);
                if (connectionsCounter.get() < maxConnections) {
                    writer.println("ready");
                    connectionsCounter.incrementAndGet();

                    System.out.println(String.format("CLIENT %s - Connected", socket.getInetAddress()));

                    new ServerConnection(socket, connectionsCounter).start();
                } else {
                    System.out.println("Reached connection limit");
                    writer.println("Too many connections active. Try again later");
                    writer.println("bye");
                    socket.close();
                }
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Creates the accepted CLI options
     * 
     * @return The CLI {@code Options} object
     */
    private static Options createCLIOptions() {
        Options options = new Options();

        Option port = Option.builder("p").hasArg().argName("port")
                .desc("Defines the port to listen to. Defaults to " + DEFAULT_PORT).longOpt("port")
                .valueSeparator(DEFAULT_VALUE_SEPARATOR).build();
        Option maxClients = Option.builder("c").hasArg().argName("clients-limit")
                .desc("Defines the maximum clients to accept simultaneously. Defaults to " + DEFAULT_CLIENT_LIMIT)
                .longOpt("max-clients").valueSeparator(DEFAULT_VALUE_SEPARATOR).build();
        Option help = Option.builder("h").desc("Prints this message").longOpt("help").build();

        options.addOption(port);
        options.addOption(maxClients);
        options.addOption(help);
        return options;
    }

    /**
     * Prints the command-line interface help message
     */
    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("server", options);
    }
}
