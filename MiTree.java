import java.io.*;
import java.util.*;

public class MiTree <Key extends Comparable<? super Key>, Value> {
	private int height;
	Page rootPage;


	public MiTree(){
		height = 1;

		rootPage = new Page(1024);
		// rootPage.setNode(1, 1, new Node(3));
	}

	public Value search(Key key){
		Node node = (Node)rootPage.getNode(height, height);
		if(node == null)
			return null;
		int level = height;
		int ki = findFirstEqualOrGreater(node, key);
		while( !node.isLeaf() ){
			ki = findFirstEqualOrGreater(node, key);
			level--;
			node = (node.getSuccessor(ki)).getNode(level, height);
		}
		if(ki < node.getSuccessors().size() && ((Key)node.getKeys().get(ki)).compareTo(key) == 0){
			return (Value)node.getValues().get(ki);
		} else {
			return null;
		}
	}

	public void insert(Key key){
		Page newPage = new Page();
		Node newNode = new Node(3);
		newNode.setTest("ASBDAEDBUEDADSADDAS");
		newNode.getKeys().add(key);
		newNode.getValues().add(key);

		newPage.setNode(1, height, newNode);
		insert(key, newPage);
	}

	private void insert(Key key, Page page){
		Page newPage = new Page();
		ResultInsertEntry tmpResult = insertEntry(key, page, newPage, rootPage, height);

		// rootPage = newPage;
		newPage = tmpResult.getPage();
		if(tmpResult.getR() == "FULL"){
			Page newPagePrim = new Page();
			Node c = newPage.getNode(height, height);
			height++;
			c.getSplitPoint();
			ArrayList<Node> splited = c.split();
			// Node cPrim = newPage.getNode(height, height);
			Node cPrim = new Node(3);

			cPrim.getKeys().add(
				c.getKeys().get(
					c.getSplitPoint()
				)
			);
			cPrim.addSuccessor(newPage);
			cPrim.addSuccessor(newPagePrim);

			newPage.setNode(height - 1, height, splited.get(0));
			newPage.setNode(height - 1, height, splited.get(1));
			newPage.setNode(height, height, cPrim);

		}
		rootPage = newPage;
	}

	public ResultInsertEntry insertEntry(Key key, Page P, Page N, Page B, int level){
		// System.out.println(((Node)P.getNode(1, height)).getTest());
		Node C = B.getNode(level, height);
		N= new Page();

		if(!C.isLeaf()){
			int i = findFirstEqualOrGreater(C, key);

			ResultInsertEntry result = insertEntry(key, P, N, (Page) C.getSuccessor(i), level - 1);
			C.getSuccessors().set(i, N.getId());
			if(result.getR() == "SPLIT"){
				key = (Key)result.getKey();
				P = result.getPage();
				N = result.getPage();
			} else {
				N.setNode(level, height, C);
				return new ResultInsertEntry("NULL", null, null);
			}
		}
		// if( C.hasSpaceFor(key, P)){ ?????????
		if(!C.isFull()){
			System.out.println("wchodze tutaj z buta" + (C instanceof Node));
			C.addSuccessor(P);
			C.getKeys().add(key);
			N.setNode(level, height, C);
			System.out.println(N.getNode(level, height).getSuccessors().size());
			if( C.isFull() ){
				return new ResultInsertEntry("FULL", key, N);
			} else {
				return new ResultInsertEntry("NULL", key, N);
			}
		} else {
			Page NPrim = new Page();
			Key splitKey = (Key)C.getKeys().get(C.getSplitPoint());
			ArrayList<Node> splited = C.split();
			if(key.compareTo(splitKey) < 0){
				splited.get(0).getKeys().add(key);
				splited.get(0).getSuccessors().add(P.getId());
			} else {
				splited.get(1).getKeys().add(key);
				splited.get(1).getSuccessors().add(P.getId());
			}
			if(!splited.get(0).isLeaf() && splited.get(1).contains(N)){
				Node tmp = splited.get(0);
				splited.set(0,splited.get(1));
				splited.set(1,tmp);
			}
			N.setNode(level, height, splited.get(0));
			NPrim.setNode(level, height, splited.get(0));
			return new ResultInsertEntry("SPLIT", (Key)(splited.get(0).getKeys().get(0)), NPrim);
		}
	}

	public void deletion(Key key){
		Page n = new Page();
		// String r = deleteEntry(key, n, rootPage, height);
		String r = "";
		if (r.compareTo("ONE") == 0) {
			Node c = ((Page)n).getNode(height, height);
			Node cPrim = (c.getSuccessor(0)).getNode(height, height);
			height--;
			n.setNode(height, height, cPrim);
		}
	}

	/**
	* Przeszukuje klucze podanego wierzchołka i wyszukuje index pierwszego większego lub równego klucza
	* @param node przeszukiwany wierzchołek
	* @param key wyszukiwany klucz
	* @return index pierwszego większego lub index wychodzący poza tablice kluczy
	*/
	private int findFirstEqualOrGreater(Node node, Key key) {
		int nrSuccessor;
		for(nrSuccessor = 0; nrSuccessor < node.getKeys().size(); nrSuccessor++){
			if(((Key)node.getKeys().get(nrSuccessor)).compareTo(key) >= 0){
				break;
			}
		}
		return nrSuccessor;
	}

	private void mergeChild(Node parent, int leftSonIndex) {
		Node leftSon =
			((Page)
				parent.getSuccessor(leftSonIndex)
			).getNode(parent.getLevel() -1 , height);
		Node rightSon =
			((Page)
				parent.getSuccessor(leftSonIndex+1)
			).getNode(parent.getLevel() -1 , height);

		leftSon.getSuccessors().addAll(rightSon.getSuccessors());

		if (!leftSon.isLeaf())
			leftSon.getKeys().add(parent.getKeys().get(leftSonIndex));
		leftSon.getKeys().addAll(rightSon.getKeys());
		parent.getKeys().remove(leftSonIndex);
		parent.getSuccessors().remove(leftSonIndex + 1);
	}
	/* ----------------------------------------------------------------------------- */
	/**
	* Rysowanie drzewa
	*/
	public void dump(){
		dump(rootPage, height, "");
	}
	private void dump(Page page, int level, String indentation){
		Node node = (Node)page.getNode(level, height);
		System.out.print(indentation + (node.isLeaf()?"Leaf[":"Node[") + node.getKeys().size() + " , " + node.getSuccessors().size() + "]");
		System.out.println();
		System.out.print(indentation);
		for(int i = 0; i < node.getKeys().size(); i++){
			System.out.print(node.getKeys().get(i) + " ");
		}

		System.out.println();

		if(node.isLeaf())
			return;

		for(int i = 0; i < node.getSuccessors().size(); i++){
			dump((Page)node.getSuccessors().get(i), level - 1, "   " + indentation);
		}

	}
/* ----------------------------------------------------------------------------- */
}
