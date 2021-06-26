package redes.client;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.ArrayUtils;

import one.util.streamex.StreamEx;
import redes.lib.Chunk;
import redes.lib.ChunkType;
import redes.lib.Encoder;
import redes.lib.EncodingOptions;
import redes.lib.Png;

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
public class Client {

    static final Options options = createCLIOptions();
    static final CommandLineParser parser = new DefaultParser();
    static final char DEFAULT_VALUE_SEPARATOR = ' ';
    static final EncodingOptions DEFAULT_ENCODING = EncodingOptions.CAESAR;
    static final Integer DEFAULT_CAESAR_OFFSET = 13;

    public static void main(String[] args) {
        var client = new Client();
        if (args.length < 1) {
            printHelp();
            System.exit(1);
        }
        client.parse(args);
    }

    /**
     * Parses the program arguments into options and runs the respective
     * funcionalities
     * 
     * @param args The arguments to be parsed into commands and options
     */
    private void parse(String[] args) {
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("h")) {
                printHelp();
                System.exit(0);
            }
            if (line.hasOption("t")) {
                runImageTest(line);
            }
            if (line.hasOption("c")) {
                // TODO create image
                var enconding = getEncoding(line.getOptionValue("e")).toString();
                var imagePath = line.getOptionValue("c");
                if (!imagePath.endsWith(".png")) {
                    throw new IllegalArgumentException("Not a valid PNG image");
                }
                var image = encodePng(line);
                createNewImage(imagePath, image, enconding);
            }
        } catch (ParseException exp) {
            System.err.println("Parsing failed. Reason: " + exp.getMessage());
            printHelp();
        } catch (IOException ex) {
            System.out.println("I/O Error: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            System.out.println("Illegal Argument Error: " + ex.getMessage());
        }
    }

    private static void runImageTest(CommandLine line) {
        var connector = new ClientConnector();
        String imagePath = line.getOptionValue("t");
        Integer port = null;
        String portStr = line.getOptionValue("p");
        if (portStr != null) {
            port = Integer.parseInt(portStr);
        }
        connector.testImage(imagePath, port);
    }

    private static Png encodePng(CommandLine line) throws IOException, IllegalArgumentException {
        var imagePath = line.getOptionValue("c");
        EncodingOptions encoding = getEncoding(line.getOptionValue("e"));
        String offsetStr = line.getOptionValue("o");
        Integer offset = null;
        if (encoding.equals(EncodingOptions.CAESAR)) {
            System.out.println("Encryption not specified. Using CAESAR encryption");
            if (offsetStr == null) {
                offset = DEFAULT_CAESAR_OFFSET;
                System.out.println("Offset not specified. Using offset " + DEFAULT_CAESAR_OFFSET);
            } else {
                offset = Integer.parseInt(offsetStr) % 26;
            }
        } else {
            if (offsetStr != null) {
                System.out.println("XOR encryption does not need an offset, ignoring it.");
            }
        }

        String message = line.getOptionValue("m");
        if (message == null) {
            throw new IllegalArgumentException("Please provide a message to encode on the image");
        }

        var imgBytes = Files.readAllBytes(Paths.get(imagePath));
        var png = Png.fromBytes(StreamEx.of(ArrayUtils.toObject(imgBytes)).toList());
        var type = ChunkType.fromString("reDe");
        var encryptedMessage = Encoder.encode(message, encoding, offset);
        var firstByte = encoding.getValue();
        if (offset != null) {
            offset = offset << 3;
            firstByte = (byte) (offset | firstByte);
        }
        var data = StreamEx.of(firstByte).append(ArrayUtils.toObject(encryptedMessage.getBytes())).toList();

        var chunk = new Chunk(type, data);
        png.appendChunk(chunk);
        return png;
    }

    private static void createNewImage(String imagePath, Png image, String encoding) throws IOException {
        var path = Paths.get(imagePath);
        var pathStr = path.getParent().toString() + '/';
        var filename = path.getFileName().toString();
        var filenameNoType = filename.substring(0, filename.length() - 4);
        String newFile = pathStr.concat(filenameNoType).concat("_" + encoding + ".png");
        try (var output = new FileOutputStream(newFile)) {
            var dos = new DataOutputStream(output);

            final var BUFFER_SIZE = 8192;
            var buffer = new byte[BUFFER_SIZE];
            var read = 0;
            var imgBytes = ArrayUtils.toPrimitive(image.asBytes().toArray(new Byte[0]));
            InputStream imgStream = new ByteArrayInputStream(imgBytes);

            while ((read = imgStream.read(buffer)) > 0) {
                dos.write(buffer, 0, read);
            }
        } catch (IOException ex) {
            throw new IOException("Error while creating new image");
        }

    }

    private static EncodingOptions getEncoding(String encoding) throws IllegalArgumentException {
        if (encoding != null) {
            return EncodingOptions.getEnum(encoding);
        }
        return EncodingOptions.CAESAR;
    }

    /**
     * Creates the accepted CLI options
     * 
     * @return The CLI {@code Options} object
     */
    private static Options createCLIOptions() {
        var options = new Options();

        Option testImage = Option.builder("t").hasArg().argName("image-path")
                .desc("Checks the specified image for a hidden message and prints it").longOpt("test-image")
                .valueSeparator(DEFAULT_VALUE_SEPARATOR).build();
        Option createImage = Option.builder("c").hasArg().argName("image-path")
                .desc("Creates a copy of the specified image containing the given message").longOpt("create-image")
                .valueSeparator(DEFAULT_VALUE_SEPARATOR).build();
        Option message = Option.builder("m").hasArg().argName("message").desc("The message to encrypt")
                .longOpt("message").valueSeparator(DEFAULT_VALUE_SEPARATOR).build();
        Option encryption = Option.builder("e").hasArg().argName("encryption")
                .desc("Defines the encryption method to be used when testing/creating the image. Defaults to CAESAR")
                .longOpt("encryption").valueSeparator(DEFAULT_VALUE_SEPARATOR).build();
        Option offset = Option.builder("o").hasArg().argName("offset")
                .desc("Defines the offset of the CAESAR encryption option. Defaults to 13").longOpt("offset")
                .valueSeparator(DEFAULT_VALUE_SEPARATOR).build();
        Option port = Option.builder("p").hasArg().argName("port")
                .desc("Defines the port used to connect with the test server. Defaults to 6868").longOpt("port")
                .valueSeparator(DEFAULT_VALUE_SEPARATOR).build();
        Option help = Option.builder("h").desc("Prints this message").longOpt("help").build();

        options.addOption(testImage);
        options.addOption(createImage);
        options.addOption(message);
        options.addOption(encryption);
        options.addOption(offset);
        options.addOption(port);
        options.addOption(help);
        return options;
    }

    /**
     * Prints the command-line interface help message
     */
    private static void printHelp() {
        var formatter = new HelpFormatter();
        formatter.printHelp("client", options);
    }

    /**
     * Checks if the encryption is supported by the encoder
     * 
     * @param encryption {@code String} to be tested
     * @return True if the encryption is supported, false otherwise
     */
    private static void validateEncryption(String encryption) {
        if (!encryption.equalsIgnoreCase(DEFAULT_ENCODING.toString())) {
            System.err.println("Encryption not supported: " + encryption);
            System.exit(2);
        }
    }
}