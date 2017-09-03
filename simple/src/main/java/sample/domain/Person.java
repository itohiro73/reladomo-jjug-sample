package sample.domain;
import java.sql.Timestamp;
public class Person extends PersonAbstract
{
	public Person()
	{
		super();
		// You must not modify this constructor. Mithra calls this internally.
		// You can call this constructor. You can also add new constructors.
	}

	public Person(String firstName, String lastName, int age)
	{
		this.setFirstName(firstName);
		this.setLastName(lastName);
		this.setAge(age);
	}

	public boolean hasPet(int petId)
	{
		return this.getPets().asGscList().anySatisfy(pet -> pet.getPetType().getPetTypeId() == petId);
	}
}
