import java.util.ArrayList;
import java.util.Date;

public class Node <Key extends Comparable<? super Key>, Value> extends Successor {

	private ArrayList<Key> keys;
	private ArrayList<Successor> successors;

	private int minKeys;
	private int maxKeys;

	private int level;

	/**
	 * @param t podstawa do określenia min i max ilości kluczy w każdym węźle
	 */
	public Node (int t){
		successors = new ArrayList();
		keys = new ArrayList();
		maxKeys = (2 * t) - 1;
		minKeys = t - 1;
		level = 1;
	}

	public ArrayList<Key> getKeys() { return keys; }
	public ArrayList<Successor> getSuccessors() { return successors; }
	public int getLevel() { return level; }

	public void setKeys(ArrayList<Key> keys) { this.keys = keys; }
	public void setSuccessors(ArrayList<Successor> successors) { this.successors = successors; }

	public Boolean isLeaf() {
		if(successors.size() == 0 || (successors.get(0) instanceof ValueNode)){
			return true;
		} else {
			return false;
		}
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

	/**
	* Dzieli węzeł
	* @return lista zawierająca lewy i prawy węzeł powstały w wyniku podziału
	*/
	public ArrayList<Successor> split() {
		int firstInRight = getSplitPoint();

		Node newChild = new Node(minKeys + 1);
		newChild.setLevel(getLevel());

		newChild.setSuccessors( new ArrayList<Successor>(
				getSuccessors().subList(firstInRight, getSuccessors().size())
			));
		newChild.setKeys( new ArrayList<Key>(
				getKeys().subList(firstInRight, getKeys().size())
			));

		getSuccessors().subList(firstInRight, getSuccessors().size()).clear();
		getKeys().subList(firstInRight - (isLeaf() ? 0 : 1), getSuccessors().size()).clear();

		ArrayList result = new ArrayList<Successor>();

		result.add(this);
		result.add(newChild);

		return result;
	}
}
