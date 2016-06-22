import java.io.*;
import java.util.*;

/**
 * Klasa imitująca komórki pamięci NAND
 * Przechowuje obiekty klasy Node
 * 
 * @see Node
 */
public class Page implements Serializable{
	private static int maxTreeHeight;	//maksymalna wysokość dzrzewa(inicjowana w konstruktorze w kostruktorze)
	private static int size = 0;		//rozmiar Page w kilobajtach(inicjowany w kostruktorze)
	private int pageId;					//klucz Page-a, imituje adres w pamięci
	private byte[] memory;				//tablica imitująca pamięć, na niej w postaci bitowej zapisane są dane Page, czyli Node-y
	public static HashMap<Integer, Page> index = new HashMap();	//statyczna tablica, na której są zapisane wszystkie page używane w MiTree
	public static Integer numberPage = 0;	//ilość utworzonych już Page-y, służy do nadawania nowemu Page unikalny pageId  (n-ty utowrzony Page ma pageId równy n - 1)
	private boolean onNAND = false;		//zmienna blokująca możliwość edytowania danych zapisanych na Page-u(gdy onNAND = true, próba nadpisania danych zgłasza błąd)			

	private int nodeBaseSize = 256;		//rozmiar pustego Node-a w bajtach

	/**
	 * Konstruktor, inicjalizuje pamięć Page-a, dodaje go do HashMapy
	 * @param kilobytes Rozmiar Page-a w kilobajtach
	 * @param maxTreeHeight Maksymalna wysokość drzewa(maksymalna liczba prechowywanych Node-ów)
	 */
	public Page(int kilobytes, int maxTreeHeight){
		size = kilobytes * 1024;
		pageId = numberPage;									//aby pageId było unikalne dla każdego Page, ustawiamy je na ilość dotychczas utworzonych Page-y
		index.put(numberPage++, this);							//właśnie utworzony Page wstawiany jest (jako klucz służy  pageId) do HashMapy przechowującej wszystkie Page
		this.maxTreeHeight = maxTreeHeight;
		memory = new byte[size + maxTreeHeight * nodeBaseSize];	//tablica imitująca pamięć otrzymuje rozmiar podany w parametrze kilobytes powiększony o rozmiar pustuch nodów
	}

	/**
	 * Konstruktor, inicjalizuje pamięć Page-a, dodaje go do HashMapy
	 * Używa domyślnych parametrów
	 */
	public Page(){
		if(size == 0){
			size = 5 * 1024;			//jeżeli parametry nie zostały podane, ustawiane są domyślne
		}
		if (maxTreeHeight == 0)
			maxTreeHeight = 4;
		pageId = numberPage;			//aby pageId było unikalne dla każdego Page, ustawiamy je na ilość dotychczas utworzonych Page-y
		index.put(numberPage++, this);	//właśnie utworzony Page wstawiany jest (jako klucz służy  pageId) do HashMapy przechowującej wszystkie Page
		memory = new byte[size + maxTreeHeight * nodeBaseSize];	//tablica imitująca pamięć otrzymuje rozmiar podany w parametrze kilobytes powiększony o rozmiar pustuch nodów
	}
	/**
	 * Zwraca ilość aktualnie zapisanych Page-y w HashMapie
	 * Aby podało poprawną ilość (tylko tych Page-y, które są używane) 
	 * wymagane jest uprzednie użycie metody deletePages() w obiekcie MiTree, która usuwa z HashMapy nieużywanie Page
	 * @return Ilość aktualnie używanych Page-y
	 */
	public static int getCountOfPage(){
		return index.size();	//zwracana jest ilość przechwywanych w HashMapie, czyli obecnie używanych Page-y
	}

	/**
	 * Zwraca id Page-a
	 * @return pageId
	 */
	public int getId(){
		return pageId;
	}
	
	/**
	 * Zwraca Page-a z HashMapy o podanym id
	 * @param id id szukanego Paga
	 * @return Page o szukanym id
	 */
	public static Page getById(int id){
		return index.get(id);	//zwracany jest Page o pageId równym paramatetrowi id
	}

