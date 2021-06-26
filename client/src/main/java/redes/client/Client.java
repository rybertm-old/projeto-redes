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

    /**
     * Client CLI options
     */
    static final Options options = createCLIOptions();
    /**
     * CLI parser
     */
    static final CommandLineParser parser = new DefaultParser();
    /**
     * Default separator
     */
    static final char DEFAULT_VALUE_SEPARATOR = ' ';
    /**
     * Default encoding to use
     */
    static final EncodingOptions DEFAULT_ENCODING = EncodingOptions.CAESAR;
    /**
     * Default offset to be used with the {@code CAESAR} encryption
     */
    static final Integer DEFAULT_CAESAR_OFFSET = 13;

    /**
     * Client ppplication entry point
     */
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

    /**
     * Sends the image to the server to be tested for an existing hidden message
     * 
     * @param line {@code CLI} object to extract the {@code CLI} options from
     */
    private static void runImageTest(CommandLine line) {
        var connector = new ClientConnector();
        String imagePath = line.getOptionValue("t");
        Integer port = null;
        String portStr = line.getOptionValue("p");
        if (portStr != null) {
            port = Integer.parseInt(portStr);
        }
        String host = line.getOptionValue("s");
        connector.testImage(imagePath, host, port);
    }

    /**
     * Encodes message in a PNG image
     * 
     * @param line {@code CLI} object to extract the {@code CLI} options from
     * @return The PNG object with the encoded message
     * @throws IOException              If the image does not exist
     * @throws IllegalArgumentException If a message is not provided
     */
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
                // Wraps the offset to the 0-26 range
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
        /**
         * The encryption method is encoded using the first 3 *bits* of a **byte**, the
         * last 5 bits is used to encode the offset, in case of the CAESAR encryption
         */
        // Here the byte value of an encoding will be a number between 0-7 (2^3), which
        // means it has it's bits represented as [0b00000XXX], X being 0 or 1
        var firstByte = encoding.getValue();
        // If offset is not null that means its using the CAESAR encryption
        if (offset != null) {
            // Since offset is at most 26, it fits in 5 bits(2^5 = 32), so if we shift it 3
            // bits to the left we get an integer with it's bits represented as
            // [0bXXXXX000], X being 0 or 1
            offset = offset << 3;
            // Here we OR offset bits with firstByte bits to get an integer with it's bits
            // represented as [0bXXXXXYYY], X being the bits beloging to offset, and Y being
            // the bits beloging to firstByte
            firstByte = (byte) (offset | firstByte);
        }
        var data = StreamEx.of(firstByte).append(ArrayUtils.toObject(encryptedMessage.getBytes())).toList();

        var chunk = new Chunk(type, data);
        png.appendChunk(chunk);
        return png;
    }

    /**
     * Creates a new image with the encoding used appended to its name
     * 
     * @param originalImagePath Original image path
     * @param image             PNG object representation of the image
     * @param encoding          Encoding used
     * @throws IOException If the there is an error while writing to the new image
     *                     buffer
     */
    private static void createNewImage(String originalImagePath, Png image, String encoding) throws IOException {
        var path = Paths.get(originalImagePath);
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

    /**
     * Gets the {@link EncodingOptions} from the given string
     * 
     * @param encoding The encoding option
     * @return The encoding option enum
     * @throws IllegalArgumentException If there is no encoding option matching the
     *                                  given string
     */
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
        Option host = Option.builder("s").hasArg().argName("host")
                .desc("Defines the host used to connect with the test server. Defaults to localhost")
                .longOpt("server-host").valueSeparator(DEFAULT_VALUE_SEPARATOR).build();
        Option port = Option.builder("p").hasArg().argName("port")
                .desc("Defines the port used to connect with the test server. Defaults to 6868").longOpt("port")
                .valueSeparator(DEFAULT_VALUE_SEPARATOR).build();
        Option help = Option.builder("h").desc("Prints this message").longOpt("help").build();

        options.addOption(testImage);
        options.addOption(createImage);
        options.addOption(message);
        options.addOption(encryption);
        options.addOption(offset);
        options.addOption(host);
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
}