import java.io.*;
import java.util.*;

public class MiTree <Key extends Comparable<? super Key>, Value> {
	private int height;
	Page rootPage;


	public MiTree(){
		height = 1;

		rootPage = new Page(10024);
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

	public void insert(Key key, Value value){
		Page newPage = new Page();
		ResultInsertEntry tmpResult = insertEntry(key, value, rootPage, newPage, height);

		if(tmpResult.getR() == "SPLIT"){
			Node newRoot = new Node(4);
			newRoot.getKeys().add(tmpResult.getKey());
			newRoot.addSuccessor(newPage);
			newRoot.addSuccessor(tmpResult.getPage());
			newRoot.setLevel(height+1);
			newPage.setNode(height, height+1, newPage.getNode(height, height));
			tmpResult.getPage().setNode(height, height+1, tmpResult.getPage().getNode(height, height));
			height++;
			newPage.setNode(height, height, newRoot);
		}

		rootPage = newPage;
	}

public ResultInsertEntry insertEntry(Key key, Value value, Page subRoot, Page newPage, int level){
		Node C = subRoot.getNode(level, height);
		int i = findFirstEqualOrGreater(C, key);

		if(C.isLeaf()){
			if(i < C.getKeys().size()) {
				C.getKeys().add(i, key);
				C.getValues().add(i, value);
			} else {
				C.getKeys().add(key);
				C.getValues().add(value);
			}
			if(C.isFull(height)){
				Key keyPrim = (Key)C.getKeys().get(C.getSplitPoint());
				Page newPagePrim = new Page();
				ArrayList<Node> splited = C.split();

				newPage.setNode(1, height, splited.get(0));
				newPagePrim.setNode(1, height, splited.get(1));

				return new ResultInsertEntry("SPLIT", keyPrim, newPagePrim);
			} else {
				newPage.setNode(1, height, C);
			}
			return new ResultInsertEntry("NULL", null, null);
		}
		else {
			ResultInsertEntry tmpResult = insertEntry(key, value, C.getSuccessor(i), newPage, level - 1);
			if(tmpResult.getR() == "SPLIT"){
				C.getKeys().add(i, tmpResult.getKey());
				C.getSuccessors().set(i, tmpResult.getPage().getId());
				C.getSuccessors().add(i, newPage.getId());
			} else {
				C.getSuccessors().set(i, newPage.getId());
			}

			if(C.isFull(height)){
				Key keyPrim = (Key)C.getKeys().get(C.getSplitPoint()-1);
				Page newPagePrim = tmpResult.getPage();
				ArrayList<Node> splited = C.split();

				newPage.setNode(level, height, splited.get(0));
				newPagePrim.setNode(level, height, splited.get(1));

				return new ResultInsertEntry("SPLIT", keyPrim, newPagePrim);
			} else {
				newPage.setNode(level, height, C);
				return new ResultInsertEntry("NULL", null, null);
			}
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

			if(merged.isFull(height)){
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
		dumpNode(node, indentation);

		if(node.isLeaf())
			return;

		for(int i = 0; i < node.getSuccessors().size(); i++){
			dump((Page)node.getSuccessor(i), level - 1, "   " + indentation);
		}

	}
	public static void dumpNode(Node node, String indentation){
// 		System.out.println(indentation + (node.isLeaf()?"Leaf":"Node") + "[K" + node.getKeys().size() + ", V" + node.getValues().size() + ", S" + node.getSuccessors().size() + "] ");
		System.out.print(indentation);
		for(int i = 0; i < node.getKeys().size(); i++){
			System.out.print(node.getKeys().get(i) + " ");
		}
		System.out.println();

// 		if (node.isLeaf()){
// 			for(int i = 0; i < node.getValues().size(); i++){
// 				System.out.print(node.getValues().get(i) + " ");
// 			}
// 		}
// 		System.out.println();
	}
/* ----------------------------------------------------------------------------- */
}
