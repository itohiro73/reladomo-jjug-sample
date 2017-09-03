package sample.domain;

import com.gs.collections.api.multimap.list.MutableListMultimap;
import com.gs.collections.impl.factory.Sets;
import com.gs.collections.impl.set.mutable.primitive.IntHashSet;
import com.gs.fw.common.mithra.MithraManagerProvider;
import com.gs.fw.common.mithra.finder.Operation;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PersonTest
        extends AbstractReladomoTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonTest.class);

    @Override
    protected String[] getTestDataFilenames()
    {
        return new String[] {"test_data.txt"}; // Look into this file to see the test data being used
    }

    @Test
    public void testPersonRetrieval()
    {
        Operation findAllOp = PersonFinder.all();
        PersonList people = PersonFinder.findMany(findAllOp);
        people.forceResolve(); //Reladomoは遅延ロードするので例示のために強制ロード

        Assert.assertEquals(4, people.size());

        Operation findTaroOp = PersonFinder.firstName().eq("太郎");
        Person taro = PersonFinder.findOne(findTaroOp);
        Assert.assertEquals("太郎", taro.getFirstName());

        Operation op1 = PersonFinder.firstName().eq("大輔");
        //　SQL: WHERE first_name = '大輔'
        Operation op2 = PersonFinder.lastName().endsWith("藤");
        // SQL: WHERE last_name LIKE '%藤'
        Operation op1OrOp2 = op1.or(op2);
        // SQL: WHERE (( first_name = '大輔') OR ( last_name LIKE '%藤'))
        Operation op1AndOp2 = op1.and(op2);
        // SQL: WHERE (( first_name = '大輔') AND ( last_name LIKE '%藤'))

        PersonList people2 = PersonFinder.findMany(op1OrOp2);
        Assert.assertEquals(3, people2.size());

        Operation op3 =
            PersonFinder.age().in(IntHashSet.newSetWith(22, 24, 30));
        //　SQL: WHERE age in (22, 24, 30)

        Operation op4 =
            PersonFinder.age().notIn(IntHashSet.newSetWith(22, 25));
        //　SQL: WHERE age in (22, 25)

        Operation op5 =
            PersonFinder.age().greaterThan(25);
        //　SQL: WHERE age > 25


        PersonList people3 = PersonFinder.findMany(op3);
        Assert.assertEquals(3, people3.size());
        PersonList people4 = PersonFinder.findMany(op4);
        Assert.assertEquals(2, people4.size());
        PersonList people5 = PersonFinder.findMany(op5);
        Assert.assertEquals(1, people5.size());

    }

    @Test
    public void testCacheHit1()
    {
        Operation findTaroOp = PersonFinder.firstName().eq("太郎");
        Person taro = PersonFinder.findOne(findTaroOp);

        Operation op1 = PersonFinder.firstName().eq("大輔");
        Operation op2 = PersonFinder.lastName().endsWith("藤");
        Operation op1OrOp2 = op1.or(op2);
        PersonList people2 = PersonFinder.findMany(op1OrOp2);
        people2.forceResolve();

        Operation op3 =
                PersonFinder.age().greaterThan(25);
        PersonList people3 = PersonFinder.findMany(op3);
        people3.forceResolve();
    }

    @Test
    public void testCacheHit2()
    {
        Operation findAllOp = PersonFinder.all();
        PersonList people = PersonFinder.findMany(findAllOp);
        people.forceResolve(); //Reladomoは通常遅延ロードするので例示のために強制ロード

        Operation findTaroOp = PersonFinder.firstName().eq("太郎");
        Person taro = PersonFinder.findOne(findTaroOp);

        Operation op1 = PersonFinder.firstName().eq("大輔");
        Operation op2 = PersonFinder.lastName().endsWith("藤");
        PersonList people2 = PersonFinder.findMany(op1.or(op2));
        people2.forceResolve();

        Operation op3 =
                PersonFinder.age().greaterThan(25);
        PersonList people3 = PersonFinder.findMany(op3);
        people3.forceResolve();
    }

    @Test
    public void testInsert()
    {
        MithraManagerProvider.getMithraManager().executeTransactionalCommand(tx -> {
            Person jiro = new Person("二郎", "山田", 45);
            jiro.insert();

            Person sakura = new Person("さくら", "鈴木", 28);
            sakura.insert();
            return sakura;
        });
    }

    @Test
    public void testInsertAll()
    {
        // トランザクションが必要な場合
        MithraManagerProvider.getMithraManager().executeTransactionalCommand(tx -> {
            PersonList newPeople = new PersonList();

            newPeople.add(new Person("二郎", "山田", 45));
            newPeople.add(new Person("さくら", "鈴木", 28));

            newPeople.insertAll();
            return newPeople;
        });

        MithraManagerProvider.getMithraManager().executeTransactionalCommand(tx -> {
            PersonList people = PersonFinder.findMany(PersonFinder.all());

            people.setAge(20);

            return null;
        });
    }

    @Test
    public void testCreate2()
    {
        Person jiro = MithraManagerProvider.getMithraManager().executeTransactionalCommand(tx -> {
            Person newPerson = new Person("二郎", "山田", 45);
            newPerson.insert();
            return newPerson;
        });

        Operation op = PersonFinder.firstName().eq("二郎");
        Person jiroFromDb = PersonFinder.findOne(op);

        Assert.assertTrue(jiro.equals(jiroFromDb));
        Assert.assertTrue(jiro == jiroFromDb);
    }

    @Test
    public void testUpdate()
    {
        Operation tanakaOp = PersonFinder.lastName().eq("田中");
        Person tanaka = PersonFinder.findOne(tanakaOp);

        MithraManagerProvider.getMithraManager().executeTransactionalCommand(tx -> {
            tanaka.setAge(25);
            return null;
        });
    }

    @Test
    public void testBatchUpdate()
    {
        Operation op = PersonFinder.lastName().eq("佐藤");
        Person sato = PersonFinder.findOne(op);

        MithraManagerProvider.getMithraManager().executeTransactionalCommand(tx -> {
            sato.setLastName("左藤");
            sato.setAge(23);
            return sato;
        });
    }

    @Test
    public void testBulkUpdate()
    {
        Person tanaka = PersonFinder.findOne(PersonFinder.lastName().eq("田中"));
        Person sato = PersonFinder.findOne(PersonFinder.lastName().eq("佐藤"));

        MithraManagerProvider.getMithraManager().executeTransactionalCommand(tx -> {
            tanaka.setAge(25);
            sato.setAge(23);
            return null;
        });
    }

    @Test
    public void testDelete()
    {
        Operation op = PersonFinder.lastName().eq("田中");
        Person tanaka = PersonFinder.findOne(op);

        MithraManagerProvider.getMithraManager().executeTransactionalCommand(tx -> {
            tanaka.delete();
            return null;
        });

    }

    @Test
    public void testDeleteAll()
    {
        Operation op = PersonFinder.lastName().in(Sets.mutable.of("田中", "佐藤"));
        PersonList people = PersonFinder.findMany(op);

        MithraManagerProvider.getMithraManager().executeTransactionalCommand(tx -> {
            people.deleteAll();
            return null;
        });
    }

    @Test
    public void testPet()
    {
        Operation op = PersonFinder.lastName().eq("佐藤");
        Person sato = PersonFinder.findOne(op);
        LOGGER.info(sato.getPets().asGscList().collect(pet -> pet.getName() + "/" + pet.getPetType().getPetType()).makeString());
    }

    @Test
    public void testOwnerRetrieval()
    {
        //犬を飼っている飼い主を取得
        Operation op = PersonFinder.pets().petTypeId().eq(PetType.DOG);

        Person dogOwner = PersonFinder.findOne(op);

        Assert.assertEquals("佐藤", dogOwner.getLastName());

        PersonList petPeople = PersonFinder.findMany(PersonFinder.pets().exists());
        Assert.assertEquals(2, petPeople.size());
    }

    @Test
    public void testPetRetrieval()
    {
        //佐藤さんが飼っているペットを取得
        Operation op =
            PetFinder.owner().lastName().eq("佐藤");

        PetList satoPets = PetFinder.findMany(op);
    }

    @Test
    public void testDeepFetch()
    {
        //ペット飼っている人を取得
        Operation op =
            PersonFinder.pets().exists();

        PersonList petOwners = PersonFinder.findMany(op);
        petOwners.deepFetch(PersonFinder.pets());
        petOwners.deepFetch(PersonFinder.pets().petType());

        petOwners.forEach(petOwner -> {
            petOwner.getPets().forEach(pet -> {
                System.out.println(
                        petOwner.getLastName() + "さんは" +
                        pet.getName() + "という名の" +
                        pet.getPetType().getPetType() + "を飼っています");
            });
        });
    }

    @Test
    public void testGSC()
    {
        PersonList people = PersonFinder.findMany(PersonFinder.all());
        people.deepFetch(PersonFinder.pets());
        people.deepFetch(PersonFinder.pets().petType());

        //苗字を取得
        List<String> lastNames =
                people.asGscList().collect(Person::getLastName);

        //猫を飼っている飼い主を取得
        List<Person> catOwner =
                people.asGscList().select(person -> person.hasPet(PetType.CAT));

        //PetTypeごとにMapを作成
        MutableListMultimap<PetType, Person> peopleByPetType =
                people.asGscList().groupByEach(
                        person -> person.getPets().asGscList().collect(Pet::getPetType));

    }

    @Test
    public void testQuery()
    {
        Operation findTaroOp = PersonFinder.firstName().eq("太郎");
        Person taro = PersonFinder.findOne(findTaroOp);
        Assert.assertEquals("田中", taro.getLastName());
        Assert.assertEquals(24, taro.getAge());
    }
}
