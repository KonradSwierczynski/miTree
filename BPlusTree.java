/**
 * @file BPLusTree.java
 * @author akcelero
 * @version 0.1
 * @date 24.03.2016
 */

import java.util.ArrayList;
import java.util.Date;

public class BPlusTree <Key extends Comparable<? super Key>, Value> {

	private Node<Key, Value> root;
	private final int t;
	
	/** 
	* @param t podstawa do określenia min i max ilości kluczy w każdym węźle
	*/
	public BPlusTree(int t) {
		this.t = t;
		root = new Node(t);
		root.setLevel(1);
	}
/* ----------------------------------------------------------------------------- */
	/**
	 * @return wysokość drzewa czyli odległość roota od każdego liścia
	 */
	public int getHeight() { return root.getLevel(); }
/* ----------------------------------------------------------------------------- */	
	/**
	* Metoda wrzuca element do drzewa
	* @param key klucz
	* @param value wartość dla klucza
	*/
	public void insert(Key key, Value value) {

		// jeżeli root ma zbyt dużo kluczy to następuje jego podział i powstaje nowy root
		// (ojciec dla dwóch wierchołków wynikających z podizału pełnego roota)
		if(root.isFull()){

			Node oldRoot = root;
			root = new Node(t);
			root.setLevel(oldRoot.getLevel() + 1);

			root.getSuccessors().add(oldRoot);
			splitChild(root, 0);
		}
		// wrzucanie do roota z gwarancją że nie jest on pełny
		insertNonFull(root, key, value);
	}
	/**
	* Usuwanie po elementu po kluczu
	* @param key klucz
	*/
	public void removeByKey(Key key) {

		// usuwanie wartości z roota
		removeByKeyFromSubTree(root, key);

		// jeżeli root ma 1 syna to drzewo stara się zamienić tego syna w roota, gubiąc aktualnego roota
		if(root.getSuccessors().size() == 1 && !root.isLeaf()){
			root = (Node)root.getSuccessors().get(0);
		}
	}
	/**
	* Wyszukiwanie wartosci po kluczu
	* @param key klucz wyszukiwanego
	* @return wartosc dla znalezionego klucza lub null gdy taki klucz nie istnieje w drzewie
	*/
	public Value searchByKey(Key key){
		return searchByKeyFromSubTree(key, root);
	}
/* ----------------------------------------------------------------------------- */
	/**
	* Metoda wrzuca do podrzewa o zadanym (nie pełnym) rootcie klucz i jego wartość
	* @param node root dla poddrzewa
	* @param key klucz
	* @param value wartość dla klucza
	* @param timestamp - znacznik czasu
	*/
	private void insertNonFull(Node node, Key key, Value value){

		// rozpatrywanie przypadku wrzucania wartości do liścia i do zwykłego innerNode'a
		if(node.isLeaf()){

			int i = findFirstEqualOrGreater(node, key);
			if(i < node.getKeys().size()){
				if(((Key)node.getKeys().get(i)).compareTo(key) != 0){
					node.getKeys().add(i, key);
					node.getSuccessors().add(i, (Successor)(new ValueNode(value)));
				} else {
					node.getKeys().set(i, key);
					node.getSuccessors().set(i, (Successor)(new ValueNode(value)));
				}
			} else {
				node.getKeys().add(i, key);
				node.getSuccessors().add(i, (Successor)(new ValueNode(value)));
			}

		} else {

			int i = findFirstEqualOrGreater(node, key);
			insertNonFull((Node)node.getSuccessors().get(i), key, value);

			if(((Node)node.getSuccessors().get(i)).isFull()){
				splitChild(node, i);
				if(((Key)node.getKeys().get(i)).compareTo(key) < 0){
					i++;
				}
			}
		}
	}
/* -----------------------------------------------------------------------------*/
	/**
	* Metoda usuwania z podrzewa o zadanym (nie pełnym) rootcie klucz i jego wartość
	* @param node root dla poddrzewa
	* @param key klucz
	*/
	private void removeByKeyFromSubTree(Node node, Key key) {

		if(node.isLeaf()){
			int i = findFirstEqualOrGreater(node, key);

			if(i<node.getKeys().size() && ((Key)node.getKeys().get(i)).compareTo(key) == 0){

				node.getKeys().remove(i);
				node.getSuccessors().remove(i);

			}

		} else {
			int i = findFirstEqualOrGreater(node, key);

			if(i <= node.getKeys().size()){
				removeByKeyFromSubTree((Node)node.getSuccessors().get(i), key);

				if(((Node)node.getSuccessors().get(i)).isThirsty()){ // jeżeli trzeba reagować
					updateNums(node, i);
				}
			}
		}
	}
/* ----------------------------------------------------------------------------- */
	/**
	 * Metoda szuka i zwraca wartosc z podrzewa o podanym kluczu
	 * @param node root dla poddrzewa
	 * @param key klucz
	 */
	private Value searchByKeyFromSubTree(Key key, Node node){
		int i = findFirstEqualOrGreater(node, key);

		if(node.isLeaf()){ // przypadek gdzie wyszukany element jest w lisciu
			if(i < node.getSuccessors().size() && ((Key)node.getKeys().get(i)).compareTo(key) == 0){
				return (Value)((ValueNode)node.getSuccessors().get(i)).getValue();
			} else {
				return null;
			}
		} else {
			return searchByKeyFromSubTree(key, (Node)node.getSuccessors().get(i));
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
/* ----------------------------------------------------------------------------- */
	/**
	 * Metoda wywoluje laczenie syna spragnionego z o jeden wiekszym i jezeli polaczony sen jest pelny dzieli go.
	 * @param node ojciec laczonego syna
	 * @param nrChild numer laczonego syna
	 */
	private void updateNums(Node node, int nrChild){

		if(nrChild != 0){

			concatSons(node, nrChild - 1);

			if(((Node)node.getSuccessors().get(nrChild - 1)).isFull()){
				splitChild(node, nrChild - 1);
			}

		} else if(node.getSuccessors().size() != 1) {

			concatSons(node, nrChild);
			if(((Node)node.getSuccessors().get(nrChild)).isFull()){
				splitChild(node, nrChild);
			}
		}
	}
	/**
	* Metoda dzieli syna
	* @param ancestor ojciec dzielonego syna
	* @param nrChild numer dzielonego syna
	*/
	private void splitChild(Node ancestor, int nrChild) {
		if(((Node)ancestor.getSuccessors().get(nrChild)).getSuccessors().size() <= 1)
			return;
		Node child = (Node)ancestor.getSuccessors().get(nrChild);
		Node newChild = new Node(t);
		newChild.setLevel(child.getLevel());

		ancestor.getKeys().add(nrChild, child.getKeys().get(t - 1));
		Key key = (Key)ancestor.getKeys().get(nrChild);


		if(child.isLeaf()){

			newChild.setKeys( new ArrayList<Key>(
					child.getKeys().subList(t, child.getKeys().size())
				));
			child.getKeys().subList(t, child.getSuccessors().size()).clear();

			newChild.setSuccessors( new ArrayList<Successor>(
					child.getSuccessors().subList(t, child.getSuccessors().size())
				));
			child.getSuccessors().subList(t, child.getSuccessors().size()).clear();

		} else {
			newChild.setKeys( new ArrayList<Key>(
					child.getKeys().subList(t, child.getKeys().size())
				));
			child.getKeys().subList(t - 1, child.getKeys().size()).clear();

			newChild.setSuccessors( new ArrayList<Successor>(
					child.getSuccessors().subList(t, child.getSuccessors().size())
				));
			child.getSuccessors().subList(t, child.getSuccessors().size()).clear();

		}
		
		ancestor.getSuccessors().add(nrChild + 1, newChild);
	}
	/**
	* Metoda łącząca syny podanego wierzchołka o numerze podanym z wierzchołkiem o indexie o 1 większym
	* @param ancestor wierzchołek rodzic dla scalanych synów
	* @param nrBaseSon numer lewego syna który będzie scalany
	*/
	private void concatSons(Node ancestor, int nrBaseSon) {
		Node baseSon = (Node)ancestor.getSuccessors().get(nrBaseSon);
		Node sourceSon = (Node)ancestor.getSuccessors().get(nrBaseSon + 1);

		baseSon.getSuccessors().addAll(sourceSon.getSuccessors());

		if(!baseSon.isLeaf()){
			baseSon.getKeys().add(ancestor.getKeys().get(nrBaseSon));
		}

		baseSon.getKeys().addAll(sourceSon.getKeys());

		ancestor.getKeys().remove(nrBaseSon);
		ancestor.getSuccessors().remove(nrBaseSon + 1);
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