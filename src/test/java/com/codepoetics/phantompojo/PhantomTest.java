package com.codepoetics.phantompojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PhantomTest {

    public interface Address extends PhantomPojo<Address.Builder> {

        Lens<Address, String> postcode = Lens.onPhantom(Address::getPostcode, Builder::withPostcode);

        interface Builder extends Supplier<Address> {
            Builder withAddressLines(String...addressLines);
            Builder withAddressLines(Collection<String> addressLines);
            Builder withPostcode(String postcode);
        }

        static Builder builder() {
            return PhantomBuilder.building(Address.class);
        }

        List<String> getAddressLines();
        String getPostcode();
    }

    public interface Person extends PhantomPojo<Person.Builder> {

        Lens<Person, Integer> age = Lens.onPhantom(Person::getAge, Builder::withAge);
        Lens<Person, Address> address = Lens.onPhantom(Person::getAddress, Builder::withAddress);

        interface Builder extends Supplier<Person> {
            Builder withName(String name);
            Builder withAge(int age);
            Builder withLotteryNumbers(int...numbers);
            Builder withFriends(Builder...friendBuilders);
            Builder withAddress(Address address);
            Builder withAddress(Address.Builder addressBuilder);
            Builder withNicknames(String...nicknames);
            Builder withNicknames(Collection<String> nicknames);
        }

        static Builder builder() {
            return PhantomBuilder.building(Person.class).withAge(-1);
        }

        String getName();
        int getAge();
        Set<Integer> getLotteryNumbers();
        List<Person> getFriends();
        Address getAddress();
        Set<String> getNicknames();

        @JsonIgnore
        default String getPostcode() {
            return getAddress().getPostcode();
        }

        @JsonCreator
        static Person create(
                Map<String, Object> properties) {
            return PhantomPojo.wrapping(properties).with(Person.class);
        }
    }

    @Test public void
    build_get_update_get() {
        Person test = Person.builder()
                .withName("Henry")
                .withAge(42)
                .withLotteryNumbers(23, 12, 15, 52, 19)
                .withFriends(Person.builder()
                        .withName("Jerry")
                        .withAge(33)
                        .withFriends())
                .get();

        assertThat(test.getName(), equalTo("Henry"));
        assertThat(test.getLotteryNumbers(), hasItems(23, 12, 15, 52, 19));
        assertThat(test.getFriends().get(0).getName(), equalTo("Jerry"));
        assertThat(test.getFriends().get(0).getAge(), equalTo(33));

        Person henrietta = test.update().withName("Henrietta").get();
        Person olderHenrietta = Person.age.update(henrietta, i -> i + 1);

        assertThat(test.getName(), equalTo("Henry"));
        assertThat(olderHenrietta.getName(), equalTo("Henrietta"));
        assertThat(olderHenrietta.getAge(), equalTo(43));
        assertThat(olderHenrietta.getFriends().get(0).getName(), equalTo("Jerry"));
    }

    @Test public void
    equality() {
        assertThat(Person.builder().withName("Phyllis").withAge(42).get(),
                equalTo(Person.builder().withName("Phyllis").withAge(42).get()));

        assertThat(Person.builder().withName("Phyllis").withAge(42).get(),
                not(equalTo(Person.builder().withName("Phyllis").withAge(43).get())));
    }

    @Test public void
    to_string() {
        assertThat(Person.builder().withName("Roger").withAge(13).get().toString(),
                allOf(
                        containsString("Person"),
                        containsString("name=Roger"),
                        containsString("age=13")));
    }

    @Test public void
    lenses() {
        Person harry = Person.builder()
                .withName("Harry")
                .withAddress(Address.builder()
                        .withAddressLines("23 Acacia Avenue", "Surbiton")
                        .withPostcode("VB6 5UX"))
                .get();

        Lens<Person, String> addressPostcode = Person.address.andThen(Address.postcode);

        assertThat(addressPostcode.get(harry), equalTo("VB6 5UX"));

        Person harryMoved = addressPostcode.set(harry, "RA8 81T");
        assertThat(harryMoved.getAddress().getPostcode(), equalTo("RA8 81T"));
    }

    @Test public void
    property_map() {
        Person harry = Person.builder()
                .withName("Harry")
                .withAddress(Address.builder()
                        .withAddressLines("23 Acacia Avenue", "Surbiton")
                        .withPostcode("VB6 5UX"))
                .get();

        Map<String, Object> properties = harry.properties();

        Person rewrapped = PhantomPojo.wrapping(properties).with(Person.class);

        assertThat(rewrapped.getName(), equalTo("Harry"));
    }

    @Test public void
    automatic_promotion_from_map_to_phantom() {
        Map<String, Object> addressProperties = new HashMap<>();
        addressProperties.put("addressLines", Arrays.asList("67 Penguin Street", "Cinderford"));
        addressProperties.put("postcode", "RA8 81T");

        Map<String, Object> personProperties = new HashMap<>();
        personProperties.put("name", "Harry");
        personProperties.put("age", 37);
        personProperties.put("address", addressProperties);
        personProperties.put("lotteryNumbers", new int[] { 1, 2, 3, 4 });

        Person person = PhantomPojo.wrapping(personProperties).with(Person.class);

        assertThat(person.getLotteryNumbers(), hasItems(1, 2, 3, 4));
        assertThat(person.getAddress().getPostcode(), equalTo("RA8 81T"));
    }

    @Test public void
    automatic_promotion_from_list_of_maps_to_list_of_phantoms() {
        Map<String, Object> harryProperties = new HashMap<>();
        harryProperties.put("name", "Harry");
        harryProperties.put("age", 37);

        Map<String, Object> sallyProperties = new HashMap<>();
        sallyProperties.put("name", "Sally");
        sallyProperties.put("age", 38);

        Map<String, Object> steveProperties = new HashMap<>();
        steveProperties.put("name", "Steve");
        steveProperties.put("age", 29);

        harryProperties.put("friends", Arrays.asList(sallyProperties, steveProperties));

        Person harry = PhantomPojo.wrapping(harryProperties).with(Person.class);
        Person sally = PhantomPojo.wrapping(sallyProperties).with(Person.class);
        Person steve = PhantomPojo.wrapping(steveProperties).with(Person.class);

        assertThat(harry.getFriends(), hasItems(sally, steve));
    }

    @Test public void
    promotion_handles_sets() {
        Map<String, Object> harryProperties = new HashMap<>();
        harryProperties.put("name", "Harry");
        harryProperties.put("age", 37);

        Set<String> nicknames = new HashSet<>();
        nicknames.add("'orrible 'arry");
        nicknames.add("Harry the Hat");

        harryProperties.put("nicknames", nicknames);

        Person harry = PhantomPojo.wrapping(harryProperties).with(Person.class);

        assertThat(harry.getNicknames(), hasItems("'orrible 'arry", "Harry the Hat"));
    }

    @Test public void
    promotion_converts_between_collection_types() {
        Map<String, Object> harryProperties = new HashMap<>();
        harryProperties.put("name", "Harry");
        harryProperties.put("age", 37);

        Map<String, Object> sallyProperties = new HashMap<>();
        sallyProperties.put("name", "Sally");
        sallyProperties.put("age", 38);

        Map<String, Object> steveProperties = new HashMap<>();
        steveProperties.put("name", "Steve");
        steveProperties.put("age", 29);

        Set<Map<String, Object>> friendSet = new HashSet<>();
        friendSet.add(sallyProperties);
        friendSet.add(steveProperties);
        harryProperties.put("friends", friendSet);

        Person harry = PhantomPojo.wrapping(harryProperties).with(Person.class);
        Person sally = PhantomPojo.wrapping(sallyProperties).with(Person.class);
        Person steve = PhantomPojo.wrapping(steveProperties).with(Person.class);

        assertThat(harry.getFriends(), hasItems(sally, steve));
    }


    @Test public void
    default_methods() {
        Person harry = Person.builder()
                .withName("Harry")
                .withAddress(Address.builder()
                        .withAddressLines("23 Acacia Avenue", "Surbiton")
                        .withPostcode("VB6 5UX"))
                .get();

        assertThat(harry.getPostcode(), equalTo("VB6 5UX"));
    }

    @Test public void
    jackson_mapping() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        Person harry = Person.builder()
                .withName("Harry")
                .withAge(42)
                .withFriends(
                        Person.builder().withName("Fred"),
                        Person.builder().withName("Jerry").withAge(54))
                .withAddress(Address.builder()
                        .withAddressLines("23 Acacia Avenue", "Surbiton")
                        .withPostcode("VB6 5UX"))
                .get();

        String json = mapper.writeValueAsString(harry);

        Person deserialised = mapper.readValue(json, Person.class);

        assertThat(harry, equalTo(deserialised));
    }

    @Test public void
    flexible_collection_creation() {
        Set<String> nicknameSet = new HashSet<>();
        nicknameSet.add("'orrible 'arry");
        nicknameSet.add("Harry the Hat");

        assertThat(Person.builder()
                .withName("Harry")
                .withNicknames("Harry the Hat", "'orrible 'arry")
                .get().getNicknames(), equalTo(nicknameSet));

        assertThat(Person.builder()
                .withName("Harry")
                .withNicknames(Arrays.asList("Harry the Hat", "'orrible 'arry"))
                .get().getNicknames(), equalTo(nicknameSet));

        assertThat(Person.builder()
                .withName("Harry")
                .withNicknames(nicknameSet)
                .get().getNicknames(), equalTo(nicknameSet));
    }
}
