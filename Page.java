import java.io.*;
import java.util.*;

//TODO Exceptions

public class Page implements Serializable{

	private static int size = 0;
	private int pageId;
	private byte[] memory;
	private static Vector<Page> index = new Vector();
	private boolean onNAND = false;

	public Page(int kilobytes){
		size = kilobytes;
		pageId = index.size();
		index.add(this);
		memory = new byte[1024 * size];
	}

	public Page(){
		if(size == 0){
			size = 1024;
		}
		pageId = index.size();
		index.add(this);
		memory = new byte[1024 * size];
	}
	public int getCountOfPage(){
		return index.size();
	}
	public int getId(){
		return pageId;
	}
	public static Page getById(int id){
		return index.elementAt(id);
	}

	public Node getNode(int level, int heightOfTree){
		int sizeOfNode = size/(1<<level);
		int offset = sizeOfNode;
		if(level == heightOfTree){
			sizeOfNode = sizeOfNode * 2;
			offset = 0;
		}
		try{
			Node result = (Node)deserializeNode(offset, sizeOfNode);
			return result;
		}catch(Exception e){
			System.out.println("abce");
			return new Node(4);
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
		if(subMemory.length > sizeOfNode){
			return;
		};
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
			return new Node(4);
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
			System.out.println("Serializacja: " + e);
			for (StackTraceElement ste : e.getStackTrace()) {
				System.out.println(ste);
			}
			System.out.println();
			return null;
		}
	}
}