	/**
	 * Metoda zwraca Noda zapisanego na podamym poziomie Page-a
	 * @param level Poziom na którym zapisany jest Node
	 * @param heightOfTree Aktualna wysokość drzewa MiTree
	 * @return Node na poziomie level
	 */
	public Node getNode(int level, int heightOfTree){
			//używamy tutaj sposobu w jaki zostają zapisywanie Node-y na Page-u, 
			//tzn. Node na najniższym poziomie(level-u równym 1) zajmuje połowę pamięci Page, kolejny na poziomie wyżej zajmuje połowę pozostałego miejsca itd.
		int sizeOfNode = size/(1<<level);		//obliczmy rozmiar zukanego Node-a
		int offset = sizeOfNode;				//początek miejsca w tablicy memory na którym zapisany jest szukany Node
		if(level == heightOfTree){				//wyjątkiem jest Node na poziomie równym wyskokości drzewa(korzeń), ponieważ zajmuje on tyle co Node z niższego poziomu(a nie 2 razy mniej)
			sizeOfNode = sizeOfNode * 2;		//dla korzenia, jego rozmiar jest dwa razy większy, niż wynika ze wzoru(linijka 84)
			offset = 0;							//korzeń jest zapisany na samym początku tablicy
		}
		offset += (maxTreeHeight - level) * nodeBaseSize;							//uwzględniamy jeszcze miejsca które dodaliśmy(dodatkowe miejsca o rozmiarach pustego Node-a)
		try{
			Node result = (Node)deserializeNode(offset, sizeOfNode + nodeBaseSize);	//próba odczytania Node-a z postaci binarnej
			return result;															//zwracamy odczytanego Node-a
		}catch(Exception e){
			System.out.println(e);
			return new Node(2 * (maxTreeHeight - level + 1));						//w razie niepowodzenia, zwracany jest pusty Node
		}
	}

	/**
	 * Metoda zapisująca Node-a na odpowiednim poziomie w Page-u
	 * @param level Poziom, na którym ma być zapisany Node
	 * @param heightOfTree Wysokość drzewa
	 * @param node Node do zapisania
	 */
	public void setNode(int level, int heightOfTree, Node node){
		if (onNAND){										//jeżeli Page został już zapisany, onNAND powstrzymuje przed nadpisaniem, czego nie można zrobić na pamięci NAND						
			System.err.println("Błąd krytyczny - Próba nadpisania strony");
			System.exit(1);
		}
		int sizeOfNode = size/(1<<level);					//obliczanie miejsca w tablicy memory podobnie jak w metodzie getNode
		int offset = sizeOfNode;
		if(level == heightOfTree){
			sizeOfNode = sizeOfNode * 2;
			offset = 0;
		}
		byte[] subMemory = serializeNode(node);				//zserializowujemy Noda
		if(subMemory == null)								//TODO some normal exception for empty Node
			System.out.println("WTF");
		if(subMemory.length > sizeOfNode + nodeBaseSize){	//TODO exception for not enough space in memory
			return;
		}
		offset += (maxTreeHeight - level) * nodeBaseSize;	//obliczanie miejsca w którmy mamy zapisać Noda w tablicy

		for(int i = offset; i < offset + subMemory.length; i++){
			memory[i] = subMemory[i - offset];				//przepisywanie Node-a po bicie to pamięci
		}
	}

	/**
	 * Gdy cały Page został wypełniony, to zapewni nas, że nie zostanie nadpisany
	 */
	public void writeToNAND() {
		onNAND = true;
	}

	/**
	 * Metoda do odczytu Noda zapisanego binarnie w tablicy memory(naszej pamięci)
	 * @param offset Początek miejca na którym jest zapisany
	 * @param sizeOfNode Długość ciągu znaków zapisanego Node-a
	 * @return Odzytany Node lub null w przypadku niepowodzenia
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private Object deserializeNode(int offset, int sizeOfNode) throws IOException, ClassNotFoundException {
		byte [] subMemory = Arrays.copyOfRange(memory, offset, sizeOfNode + offset);	//kopiujemy zapisanego Node-a

		try (ByteArrayInputStream byteArrayIn = new ByteArrayInputStream(subMemory);	//próbujemy odczytać z postaci binarnej
		ObjectInputStream objectIn = new ObjectInputStream(byteArrayIn)) {
			return objectIn.readObject();
		}catch (IOException e){
			return null;
		}
	}

	/**
	 * Funkcja zwracająca podany obiekt w zapisie binarnym w tablicy
	 * @param node Obiekt do zamiany
	 * @return Tablica zawierająca zapisany binzarnie obiekt
	 */
	public byte[] serializeNode(Object node) {
		if (node == null) {				//przyapdek pustego obiektu
			return new byte[0];
		}
		try (ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();	//próba serializacji
	 	ObjectOutputStream objectOut = new ObjectOutputStream(byteArrayOut)) {
			objectOut.writeObject(node);
			return byteArrayOut.toByteArray();
		} catch (final IOException e) {											//przypadek błędu serializacji
			for (StackTraceElement ste : e.getStackTrace()) {
				System.out.println(ste);
			}
			System.out.println();
			return null;
		}
	}
}