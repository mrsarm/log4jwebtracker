package log4jwebtracker.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;


/**
 * Stream I/O utils.
 *
 * @author Mariano Ruiz
 */
public abstract class StreamUtils {

    static public void readStream(InputStream inputStream, OutputStream outputStream, int bufferSize)
            throws IOException {
        byte[] buf = new byte[bufferSize];
        int bytesRead = inputStream.read(buf);
        while (bytesRead != -1) {
            outputStream.write(buf, 0, bytesRead);
            bytesRead = inputStream.read(buf);
        }
    }

    static public void readFile(RandomAccessFile inputFile, OutputStream outputStream, int bufferSize)
            throws IOException {
        byte[] buf = new byte[bufferSize];
        int bytesRead = inputFile.read(buf);
        while (bytesRead != -1) {
            outputStream.write(buf, 0, bytesRead);
            bytesRead = inputFile.read(buf);
        }
    }

    static private void readLines(RandomAccessFile inputFile, OutputStream outputStream, long seek)
            throws IOException {
    	inputFile.seek(seek);
        String line = inputFile.readLine();
        while (line != null) {
        	outputStream.write((line + "\n").getBytes());
            line = inputFile.readLine();
        }
    }

    static public void tailFile(RandomAccessFile inputFile, OutputStream outputStream,
    		int bufferSize, int numLines) throws IOException {
    	if(bufferSize<1) {
    		throw new IllegalArgumentException("bufferSize < 1");
    	}
    	if(numLines<1) {
    		throw new IllegalArgumentException("numLines < 1");
    	}
    	byte[] buf = new byte[bufferSize];
    	long fileSizeMark = inputFile.length();
        long offset = fileSizeMark;
        long seek;
        int len;
        if(offset-bufferSize>=0) {
        	seek = offset-bufferSize;
        	len = bufferSize;
        } else {
        	seek = 0;
        	len = (int)offset;
        }
        inputFile.seek(seek);
        int bytesRead = inputFile.read(buf, 0, len);
        while(bytesRead != -1 && bytesRead != 0) {
        	int i=bytesRead-1;
        	while(i>=0 && numLines>0) {
        		if(buf[i]=='\n') {  // End line & not EOF
        			if(offset==fileSizeMark && i==bytesRead-1) {
        				fileSizeMark=-1;
        			} else {
        				numLines--;
        			}
        		}
        		i--;
        	}
        	if(numLines==0) {
        		readLines(inputFile, outputStream, offset-bytesRead+i+2);
        		return;
        	}
        	offset -= bytesRead;
            if(offset-bufferSize>=0) {
            	seek = offset-bufferSize;
            	len = bufferSize;
            } else {
            	seek = 0;
            	len = (int)offset;
            }
        	inputFile.seek(seek);
            bytesRead = inputFile.read(buf, 0, len);
        }
		readLines(inputFile, outputStream, 0);
        // input and output stream must be closed
    }
}
