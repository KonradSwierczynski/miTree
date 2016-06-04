import java.io.*;
import java.util.*;

public class MiTree <Key extends Comparable<? super Key>, Value> {
	private int height;
	private final int keysInNode = 4;
	Page rootPage;


	public MiTree(){
		height = 1;

		rootPage = new Page(1024);
		rootPage.setNode(1, 1, new Node(4));
		// rootPage.setNode(1, 1, new Node(keysInNode));
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
			System.out.println(ki);
			if(node.getKeys().size()> 0)
			System.out.println(node.getKeys().get(0));
		if(ki < node.getKeys().size() && ((Key)node.getKeys().get(ki)).compareTo(key) == 0){
			return (Value)node.getValues().get(ki);
		} else {
			return null;
		}
	}

	public void insert(Key key, Value value){
		if( search(key) != null)	//warunek nie powtarzania się elementów
			return;
		Page newPage = new Page();
		Page newPagePrim = new Page();
		// ResultInsertEntry tmpResult = 
		Node root = rootPage.getNode(height,height);
		insertEntry(key, value, root, newPage, newPagePrim);
		// root = newPage.getNode(height,height);

		if(root.isFull(height)){
			Node newRoot = new Node(4);
			newRoot.setLevel(root.getLevel()+1);
			newRoot.getKeys().add(root.getKeys().get(root.getSplitPoint()-1));
			ArrayList<Node> splitted = root.split();
			newRoot.getSuccessors().add(newPage.getId());
			newRoot.getSuccessors().add(newPagePrim.getId());
			height++;
			newPage.setNode(height-1, height, splitted.get(0));
			newPagePrim.setNode(height-1, height, splitted.get(1));
			newPage.setNode(height, height, newRoot);


		} else {
			newPage.setNode(height, height, root);
		}
		rootPage = newPage;
	}

	public ResultInsertEntry insertEntry2(Key key, Value value, Page subRoot, Page newPage, int level){
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
				Key keyPrim = (Key)C.getKeys().get(C.getSplitPoint()-1);
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
			ResultInsertEntry tmpResult = insertEntry2(key, value, C.getSuccessor(i), newPage, level - 1);
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
			// insertEntry(key, value, C.getSuccessor(i), newPage, level - 1);
			Node tmp = subRoot.getSuccessor(i).getNode(subRoot.getLevel()-1,height);
			insertEntry(key, value, tmp, newPage, newPagePrim);
			subRoot.getSuccessors().set(i, newPage.getId());
			// newPage.setNode(subRoot.getLevel() - 1, height, tmp);
			if(tmp.isFull(height))
				splitChild(subRoot, i, newPage, newPagePrim);
		}
		newPage.setNode(subRoot.getLevel(), height, subRoot);
	}

	public void delete(Key key){
		if (search(key) == null){
			// System.out.println("Nie znaleziono " + key);
			return;
		}
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
		//TODO nie umiemy usuwać ostatniego elementu

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
				// System.out.println(node.getLevel()+"przed "+node.getSuccessor(i).getNode(node.getLevel() - 1, height).getKeys().size());
				mergeChild(node, i, newPage, newPagePrim);
				System.out.println("scalanie");
				// System.out.println(node.getLevel()+"po "+node.getSuccessor(i).getNode(node.getLevel() - 1, height).getKeys().size());
				// i--;
			}
			i = findFirstEqualOrGreater(node, key);
			if ( node.getKeys().size() > 0 && ((Node)node.getSuccessor(i).getNode(node.getLevel()-1, height)).isFull(height)){
				splitChild(node, i, newPage, newPagePrim);
				System.out.println("splitowanie");
			}
			newPage.setNode(node.getLevel(), height, node);
			/* deleteEntry(key, node.getSuccessor(i).getNode(node.getLevel()-1, height), newPage);
			node.getSuccessors().set(i, newPage.getId());

			if( ((Node)node.getSuccessor(i).getNode(node.getLevel()-1, height)).isThirsty()){
				ResultInsertEntry afterMerge = mergeAndSplit(node, i, newPage);

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
			*/

		}

	}

	private ResultInsertEntry mergeAndSplit(Node node, int nrChild, Page page){
		if(node.getSuccessors().size() != 1) {
			// mergeChild(node, Math.max(0, nrChild-1));

			Node merged = (Node)page
				.getById((int)node.getSuccessors().get(nrChild))
				.getNode(node.getLevel() - 1, height);	//TODO co jak node.getLevel == 1?

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
		// Page tmp = new Page();
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

		for(int i = 0; i < node.getSuccessors().size(); i++){
			dump((Page)node.getSuccessor(i), level - 1, "   " + indentation);
		}

	}
	private static void dumpNode(Node node, String indentation){
 		System.out.println(indentation + (node.isLeaf()?"Leaf":"Node") + "[K" + node.getKeys().size() + ", V" + node.getValues().size() + ", S" + node.getSuccessors().size() + "] ");
		System.out.print(indentation);
		for(int i = 0; i < node.getKeys().size(); i++){
			System.out.print(node.getKeys().get(i) + " ");
		}
		System.out.println();

		// System.out.print(indentation);
 	// 	if (node.isLeaf()){
 	// 		for(int i = 0; i < node.getValues().size(); i++){
 	// 			System.out.print(node.getValues().get(i) + " ");
 	// 		}
 	// 	}
 	// 	System.out.println();
	}
/* ----------------------------------------------------------------------------- */
}
