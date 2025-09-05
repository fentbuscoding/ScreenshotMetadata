import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.util.Iterator;
import org.w3c.dom.NodeList;

public class CheckMetadata {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java CheckMetadata <path_to_png>");
            return;
        }
        
        try {
            File file = new File(args[0]);
            ImageInputStream iis = ImageIO.createImageInputStream(file);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                reader.setInput(iis, true);
                
                IIOMetadata metadata = reader.getImageMetadata(0);
                String[] names = metadata.getMetadataFormatNames();
                
                for (String name : names) {
                    System.out.println("Format: " + name);
                    IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(name);
                    displayMetadata(root, 0);
                }
            }
            
            iis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void displayMetadata(IIOMetadataNode node, int level) {
        String indent = "  ".repeat(level);
        System.out.println(indent + node.getNodeName());
        
        if (node.getNodeName().equals("tEXtEntry")) {
            String keyword = node.getAttribute("keyword");
            String value = node.getAttribute("value");
            System.out.println(indent + "  " + keyword + " = " + value);
        }
        
        for (int i = 0; i < node.getLength(); i++) {
            displayMetadata((IIOMetadataNode) node.item(i), level + 1);
        }
    }
}
