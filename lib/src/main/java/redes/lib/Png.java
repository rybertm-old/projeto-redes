package redes.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Robert Broketa
 * @author Hiago Rios
 */
@NoArgsConstructor
@AllArgsConstructor
public class Png {
   private static final Byte[] STANDARD_HEADER = { (byte) 137, 80, 78, 71, 13, 10, 26, 10 };

   @Getter
   private List<Chunk> chunks;

   /**
    * Creates a {@code Png} from a {@code List<Byte>}
    * 
    * @param bytes The bytes to create the Png from
    * @return A {@code Png} instance
    * @throws IllegalArgumentException If the header does not correspond the valid
    *                                  PNG header
    */
   public static Png fromBytes(List<Byte> bytes) throws IllegalArgumentException {
      List<Byte> header = bytes.subList(0, 8);
      if (Png.isHeaderValid(header)) {
         var begin = 8;
         var end = begin + 4;
         List<Chunk> chunks = new ArrayList<>();
         while (end < bytes.size()) {
            int length = ChunkHelper.fromBytesToInt(new ArrayList<>(bytes.subList(begin, end)));
            end += length + 8;
            chunks.add(Chunk.fromBytes(new ArrayList<>(bytes.subList(begin, end))));
            begin = end;
            end += 4;
         }
         return new Png(chunks);
      } else {
         throw new IllegalArgumentException("Invalid Header. The file may not be a valid PNG image");
      }
   }

   /**
    * Creates a {@code Png} from a {@code List<Chunk>}
    * 
    * @param chunks The chunks to create the Png from
    * @return A {@code Png} instance
    */
   public static Png fromChunks(List<Chunk> chunks) {
      return new Png(chunks);
   }

   /**
    * Checks if the {@code List<byte>} is a valid PNG header
    * 
    * @param header The {@code List<Byte>} to be tested
    * @return True if the header is a valid PNG header, false otherwise
    */
   public static boolean isHeaderValid(List<Byte> header) {
      return Arrays.equals(header.toArray(new Byte[0]), Png.STANDARD_HEADER);
   }

   /**
    * Appends the given {@code Chunk} to the end of this {@code Png} list of chunks
    * 
    * @param chunk The {@code Chunk} to be appended
    */
   public void appendChunk(Chunk chunk) {
      this.chunks.add(chunk);
   }

   /**
    * Removes the first chunk of the specified {@code ChunkType} from this
    * {@code Png} list of chunks
    * 
    * @param chunkType
    * @throws IllegalArgumentException
    */
   public void removeChunk(String chunkType) throws IllegalArgumentException {
      var chunkOpt = chunkByType(chunkType);
      if (chunkOpt.isPresent()) {
         var chunk = chunkOpt.get();
         this.chunks.remove(chunk);
      } else {
         throw new IllegalArgumentException("Chunk type specified does not exist");
      }
   }

   /**
    * Returns the standard {@code Byte[]} header expected for PNG files
    * 
    * @return The {@code Byte[]} header
    */
   public static Byte[] header() {
      return Png.STANDARD_HEADER;
   }

   /**
    * Creates a {@code List<Byte>} representation of this {@code Png} data
    * 
    * @return The {@code List<Byte>}
    */
   public List<Byte> asBytes() {
      var data = new ArrayList<Byte>();
      data.addAll(Arrays.asList(Png.STANDARD_HEADER));

      for (var chunk : this.chunks) {
         data.addAll(chunk.asBytes());
      }

      return data;
   }

   /**
    * Iterates over this {@code Png} chunks checking if each one's type matches the
    * specified {@code ChunkType} and returns the first match
    * 
    * @param chunkType The {@code ChunkType} to search for
    * @return The first {@code Chunk} of the specified {@code ChunkType} if any is
    *         found, empty otherwise
    */
   public Optional<Chunk> chunkByType(String chunkType) {
      for (var chunk : this.chunks) {
         if (chunk.getChunkType().toString().equals(chunkType)) {
            return Optional.of(chunk);
         }
      }
      return Optional.empty();
   }
}
