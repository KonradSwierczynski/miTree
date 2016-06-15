import java.io.*;
import java.util.*;

//TODO Exceptions

public class Page implements Serializable{
	private static int maxTreeHeight;
	private static int size = 0;
	private int pageId;
	private byte[] memory;
	public static HashMap<Integer, Page> index = new HashMap();
	public static Integer numberPage = 0;
	private boolean onNAND = false;

	private int nodeBaseSize = 256;


	public Page(int kilobytes, int maxTreeHeight){
		size = kilobytes * 1024;
		pageId = numberPage;
		index.put(numberPage++, this);
		this.maxTreeHeight = maxTreeHeight;
		memory = new byte[size + maxTreeHeight * nodeBaseSize];
	}

	public Page(){
		if(size == 0){
			size = 1024 * 1024;
		}
		if (maxTreeHeight == 0)
			maxTreeHeight = 4;
		pageId = numberPage;
		index.put(numberPage++, this);
		memory = new byte[size + maxTreeHeight * nodeBaseSize];
	}
	public static int getCountOfPage(){
		return index.size();
	}
	public int getId(){
		return pageId;
	}
	public static Page getById(int id){
		return index.get(id);
	}

	public Node getNode(int level, int heightOfTree){
		int sizeOfNode = size/(1<<level);
		int offset = sizeOfNode;
		if(level == heightOfTree){
			sizeOfNode = sizeOfNode * 2;
			offset = 0;
		}
		offset += (maxTreeHeight - level) * nodeBaseSize;
		// System.err.println("DES " + offset + ", " + level + ", " + heightOfTree + ", " 
		//  			+ (size/(1<<(level - 1)) + (maxTreeHeight - level + 1) * nodeBaseSize) + ", " + (sizeOfNode + nodeBaseSize));
		//System.err.println("DES "+ offset + ", " + level + ", " + heightOfTree);
		//System.err.println("DES " + (offset + sizeOfNode + nodeBaseSize) + ", " + (size/(1<<(level - 1)) + (maxTreeHeight - level + 1) * nodeBaseSize));
		try{
			Node result = (Node)deserializeNode(offset, sizeOfNode + nodeBaseSize);
			return result;
		}catch(Exception e){
			System.out.println(e);

			return new Node(2 * (maxTreeHeight - level + 1));
		}
		// return null;
	}

	public void setNode(int level, int heightOfTree, Node node){
		if (onNAND){
			System.err.println("Błąd krytyczny - Próba nadpisania strony");
			System.exit(1);
		}
		int sizeOfNode = size/(1<<level);
		int offset = sizeOfNode;
		if(level == heightOfTree){
			sizeOfNode = sizeOfNode * 2;
			offset = 0;
		}
		byte[] subMemory = serializeNode(node);
		if(subMemory == null)
			System.out.println("WTF");
		if(subMemory.length > sizeOfNode + nodeBaseSize){
			return;
		};
		offset += (maxTreeHeight - level) * nodeBaseSize;

		//System.err.println("SER " + offset + ", " + level + ", " + heightOfTree + ", " + subMemory.length);
		for(int i = offset; i < offset + subMemory.length; i++){
			memory[i] = subMemory[i - offset];
		}
	}

	public void writeToNAND() {
		onNAND = true;
	}

	private Object deserializeNode(int offset, int sizeOfNode) throws IOException, ClassNotFoundException {
		byte [] subMemory = Arrays.copyOfRange(memory, offset, sizeOfNode + offset);

		try (ByteArrayInputStream byteArrayIn = new ByteArrayInputStream(subMemory);
		ObjectInputStream objectIn = new ObjectInputStream(byteArrayIn)) {
			return objectIn.readObject();
		}catch (IOException e){
			// System.err.println("HEHEHEHEHEHE" + sizeOfNode + ", " + offset + ", " +( (size + maxTreeHeight * nodeBaseSize) - sizeOfNode - offset));
			// System.err.println(e);
			return null;
		}
	}

	public byte[] serializeNode(Object node) {
		if (node == null) {
			return new byte[0];
		}
		try (ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
	 	ObjectOutputStream objectOut = new ObjectOutputStream(byteArrayOut)) {
			objectOut.writeObject(node);
			return byteArrayOut.toByteArray();
		} catch (final IOException e) {
			//System.out.println("Serializacja: " + e);
			for (StackTraceElement ste : e.getStackTrace()) {
				System.out.println(ste);
			}
			System.out.println();
			return null;
		}
	}
}