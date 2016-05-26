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
		Node newNode = new Node(4);
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
		// newPage = tmpResult.getPage();
		// newPage
		if(tmpResult.getR() == "FULL"){
			Node oldRoot = newPage.getNode(height, height);
			height++;

			ArrayList<Node> newSons = oldRoot.split();
			Node newRoot = new Node(4);

			newRoot.setLevel(oldRoot.getLevel() + 1);
			System.out.println("Przepełnienie roota ");

			newRoot.getKeys().add(
					oldRoot.getKeys().get(
						oldRoot.getSplitPoint()
					)
			);
			Page a = new Page();
			Page b = new Page();
			b.setNode(height-1, height, newSons.get(1));
			a.setNode(height-1, height, newSons.get(0));
			newRoot.addSuccessor(a);
			newRoot.addSuccessor(b);
			a.setNode(height, height, newRoot);
			newPage = a;

		}
		rootPage = newPage;
	}

	public ResultInsertEntry insertEntry(Key key, Page P, Page N, Page B, int level){
		// System.out.println(((Node)P.getNode(1, height)).getTest());
		Node C = B.getNode(level, height);
		// N= new Page();

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
				return new ResultInsertEntry("NULL", key, N);
			}
		}
		// if( C.hasSpaceFor(key, P)){ ?????????
		if(!C.isFull()){
			C.addSuccessor(P);
			C.getKeys().add(key);
			N.setNode(level, height, C);
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
			NPrim.setNode(level, height, splited.get(1));
			return new ResultInsertEntry("SPLIT", key, NPrim);
		}
	}

	public void deletion(Key key){
		Page n = new Page();
		deleteEntry(key, rootPage.getNode(height, height), n);

		rootPage = n;
	}

	public void deleteEntry(Key key, Node node, Page newPage){
		//TODO nie umiemy usuwać ostatniego elementu

		int i = findFirstEqualOrGreater(node, key);

		if(node.isLeaf()){
			if(i<node.getKeys().size() && ((Key)node.getKeys().get(i)).compareTo(key) == 0){
				node.getKeys().remove(i);
				node.getValues().remove(i);

				newPage.setNode(node.getLevel(), height, node);
			}
		} else {
			deleteEntry(key, node.getSuccessor(i).getNode(node.getLevel()-1, height), newPage);
			node.getSuccessors().set(i, newPage.getId());
			ResultInsertEntry afterMerge;

			if(((Node)node.getSuccessor(i).getNode(node.getLevel()-1, height)).isThirsty()){
				afterMerge = mergeAndSplit(node, i, newPage);

				if (afterMerge.getR().equals("ONE")) {
					node.getSuccessors().set(i, newPage.getId());

					node.getSuccessors().remove(i == node.getSuccessors().size() - 1 ? i : i+1);

					if (node.getLevel() == height && node.getSuccessors().size() == 1) {

						height--;
						newPage.setNode(height, height, newPage.getNode(height, height+1));

						return;
					}
				} else {
					node.getSuccessors().set(i, newPage);
					node.getSuccessors().set(i + 1, afterMerge.getPage());
				}
			}

			newPage.setNode(node.getLevel(), height, node);
		}

	}

	private ResultInsertEntry mergeAndSplit(Node node, int nrChild, Page page){
		if(node.getSuccessors().size() != 1) {
			mergeChild(node, Math.max(0, nrChild-1));

			Node merged = (Node)node.getSuccessors().get(nrChild);

			if(merged.isFull()){
				node.getKeys().add(nrChild+1, merged.getKeys().get(merged.getSplitPoint()));

				ArrayList<Node> splitted = merged.split();

				node.getSuccessors().set(nrChild, splitted.get(0));

				if (nrChild+1 == node.getSuccessors().size()) {
					node.getSuccessors().add(splitted.get(1));
				} else {
					node.getSuccessors().add(nrChild+1, splitted.get(1));
				}
				page.setNode(merged.getLevel()-1, height, splitted.get(0));

				Page otherPage = new Page();

				otherPage.setNode(merged.getLevel()-1, height, splitted.get(1));

				return new ResultInsertEntry("TWO", null, otherPage);
			} else {
				page.setNode(merged.getLevel(), height, merged);

				return new ResultInsertEntry("ONE", null, null);
			}
		}
		return new ResultInsertEntry("ONE", null, null);
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
		System.out.println("--------------------------------");
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
			dump((Page)node.getSuccessor(i), level - 1, "   " + indentation);
		}

	}
/* ----------------------------------------------------------------------------- */
}
