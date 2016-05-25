import java.io.*;
import java.util.*;

public class MiTree <Key extends Comparable<? super Key>, Value> {
	private int height;
	Page rootPage;


	public MiTree(){
		height = 1;
		rootPage = new Page();
	}

	public Value search(Key key){
		Node node = (Node)rootPage.getNode(height, height);
		int level = height;
		int ki = findFirstEqualOrGreater(node, key);
		while( !node.isLeaf() ){
			ki = findFirstEqualOrGreater(node, key);
			level--;
			node = (Node)((Page)node.getSuccessors().get(ki)).getNode(level, height);
		}
		if(ki < node.getSuccessors().size() && ((Key)node.getKeys().get(ki)).compareTo(key) == 0){
			return (Value)((ValueNode)((Page)node.getSuccessors().get(ki)).getNode(level, height)).getValue();
		} else {
			return null;
		}
	}

	public void insert(Key key){
		insert(key, rootPage);
	}

	private void insert(Key key, Page page){
		Page newPage = new Page();
		ResultInsertEntry tmpResult = insertEntry(key, page, newPage, rootPage, height);
		if(tmpResult.getR() == "FULL"){
			Page newPagePrim = new Page();
			Successor c = newPage.getNode(height);
			height++;
			c.getSplitPoint();
			Array<Successor> splited = c.split();
			Successor cPrim = n.getNode(height, height);

			cPrim.getKeys().add(
				c.getKeys().get(
					c.getSplitPoint()
				)
			);
			cPrim.getSuccessors().add(newPage);
			cPrim.getSuccessors().add(newPagePrim);

			n.setNode(height - 1, height, splited.get(0));
			n.setNode(height - 1, height, splited.get(1));
			n.setNode(height, height, cPrim);
			
		}
	}

	public ResultInsertEntry insertEntry(Key key, Page P, Page N, Page B, int level){
		Node C = B.getNode(level, height);
		if(C.isLeaf()){
			int i = findFirstEqualOrGreater(C, key);
			if(i > C.getSuccessors().get.size()){
				i = C.getSuccessors();
			}
			ResultInsertEntry result = insertEntry(key, P, N, C.getSuccessors().get(i), level - 1);
			C.getSuccessors().set(i, N);
			if(result.getR() == "SPLIT"){
				key = result.getK();
				P = result.getP();
				N = result.getP();
			} else {
				N.add(C);
				return new ResultInsertEntry("NULL", null, null);
			}
		}
		if( C.hasSpaceFor(key, P)){
			C.insert(key, P);
			N.add(C);
			if( C.isFull() ){
				return new resultInsertEntry("FULL", null, null);
			} else {
				return new resultInsertEntry("NULL", null, null);
			}
		} else {
			Page NPrim = new Page();
			Key splitKey = c.getKeys().get(c.getSplitPoint());
			Array<Successor> splited = c.split();
			if(key < splitKey){
				splited.get(0).getKeys().add(key);
				splited.get(0).getSuccessors().add(P);
			} else {
				splited.get(1).getKeys().add(key);
				splited.get(1).getSuccessors().add(P);
			}
			if(!splited.get(0).isLeaf() && splited.get(1).contains(N)){
				Node tmp = splited.get(0);
				splited.set(0,splited.get(1));
				splited.set(1,tmp);
			}
			N.setNode(splited.get(0), level);
			NPrim.setNode(splited.get(0), level);
			return new insertEntry("SPLIT", splited.get(0).getKeys().get(0), NPrim);
		}
	}

	public void deletion(Key key){
		Page n = new Page();
		String r = deleteEntry(key, n, rootPage, height);
		if (r.compareTo("ONE") == 0) {
			Node c = n.getNode(height, height);
			Node cPrim = c.getSuccessors().get(0).getNode(height, height);
			height--;
			n.setNode(level, height, cPrim);
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
		Node leftSon = (Node)parent.getSuccessors().getNode(parent.getLevel() -1 , height).get(leftSonIndex);
		Node rightSon = (Node)parent.getSuccessors().getNode(parent.getLevel() -1 , height).get(leftSonIndex + 1);
		
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
		dump(rootPage, height - 1, "");
	}
	private void dump(Page page, int level, String indentation){
		if(level > 1){
			Node node = (Node)page.getNode(level, height);
			System.out.print(indentation + "InnerNode [" + node.getKeys().size() + " , " + node.getSuccessors().size() + "]");
			System.out.println();
			System.out.print(indentation);
			for(int i = 0; i < node.getKeys().size(); i++){
				System.out.print(node.getKeys().get(i) + " ");
			}

			System.out.println();
			
			for(int i = 0; i < node.getSuccessors().size(); i++){
				dump((Page)node.getSuccessors().get(i), level - 1, "   " + indentation);
			}
		} else {
			//testowa linijka, do usunięcia
			if( level != 1) System.out.println(">> Level: " + level + " why not 1 <<");

			ValueNode valueNode = (ValueNode)page.getNode(level, height);
			System.out.println(indentation + "Leaf [ Value: " + (Key)valueNode.getValue() + "]");

			System.out.println();
		}
	}
/* ----------------------------------------------------------------------------- */
}