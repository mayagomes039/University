//VideoStream
package src;
import java.io.*;
import java.util.Arrays;

public class VideoStream {

  FileInputStream fis; //video file
  int frame_nb; //current frame nb
  String filename;

  //-----------------------------------
  //constructor
  //-----------------------------------
  public VideoStream(String filename) throws Exception{

    this.filename = filename;
    //init variables
    fis = new FileInputStream(filename);
    frame_nb = 0;
  }

  public int getFrameNumber() {
    return frame_nb;
  }

  //-----------------------------------
  // getnextframe
  //returns the next frame as an array of byte and the size of the frame
  //-----------------------------------
  public int getnextframe(byte[] frame) throws Exception
  {
    int length = 0;
    String length_string;
    byte[] frame_length = new byte[5];

    //read current frame length
    int bytesRead = fis.read(frame_length, 0, 5);
    if (bytesRead < 5) {
      Arrays.fill(frame, 0, frame.length, (byte) 0);
      CustomLogger.logWarning("Fim do arquivo ou dados insuficientes ao ler o comprimento do quadro. Enviando quadro em branco.");
      
      //reiniciar video
      fis = new FileInputStream(filename);
      frame_nb = 0;

      return frame.length;
    }

    length_string = new String(frame_length).trim();
    if (length_string.isEmpty()) {
      Arrays.fill(frame, 0, frame.length, (byte) 0);
      CustomLogger.logWarning("Comprimento do quadro vazio. Enviando quadro em branco.");
      return frame.length;
    }

    length = Integer.parseInt(length_string);

    bytesRead = fis.read(frame, 0, length);
    if (bytesRead < length) {
      Arrays.fill(frame, bytesRead, length, (byte) 0);
      throw new EOFException("Fim do arquivo ou dados insuficientes ao ler o quadro.");
    }

    return length;
  }

  public void close() throws IOException {
    if (fis != null) {
      fis.close();
    }
  }

}
