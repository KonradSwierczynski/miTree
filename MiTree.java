import java.io.*;
import java.util.*;

public class MiTree {
	private int height = 1;
	Page rootPage;

	/*
	 * Te trzy wartosci powinny byc odpowiednio dobrane, poniewaz w przeciwnym wypadku drzewo
	 * bedzie sie wysypywac.
	 * Przetestowane trojki to [1, 4, 1], [2, 4, 2], [5, 3, 2]
	 * Pierwsza wartosc to wartosc maksymalna, mozna zmniejszac.
	 * Druga wartosc rowniez jest maksymalna, mozna zmniejszac
	 * Trzecia wartosc jest minimalna, ale mozna zwiekszac
	 * Jezeli maksymalna wysokosc drzewa zostanie przekroczona, to drzewo zostanie wydrukowane
	 * w stanie sprzed proby dodania, wyswietlony zostanie komunikat i program zakonczy prace
	 * (zgodnie z poleceniem).
	 * Trzeba uwazac na rozmiar Page, poniewaz, jezeli 
	 */
	private final int keysInNode = 2;
	private final int maxTreeHeight = 4; //maksymalna dopuszczalna wysokosc drzewa
	private final int maxPageSize = 2; //w kilobajtach

	public MiTree(){
		rootPage = new Page(maxPageSize, maxTreeHeight);
		rootPage.setNode(1, 1, new Node(keysInNode * 8));
	}

	public Integer search(Integer key){
		Node node = (Node)rootPage.getNode(height, height);
		int level = height;
		int ki = findFirstEqualOrGreater(node, key);
		while( !node.isLeaf() ){
			level--;
			node = (node.getSuccessor(ki)).getNode(level, height);
			ki = findFirstEqualOrGreater(node, key);
		}
		if(ki < node.getKeys().size() && ((Integer)node.getKeys().get(ki)).compareTo(key) == 0)
			return (Integer)node.getKeys().get(ki);
		 else 
			return null;
	}

	public void insert(Integer key) throws Exception{
		if( search(key) != null)	//warunek nie powtarzania się elementów
			return;
		Page newPage = new Page();
		Page newPagePrim = new Page();
		Node root = rootPage.getNode(height,height);
		insertEntry(key, root, newPage, newPagePrim);

		if(root.isFull(height)){
			if (height + 1 > maxTreeHeight)
				throw new Exception("Drzewo przekroczylo dopuszczalny rozmiar");
			int newSize = keysInNode * (int)Math.pow(2, maxTreeHeight - height);
			Node newRoot = new Node(newSize);
			newRoot.setLevel(root.getLevel()+1);
			newRoot.getKeys().add(root.getKeys().get(root.getSplitPoint()-1));
			ArrayList<Node> splitted = root.split();
			splitted.get(0).setSize(newSize);
			splitted.get(1).setSize(newSize);
			newRoot.getSuccessors().add(newPage.getId());
			newRoot.getSuccessors().add(newPagePrim.getId());
			height++;
			newPage.setNode(height-1, height, splitted.get(0));
			newPagePrim.setNode(height-1, height, splitted.get(1));
			newPage.setNode(height, height, newRoot);

		} else 
			newPage.setNode(height, height, root);
	//	System.err.println(height);
		newPage.writeToNAND();
		newPagePrim.writeToNAND();
		rootPage = newPage;
	}

	public void insertEntry(Integer key, Node subRoot, Page newPage, Page newPagePrim){
		int i = findFirstEqualOrGreater(subRoot, key);
		if(subRoot.isLeaf()){
			if(i < subRoot.getKeys().size()) {
				subRoot.getKeys().add(i, key);
			} else {
				subRoot.getKeys().add(key);
			}
		} else {
			Node tmp = subRoot.getSuccessor(i).getNode(subRoot.getLevel()-1,height);
			insertEntry(key, tmp, newPage, newPagePrim);
			subRoot.getSuccessors().set(i, newPage.getId());
			if(tmp.isFull(height))
				splitChild(subRoot, i, newPage, newPagePrim);
		}
		newPage.setNode(subRoot.getLevel(), height, subRoot);
	}

	public void delete(Integer key){
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
		newPage.writeToNAND();
		newPagePrim.writeToNAND();
		rootPage = newPage;
	}

	private void deleteEntry(Integer key, Node node, Page newPage, Page newPagePrim){
		int i = findFirstEqualOrGreater(node, key);

		if(node.isLeaf()){
			if(i<node.getKeys().size() && ((Integer)node.getKeys().get(i)).compareTo(key) == 0){
				node.getKeys().remove(i);
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
	private int findFirstEqualOrGreater(Node node, Integer key) {
		int nrSuccessor;
		for(nrSuccessor = 0; nrSuccessor < node.getKeys().size(); nrSuccessor++){
			if(((Integer)node.getKeys().get(nrSuccessor)).compareTo(key) >= 0){
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
 		System.out.println(indentation + (node.isLeaf()?"Leaf":"Node") + "[K" + node.getKeys().size() + ", S" + node.getSuccessors().size() + "] ");
		System.out.print(indentation);
		for(int i = 0; i < node.getKeys().size(); i++)
			System.out.print(node.getKeys().get(i) + " ");
		
		System.out.println();
	}
/* ----------------------------------------------------------------------------- */
}
