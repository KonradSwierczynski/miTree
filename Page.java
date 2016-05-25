import java.io.*;
import java.util.*;

//TODO Exceptions

public class Page implements Serializable{

	private static int size = 0;
	private int pageId;
	private byte[] memory;
	private static Vector<Page> index = new Vector();

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
		memory = new byte[1024 * size];
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
			if(result == null){
				return new Node(3);
			}
			return result;
		}catch(Exception e){
			System.out.println("abce");
			return new Node(3);
		}
		// return null;
	}

	public void setNode(int level, int heightOfTree, Node node){
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
			// System.out.print(subMemory[i - offset] + " ");
			memory[i] = subMemory[i - offset];
		}
		// System.out.println();

	}

	private Object deserializeNode(int offset, int sizeOfNode) throws IOException, ClassNotFoundException {
		byte [] subMemory = Arrays.copyOfRange(memory, offset, sizeOfNode + offset);
		// System.out.println("Deserializacja " + offset + " " + sizeOfNode + " " + subMemory.length);
		// for(int i=offset; i<offset+sizeOfNode;i++)
			// System.out.print(subMemory[i-offset]);

		try (ByteArrayInputStream byteArrayIn = new ByteArrayInputStream(subMemory);
		ObjectInputStream objectIn = new ObjectInputStream(byteArrayIn)) {
			return objectIn.readObject();
		/*} catch (final Exception e) {

			System.out.println("Deserializacja: " + e);
			for (StackTraceElement ste : e.getStackTrace()) {
				System.out.println(ste);
			}
			System.out.println();
			return null;*/
		}catch (IOException e){

			System.out.println("abce");
			return new Node(3);
		}
	}

	public byte[] serializeNode(Object node) {
		if (node == null) {
			System.out.println("dostarczono nulla");
			return new byte[0];
		}
		try (ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
	 	ObjectOutputStream objectOut = new ObjectOutputStream(byteArrayOut)) {
	 		if(node == null)
	 			System.out.println("to jest qpa");
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
