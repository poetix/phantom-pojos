# phantom-pojos
Immutable value types and builders implemented with dynamic proxies.

An ```PhantomPojo``` is a bean-like object defined using two interfaces:

```java
public interface Person extends PhantomPojo<Person.Builder> {

    Lens<Person, Integer> age = Lens.onPhantom(Person::getAge, Builder::withAge);
    Lens<Person, Address> address = Lens.onPhantom(Person::getAddress, Builder::withAddress);

    interface Builder extends Supplier<Person> {
        Builder withName(String name);
        Builder withAge(int age);
        Builder withFriends(Builder...friendBuilders);
        Builder withAddress(Address address);
        Builder withAddress(Address.Builder addressBuilder);
    }

    static Builder builder() {
        return PhantomBuilder.building(Person.class);
    }

    String getName();
    int getAge();
    List<Person> getFriends();
    Address getAddress();
}
```

Implementations of these interfaces are automatically created by ```PhantomBuilder``` (using dynamic proxies).
They implement ```equals```, ```hashCode``` and ```toString```.
 
You create an instance of a ```PhantomPojo``` with its ```Builder```, like so:

```java
Person henry = Person.builder()
                .withName("Henry")
                .withAge(42)
                .withFriends(Person.builder()
                    .withName("Jerry")
                    .withAge(33)
                    .withFriends())
                .get();
```

The created object has getter methods for each of its properties:

```java
assertThat(henry.getName(), equalTo("Henry"));
assertThat(henry.getFriends().get(0).getAge(), equalTo(33));
```

Once it has been created it cannot be changed, but a modified copy can be created using the ```update``` method:

```java
Person henrietta = henry.update().withName("Henrietta").get();
```

A ```PhantomPojo``` wraps a simple ```Map<String, Object>``` of property values, and can be created directly out of such a map:

```java
Map<String, Object> properties = new HashMap<>();
properties.put("name", "Angie");
properties.put("age", 63);
Person angie = PhantomPojo.wrapping(properties).with(Person.class);

assertThat(angie.getAge(), equalTo(63));
```

You can always retrieve this map of property values from the ```PhantomPojo``` via its ```getProperties``` method:

```java
assertThat(angie.getProperties().get("name"), equalTo("Angie"));
```

Nested maps are automatically promoted to ```PhantomPojo```s:

```java
Map<String, Object> addressProperties = new HashMap<>();
addressProperties.put("addressLines", Arrays.asList("67 Penguin Street", "Cinderford"));
addressProperties.put("postcode", "RA8 81T");

Map<String, Object> personProperties = new HashMap<>();
personProperties.put("name", "Harry");
personProperties.put("age", 37);
personProperties.put("address", addressProperties);

Person person = PhantomPojo.wrapping(personProperties).with(Person.class);

assertThat(person.getAddress().getPostcode(), equalTo("RA8 81T"));
```
