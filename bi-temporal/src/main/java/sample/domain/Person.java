package sample.domain;
import java.sql.Timestamp;
public class Person extends PersonAbstract
{
	public Person(Timestamp businessDate
	, Timestamp processingDate
	)
	{
		super(businessDate
		,processingDate
		);
		// You must not modify this constructor. Mithra calls this internally.
		// You can call this constructor. You can also add new constructors.
	}

	public Person(Timestamp businessDate)
	{
		super(businessDate);
	}

	public Person(String firstName, String lastName, int age, Timestamp businessDate)
	{
		this(businessDate);
		this.setFirstName(firstName);
		this.setLastName(lastName);
		this.setAge(age);
	}

	public boolean hasPet(int petId)
	{
		return this.getPets().asGscList().anySatisfy(pet -> pet.getPetType().getPetTypeId() == petId);
	}
}
