package redes.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class Png {
   private static final Byte[] STANDARD_HEADER = { (byte) 137, 80, 78, 71, 13, 10, 26, 10 };

   @Getter
   private List<Chunk> chunks;

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
         throw new IllegalArgumentException("Invalid Header");
      }
   }

   public static Png fromChunks(List<Chunk> chunks) {
      return new Png(chunks);
   }

   public static boolean isHeaderValid(List<Byte> header) {
      return Arrays.equals(header.toArray(new Byte[0]), Png.STANDARD_HEADER);
   }

   public void appendChunk(Chunk chunk) {
      this.chunks.add(chunk);
   }

   public void removeChunk(String chunkType) throws IllegalArgumentException {
      var chunkOpt = chunkByType(chunkType);
      if (chunkOpt.isPresent()) {
         var chunk = chunkOpt.get();
         this.chunks.remove(chunk);
      } else {
         throw new IllegalArgumentException("Chunk type specified does not exist");
      }
   }

   public static Byte[] header() {
      return Png.STANDARD_HEADER;
   }

   public List<Byte> asBytes() {
      var data = new ArrayList<Byte>();
      data.addAll(Arrays.asList(Png.STANDARD_HEADER));

      for (var chunk : this.chunks) {
         data.addAll(chunk.asBytes());
      }

      return data;
   }

   public Optional<Chunk> chunkByType(String chunkType) {
      for (var chunk : this.chunks) {
         if (chunk.getChunkType().toString().equals(chunkType)) {
            return Optional.of(chunk);
         }
      }
      return Optional.empty();
   }
}
