public class ResultInsertEntry <Key extends Comparable<? super Key>, Value>{
	String r;
	Key key;
	Page page;
	public ResultInsertEntry(String r, Key key, Page page){
		this.r = r;
		this.key = key;
		this.page = page;
	}

	public String getR(){ return r; }
	public Key getKey(){ return key; }
	public Page getPage(){ return page; }
}
