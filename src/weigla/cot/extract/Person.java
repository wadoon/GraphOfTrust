package weigla.cot.extract;

public class Person {
    private String name, hex;

    public Person(String name, String hex) {
	this.name = name;
	this.hex = hex;
    }

    public String getName() {
	return name;
    }

    @Override
    public String toString() {
	return "Person (" + name + "," + hex + ")";
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getHex() {
	return hex;
    }

    public void setHex(String hex) {
	this.hex = hex;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((hex == null) ? 0 : hex.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Person other = (Person) obj;
	if (hex == null) {
	    if (other.hex != null)
		return false;
	} else if (!hex.equals(other.hex))
	    return false;
	return true;
    }

    public boolean selfsigned() {
	return name.equalsIgnoreCase("[selfsig]");
    }
}
