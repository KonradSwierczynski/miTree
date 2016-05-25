import java.util.ArrayList;
import java.util.Date;
import java.io.*;

public class Node <Key extends Comparable<? super Key>, Value> implements Serializable {

	private ArrayList<Key> keys;
	private ArrayList<Key> values;
	private ArrayList<Integer> successors;

	private int minKeys;
	private int maxKeys;

	private int level;

	/**
	 * @param t podstawa do określenia min i max ilości kluczy w każdym węźle
	 */
	public Node (int t){
		values = new ArrayList();
		successors = new ArrayList();
		keys = new ArrayList();
		maxKeys = (2 * t) - 1;
		minKeys = t - 1;
		level = 1;
	}

	public ArrayList<Key> getKeys() { return keys; }
	public ArrayList<Integer> getSuccessors() { return successors; }
	public Page getSuccessor(int i){ return Page.getById(successors.get(i)); }
	public void addSuccessor(Page p){ successors.add(p.getId()); }
	public int getLevel() { return level; }

	public void setKeys(ArrayList<Key> keys) { this.keys = keys; }
	public void setSuccessors(ArrayList<Integer> successors) { this.successors = successors; }

	public Boolean isLeaf() {
		return getLevel() == 1;
	}
	public Boolean isFull() {
		if(maxKeys <= keys.size()){
			return true;
		} else {
			return false;
		}
	}
	public Boolean isThirsty() {
		if(keys.size() <= minKeys){
			return true;
		} else {
			return false;
		}
	}
	public void setLevel(int level){
		this.level = level;
	}

	/**
	* Zwraca liczbę wartości, które mają należeć do lewego węzła po podziale
	* @return liczba wartości, które mają należeć do lewego węzła po podziale
	*/
	public int getSplitPoint()
	{
		return (getSuccessors().size()+1)/2;
	}

	public void setValue(int i, Key value){
		if(isLeaf()){
			values.set(i, value);
		}
	}

	public ArrayList<Key> getValues(){
		return values;
	}

	/**
	* Dzieli węzeł
	* @return lista zawierająca lewy i prawy węzeł powstały w wyniku podziału
	*/
	public ArrayList<Node> split() {
		int firstInRight = getSplitPoint();

		Node newChild = new Node(minKeys + 1);
		newChild.setLevel(getLevel());

		newChild.setSuccessors( new ArrayList<Integer>(
				getSuccessors().subList(firstInRight, getSuccessors().size())
			));
		newChild.setKeys( new ArrayList<Key>(
				getKeys().subList(firstInRight, getKeys().size())
			));

		getSuccessors().subList(firstInRight, getSuccessors().size()).clear();
		getKeys().subList(firstInRight - (isLeaf() ? 0 : 1), getSuccessors().size()).clear();

		ArrayList result = new ArrayList<Node>();

		result.add(this);
		result.add(newChild);

		return result;
	}

	public Boolean contains(Page successor){
		for(int i=0;i<successors.size(); i++){
			if(successors.get(i) == successor.getId())
				return true;
		}
		return false;
	}

	// ---------------------------------------------
	private String test;
	public void setTest(String test){ this.test = test; }
	public String getTest(){ return this.test; }
}
