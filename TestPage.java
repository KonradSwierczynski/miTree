public class TestPage{
	public static void main(String [] args){
		Page testowy = new Page(1024);
		Node node = new Node(3);
		node.setTest("kartkadasd");
		int l = 4;
		testowy.setNode(l,4, node);
		Node node2 = (Node)testowy.getNode(l,4);
		System.out.println(node2.getTest());
	}
}