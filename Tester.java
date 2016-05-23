import java.util.HashSet;
import java.util.Set;
import java.util.Random;


/**
 * Klasa testująca działanie LazyBPlusTree
 * porównywanie na podstawie wykonywania tego samego na B+ drzewie jak i na secie
 */
class Tester{
	public static void main(String[] args) {

		BPlusTree<Integer, Integer> tree = new BPlusTree(3); //<Klucz, Wartosc>(t,k,maxSize)
		Set<Integer> set = new HashSet<Integer>();
		Random generator = new Random();
		Integer operation;
		Integer number;
		Integer errors = 0; // liczba niepoprawnych odpowiedzi na temat losowego kluczas
		Integer queries = 0; // liczba zapytań o wartość dla losowego klucza
		Integer maxHeight = 0; // maxymalny poziom drzewa podczas testu

		for(long i = 0L; i < 1000000L; i++){

			operation = generator.nextInt(3); // losowanie operacji jaka sie ma wykonać
			number = generator.nextInt(200); // losowanie klucza dla wylosowanej operacji powyżej

			// w zależności od wylosowanej operacji są podejmowane jakieś zabiegi na drzewie i secie
			switch(operation){
				case 0:
					set.add(number);
					tree.insert(number, number);
				break;
				case 1:
					set.remove(number);
					tree.removeByKey(number);
				break;
				case 2:
					queries++;
					if(
							(set.contains(number) && tree.searchByKey(number) == null) ||
							(!set.contains(number) && tree.searchByKey(number) != null)
						){
							errors++;
						}
				break;
			}
			maxHeight = Math.max(maxHeight, tree.getHeight());
		}

		// printowanie drzewa (efektu końcowego powyższych operacji)
		tree.dump();
		// wypisywanie info o teście
		System.out.println("Max height of tree: " + maxHeight);
		System.out.println("Count of errors: " + errors);
		System.out.println("Queries: " + queries);
	}
}