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

   public boolean isHeaderValid(List<Byte> header) {
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

   public Byte[] header() {
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
