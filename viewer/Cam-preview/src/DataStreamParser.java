import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class DataStreamParser extends AbstractSerialParser implements Runnable,
		Observer {
	private final static byte NEW_FRAME = 0x55; // there is a 3 pixel gap on picture edges so 'one' should never occur ...
	private final static int BYTE_PER_BLOCK = 4;

	PreviewPanel display;
	byte[] imageBuffer;
	List<Block> blocks = new ArrayList<Block>();

	boolean newFrame;

	public DataStreamParser() {
		this.in = null;
	}
	
	public DataStreamParser(InputStream in) {
		this.in = in;
	}
	
	public static int unsignedByteToInt(byte b) {
	    return (int) b & 0xFF;
	    }

	public void run() {
		int len = 0;
		int nb_byte = 0;
		byte[] buffer = new byte[1024];
		byte[] blockData = new byte[BYTE_PER_BLOCK];
		System.out.println("Running data parser");
		try {
			while ((len = in.read(buffer)) > -1) {
				for (int i = 0; i < len; i++) {
					System.out.println("Byte received :"+buffer[i]);
					if (nb_byte == BYTE_PER_BLOCK) {
						int x, y, w, h;
						x = unsignedByteToInt(blockData[0])*2;
						y = unsignedByteToInt(blockData[1])*2;
						w = unsignedByteToInt(blockData[2])*2;
						h = unsignedByteToInt(blockData[3])*2;
						System.out.println("new Block : x=" + x + ", y=" + y
								+ ", w=" + w + ", h=" + h);
						blocks.add(new Block(x, y, w, h));
						nb_byte = 0  ;
					}
					if (buffer[i] == NEW_FRAME) {
						System.out.println("New Frame \n \n \n");
						blocks = new ArrayList<Block>();
						nb_byte = 0;
					}else if(nb_byte < BYTE_PER_BLOCK) {
						blockData[nb_byte] = buffer[i];
						nb_byte ++ ;
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if(o instanceof ImageStreamParser){
			ImageStreamParser imageParser = (ImageStreamParser) o ;
			for(Block b : blocks){
				imageParser.getDisplay().image.getGraphics().drawRect(b.x/4, b.y/4, b.w/4, b.h/4) ;
			}
			imageParser.getDisplay().repaint();
		}
	}

	public class Block {
		public int x, y, w, h;

		public Block(int x, int y, int w, int h) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}
	}

}
