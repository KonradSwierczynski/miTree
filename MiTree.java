import java.io.*;
import java.util.*;

public class MiTree <Key extends Comparable<? super Key>, Value> {
	private int height = 1;
	private final int keysInNode = 4;
	Page rootPage;

	public MiTree(){
		rootPage = new Page(1024);
		rootPage.setNode(1, 1, new Node(4));
	}

	public Value search(Key key){
		Node node = (Node)rootPage.getNode(height, height);
		int level = height;
		int ki = findFirstEqualOrGreater(node, key);
		while( !node.isLeaf() ){
			level--;
			node = (node.getSuccessor(ki)).getNode(level, height);
			ki = findFirstEqualOrGreater(node, key);
		}
		if(ki < node.getKeys().size() && ((Key)node.getKeys().get(ki)).compareTo(key) == 0)
			return (Value)node.getValues().get(ki);
		 else 
			return null;
	}

	public void insert(Key key, Value value){
		if( search(key) != null)	//warunek nie powtarzania się elementów
			return;
		Page newPage = new Page();
		Page newPagePrim = new Page();
		Node root = rootPage.getNode(height,height);
		insertEntry(key, value, root, newPage, newPagePrim);

		if(root.isFull(height)){
			Node newRoot = new Node(keysInNode);
			newRoot.setLevel(root.getLevel()+1);
			newRoot.getKeys().add(root.getKeys().get(root.getSplitPoint()-1));
			ArrayList<Node> splitted = root.split();
			newRoot.getSuccessors().add(newPage.getId());
			newRoot.getSuccessors().add(newPagePrim.getId());
			height++;
			newPage.setNode(height-1, height, splitted.get(0));
			newPagePrim.setNode(height-1, height, splitted.get(1));
			newPage.setNode(height, height, newRoot);

		} else 
			newPage.setNode(height, height, root);
		
		rootPage = newPage;
	}

	public void insertEntry(Key key, Value value, Node subRoot, Page newPage, Page newPagePrim){
		int i = findFirstEqualOrGreater(subRoot, key);
		if(subRoot.isLeaf()){
			if(i < subRoot.getKeys().size()) {
				subRoot.getKeys().add(i, key);
				subRoot.getValues().add(i, value);
			} else {
				subRoot.getKeys().add(key);
				subRoot.getValues().add(value);
			}
		} else {
			Node tmp = subRoot.getSuccessor(i).getNode(subRoot.getLevel()-1,height);
			insertEntry(key, value, tmp, newPage, newPagePrim);
			subRoot.getSuccessors().set(i, newPage.getId());
			if(tmp.isFull(height))
				splitChild(subRoot, i, newPage, newPagePrim);
		}
		newPage.setNode(subRoot.getLevel(), height, subRoot);
	}

	public void delete(Key key){
		if (search(key) == null)
			return;
		
		Page newPage = new Page();
		Page newPagePrim = new Page();
		deleteEntry(key, rootPage.getNode(height, height), newPage, newPagePrim);
		Node tmpNode = newPage.getNode(height, height); 
		if(tmpNode.getKeys().size() == 0 && tmpNode.isLeaf() == false){
			Node tmp = newPage.getNode(height, height).getSuccessor(0).getNode(height - 1, height);
			height--;
			newPage.setNode(height, height, tmp);
		}

		rootPage = newPage;
	}

	private void deleteEntry(Key key, Node node, Page newPage, Page newPagePrim){
		int i = findFirstEqualOrGreater(node, key);

		if(node.isLeaf()){
			if(i<node.getKeys().size() && ((Key)node.getKeys().get(i)).compareTo(key) == 0){
				node.getKeys().remove(i);
				node.getValues().remove(i);
			}
			newPage.setNode(node.getLevel(), height, node);
		} else {
			deleteEntry(key, (Node)node.getSuccessor(i).getNode(node.getLevel()-1, height), newPage, newPagePrim);
			node.getSuccessors().set(i, newPage.getId());
			if(node.getSuccessors().size() > 1 && 
				((Node)node.getSuccessor(i).getNode(node.getLevel()-1, height)).isThirsty()){
				mergeChild(node, i, newPage, newPagePrim);
			}
			i = findFirstEqualOrGreater(node, key);
			if ( node.getKeys().size() > 0 && ((Node)node.getSuccessor(i).getNode(node.getLevel()-1, height)).isFull(height)){
				splitChild(node, i, newPage, newPagePrim);
			}
			newPage.setNode(node.getLevel(), height, node);
		}

	}

	private void mergeChild(Node parent, int leftSonIndex, Page newPage, Page newPagePrim) {
		if(parent.getSuccessors().size() == leftSonIndex + 1)
			leftSonIndex--;
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
		else
			leftSon.getValues().addAll(rightSon.getValues());
		leftSon.getKeys().addAll(rightSon.getKeys());
		parent.getKeys().remove(leftSonIndex);
		parent.getSuccessors().remove(leftSonIndex + 1);
		newPage.setNode(leftSon.getLevel(), height, leftSon);
		parent.getSuccessors().set(leftSonIndex, newPage.getId());
	}

	private void splitChild(Node parent, int index, Page newPage, Page newPagePrim) {
		Node node = parent.getSuccessor(index).getNode(parent.getLevel() - 1, height);
		parent.getKeys().add(index, node.getKeys().get(node.getSplitPoint()-1));

		ArrayList<Node> splitted = node.split();
		node = splitted.get(0);
		Node newNode = splitted.get(1);

		newPage.setNode(node.getLevel(), height, node);
		newPagePrim.setNode(node.getLevel(), height, newNode);
		parent.getSuccessors().set(index, newPage.getId());
		parent.getSuccessors().add(index + 1, newPagePrim.getId());
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

	
	/* ----------------------------------------------------------------------------- */
	/**
	* Rysowanie drzewa
	*/
	public void dump(){
		dump(rootPage, height, "");
		System.out.println("--------------------------------");
	}
	private void dump(Page page, int level, String indentation){
		Node node = (Node)page.getNode(level, height);
		dumpNode(node, indentation);

		if(node.isLeaf())
			return;

		for(int i = 0; i < node.getSuccessors().size(); i++)
			dump((Page)node.getSuccessor(i), level - 1, "   " + indentation);
	}

	private static void dumpNode(Node node, String indentation){
 		System.out.println(indentation + (node.isLeaf()?"Leaf":"Node") + "[K" + node.getKeys().size() + ", V" + node.getValues().size() + ", S" + node.getSuccessors().size() + "] ");
		System.out.print(indentation);
		for(int i = 0; i < node.getKeys().size(); i++)
			System.out.print(node.getKeys().get(i) + " ");
		
		System.out.println();
	}
/* ----------------------------------------------------------------------------- */
}
