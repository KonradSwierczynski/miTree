import java.util.HashSet;
import java.util.Set;
import java.util.Random;
import java.util.Iterator;

class Tester{
	public static void main(String[] args) {

		MiTree tree = new MiTree();
		Set<Integer> set = new HashSet<Integer>();
		Random generator = new Random();
		Integer operation;
		Integer number;
		Integer errors = 0; // liczba niepoprawnych odpowiedzi na temat losowego kluczas
		Integer queries = 0; // liczba zapytań o wartość dla losowego klucza
		Integer maxHeight = 0; // maxymalny poziom drzewa podczas testu
		Integer leaks = 0; //liczba elementów, których nie udało się zapisać
		Integer inserts = 0;
		for(long i = 0L; i < 2000L; i++){

			operation = generator.nextInt(3); // losowanie operacji jaka sie ma wykonać
			number = generator.nextInt(1000000); // losowanie klucza dla wylosowanej operacji powyżej
			//operation = 0;
			// w zależności od wylosowanej operacji są podejmowane jakieś zabiegi na drzewie i secie
			switch(operation){
				case 0:
					//System.out.println("Insert " + number);
 					set.add(number);
 					try {
						tree.insert(number);
						inserts++;
 					} catch (Exception e) {
 						tree.dump();
 						System.err.println(e.getMessage() + "\n\nInserts: " + inserts);
 						System.out.println("Count of pages: " + Page.index.size());
 						return;
 					}
				break;
				case 1:
					//System.out.println("Delete " + number);
					tree.delete(number);
					set.remove(number);
				break;
				case 2:
					//System.out.println("Query " + number);
					queries++;
					if(
							(set.contains(number) && tree.search(number) == null) ||
							(!set.contains(number) && tree.search(number) != null)
						){
							errors++;
						}
				break;
			}
			//tree.dump();
			//System.out.println("Inserts: " + inserts);
			//System.out.println("Count of errors: " + errors);
			//System.out.println("Count of pages: " + Page.getCountOfPage());
			tree.deletePages();
		}

		//Porównywanie zawartości set-a i drzewa
		Iterator<Integer> iterator = set.iterator();
    	while(iterator.hasNext()) {
    		Integer element = iterator.next();
            if(tree.search(element) == null) 
            	leaks++;
    	}

		// printowanie drzewa (efektu końcowego powyższych operacji)
		tree.dump();
		// wypisywanie info o teście
		System.out.println("Count of pages: " + Page.index.size());
		System.out.println("Count of errors: " + errors);
		System.out.println("Queries: " + queries);
		System.out.println("Count of leaks: " + leaks); //póki co może wskazywać na błędy w tree.search
		System.out.println("Inserts: " + inserts);
	/**/	
	}

}
// insert 61 - 1149
// delete 61 - 1569
//
//