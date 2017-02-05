
public class Entry {
	private String type;
	private String name;
	private String date;
	private String address;
	
	public Entry(String type, String name, String date, String address) {
		this.type = type;
		this.name = name;
		this.date = date;
		this.address = address;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getAddress() {
		return address;
	}
	public void setAdress(String address) {
		this.address = address;
	}
	
	@Override
	public String toString() {
		return 	"Name: " + this.name
				+ " Type: " + this.type
				+ " Address: " + this.address
				+ " Date: " + this.date;
	}
}
