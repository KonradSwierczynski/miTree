import java.util.ArrayList;
import java.util.Date;
import java.io.*;

public class Node implements Serializable {

	private ArrayList<Integer> keys;
	//private ArrayList<Integer> values;
	private ArrayList<Integer> successors;

	private int minKeys;
	private int maxKeys;
	private int t;

	private int level;

	/**
	 * @param t podstawa do określenia min i max ilości kluczy w każdym węźle
	 */
	public Node (int t){
		keys = new ArrayList<Integer>();
		//values = new ArrayList<Integer>();
		successors = new ArrayList<Integer>();
		maxKeys = (2 * t) - 1;
		minKeys = t - 1;
		level = 1;
		this.t = t;
	}
	public void setSize(int t) {
		maxKeys = (2 * t) - 1;
		minKeys = t - 1;
		this.t = t;
	}
	public ArrayList<Integer> getKeys() { return keys; }
	public ArrayList<Integer> getSuccessors() { return successors; }
	public Page getSuccessor(int i){ return Page.getById(successors.get(i)); }
	public void addSuccessor(Page p){ successors.add(p.getId()); }
	public int getLevel() { return level; }

	public void setKeys(ArrayList<Integer> keys) { this.keys = keys; }
	public void setSuccessors(ArrayList<Integer> successors) { this.successors = successors; }
	//private void setValues(ArrayList<Integer> values) { this.values = values; }

	public Boolean isLeaf() {
		return getLevel() == 1;
	}
	public Boolean isFull(int height) {
		return (maxKeys <= keys.size());
	}
	public Boolean isThirsty() {
		return keys.size() < minKeys;
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
		return ((isLeaf() ? getKeys() : getSuccessors()).size()+1)/2;
	}

	// public ArrayList<Integer> getValues(){
	// 	return values;
	// }

	/**
	* Dzieli węzeł
	* @return lista zawierająca lewy i prawy węzeł powstały w wyniku podziału
	*/
	public ArrayList<Node> split() {
		int firstInRight = getSplitPoint();

		Node newChild = new Node(t);
		newChild.setLevel(getLevel());

		if (isLeaf()){
			// newChild.setValues( new ArrayList<Integer>(
			// 	getValues().subList(firstInRight, getValues().size())
			// ));
			// setValues( new ArrayList<Integer>(
			// 	getValues().subList(0, firstInRight)
			// ));
		} else {
			newChild.setSuccessors( new ArrayList<Integer>(
				getSuccessors().subList(firstInRight, getSuccessors().size())
			));
			setSuccessors( new ArrayList<Integer>(
				getSuccessors().subList(0, firstInRight)
			));
		}

		newChild.setKeys( new ArrayList<Integer>(
			getKeys().subList(firstInRight, getKeys().size())
		));

		setKeys( new ArrayList<Integer>(
			getKeys().subList(0, firstInRight - (isLeaf() ? 0 : 1))
		));

		ArrayList result = new ArrayList<Node>();

		result.add(this);
		result.add(newChild);

		return result;
	}

	public Boolean contains(Page successor){
		for(int i = 0; i < successors.size(); i++){
			if(successors.get(i) == successor.getId())
				return true;
		}
		return false;
	}
}
