import java.io.*;
import java.util.*;

//TODO Exceptions

public class Page{

	private static int size = 0;
	private byte[] memory;

	public Page(int kilobytes){
		size = kilobytes;
		memory = new byte[1024 * size];
	}

	public Page(){
		if(size == 0){
			size = 1024;
		}
		memory = new byte[1024 * size];
	}

	public Node getNode(int level, int heightOfTree){
		int sizeOfNode = size/(1<<level);
		int offset = sizeOfNode;
		if(level == heightOfTree){
			sizeOfNode = sizeOfNode * 2;
			offset = 0;
		}
		try{
			Node result = deserialize(offset, sizeOfNode);
			if(result == null){
				return new Node(3);
			}
			return result;
		}catch(Exception e){
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
		byte[] subMemory = serialize(node);
		if(subMemory.length > sizeOfNode){
			return;
		};
		for(int i = offset; i < offset + subMemory.length; i++){
			// System.out.print(subMemory[i - offset] + " ");
			memory[i] = subMemory[i - offset];
		}
		System.out.println();

	}

	private Node deserialize(int offset, int sizeOfNode) throws IOException, ClassNotFoundException {
		byte [] subMemory = Arrays.copyOfRange(memory, offset, sizeOfNode + offset);
		
		try (ByteArrayInputStream byteArrayIn = new ByteArrayInputStream(subMemory);
		ObjectInputStream objectIn = new ObjectInputStream(byteArrayIn)) {
			return (Node) objectIn.readObject();
		} catch (final Exception e) {

			System.out.println("Deserializacja: " + e);
		
			return null;
		}
	}

	public static byte[] serialize(Node node) {
		if (node == null) {
			return new byte[0];
		}
		try (ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
	 	ObjectOutputStream objectOut = new ObjectOutputStream(byteArrayOut)) {
			objectOut.writeObject(node);
			return byteArrayOut.toByteArray();
		} catch (final IOException e) {	
			System.out.println("Serializacja: " + e);	
			return new byte[0];
		}
	}
}