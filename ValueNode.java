/**
 * Instancja tej klasy przechowuje pojedyńczą wartość wrzuconą do drzewa
 */
public class ValueNode <Value> extends Successor {
	private Value value;
	public ValueNode(Value value) {
		this.value = value;
	}
	public Value getValue() {
		return value;
	}
}